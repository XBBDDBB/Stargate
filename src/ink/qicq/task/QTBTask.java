package ink.qicq.task;


import java.util.List;
import java.util.Map;

public class QTBTask {
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

    public QTBTask setSchemaName(String schemaName) {
        this.schemaName = schemaName;
        return this;
    }

    public String getTableName() {
        return tableName;
    }

    public QTBTask setTableName(String tableName) {
        this.tableName = tableName;
        return this;
    }

    public Long getTotalDataCount() {
        return totalDataCount;
    }

    public QTBTask setTotalDataCount(Long totalDataCount) {
        this.totalDataCount = totalDataCount;
        return this;
    }

    public String getColumnSql() {
        return columnSql;
    }

    public QTBTask setColumnSql(String columnSql) {
        this.columnSql = columnSql;
        return this;
    }

    public String getSelectSql() {
        return selectSql;
    }

    public QTBTask setSelectSql(String selectSql) {
        this.selectSql = selectSql;
        return this;
    }

    public String getInsertSql() {
        return insertSql;
    }

    public QTBTask setInsertSql(String insertSql) {
        this.insertSql = insertSql;
        return this;
    }

    public List<String> getColumnList() {
        return columnList;
    }

    public QTBTask setColumnList(List<String> columnList) {
        this.columnList = columnList;
        return this;
    }

    public Map<String, String> getColumnMapping() {
        return columnMapping;
    }

    public QTBTask setColumnMapping(Map<String, String> columnMapping) {
        this.columnMapping = columnMapping;
        return this;
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
