#!/bin/sh  
  
WORK_DIR=$(cd `dirname $0`; pwd)/../
LOG_CONF=file:${WORK_DIR}/conf/logback.xml
LOG_DIR=/services/logs/proxy-scanner

if [ ! -d "$LOG_DIR" ] ; then
   mkdir "$LOG_DIR"
fi

PID_FILE=/var/run/proxy-scanner.pid

JAVA=/usr/bin/java
JAVA_OPTS="-server -Xms4096m -Xmx4096m -Xmn1536m -XX:+HeapDumpOnOutOfMemoryError
 -XX:HeapDumpPath=/services/logs/proxy-scanner/oom.hprof -XX:+UseParNewGC -XX:+UseConcMarkSweepGC
 -XX:CMSInitiatingOccupancyFraction=75 -XX:+UseCMSInitiatingOccupancyOnly -XX:+ExplicitGCInvokesConcurrent
 -XX:-UseBiasedLocking -XX:+AlwaysPreTouch -XX:+CMSParallelRemarkEnabled -XX:AutoBoxCacheMax=20000
 -Dwork.dir=${WORK_DIR}
 -Dcom.sun.management.jmxremote.port=9000
 -Dcom.sun.management.jmxremote.ssl=false
 -Dcom.sun.management.jmxremote.authenticate=false
 -Dio.netty.leakDetection.level=advanced
 -Dlogger.file=${LOG_CONF} -Dfile.encoding=UTF-8 -Duser.timezone=UTC"
CLASS_PATH=" -classpath ":$(echo ${WORK_DIR}/lib/*.jar|sed 's/ /:/g')
CLASS_PATH=${CLASS_PATH}:${WORK_DIR}/conf/
CLASS=com.maxent.proxy.App

cd $WORK_DIR
  
case "$1" in  
  
  start)
  	if [ -f "${PID_FILE}" ]; then
    	echo "proxy-scanner is running,pid=`cat ${PID_FILE}`."
    else
    	exec "$JAVA" $JAVA_OPTS $CLASS_PATH $CLASS >> ${LOG_DIR}/startup.log 2>&1 &
		echo "proxy-scanner is running,pid=$!."
    	echo $! > ${PID_FILE}
    fi
    ;;  
  
  stop)  
  	if [ -f "${PID_FILE}" ]; then
    	kill -9 `cat ${PID_FILE}`  
    	rm -rf ${PID_FILE}  
    	echo "proxy-scanner is stopped."
    else
    	echo "proxy-scanner is not running."
    fi
    ;;  
  
  restart)  
    $0 stop
    sleep 1  
    $0 start
    ;;  

  status)
  	if [ -f "${PID_FILE}" ]; then
    	echo "proxy-scanner is running,pid=`cat ${PID_FILE}`."
    else
    	echo "proxy-scanner is not running."
    fi
    ;;
    
  *)  
    echo "Usage: proxy-scanner.sh {start|stop|restart|status}"
    ;;  
  
esac
  
exit 0 