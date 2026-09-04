#!/bin/bash
readonly V_RUN_PATH=$(readlink -e "$(dirname "$0")")
if ((${#V_RUN_PATH} == 0)); then
  echo "错误：无法获取脚本所在路径，请检查脚本。"
  exit 1
fi

readonly V_UP_PATH=$(echo ${V_RUN_PATH%/*})

readonly PTL_BIN_PATH=${V_RUN_PATH}
readonly PTL_CONFIG_PATH=${V_UP_PATH}/config
readonly PTL_LIB_PATH=${V_UP_PATH}/lib
readonly PTL_BACKUP_PATH=${V_UP_PATH}/backups
source ${PTL_LIB_PATH}/function.conf

source ${PTL_CONFIG_PATH}/System.conf
source ${PTL_CONFIG_PATH}/$1.conf

BACKUP_PATH=${PTL_BACKUP_PATH}/${DB_NAME}/
CONFIG_PATH=${PTL_CONFIG_PATH}/

TABLE_NAME=$2

java -Dfile.encoding=UTF-8 -jar  ${PTL_LIB_PATH}/photon-transmission-lens.jar ${DB_NAME} ${DRIVER_CLASS} ${JDBC_URL} ${USER_NAME} ${PASS_WORD} ${CONFIG_PATH} ${TABLE_NAME}

exit_code=$?

if [ $exit_code -eq 0 ]; then
    log "「光子透射」光子透射率100%。"
else
    log "「光子透射」光子透射率0%！"
    exit 1
fi