
import ink.qicq.task.AntiMAS;
import ink.qicq.thread.*;
import ink.qicq.utils.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

public class MemoryAnchorStake {

    public static void main(String[] args) throws ExecutionException, InterruptedException, IOException {
        long startTime = System.currentTimeMillis();
        //开发测试专用，生产需要注释掉本代码
        //args=initParam(args);
        //args=initParam(args);
        //args=initParam2(args);
        //初始化程序
        InitUtils initUtils = InitUtils.getInstance();
        ParamUtils.paramMap = initUtils.init(args,"MAS");
        if("MASCreateFile".equals(ParamUtils.paramMap.get("taskName"))){
            //说明此时是逆向工程，想通过锚点创建上线脚本。
            AntiMAS antiMAS = new AntiMAS();
            antiMAS.MASCreateSqlFile(ParamUtils.paramMap);
            return;
        }
        //正式开始
        String rootPath = ParamUtils.paramMap.get("outputUrl");
        //以时间戳创建文件夹
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String now = sdf.format(new Date());
        Path path = Paths.get(rootPath, now);
        if(path !=null && !Files.exists(path)){
            Files.createDirectories(path);
        }
        //创建错误日志文件
        Path errFilePath = Paths.get(path.toString(),"error.log");
        if(!Files.exists(errFilePath)){
            Files.createFile(errFilePath);
        }
        File file = errFilePath.toFile();
        //往错误文件中写入内容
        FileOutputStream fos = new FileOutputStream(file);
        OutputStreamWriter osw = new OutputStreamWriter(fos);
        BufferedWriter bw = new BufferedWriter(osw);
        //本次备份所有的用户
        String[] backupUserArray = ParamUtils.paramMap.get("backupUser").split(",");
        LogUtils.recordINFOLog("参数识别完毕。本次需要备份的用户共有"+backupUserArray.length+"个。","MAS");
        //有几个用户，线程池就有多大，一个用户一个线程来备份
        ParamUtils.paramMap.put("poolSize",backupUserArray.length+"");
        LogUtils.recordINFOLog("开始创建线程池。。。","MAS");
        ExecutorService poolExecutors = Executors.newFixedThreadPool(Integer.parseInt(ParamUtils.paramMap.get("poolSize")));
        LogUtils.recordINFOLog("线程池创建完毕！开始分配任务。。。","MAS");
        List<Future> futureList = new ArrayList<Future>();
        Map<String,String> everyThreadMap;
        for(int i=0;i<backupUserArray.length;i++){
            everyThreadMap = new HashMap<String,String>(ParamUtils.paramMap);
            //每个线程处理一个用户的所有内容
            //所以每个线程的用户变量和路径变量不太一样
            everyThreadMap.put("backupCurrentUser",backupUserArray[i]);
            everyThreadMap.put("backupCurrentPath",Paths.get(path.toString(),backupUserArray[i]).toString());
            Future future = poolExecutors.submit(new MASThread(everyThreadMap,i));
            futureList.add(future);
        }
        LogUtils.recordINFOLog("分配任务成功，线程池已启动，开始进入包工头模式。。。","MAS");
        Object result;
        List<Future> finishList = new ArrayList<Future>();
        List<String> errorJobList = new ArrayList<String>();
        do {
            Thread.currentThread().sleep(5000);
            for(Future future : futureList){
                if(future.isDone()){
                    LogUtils.recordERRORLog("好耶，有一个人跟我说他干完了！让我看看。。。","MAS");
                    result = future.get();
                    if("OK".equals(result)){
                        LogUtils.recordERRORLog("好耶干的不错！","MAS");
                    }else if(result!=null){
                        LogUtils.recordERRORLog("哦天啊他炸了。信息如下："+result,"MAS");
                        errorJobList.add(result.toString());
                    }else{
                        //什么也不干。。。
                    }
                    finishList.add(future);
                }
            }
            futureList.removeAll(finishList);
            finishList.clear();
            LogUtils.recordERRORLog("检查线程池状态，目前还有"+futureList.size()+"个人没有干完活。","MAS");
        }while(futureList.size()>0);
        poolExecutors.shutdown();
        LogUtils.recordERRORLog("总计"+backupUserArray.length+"个用户需要备份，本次一共"+ParamUtils.paramMap.get("poolSize")+"只牛马给我干活，任务全部执行完毕,共有"+errorJobList.size()+"个人爆炸。","MAS");
        if(errorJobList.size()==0){
            //如果错误文件为空，删掉错误文件
            //即有错误文件一定有问题
            file.deleteOnExit();
        }
        for(int i=0;i<errorJobList.size();i++){
            if(i==0){
                LogUtils.recordERRORLog("为了防止前面的错误信息刷没，我再统一告诉你一次吧。","MAS");
            }
            LogUtils.recordERRORLog("第"+(i+1)+"个错误："+errorJobList.get(i).toString(),"MAS");
            bw.write("第"+(i+1)+"个错误："+errorJobList.get(i).toString());
            bw.newLine();
        }
        bw.close();
        osw.close();
        fos.close();
        LogUtils.calcTimeInMilliseconds(startTime,"程序总共用时：");
    }
    public static String[] initParam(String[] args){
        args=new String[32];
        args[0]="GaussDB";
        args[1]="com.huawei.gaussdb.jdbc.Driver";
        args[2]="jdbc:gaussdb://ip1:port1,ip2:port2,ip3:port3/database?targetServerType=master";
//        args[0]="GBase8AMPP";
//        args[1]="com.gbase.jdbc.Driver";
//        args[2]="jdbc:gbase://ip1:port1/database?rewriteBatchedStatements=true&failoverEnable=true&hostList=ip2,ip3";
        args[3]="username";
        args[4]="password";
        args[5]="Schema1,Schema2,Schema3";
        args[6]="0";
        args[7]="200000";
        args[8]="XXXparameter.conf's Path";
        args[9]="MASBackupPath";
        return args;
    }
    public static String[] initParam2(String[] args) {
        args = new String[32];
        args[0] = "MASCreateFile";
        args[1] = "MASBackupPath";
        args[2] = "DatabaseName";
        args[3] = "MASBackupPath";
        return args;
    }
}
