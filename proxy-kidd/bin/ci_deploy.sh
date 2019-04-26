#!/usr/bin/env bash
component=$1
branch=$2

if [ $branch == develop ]; then
    salt_master=10.18.1.10
elif [[ $branch == release/* ]] || [[ $branch == hotfix/* ]]; then
    salt_master=10.17.0.7
fi

ng_repo="http://10.13.0.8:8081/nexus/content/sites/maxent-raw-ci"


head=`git ls-remote --heads git@gitlab.maxent-inc.com:infra/proxy-scanner.git |grep $branch |cut -f1`

tar czf proxy_scanner_xconf.tgz xconf
md5sum  proxy_scanner_xconf.tgz > proxy_scanner_xconf.tgz+md5
curl -v --user 'admin:admin123' --upload-file ./proxy_scanner_xconf.tgz "$ng_repo/$component/$head/proxy_scanner_xconf.tgz"
curl -v --user 'admin:admin123' --upload-file ./proxy_scanner_xconf.tgz+md5 "$ng_repo/$component/$head/proxy_scanner_xconf.tgz+md5"

cd ./target; tar czf proxy_scanner.tgz *.tar.gz
md5sum proxy_scanner.tgz > proxy_scanner.tgz+md5
curl -v --user 'admin:admin123' --upload-file ./proxy_scanner.tgz "$ng_repo/$component/$head/proxy_scanner.tgz"
curl -v --user 'admin:admin123' --upload-file ./proxy_scanner.tgz+md5 "$ng_repo/$component/$head/proxy_scanner.tgz+md5"

ssh track@$salt_master "sudo salt '*' saltutil.refresh_pillar;sudo salt -I 'roles:proxy_scanner' state.sls pillar=\"{'CI':'yes','head':'$head'}\" proxy_scanner"
