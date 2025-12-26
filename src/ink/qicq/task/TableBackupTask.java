package ink.qicq.task;

import ink.qicq.utils.SQLUtils;
import java.util.Map;

public class TableBackupTask extends BackupTask{

    public TableBackupTask(Map<String, String> paramMap) {
        super(paramMap);
    }
    @Override
    public String getObjListByUser(String DBName, String user) {
        return SQLUtils.generateAllTableListSqlByTableSchema(DBName, user);
    }

    @Override
    public String getSqlByUserAndTableName(String DBName, String user, String tableName) {
        return SQLUtils.generateCreateTableSqlByTableSchemaAndName(DBName, user, tableName);
    }

    @Override
    public String getObjListColumnName() {
        if("GBase8AMPP".equals(super.dbUtils.getDBName()) || "GaussDB".equals(super.dbUtils.getDBName())){
            return "TABLE_FULL_NAME";
        }
        return "ERROR";
    }

    @Override
    public String getSqlColumnName() {
        if("GBase8AMPP".equals(super.dbUtils.getDBName())){
            return "CREATE TABLE";
        }else if("GaussDB".equals(super.dbUtils.getDBName())){
            return "PG_GET_TABLEDEF";
        }
        return "ERROR";
    }

    @Override
    public String getType() {
        return "Table";
    }

    @Override
    public String specialTreatment(String tableName, String sql) {
        if("GBase8AMPP".equals(super.dbUtils.getDBName())){
            return "DROP TABLE IF EXISTS "+tableName+";"+"\n"+sql+";";
        }else if("GaussDB".equals(super.dbUtils.getDBName())){
            return "DROP TABLE IF EXISTS "+tableName+";"+"\n"+sql;
        }
        return sql;
    }

}
