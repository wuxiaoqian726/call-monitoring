#!/bin/sh

#===========================================================================================
# Java Environment Setting
#===========================================================================================
error_exit ()
{
    echo "ERROR: $1 !!"
    exit 1
}

[ ! -e "$JAVA_HOME/bin/java" ] && JAVA_HOME=$HOME/jdk/java
[ ! -e "$JAVA_HOME/bin/java" ] && JAVA_HOME=/usr/java
[ ! -e "$JAVA_HOME/bin/java" ] && error_exit "Please set the JAVA_HOME variable in your environment, We need java(x64)!"

export JAVA_HOME
export JAVA="$JAVA_HOME/bin/java"
export BASE_DIR=$(dirname $0)/..
$ export CLASSPATH=.:${BASE_DIR}:${CLASSPATH}

#===========================================================================================
# JVM Configuration
#===========================================================================================
# The RAMDisk initializing size in MB on Darwin OS for gc-log
DIR_SIZE_IN_MB=600

choose_gc_log_directory()
{
    case "`uname`" in
        Darwin)
            if [ ! -d "/Volumes/RAMDisk" ]; then
                # create ram disk on Darwin systems as gc-log directory
                DEV=`hdiutil attach -nomount ram://$((2 * 1024 * DIR_SIZE_IN_MB))` > /dev/null
                diskutil eraseVolume HFS+ RAMDisk ${DEV} > /dev/null
                echo "Create RAMDisk /Volumes/RAMDisk for gc logging on Darwin OS."
            fi
            GC_LOG_DIR="/Volumes/RAMDisk"
        ;;
        *)
            # check if /dev/shm exists on other systems
            if [ -d "/dev/shm" ]; then
                GC_LOG_DIR="/dev/shm"
            else
                GC_LOG_DIR=${BASE_DIR}
            fi
        ;;
    esac
}

choose_gc_log_directory

JAVA_OPT="${JAVA_OPT} -server -Xms4g -Xmx4g -Xmn1g"
JAVA_OPT="${JAVA_OPT} -XX:+UseG1GC -XX:G1HeapRegionSize=16m -XX:G1ReservePercent=25 -XX:InitiatingHeapOccupancyPercent=30 -XX:SoftRefLRUPolicyMSPerMB=0"
JAVA_OPT="${JAVA_OPT} -verbose:gc -Xloggc:${GC_LOG_DIR}/rmq_broker_gc_%p_%t.log -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -XX:+PrintAdaptiveSizePolicy"
JAVA_OPT="${JAVA_OPT} -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=5 -XX:GCLogFileSize=30m"
JAVA_OPT="${JAVA_OPT} -XX:-OmitStackTraceInFastThrow"
JAVA_OPT="${JAVA_OPT} -XX:+AlwaysPreTouch"
JAVA_OPT="${JAVA_OPT} -XX:MaxDirectMemorySize=15g"
JAVA_OPT="${JAVA_OPT} -XX:-UseLargePages -XX:-UseBiasedLocking"
JAVA_OPT="${JAVA_OPT} -Djava.ext.dirs=${JAVA_HOME}/jre/lib/ext:${BASE_DIR}/lib:${JAVA_HOME}/lib/ext"
#JAVA_OPT="${JAVA_OPT} -Xdebug -Xrunjdwp:transport=dt_socket,address=9555,server=y,suspend=n"
JAVA_OPT="${JAVA_OPT} ${JAVA_OPT_EXT}"
JAVA_OPT="${JAVA_OPT} -cp ${CLASSPATH}"

$JAVA ${JAVA_OPT} server-1.0.0.jar com.raymond.callmonitoring.server.Server
