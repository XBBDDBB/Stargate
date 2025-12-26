package ink.qicq.utils;


import java.util.List;
import java.util.Map;

public class TaskUtils {
    private String schemaName;
    private String tableName;
    private Long totalDataCount;
    private String columnSql;
    private String selectSql;
    private String insertSql;
    private List<String> columnList;
    private Map<String, String> columnMapping;

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Long getTotalDataCount() {
        return totalDataCount;
    }

    public void setTotalDataCount(Long totalDataCount) {
        this.totalDataCount = totalDataCount;
    }

    public String getColumnSql() {
        return columnSql;
    }

    public void setColumnSql(String columnSql) {
        this.columnSql = columnSql;
    }

    public String getSelectSql() {
        return selectSql;
    }

    public void setSelectSql(String selectSql) {
        this.selectSql = selectSql;
    }

    public String getInsertSql() {
        return insertSql;
    }

    public void setInsertSql(String insertSql) {
        this.insertSql = insertSql;
    }

    public List<String> getColumnList() {
        return columnList;
    }

    public void setColumnList(List<String> columnList) {
        this.columnList = columnList;
    }

    public Map<String, String> getColumnMapping() {
        return columnMapping;
    }

    public void setColumnMapping(Map<String, String> columnMapping) {
        this.columnMapping = columnMapping;
    }

    @Override
    public String toString() {
        return "TaskUtils{" +
                "schemaName='" + schemaName + '\'' +
                ", tableName='" + tableName + '\'' +
                ", totalDataCount=" + totalDataCount +
                ", columnSql='" + columnSql + '\'' +
                ", selectSql='" + selectSql + '\'' +
                ", insertSql='" + insertSql + '\'' +
                ", columnList=" + columnList +
                ", columnMapping=" + columnMapping +
                '}';
    }
}
