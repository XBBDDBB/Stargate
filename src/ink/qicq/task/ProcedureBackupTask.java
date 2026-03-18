package ink.qicq.task;

import ink.qicq.utils.SQLUtils;

import java.util.Map;

public class ProcedureBackupTask extends BackupTask{

    public ProcedureBackupTask(Map<String, String> paramMap) {
        super(paramMap);
    }

    @Override
    public String getObjListByUser(String DBName, String user) {
        return SQLUtils.generateAllProcedureListSqlByTableSchema(DBName, user);
    }

    @Override
    public String getSqlByUserAndTableName(String DBName, String user, String tableName) {
        return SQLUtils.generateCreateProcedureSqlByTableSchemaAndName(DBName, user, tableName);
    }

    @Override
    public String getObjListColumnName() {
        if("GBase8AMPP".equals(super.dbUtils.getDBName()) || "GaussDB".equals(super.dbUtils.getDBName()) || "DM8".equals(super.dbUtils.getDBName())){
            return "FULL_PROCEDURE_NAME";
        }
        return "ERROR";
    }

    @Override
    public String getSqlColumnName() {
        if("GBase8AMPP".equals(super.dbUtils.getDBName())) {
            return "CREATE PROCEDURE";
        }else if("GaussDB".equals(super.dbUtils.getDBName())){
            return "PG_GET_FUNCTIONDEF";
        }else if("DM8".equals(super.dbUtils.getDBName())){
            return "PROCDDL";
        }
        return "ERROR";
    }

    @Override
    public String getType() {
        return "Procedure";
    }

    @Override
    public String specialTreatment(String tableName, String sql) {
        if("GBase8AMPP".equals(super.dbUtils.getDBName())){
            //因GBase的存储过程可能存在CTE，所以需要在前面添加session参数防止创建时报错。
            String dbParameter = super.paramMap.get(super.dbUtils.getDBName()+"_parameter");
            dbParameter = dbParameter==null?"":dbParameter;
            return dbParameter+"DROP PROCEDURE IF EXISTS "+tableName+";"+"\n"+"delimiter ;;"+"\n"+sql+"\n"+";;"+"\n"+"delimiter ;";
        }else if("GaussDB".equals(super.dbUtils.getDBName())){
            int index = sql.indexOf(",");
            sql = sql.substring(index+2);
            return sql.substring(0,sql.length()-2);
        }else if("DM8".equals(super.dbUtils.getDBName())){
            //DM8的存储过程自带CREATE OR REPLACE，所以这里什么都不加了。
            return sql;
        }
        return sql;
    }
}
