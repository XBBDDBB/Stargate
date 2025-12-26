package ink.qicq.utils;

public class SQLUtils {
    public static String generateTableStructureSqlByTableSchemaAndTableName(String DBType, String schema, String name){
        String sql = null;
        if("GBase8AMPP".equals(DBType)){
            sql = "SELECT COLUMN_NAME,DATA_TYPE FROM INFORMATION_SCHEMA.COLUMNS WHERE UPPER(TABLE_SCHEMA)='" + schema + "' AND UPPER(TABLE_NAME)='" + name + "' ORDER BY ORDINAL_POSITION";
        }else if("DM8".equals(DBType)){
            sql = "SELECT COLUMN_NAME,DATA_TYPE FROM ALL_TAB_COLUMNS WHERE UPPER(OWNER)='" + schema + "' AND UPPER(TABLE_NAME)='" + name + "' ORDER BY COLUMN_ID";
        }else if("GaussDB".equals(DBType)){
            sql = "SELECT COLUMN_NAME,DATA_TYPE FROM PG_CATALOG.MY_TAB_COLUMNS WHERE UPPER(SCHEMA)='" + schema + "' AND UPPER(TABLE_NAME)='" + name + "' ORDER BY COLUMN_ID";
        }
        return sql;
    }
    public static String generateTableCountSqlByTableSchemaAndTableName(String DBType, String schema, String name){
        String sql = null;
        if("DM8".equals(DBType) || "GBase8AMPP".equals(DBType) || "GaussDB".equals(DBType)){
            sql = "SELECT COUNT(*) AS CT FROM "+schema+"."+name;
        }
        return sql;
    }
    public static String generateTableSelectSqlWithPageNumberAndPageSize(String DBType, String oriSql, int pageNumber, int pageSize){
        String sql = null;
        if("DM8".equals(DBType)){
            sql = oriSql+" LIMIT "+(pageNumber*pageSize)+","+pageSize;
        }else if("GaussDB".equals(DBType)){
            sql = oriSql+" ORDER BY CTID LIMIT "+(pageNumber*pageSize)+","+pageSize;
        }else if("GBase8AMPP".equals(DBType)){
            sql = oriSql+" ORDER BY ROWID,SEGMENT_ID LIMIT "+(pageNumber*pageSize)+","+pageSize;
        }
        return sql;
    }
    public static String generateTableDeleteSqlByTableSchemaAndTableName(String DBType, String schema, String name){
        String sql = null;
        if("GBase8AMPP".equals(DBType) || "DM8".equals(DBType) || "GaussDB".equals(DBType)){
            sql = "DELETE FROM "+schema+"."+name;
        }
        return sql;
    }
    public static String generateTableTruncateSqlByTableSchemaAndTableName(String DBType, String schema, String name){
        String sql = null;
        if("GBase8AMPP".equals(DBType) || "DM8".equals(DBType) || "GaussDB".equals(DBType)){
            sql = "TRUNCATE TABLE "+schema+"."+name;
        }
        return sql;
    }
    public static String generateTablePartitionTypeSqlByTableSchemaAndTableName(String DBType, String schema, String name){
        String sql = null;
        if("GaussDB".equals(DBType)){
            sql = "SELECT DISTINCT CASE WHEN UPPER(PARTITION_NAME) LIKE 'P_________' THEN '1' WHEN UPPER(PARTITION_NAME) LIKE 'P__________%' THEN '2' END AS PARTITION_NM_TYPE,CASE WHEN REPLACE(HIGH_VALUE,'-','') LIKE '________' THEN '1' WHEN REPLACE(HIGH_VALUE,'-','') LIKE '__________%' THEN '2' END AS HIGH_VALUE_TYPE FROM PG_CATALOG.MY_TAB_PARTITIONS WHERE UPPER(SCHEMA) = '" + schema + "' AND UPPER(TABLE_NAME) = '"+ name + "'";
        }
        return sql;
    }
    public static String generateCheckPartitionIsExistSqlByTableSchemaAndTableNameAndPartitionName(String DBType, String schema, String name, String partitionName){
        String sql = null;
        if("GaussDB".equals(DBType)){
            sql = "SELECT COUNT(*) AS CT FROM PG_CATALOG.MY_TAB_PARTITIONS WHERE UPPER(SCHEMA) = '" + schema + "' AND UPPER(TABLE_NAME) = UPPER('" + name + "') AND UPPER(PARTITION_NAME) = UPPER('" + partitionName + "')";
        }
        return sql;
    }
    public static String generateTruncatePartitionSqlByTableSchemaAndTableNameAndPartitionName(String DBType, String schema, String name, String partitionName){
        String sql = null;
        if("GaussDB".equals(DBType)){
            sql = "ALTER TABLE "+schema+"."+name+" TRUNCATE PARTITION "+partitionName;
        }
        return sql;
    }
    public static String generateCreatePartitionSqlByTableSchemaAndTableNameAndPartitionNameAndPartitionId(String DBType, String schema, String name, String partitionName, String partitionId){
        String sql = null;
        if("GaussDB".equals(DBType)){
            sql = "ALTER TABLE "+schema+"."+name+" ADD PARTITION "+partitionName+" VALUES('"+partitionId+"')";
        }
        return sql;
    }
}
