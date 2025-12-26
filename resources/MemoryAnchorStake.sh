#!/bin/bash
readonly V_RUN_PATH=$(readlink -e "$(dirname "$0")")
if ((${#V_RUN_PATH} == 0)); then
  echo "错误：无法获取脚本所在路径，请检查脚本。"
  exit 1
fi

readonly V_UP_PATH=$(echo ${V_RUN_PATH%/*})

readonly MAS_BIN_PATH=${V_RUN_PATH}
readonly MAS_CONFIG_PATH=${V_UP_PATH}/config
readonly MAS_LIB_PATH=${V_UP_PATH}/lib
readonly MAS_BACKUP_PATH=${V_UP_PATH}/backups

echo "「记忆锚点」开始生成记忆锚点"

source ${MAS_CONFIG_PATH}/System.conf
source ${MAS_CONFIG_PATH}/$1

BACKUP_PATH=${MAS_BACKUP_PATH}/${DB_NAME}/
CONFIG_PATH=${MAS_CONFIG_PATH}/

DEL_DATE=$(date -d "1 month ago" +%Y%m%d)
echo "「记忆锚点」本次清理历史的日期为：${DEL_DATE}"
find ${BACKUP_PATH} -type d -name "${DEL_DATE}*" -exec rm -rf {} \;

java -Dfile.encoding=UTF-8 -jar  ${MAS_LIB_PATH}/ink.qicq.backups.jar ${DB_NAME} ${DRIVER_CLASS} ${JDBC_URL} ${USER_NAME} ${PASS_WORD} ${BACK_SCHEMA} ${BACK_DATA} ${BUFFER_SIZE} ${CONFIG_PATH} ${BACKUP_PATH}

echo "「记忆锚点」本次记忆锚点生成完毕。"


