
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
    private static Map<String,String> paramMap = new HashMap<String,String>();

    public static void main(String[] args) throws ExecutionException, InterruptedException, IOException {
        long startTime = System.currentTimeMillis();
        //开发测试专用，生产需要注释掉本代码
        args=initParam(args);
        //args=initParam2(args);
        if(args.length==0){
            LogUtils.recordERRORLog("传入参数有误，请检查。。。","MAS");
            return;
        }
        if("MASCreateFile".equals(args[0])){
            //说明此时是逆向工程，想通过锚点创建上线脚本。
            MASCreateSqlFile(args);
            return;
        }

        LogUtils.recordINFOLog("正在初始化参数。。。","MAS");
        getParam(args);
        String rootPath = paramMap.get("backupPath");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String now = sdf.format(new Date());
        Path path = Paths.get(rootPath, now);
        if(path !=null && !Files.exists(path)){
            Files.createDirectories(path);
        }
        Path errFilePath = Paths.get(path.toString(),"error.log");
        if(!Files.exists(errFilePath)){
            Files.createFile(errFilePath);
        }
        File file = errFilePath.toFile();
        FileOutputStream fos = new FileOutputStream(file);
        OutputStreamWriter osw = new OutputStreamWriter(fos);
        BufferedWriter bw = new BufferedWriter(osw);
        String[] backupUserArray = paramMap.get("backupUser").split(",");
        LogUtils.recordINFOLog("参数识别完毕。本次需要备份的用户共有"+backupUserArray.length+"个。","MAS");
        //有几个用户，线程池就有多大，一个用户一个线程来备份
        paramMap.put("poolSize",backupUserArray.length+"");
        LogUtils.recordINFOLog("开始创建线程池。。。","MAS");
        ExecutorService poolExecutors = Executors.newFixedThreadPool(Integer.parseInt(paramMap.get("poolSize")));
        LogUtils.recordINFOLog("线程池创建完毕！开始分配任务。。。","MAS");
        List<Future> futureList = new ArrayList<Future>();
        Map<String,String> everyThreadMap;
        for(int i=0;i<backupUserArray.length;i++){
            everyThreadMap = new HashMap<String,String>(paramMap);
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
        LogUtils.recordERRORLog("总计"+backupUserArray.length+"个用户需要备份，本次一共"+paramMap.get("poolSize")+"只牛马给我干活，任务全部执行完毕,共有"+errorJobList.size()+"个人爆炸。","MAS");
        if(errorJobList.size()==0){
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
        args[2]="jdbc:gaussdb://10.20.160.105:7033,10.20.160.106:7033,10.20.160.107:7033/almdb?targetServerType=master";
//        args[0]="GBase8AMPP";
//        args[1]="com.gbase.jdbc.Driver";
//        args[2]="jdbc:gbase://10.0.82.55:5258/syd?rewriteBatchedStatements=true";
        args[3]="nmns_zcfz";
        args[4]="Sunyard123";
        args[5]="syd,run,rpt";
//        args[5]="leo,virgo,syd,run,rpt";
        args[6]="0";
        args[7]="200000";
        args[8]="C:\\Development\\01.代码\\项目组SVN\\03-开发相关\\02-自主知识产权工具\\01-source\\ink.qicq.backups\\ink\\qicq\\shell\\";
        args[9]="Z:\\临时用的乱七八糟\\Test\\";
        return args;
    }
    public static String[] initParam2(String[] args){
        args=new String[32];
        args[0]="MASCreateFile";
        args[1]="Z:\\临时用的乱七八糟\\Test";
        args[2]="GaussDB";
        args[3]="Z:\\临时用的乱七八糟\\Test";
        return args;
    }
    public static void getParam(String[] args){
        paramMap.put("dbName",args[0]);//数据库的名字
        paramMap.put("driver",args[1]);//数据库的驱动类
        paramMap.put("jdbcUrl",args[2]);//数据库的jdbc串
        paramMap.put("username",args[3]);//数据库的用户名
        paramMap.put("password",args[4]);//数据库的密码
        paramMap.put("backupUser",args[5]);//需要备份的用户
        paramMap.put("backupData",args[6]);//是否打开备份数据的功能
        paramMap.put("bufferSize",args[7]);//数据同步分页大小（如设置成1234567890即为无缓冲区，直接全量读全量写）
        paramMap.put("bufferSwitch","1");
        if("1234567890".equals(args[7])){
            paramMap.put("bufferSwitch","0");
        }
        paramMap.put("propertiesUrl",args[8]);//配置文件的路径
        paramMap.put("backupPath",args[9]);//备份文件生成的路径
    }

    public static void MASCreateSqlFile(String[] args) throws IOException {
        LogUtils.recordINFOLog("正在初始化参数。。。","MAS");
        String backupUrl = args[1];
        String backupDatabaseName = args[2];
        String finalUrl = args[3];
        File rootFile = new File(backupUrl);
        File[] backupDatabaseFiles = rootFile.listFiles(pathname ->
                pathname.isDirectory() && pathname.getName().equals(backupDatabaseName)
        );
        if(backupDatabaseFiles == null || backupDatabaseFiles.length>1 || backupDatabaseFiles.length==0){
            LogUtils.recordERRORLog("备份文件或目录获取异常，请检查参数！","MAS");
        }
        LogUtils.recordINFOLog("初始化参数完毕！本次读取的锚点为"+backupUrl+"的"+backupDatabaseName+"，解析完锚点后会将SQL文件输出至"+finalUrl+"。","MAS");
        rootFile = backupDatabaseFiles[0];
        File[] timestameFiles = rootFile.listFiles();
        Arrays.sort(timestameFiles,(file1,file2)->{
            return file2.getName().compareTo(file1.getName());
        });
        File lastestFile = timestameFiles[0];
        File[] schemaFiles = lastestFile.listFiles();
        File[] typeFiles = null;
        File[] sqlFiles = null;
        Path path = null;
        Path parentPath = null;
        File finalCurrentSchemaFile = null;
        FileOutputStream fos = null;
        OutputStreamWriter osw = null;
        BufferedWriter bw = null;
        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        String tempPath = null;
        String tmp = null;
        LogUtils.recordINFOLog("开始读取记忆锚点桩。。。","MAS");
        LogUtils.recordINFOLog("该锚点共计"+schemaFiles.length+"个树杈。","MAS");
        for(File currentSchemaFile:schemaFiles){
            LogUtils.recordINFOLog("当前正在解析"+currentSchemaFile.getName()+"树杈。","MAS");
            path = Paths.get(finalUrl, backupDatabaseName+"_MASSqlFile", currentSchemaFile.getName()+".sql");
            parentPath = path.getParent();
            if(parentPath !=null && !Files.exists(parentPath)){
                Files.createDirectories(parentPath);
            }
            Files.deleteIfExists(path);
            Files.createFile(path);
            finalCurrentSchemaFile = path.toFile();
            fos = new FileOutputStream(finalCurrentSchemaFile);
            osw = new OutputStreamWriter(fos);
            bw = new BufferedWriter(osw);
            //这里是哪个schema，一个schema一个文件。
            typeFiles = currentSchemaFile.listFiles();
            Arrays.sort(typeFiles,(file1,file2)->{
                return getFileWeight(file1)-getFileWeight(file2);
            });
            LogUtils.recordINFOLog("该树杈共计"+typeFiles.length+"个树枝。","MAS");
            for(File currentTypeFiles:typeFiles){
                LogUtils.recordINFOLog("当前正在解析"+currentTypeFiles.getName()+"树枝。","MAS");
                //这里是一个schema的所有类型的数据。
                sqlFiles = currentTypeFiles.listFiles();
                LogUtils.recordINFOLog("该树枝共计"+sqlFiles.length+"个树叶。","MAS");
                for(File currentSqlFiles:sqlFiles){
                    LogUtils.recordINFOLog("当前正在解析"+currentSqlFiles.getName()+"树叶。","MAS");
                    //这里是一个类型的所有数据。
                    tempPath = currentSqlFiles.getAbsolutePath();
                    fis = new FileInputStream(tempPath);
                    isr = new InputStreamReader(fis);
                    br = new BufferedReader(isr);
                    while((tmp=br.readLine())!=null){
                        bw.write(tmp);
                        bw.newLine();
                        bw.flush();
                    }
                    br.close();
                    isr.close();
                    fis.close();
                    bw.newLine();
                    bw.newLine();
                }
            }
            bw.close();
            osw.close();
            fos.close();
        }
        LogUtils.recordINFOLog("记忆锚点桩解析完毕！已生成SQL文件！","MAS");
    }
    private static int getFileWeight(File file){
        String name = file.getName();
        if("Table".equals(name))
            return 1;
        if("Function".equals(name))
            return 2;
        if("View".equals(name))
            return 3;
        if("Procedure".equals(name))
            return 4;
        return 999;
    }
}
