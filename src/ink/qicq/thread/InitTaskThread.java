package ink.qicq.thread;

import ink.qicq.utils.*;

import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public class InitTaskThread implements Callable {

    private Map<String,String> paramMap;
    private int threadId;
    private List<String> jobList;
    private String threadName;
    private DBUtils SourceDB = new DBUtils();;
    private DBUtils TargetDB = new DBUtils();;

    public InitTaskThread(Map<String,String> paramMap, int threadId, List<String> jobList) {
        this.paramMap = paramMap;
        this.threadId = threadId;
        this.threadName = "「第"+(threadId+1)+"号」";
        this.jobList = jobList;
    }

    @Override
    public Object call(){
        //初始化数据链接
        InitUtils initUtils = InitUtils.getInstance();
        initUtils.initDB(SourceDB,TargetDB,paramMap);
        LogUtils.recordINFOLog(this.threadName + "初始化数据连接成功，已经拿到任务列表，共计" + jobList.size() + "个任务，开始干活。。。","QTB");
        String currentSchemaName = null;
        String currentTableName = null;
        String[] splitArray = null;
        String deleteType = null;
        StringBuilder result = new StringBuilder("$O$K$");
        ResultSet rs = null;
        List<String> columnList = null;
        Map<String, String> columnMapping = null;
        StringBuffer columnsSql = null;
        StringBuffer insertSql = null;
        StringBuffer selectSql = null;
        long totalCount = 0;
        int bufferForCount = 0;
        String partitionType = null;
        String highValueType = null;
        String partitionName = null;
        String partitionId = null;
        int tempInt = 0;
        TaskUtils taskUtils = null;
        for (String currentTable : jobList) {
            try {
                //分隔任务列表中的表信息
                splitArray = currentTable.split("\\.");
                if (splitArray.length <= 1) {
                    //可能这个人懒，只写了个表名，那就所有的都用默认值
                    //这里因为要去目标表查表结构，所以要用目标信息
                    currentSchemaName = TargetDB.getCurrentUserName().toUpperCase();
                    currentTableName = splitArray[0].toUpperCase();
                    deleteType = "0";
                } else {
                    currentSchemaName = splitArray[0].toUpperCase();
                    currentTableName = splitArray[1].toUpperCase();
                    deleteType = splitArray[2];
                }
                LogUtils.recordINFOLog(this.threadName + "开始去目标数据库获取当前表「" + currentTable + "」表结构信息。。。","QTB");
                rs = TargetDB.getNewRs(SQLUtils.generateTableStructureSqlByTableSchemaAndTableName(TargetDB.getDBName(), currentSchemaName, currentTableName));
                columnList = new ArrayList<String>();
                columnMapping = new HashMap<String, String>();
                while (rs.next()) {
                    columnList.add(rs.getString("COLUMN_NAME").toUpperCase());
                    columnMapping.put(rs.getString("COLUMN_NAME").toUpperCase(), rs.getString("DATA_TYPE").toUpperCase());
                }
                if(columnList.size()==0 || columnMapping.size()==0){
                    LogUtils.recordERRORLog(threadName + "当前表「" + currentTable + "」表结构获取异常，未查询到本表的表结构！","QTB");
                    throw new Exception("表结构信息获取异常，请检查！");
                }
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
                insertSql = new StringBuffer("insert into "+currentSchemaName+"."+currentTableName+"("+columnsSql.toString()+") values(");
                for(int i = 0;i<columnList.size();i++){
                    if(i==columnList.size()-1){
                        insertSql.append("?");
                    }else{
                        insertSql.append("?,");
                    }
                }
                insertSql.append(")");
                //拼接select语句
                selectSql = new StringBuffer("select "+columnsSql.toString()+" from "+currentSchemaName+"."+currentTableName);
                TargetDB.closeAllExceptConnection();
                LogUtils.recordINFOLog(this.threadName + "表结构信息获取完毕，开始去来源数据库获取当前表「" + currentTable + "」表数据总数。。。","QTB");
                rs = SourceDB.getNewRs(SQLUtils.generateTableCountSqlByTableSchemaAndTableName(SourceDB.getDBName(), currentSchemaName, currentTableName));
                while (rs.next()) {
                    totalCount = rs.getLong(1);
                }
                SourceDB.closeAllExceptConnection();
                LogUtils.recordINFOLog(this.threadName + "表数据总数获取完毕，当前表「" + currentTable + "」共计" + totalCount + "条数据，开始进行数据清理工作。。。","QTB");
                if("1".equals(deleteType)){
                    LogUtils.recordINFOLog(this.threadName + "获取清理配置完毕，数据拆分前需要先DELETE全表，正在清理数据。。。","QTB");
                    TargetDB.getNewPst(SQLUtils.generateTableDeleteSqlByTableSchemaAndTableName(TargetDB.getDBName(), currentSchemaName, currentTableName)).execute();
                    TargetDB.closeAllExceptConnection();
                }else if("2".equals(deleteType)){
                    LogUtils.recordINFOLog(this.threadName + "获取清理配置完毕，数据拆分前需要先TRUNCATE全表，正在清理数据。。。","QTB");
                    TargetDB.getNewPst(SQLUtils.generateTableTruncateSqlByTableSchemaAndTableName(TargetDB.getDBName(), currentSchemaName, currentTableName)).execute();
                    TargetDB.closeAllExceptConnection();
                }else if("3".equals(deleteType)){
                    LogUtils.recordINFOLog(this.threadName + "获取清理配置完毕，数据拆分前需要先智能建删分区该表，正在清理数据。。。","QTB");
                    String datadate = paramMap.get("datadate");
                    String evaluationId = paramMap.get("evaluationId");
                    rs = TargetDB.getNewRs(SQLUtils.generateTablePartitionTypeSqlByTableSchemaAndTableName(TargetDB.getDBName(), currentSchemaName, currentTableName));
                    while(rs.next()){
                        partitionType = rs.getString("PARTITION_NM_TYPE");
                        highValueType = rs.getString("HIGH_VALUE_TYPE");
                    }
                    TargetDB.closeAllExceptConnection();
                    if("1".equals(partitionType)){
                        //此时分区格式为：p_19700101。分区键为：1970-01-01
                        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
                        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMdd");
                        partitionName = "P_"+datadate;
                        partitionId = sdf1.format(sdf2.parse(datadate));
                    }else if("2".equals(partitionType)){
                        //此时分区格式为：p_19700101_0。分区键为：19700101_0
                        partitionName = "P_"+datadate+"_"+evaluationId;
                        partitionId = datadate+"_"+evaluationId;
                    }
                    rs = TargetDB.getNewRs(SQLUtils.generateCheckPartitionIsExistSqlByTableSchemaAndTableNameAndPartitionName(TargetDB.getDBName(), currentSchemaName, currentTableName, partitionName));
                    while(rs.next()){
                        tempInt = rs.getInt(1);
                    }
                    TargetDB.closeAllExceptConnection();
                    if(tempInt>0){
                        //说明分区存在，则清空分区即可。
                        TargetDB.getNewPst(SQLUtils.generateTruncatePartitionSqlByTableSchemaAndTableNameAndPartitionName(TargetDB.getDBName(), currentSchemaName, currentTableName, partitionName)).execute();
                        TargetDB.closeAllExceptConnection();
                    }else{
                        //说明分区不存在，需要创建分区。
                        TargetDB.getNewPst(SQLUtils.generateCreatePartitionSqlByTableSchemaAndTableNameAndPartitionNameAndPartitionId(TargetDB.getDBName(), currentSchemaName, currentTableName, partitionName, partitionId)).execute();
                        TargetDB.closeAllExceptConnection();
                    }
                }else{
                    //一般都是deleteType=0的时候，表示着不删除直接插入，所以什么都不需要做。
                    LogUtils.recordINFOLog(this.threadName + "获取清理配置完毕，但是配置的是不删只插入，所以这里什么都不清理。。。","QTB");
                }
                LogUtils.recordINFOLog(this.threadName + "数据清理完毕，开始进行数据拆分工作。。。","QTB");
                if ("0".equals(paramMap.get("bufferSwitch")) || ("1".equals(paramMap.get("bufferSwitch")) && totalCount <= Integer.parseInt(paramMap.get("bufferSize")))) {
                    //0表示无缓冲区，即全量读全量写，内存大就是任性
                    //或者，如果总条数，还没有缓冲区大，那还缓冲个屁了，就当全量干了
                    LogUtils.recordINFOLog(this.threadName + "数据拆分完毕，经计算，该表「" + currentTable + "」选择直接干就完了！","QTB");
                    //初始化任务组信息
                    taskUtils = new TaskUtils();
                    taskUtils.setSchemaName(currentSchemaName);
                    taskUtils.setTableName(currentTableName);
                    taskUtils.setTotalDataCount(totalCount);
                    taskUtils.setSelectSql(selectSql.toString());
                    taskUtils.setColumnSql(columnsSql.toString());
                    taskUtils.setColumnList(columnList);
                    taskUtils.setColumnMapping(columnMapping);
                    taskUtils.setInsertSql(insertSql.toString());
                    ParamUtils.queue.offer(taskUtils);
                } else if ("1".equals(paramMap.get("bufferSwitch")) && totalCount > Integer.parseInt(paramMap.get("bufferSize"))) {
                    //这里是需要走缓冲的，需要分页读取，然后再写
                    bufferForCount = (int) Math.ceil(totalCount * 1.0 / Double.parseDouble(paramMap.get("bufferSize") + ""));
                    LogUtils.recordINFOLog(this.threadName + "数据拆分完毕，经计算，该表「" + currentTable + "」每次缓冲" + paramMap.get("bufferSize") + "条总共需要获取" + bufferForCount + "次！","QTB");
                    for(int i = 0;i<bufferForCount;i++){
                        taskUtils = new TaskUtils();
                        taskUtils.setSchemaName(currentSchemaName);
                        taskUtils.setTableName(currentTableName);
                        taskUtils.setTotalDataCount(totalCount);
                        taskUtils.setSelectSql(SQLUtils.generateTableSelectSqlWithPageNumberAndPageSize(SourceDB.getDBName(), selectSql.toString(), i, Integer.parseInt(paramMap.get("bufferSize"))));
                        taskUtils.setColumnSql(columnsSql.toString());
                        taskUtils.setColumnList(columnList);
                        taskUtils.setColumnMapping(columnMapping);
                        taskUtils.setInsertSql(insertSql.toString());
                        ParamUtils.queue.offer(taskUtils);
                    }
                } else {
                    //这是什么玩意？？？
                    throw new Exception("bufferSwitch参数配置错误，请检查bufferSwitch参数值。");
                }
                LogUtils.recordINFOLog(this.threadName + "该表「" + currentTable + "」已经加入任务队列准备消费！","QTB");
            }catch (Exception e){
                result.append(e.getMessage());
                e.printStackTrace();
                if("1".equals(paramMap.get("errorHandingType"))){
                    //遇到错误就终止。但不能在这直接停，因为停了后面写日志的就啥也拿不到了，就不知道什么问题了。
                    //所以可以直接结束循环，后面的不处理了，赶紧汇报给包工头，让包工头停止。
                    return result.toString();
                }
            }
        }
        return result.toString();
    }
}
