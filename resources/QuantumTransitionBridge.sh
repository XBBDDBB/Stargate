#!/bin/bash
readonly V_RUN_PATH=$(readlink -e "$(dirname "$0")")
if ((${#V_RUN_PATH} == 0)); then
  echo "错误：无法获取脚本所在路径，请检查脚本。"
  exit 1
fi

readonly V_UP_PATH=$(echo ${V_RUN_PATH%/*})

readonly QTB_BIN_PATH=${V_RUN_PATH}
readonly QTB_CONFIG_PATH=${V_UP_PATH}/config
readonly QTB_LIB_PATH=${V_UP_PATH}/lib
readonly QTB_LOG_PATH=${V_UP_PATH}/logs
readonly QTB_TABLE_PATH=${V_UP_PATH}/table
readonly QTB_DATABASEPARAMETER_PATH=${QTB_CONFIG_PATH}/
source ${QTB_LIB_PATH}/function.conf
TABLE_LIST=$1
SYNC_DIRECTION=$2
DATA_DATE=$3
EVA_ID=$4

log "获取表信息成功，同步内容如下："$TABLE_LIST
log "开始读取系统配置。。。"
source ${QTB_CONFIG_PATH}/System.conf
log "读取系统配置完毕，开始读取数据库配置。。。"
source ${QTB_CONFIG_PATH}/${OLTP_DB}.conf
FIRST_DB_NAME=${DB_NAME}
FIRST_DRIVER_CLASS=${DRIVER_CLASS}
FIRST_JDBC_URL=${JDBC_URL}
FIRST_USER_NAME=${USER_NAME}
FIRST_PASS_WORD=${PASS_WORD}
source ${QTB_CONFIG_PATH}/${OLAP_DB}.conf
SECOND_DB_NAME=${DB_NAME}
SECOND_DRIVER_CLASS=${DRIVER_CLASS}
SECOND_JDBC_URL=${JDBC_URL}
SECOND_USER_NAME=${USER_NAME}
SECOND_PASS_WORD=${PASS_WORD}
log "数据库配置读取完毕，开始创建量子跃迁桥。。。"

find  $QTB_LOG_PATH -type f -name "QTB*.log" ! -mtime -14 -delete

if [[ "$SYNC_DIRECTION" -eq 1 ]]; then
  log "本次同步由「${FIRST_DB_NAME}」同步至「${SECOND_DB_NAME}」"
elif [[ "$SYNC_DIRECTION" -eq 2 ]]; then
  log "本次同步由「${SECOND_DB_NAME}」同步至「${FIRST_DB_NAME}」"
else
  log "未知同步方向，量子跃迁桥创建失败。"
  exit 1
fi

STDOUT_LOG="$QTB_LOG_PATH/QTBOUT_$(date +%Y%m%d%H%M%S).log"
STDERR_LOG="$QTB_LOG_PATH/QTBERR_$(date +%Y%m%d%H%M%S).log"

java -Dfile.encoding=UTF-8 -jar ${QTB_LIB_PATH}/ink.qicq.synchronization.jar $FIRST_DB_NAME $FIRST_DRIVER_CLASS $FIRST_JDBC_URL $FIRST_USER_NAME $FIRST_PASS_WORD $SECOND_DB_NAME $SECOND_DRIVER_CLASS $SECOND_JDBC_URL $SECOND_USER_NAME $SECOND_PASS_WORD $QTB_DATABASEPARAMETER_PATH $SYNC_DIRECTION $OUTPUT_TYPE $OUTPUT_URL $BUFFER_SIZE $COMMIT_COUNT $POOL_SIZE $POOL_SIZE $TABLE_LIST $ERROR_HANDING_TYPE $DATA_DATE $EVA_ID > $STDOUT_LOG 2> $STDERR_LOG

exit_code=$?

if [ $exit_code -eq 0 ]; then
    log "量子跃迁桥传输完成，本次跃迁已关闭。"
else
    log "量子跃迁桥传输失败，请查看错误日志。"
    exit 1
fi
