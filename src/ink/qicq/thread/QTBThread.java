package ink.qicq.thread;

import ink.qicq.utils.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.Callable;

public class QTBThread implements Callable {

    private Map<String,String> paramMap;
    private int threadId;
    private String threadName;
    private DBUtils SourceDB = new DBUtils();;
    private DBUtils TargetDB = new DBUtils();;

    public QTBThread(Map<String,String> paramMap, int threadId) {
        this.paramMap = paramMap;
        this.threadId = threadId;
        this.threadName = "「第"+(threadId+1)+"号」";
    }

    @Override
    public Object call(){
        //初始化数据链接
        InitUtils initUtils = InitUtils.getInstance();
        initUtils.initDB(SourceDB,TargetDB,paramMap);
        LogUtils.recordINFOLog(this.threadName + "初始化数据连接成功，开始消费任务队列！","QTB");
        StringBuilder result = new StringBuilder("$O$K$");
        TaskUtils taskUtils = null;
        ResultSet rs = null;
        PreparedStatement targetPS = null;
        int currentInt = 0;
        int commitInt = 0;
        //无限消费任务，直到任务队列为空。
        while((taskUtils = ParamUtils.queue.poll())!=null){
            try{
                targetPS = TargetDB.getNewPst(taskUtils.getInsertSql());
                rs = SourceDB.getNewRs(taskUtils.getSelectSql());
                currentInt=0;
                while (rs.next()) {
                    currentInt++;
                    targetPS = ResultSetUtils.preparedStatementSetting(taskUtils.getColumnList(),taskUtils.getColumnMapping(),rs,targetPS,SourceDB.getDBName());
                    targetPS.addBatch();
                    commitInt++;
                    if(commitInt>=Integer.parseInt(paramMap.get("commitCount"))){
                        targetPS.executeBatch();
                        targetPS.clearBatch();
                        commitInt=0;
                    }
                }
                if(commitInt!=0){
                    targetPS.executeBatch();
                    targetPS.clearBatch();
                    commitInt=0;
                }
                SourceDB.closeAllExceptConnection();
                TargetDB.closeAllExceptConnection();
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
        LogUtils.recordINFOLog(this.threadName + "数据同步完毕，正在关闭数据连接。。。","QTB");
        try {
            SourceDB.closeAll();
            TargetDB.closeAll();
            LogUtils.recordINFOLog(this.threadName + "数据连接关闭成功，向包工头汇报工作然后下班。","QTB");
        } catch (SQLException e) {
            e.printStackTrace();
            LogUtils.recordINFOLog(this.threadName + "数据连接关闭失败！！！！"+e.getMessage(),"QTB");
        }
        return result.toString();
    }
}
