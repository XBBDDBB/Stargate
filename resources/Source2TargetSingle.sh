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

SYNC_DIRECTION=1
result=$1

if [[ $result =~ ^[^.]+\.[^.]+$ ]]; then
    if [ -z "$2" ]; then
        echo "输入参数不合法！"
        exit 1
    fi
    DELETE_TYPE=$2
    if [ "$DELETE_TYPE" = "3" ]; then
        if [ -z "$3" ] || [ -z "$4" ]; then
            echo "输入参数不合法！"
            exit 1
        fi
        DATA_DATE=$3
        EVA_ID=$4
    else
        DATA_DATE=19000101
        EVA_ID=0
    fi
    result=$result"."$DELETE_TYPE
    sh ${QTB_BIN_PATH}/QuantumTransitionBridge.sh $result $SYNC_DIRECTION $DATA_DATE $EVA_ID

    exit_code=$?

    exit $exit_code
else
    echo "输入参数不合法！"
    exit 1
fi
