component=$1
version=$2
subversion=$3 

ng_repo="http://10.13.0.8:8081/nexus/content/sites/maxent-raw-releases"

tar czf proxy_scanner_xconf.tgz xconf
md5sum proxy_scanner_xconf.tgz > proxy_scanner_xconf.tgz+md5
curl -v --user 'admin:admin123' --upload-file ./proxy_scanner_xconf.tgz "$ng_repo/$component/$version/$version.$subversion/proxy_scanner_xconf.tgz"
curl -v --user 'admin:admin123' --upload-file ./proxy_scanner_xconf.tgz+md5 "$ng_repo/$component/$version/$version.$subversion/proxy_scanner_xconf.tgz+md5"

cd ./target; tar czf proxy_scanner.tgz *.tar.gz
md5sum proxy_scanner.tgz > proxy_scanner.tgz+md5
curl -v --user 'admin:admin123' --upload-file ./proxy_scanner.tgz "$ng_repo/$component/$version/$version.$subversion/proxy_scanner.tgz"
curl -v --user 'admin:admin123' --upload-file ./proxy_scanner.tgz+md5 "$ng_repo/$component/$version/$version.$subversion/proxy_scanner.tgz+md5"
