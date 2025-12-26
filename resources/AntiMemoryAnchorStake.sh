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
source ${MAS_LIB_PATH}/function.conf

log "「反记忆锚点」开始解析记忆锚点"

BACKUP_DATABASE_NAME=$1

java -Dfile.encoding=UTF-8 -jar  ${MAS_LIB_PATH}/memory-anchor-stake.jar MASCreateFile ${MAS_BACKUP_PATH} ${BACKUP_DATABASE_NAME} ${MAS_BACKUP_PATH}

log "「反记忆锚点」本次记忆锚点生成完毕。"


