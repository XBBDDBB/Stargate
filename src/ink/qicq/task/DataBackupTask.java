package ink.qicq.task;

import ink.qicq.utils.LogUtils;
import ink.qicq.utils.ResultSetUtils;
import ink.qicq.utils.SQLUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataBackupTask extends BackupTask{
    public DataBackupTask(Map<String, String> paramMap) {
        super(paramMap);
    }

    @Override
    public String getObjListByUser(String DBName, String user) {
        return null;
    }

    @Override
    public String getSqlByUserAndTableName(String DBName, String user, String tableName) {
        return null;
    }

    @Override
    public String getObjListColumnName() {
        return null;
    }

    @Override
    public String getSqlColumnName() {
        return null;
    }

    @Override
    public String getType() {
        return null;
    }

    @Override
    public String specialTreatment(String tableName, String sql) {
        return null;
    }

    @Override
    public String backup(String backupUser) {
        backupUser = backupUser.toUpperCase();
        super.result = new StringBuffer("");
        List<String> tableList = new ArrayList<String>();
        ResultSet resultSet;
        List<String> columnList;
        Map<String, String> columnMapping;
        StringBuffer columnsSql;
        StringBuffer insertSql;
        StringBuffer selectSql;
        int totalCount = 0;
        int currentInt = 0;
        int bufferForCount = 0;
        String valueSql;
        try{
            resultSet = super.dbUtils.getNewRs(SQLUtils.generateAllTableListSqlByTableSchema(super.dbUtils.getDBName(), backupUser));
            while (resultSet.next()) {
                tableList.add(resultSet.getString("TABLE_FULL_NAME").toUpperCase());
            }
            super.dbUtils.closeAllExceptConnection();
            File file = createDirectorys("", backupUser+"_ALL_DATA");
            FileOutputStream fos = new FileOutputStream(file);
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            BufferedWriter bw = new BufferedWriter(osw);
            for(String currentTableName:tableList){
                currentTableName = currentTableName.replace(backupUser+".","");
                resultSet = super.dbUtils.getNewRs(SQLUtils.generateTableStructureSqlByTableSchemaAndTableName(super.dbUtils.getDBName(), backupUser, currentTableName));
                columnList = new ArrayList<String>();
                columnMapping = new HashMap<String, String>();
                while (resultSet.next()) {
                    columnList.add(resultSet.getString("COLUMN_NAME").toUpperCase());
                    columnMapping.put(resultSet.getString("COLUMN_NAME").toUpperCase(), resultSet.getString("DATA_TYPE").toUpperCase());
                }
                super.dbUtils.closeAllExceptConnection();
                //拼接字段sql
                columnsSql = new StringBuffer("");
                for(int i = 0;i<columnList.size();i++){
                    if(i==columnList.size()-1){
                        columnsSql.append(columnList.get(i));
                    }else{
                        columnsSql.append(columnList.get(i)+",");
                    }
                }
                //拼接insert预处理语句
                insertSql = new StringBuffer("insert into "+backupUser+"."+currentTableName+"("+columnsSql.toString()+") values(");
                //拼接select语句
                selectSql = new StringBuffer("select "+columnsSql.toString()+" from "+backupUser+"."+currentTableName);
                resultSet = super.dbUtils.getNewRs(SQLUtils.generateTableCountSqlByTableSchemaAndTableName(super.dbUtils.getDBName(), backupUser, currentTableName));
                while (resultSet.next()) {
                    totalCount = resultSet.getInt(1);
                }
                super.dbUtils.closeAllExceptConnection();
                if ("0".equals(super.paramMap.get("bufferSwitch")) || ("1".equals(super.paramMap.get("bufferSwitch")) && totalCount <= Integer.parseInt(super.paramMap.get("bufferSize")))) {
                    //0表示无缓冲区，即全量读全量写，内存大就是任性
                    //或者，如果总条数，还没有缓冲区大，那还缓冲个屁了，就当全量干了
                    resultSet = super.dbUtils.getNewRs(selectSql.toString());
                    while(resultSet.next()){
                        valueSql = ResultSetUtils.preparedStatementAppending(columnList,columnMapping,resultSet,super.dbUtils.getDBName());
                        valueSql = valueSql.substring(0, valueSql.length()-1);
                        bw.write(insertSql+valueSql+");");
                        bw.newLine();
                        bw.flush();
                    }
                    super.dbUtils.closeAllExceptConnection();
                } else if ("1".equals(super.paramMap.get("bufferSwitch")) && totalCount > Integer.parseInt(super.paramMap.get("bufferSize"))) {
                    //这里是需要走缓冲的，需要分页读取，然后再写
                    bufferForCount = (int) Math.ceil(totalCount * 1.0 / Double.parseDouble(super.paramMap.get("bufferSize") + ""));
                    for(int i = 0;i<bufferForCount;i++){
                        resultSet = super.dbUtils.getNewRs(SQLUtils.generateTableSelectSqlWithPageNumberAndPageSize(super.dbUtils.getDBName(), selectSql.toString(), i, Integer.parseInt(super.paramMap.get("bufferSize"))));
                        currentInt=0;
                        while (resultSet.next()) {
                            currentInt++;
                            valueSql = ResultSetUtils.preparedStatementAppending(columnList,columnMapping,resultSet,super.dbUtils.getDBName());
                            valueSql = valueSql.substring(0, valueSql.length()-1);
                            bw.write(insertSql+valueSql+");");
                            bw.newLine();
                            bw.flush();
                        }
                        super.dbUtils.closeAllExceptConnection();
                    }
                }
            }
            super.dbUtils.closeAll();
            if(bw!=null)
                bw.close();
            if(osw!=null)
                osw.close();
            if(fos!=null)
                fos.close();
        }catch (Exception e){
            super.result.append("用户「"+backupUser+"」查询所有信息失败！"+e.getMessage()+"。");
        }
        return super.result.toString();
    }
}
