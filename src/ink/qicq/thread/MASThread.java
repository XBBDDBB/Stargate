package ink.qicq.thread;

import ink.qicq.task.*;
import ink.qicq.utils.LogUtils;
import java.util.Map;
import java.util.concurrent.Callable;

public class MASThread implements Callable {

    private Map<String,String> paramMap;
    private int threadId;
    private String backupUser;
    private String threadName;

    public MASThread(Map<String,String> paramMap, int threadId) {
        this.paramMap = paramMap;
        this.threadId = threadId;
        this.threadName = "「第"+(threadId+1)+"号」";
        this.backupUser = paramMap.get("backupCurrentUser")==null?"WhyBackupUserIsNull??? :(":paramMap.get("backupCurrentUser").toUpperCase();
    }

    @Override
    public Object call(){
        BackupTask tableBackup = new TableBackupTask(paramMap);
        BackupTask viewBackup = new ViewBackupTask(paramMap);
        BackupTask functionBackup = new FunctionBackupTask(paramMap);
        BackupTask procedureBackup = new ProcedureBackupTask(paramMap);
        BackupTask dataBackup = new DataBackupTask(paramMap);
        LogUtils.recordINFOLog(this.threadName + "初始化数据连接成功，已经拿到任务列表，开始干活。。。","MAS");
        StringBuilder result = new StringBuilder("OK");
        LogUtils.recordINFOLog(this.threadName + "开始去数据库获取当前用户「" + backupUser + "」表级信息。。。","MAS");
        //备份表
        result.append(tableBackup.backup(backupUser));
        result.append(tableBackup.close());
        LogUtils.recordINFOLog(this.threadName + "表级信息备份完毕，开始去数据库获取当前用户「" + backupUser + "」试图级信息。。。","MAS");
        //备份试图
        result.append(viewBackup.backup(backupUser));
        result.append(viewBackup.close());
        LogUtils.recordINFOLog(this.threadName + "试图级信息备份完毕，开始去数据库获取当前用户「" + backupUser + "」函数级信息。。。","MAS");
        //备份函数
        result.append(functionBackup.backup(backupUser));
        result.append(functionBackup.close());
        LogUtils.recordINFOLog(this.threadName + "函数级信息备份完毕，开始去数据库获取当前用户「" + backupUser + "」存储过程级信息。。。","MAS");
        //备份存储过程
        result.append(procedureBackup.backup(backupUser));
        result.append(procedureBackup.close());
        LogUtils.recordINFOLog(this.threadName + "存储过程级级信息备份完毕，开始去数据库获取当前用户「" + backupUser + "」全量数据信息。。。（如果开启的话）","MAS");
        //备份全量sql数据
        if("1".equals(paramMap.get("backupData"))){
            result.append(dataBackup.backup(backupUser));
        }
        LogUtils.recordINFOLog(this.threadName + "都干完了，向包工头汇报工作然后下班。。。","MAS");
        if(!"OK".contentEquals(result)){
            return result.toString().replace("OK","");
        }
        return result.toString();
    }

}
