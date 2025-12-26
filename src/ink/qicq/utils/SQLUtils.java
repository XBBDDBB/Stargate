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




    public static String generateAllTableListSqlByTableSchema(String DBType, String schema){
        String sql = null;
        if("GBase8AMPP".equals(DBType)){
            sql = "SELECT TABLE_NAME AS TABLE_FULL_NAME FROM INFORMATION_SCHEMA.TABLES WHERE UPPER(TABLE_SCHEMA)=UPPER('" + schema + "') AND UPPER(TABLE_TYPE)=UPPER('BASE TABLE')";
        }else if("GaussDB".equals(DBType)){
            sql = "SELECT SCHEMANAME||'.'||TABLENAME AS TABLE_FULL_NAME FROM PG_CATALOG.PG_TABLES WHERE UPPER(SCHEMANAME)=UPPER('" + schema + "')";
        }
        return sql;
    }
    public static String generateCreateTableSqlByTableSchemaAndName(String DBType, String schema, String name){
        String sql = null;
        if("GBase8AMPP".equals(DBType)){
            sql = "SHOW CREATE TABLE " + schema + "." + name;
        }else if("GaussDB".equals(DBType)){
            //GaussDB的name其实已经时schema.tablename了
            sql = "SELECT PG_GET_TABLEDEF('" +  name + "')";
        }
        return sql;
    }
    public static String generateAllFunctionListSqlByTableSchema(String DBType, String schema){
        String sql = null;
        if("GBase8AMPP".equals(DBType)){
            sql = "SELECT ROUTINE_NAME AS FULL_FUNCTION_NAME FROM INFORMATION_SCHEMA.ROUTINES WHERE UPPER(ROUTINE_SCHEMA)=UPPER('" + schema + "') AND UPPER(ROUTINE_TYPE)=UPPER('FUNCTION')";
        }else if("GaussDB".equals(DBType)){
            sql = "SELECT OBJECT_ID||'$D$M$'||OBJECT_NAME AS FULL_FUNCTION_NAME FROM MY_OBJECTS " +
                    "WHERE OBJECT_NAME IN ( " +
                    "SELECT PRONAME FROM PG_PROC WHERE PRONAMESPACE IN ( " +
                    "SELECT NAMESPACE FROM MY_OBJECTS WHERE OBJECT_NAME IN ( " +
                    "SELECT TABLENAME FROM PG_CATALOG.PG_TABLES WHERE UPPER(SCHEMANAME)=UPPER('" + schema + "') LIMIT 1 " +
                    ") " +
                    "AND UPPER(OBJECT_TYPE) = 'TABLE' " +
                    ") " +
                    ") " +
                    "AND UPPER(OBJECT_TYPE)='FUNCTION'";
        }
        return sql;
    }
    public static String generateCreateFunctionSqlByTableSchemaAndName(String DBType, String schema, String name){
        String sql = null;
        if("GBase8AMPP".equals(DBType)){
            sql = "SHOW CREATE FUNCTION " + schema + "." + name;
        }else if("GaussDB".equals(DBType)){
            //GaussDB获取函数DDL无法通过名字直接获得，需要先根据函数名字获取OBJECT_ID，然后通过OBJECT_ID再获取DDL，name字段目前是【OBJECT_ID$D$M$函数名字】的格式。
            sql = "SELECT PG_GET_FUNCTIONDEF(" + name.split("\\$D\\$M\\$")[0] + ")";
        }
        return sql;
    }
    public static String generateAllProcedureListSqlByTableSchema(String DBType, String schema){
        String sql = null;
        if("GBase8AMPP".equals(DBType)){
            sql = "SELECT ROUTINE_NAME AS FULL_PROCEDURE_NAME FROM INFORMATION_SCHEMA.ROUTINES WHERE UPPER(ROUTINE_SCHEMA)=UPPER('" + schema + "') AND UPPER(ROUTINE_TYPE)=UPPER('PROCEDURE')";
        }else if("GaussDB".equals(DBType)){
            sql = "SELECT OBJECT_ID||'$D$M$'||OBJECT_NAME AS FULL_PROCEDURE_NAME FROM MY_OBJECTS " +
                    "WHERE OBJECT_NAME IN ( " +
                    "SELECT PRONAME FROM PG_PROC WHERE PRONAMESPACE IN ( " +
                    "SELECT NAMESPACE FROM MY_OBJECTS WHERE OBJECT_NAME IN ( " +
                    "SELECT TABLENAME FROM PG_CATALOG.PG_TABLES WHERE UPPER(SCHEMANAME)=UPPER('" + schema + "') LIMIT 1 " +
                    ") " +
                    "AND UPPER(OBJECT_TYPE) = 'TABLE' " +
                    ") " +
                    ") " +
                    "AND UPPER(OBJECT_TYPE)='PROCEDURE'";
        }
        return sql;
    }
    public static String generateCreateProcedureSqlByTableSchemaAndName(String DBType, String schema, String name){
        String sql = null;
        if("GBase8AMPP".equals(DBType)){
            sql = "SHOW CREATE PROCEDURE " + schema + "." + name;
        }else if("GaussDB".equals(DBType)){
            //GaussDB获取存过DDL无法通过名字直接获得，需要先根据存过名字获取OBJECT_ID，然后通过OBJECT_ID再获取DDL，name字段目前是【OBJECT_ID$D$M$存过名字】的格式。
            //GaussDB，查函数查存过，都是这一个，不是写错了。
            sql = "SELECT PG_GET_FUNCTIONDEF(" + name.split("\\$D\\$M\\$")[0] + ")";
        }
        return sql;
    }
    public static String generateAllViewListSqlByTableSchema(String DBType, String schema){
        String sql = null;
        if("GBase8AMPP".equals(DBType)){
            sql = "SELECT TABLE_NAME AS VIEW_FULL_NAME FROM INFORMATION_SCHEMA.VIEWS WHERE UPPER(TABLE_SCHEMA)=UPPER('" + schema + "')";
        }else if("GaussDB".equals(DBType)){
            sql = "SELECT SCHEMANAME||'.'||VIEWNAME AS VIEW_FULL_NAME FROM PG_CATALOG.PG_VIEWS WHERE UPPER(SCHEMANAME)=UPPER('" + schema + "')";
        }
        return sql;
    }
    public static String generateCreateViewSqlByTableSchemaAndName(String DBType, String schema, String name){
        String sql = null;
        if("GBase8AMPP".equals(DBType)){
            sql = "SHOW CREATE VIEW " + schema + "." + name;
        }else if("GaussDB".equals(DBType)){
            //GaussDB的name其实已经时schema.tablename了
            sql = "SELECT PG_GET_VIEWDEF('" + name + "')";
        }
        return sql;
    }
    public static String generateVCList(String DBType){
        String sql = null;
        if("GBase8AMPP".equals(DBType)){
            sql = "SELECT ID AS VC FROM INFORMATION_SCHEMA.VC UNION SELECT NAME AS VC FROM INFORMATION_SCHEMA.VC";
        }
        return sql;
    }
}
