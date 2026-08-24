#!/bin/bash
readonly V_RUN_PATH=$(readlink -e "$(dirname "$0")")
if ((${#V_RUN_PATH} == 0)); then
  echo "错误：无法获取脚本所在路径，请检查脚本。"
  exit 1
fi

readonly V_UP_PATH=$(echo ${V_RUN_PATH%/*})

readonly CTN_BIN_PATH=${V_RUN_PATH}
readonly CTN_CONFIG_PATH=${V_UP_PATH}/config
readonly CTN_LIB_PATH=${V_UP_PATH}/lib
readonly CTN_BACKUP_PATH=${V_UP_PATH}/backups
source ${CTN_LIB_PATH}/function.conf

source ${CTN_CONFIG_PATH}/System.conf
source ${CTN_CONFIG_PATH}/$1.conf

BACKUP_PATH=${CTN_BACKUP_PATH}/${DB_NAME}/
CONFIG_PATH=${CTN_CONFIG_PATH}/

EXEC_SQL=$2

java -Dfile.encoding=UTF-8 -jar  ${CTN_LIB_PATH}/command-targeting-needle.jar ${DB_NAME} ${DRIVER_CLASS} ${JDBC_URL} ${USER_NAME} ${PASS_WORD} ${CONFIG_PATH} "${EXEC_SQL}"

exit_code=$?

if [ $exit_code -eq 0 ]; then
    log "「指令靶向」本次指令靶向治疗完毕。"
else
    log "「指令靶向」本次指令靶向治疗失败！"
    exit 1
fi