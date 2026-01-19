package ink.qicq.task;

import ink.qicq.utils.SQLUtils;

import java.util.Map;

public class FunctionBackupTask extends BackupTask{

    public FunctionBackupTask(Map<String, String> paramMap) {
        super(paramMap);
    }

    @Override
    public String getObjListByUser(String DBName, String user) {
        return SQLUtils.generateAllFunctionListSqlByTableSchema(DBName, user);
    }

    @Override
    public String getSqlByUserAndTableName(String DBName, String user, String tableName) {
        return SQLUtils.generateCreateFunctionSqlByTableSchemaAndName(DBName, user, tableName);
    }

    @Override
    public String getObjListColumnName() {
        if("GBase8AMPP".equals(super.dbUtils.getDBName()) || "GaussDB".equals(super.dbUtils.getDBName())){
            return "FULL_FUNCTION_NAME";
        }
        return "ERROR";
    }

    @Override
    public String getSqlColumnName() {
        if("GBase8AMPP".equals(super.dbUtils.getDBName())){
            return "CREATE FUNCTION";
        }else if("GaussDB".equals(super.dbUtils.getDBName())){
            return "PG_GET_FUNCTIONDEF";
        }
        return "ERROR";
    }

    @Override
    public String getType() {
        return "Function";
    }

    @Override
    public String specialTreatment(String tableName, String sql) {
        if("GBase8AMPP".equals(super.dbUtils.getDBName())){
            String dbParameter = super.paramMap.get(super.dbUtils.getDBName()+"_parameter");
            dbParameter = dbParameter==null?"":dbParameter;
            return dbParameter+"DROP FUNCTION IF EXISTS "+tableName+";"+"\n"+"delimiter ;;"+"\n"+sql+"\n"+";;"+"\n"+"delimiter ;";
        }else if("GaussDB".equals(super.dbUtils.getDBName())){
            int index = sql.indexOf(",");
            sql = sql.substring(index+2);
            return sql.substring(0,sql.length()-2);
        }
        return sql;
    }
}
