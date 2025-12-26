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

SYNC_DIRECTION=2
result=""
tablepath=${QTB_TABLE_PATH}/$1
if [[ -f "$tablepath" ]]; then
    while read id fullName fundType deleteType
    do
    if [[ "$id" =~ ^[[:space:]]*$ ]] || [[ "$id" =~ ^[[:space:]]*# ]]; then
        continue
    fi
    if [[ "$fundType" -eq 2 ]]; then
        result+="$fullName.$deleteType,"
    fi
    done < $tablepath
    result=${result%,}
    if [ -z "$2" ] || [ -z "$3" ]; then
        echo "输入参数不合法！"
        exit 1
    fi
    DATA_DATE=$2
    EVA_ID=$3

    sh ${QTB_BIN_PATH}/QuantumTransitionBridge.sh $result $SYNC_DIRECTION $DATA_DATE $EVA_ID

    exit_code=$?

    exit $exit_code
else
    echo "输入参数不合法！"
    exit 1
fi
