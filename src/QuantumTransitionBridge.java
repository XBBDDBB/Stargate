
import ink.qicq.thread.*;
import ink.qicq.utils.*;

import java.util.*;
import java.util.concurrent.*;

public class QuantumTransitionBridge {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        long startTime = System.currentTimeMillis();
        //开发测试专用，生产需要注释掉本代码
        //args=initParam(args);
        //初始化程序
        InitUtils initUtils = InitUtils.getInstance();
        ParamUtils.paramMap = initUtils.init(args);
        DBUtils SourceDB = new DBUtils();
        DBUtils TargetDB = new DBUtils();
        initUtils.initDB(SourceDB,TargetDB,ParamUtils.paramMap);
        //正式开始
        String[] tableArray = ParamUtils.paramMap.get("tableList").split(",");
        LogUtils.recordINFOLog("参数识别完毕。本次需要同步的表共有"+tableArray.length+"个。正在初始化任务组信息。。。","QTB");
        //如果表的数量少于默认线程数量，则多余的线程也没有用，直接给成表数量。
        if(tableArray.length<Integer.parseInt(ParamUtils.paramMap.get("tablePoolSize"))){
            ParamUtils.paramMap.put("tablePoolSize",tableArray.length+"");
        }
        LogUtils.recordINFOLog("开始创建线程池。。。","QTB");
        //创建固定长度的线程组
        ExecutorService poolExecutors = Executors.newFixedThreadPool(Integer.parseInt(ParamUtils.paramMap.get("tablePoolSize")));
        LogUtils.recordINFOLog("线程池创建完毕！开始分配「数据拆分」任务。。。","QTB");
        //任务信息，用来存每个线程处理多少任务，采用轮询分配的方式
        Map<Integer,List<String>> jobMap = new HashMap<Integer,List<String>>();
        int index=0;
        List<String> tempList;
        //轮询分配任务信息
        for(String table:tableArray){
            if(index>=Integer.parseInt(ParamUtils.paramMap.get("tablePoolSize"))){
                index=0;
            }
            if(jobMap.get(index)==null){
                tempList = new ArrayList<String>();
                tempList.add(table);
                jobMap.put(index,tempList);
            }else{
                tempList = jobMap.get(index);
                tempList.add(table);
                jobMap.put(index,tempList);
            }
            index++;
        }
        //第一次用线程池，为了拆分数据，生成任务队列
        //用来接初始化任务组线程的返回值
        List<Future> futureList = new ArrayList<Future>();
        for(int i=0;i<Integer.parseInt(ParamUtils.paramMap.get("tablePoolSize"));i++){
            Future future = poolExecutors.submit(new InitQTBThread(ParamUtils.paramMap,i,jobMap.get(i)));
            futureList.add(future);
        }
        LogUtils.recordINFOLog("分配「数据拆分」任务成功，线程池已启动，开始进入包工头模式。。。","QTB");
        Object result;
        List<Future> finishList = new ArrayList<Future>();
        List<String> errorJobList = new ArrayList<String>();
        do {
            Thread.currentThread().sleep(1000);
            for(Future future : futureList){
                if(future.isDone()){
                    LogUtils.recordINFOLog("好耶，有一个人跟我说他干完了！让我看看。。。","QTB");
                    result = future.get();
                    if("$O$K$".equals(result)){
                        LogUtils.recordINFOLog("好耶干的不错！","QTB");
                    }else if(result!=null){
                        LogUtils.recordERRORLog("哦天啊他炸了。信息如下："+result.toString().replace("$O$K$",""),"QTB");
                        errorJobList.add(result.toString().replace("$O$K$",""));
                        if("1".equals(ParamUtils.paramMap.get("errorHandingType"))){
                            //遇到错误直接停止。
                            System.exit(1);
                        }
                    }else{
                        //什么也不干。。。
                    }
                    finishList.add(future);
                }
            }
            futureList.removeAll(finishList);
            finishList.clear();
            LogUtils.recordINFOLog("检查线程池状态，目前还有"+futureList.size()+"个人没有干完活。","QTB");
        }while(futureList.size()>0);
        poolExecutors.shutdown();
        LogUtils.recordINFOLog("总计"+tableArray.length+"个表需要同步，本次一共"+ParamUtils.paramMap.get("tablePoolSize")+"只牛马给我干活，「数据拆分」任务全部执行完毕,共有"+errorJobList.size()+"个人爆炸。","QTB");
        for(int i=0;i<errorJobList.size();i++){
            if(i==0){
                LogUtils.recordERRORLog("为了防止前面的错误信息刷没，我再统一告诉你一次吧。","QTB");
            }
            LogUtils.recordERRORLog("第"+(i+1)+"个工人跟我说："+errorJobList.get(i).toString(),"QTB");
        }
        if(errorJobList.size()>0){
            if("1".equals(ParamUtils.paramMap.get("errorHandingType"))){
                LogUtils.recordERRORLog("程序遇到严重错误，直接中断程序吧","QTB");
                //遇到严重错误，直接中断程序吧
                System.exit(1);
            }
        }
        String errmsg = errorJobList.size()>0?"有报错":"无报错";
        LogUtils.recordINFOLog("数据拆分任务执行完毕！"+errmsg+"，当前任务队列共有"+ParamUtils.queue.size()+"个任务待消费，开始创建线程池。。。","QTB");

        //第二次用线程池，为了数据同步，消费任务队列
        //创建固定长度的线程组
        poolExecutors = Executors.newFixedThreadPool(Integer.parseInt(ParamUtils.paramMap.get("dataPoolSize")));
        LogUtils.recordINFOLog("线程池创建完毕！开始分配「数据同步」任务。。。","QTB");
        //用来接任务组线程的返回值
        futureList = new ArrayList<Future>();
        for(int i=0;i<Integer.parseInt(ParamUtils.paramMap.get("dataPoolSize"));i++){
            Future future = poolExecutors.submit(new QTBThread(ParamUtils.paramMap,i));
            futureList.add(future);
        }
        LogUtils.recordINFOLog("分配「数据同步」任务成功，线程池已启动，开始进入包工头模式。。。","QTB");
        result = null;
        finishList = new ArrayList<Future>();
        errorJobList = new ArrayList<String>();
        do {
            Thread.currentThread().sleep(1000);
            for(Future future : futureList){
                if(future.isDone()){
                    LogUtils.recordINFOLog("好耶，有一个人跟我说他干完了！让我看看。。。","QTB");
                    result = future.get();
                    if("$O$K$".equals(result)){
                        LogUtils.recordINFOLog("好耶干的不错！","QTB");
                    }else if(result!=null){
                        LogUtils.recordERRORLog("哦天啊他炸了。信息如下："+result.toString().replace("$O$K$",""),"QTB");
                        errorJobList.add(result.toString().replace("$O$K$",""));
                        if("1".equals(ParamUtils.paramMap.get("errorHandingType"))){
                            //遇到错误直接停止。
                            System.exit(1);
                        }
                    }else{
                        //什么也不干。。。
                    }
                    finishList.add(future);
                }
            }
            futureList.removeAll(finishList);
            finishList.clear();
            LogUtils.recordINFOLog("检查线程池状态，目前还有"+futureList.size()+"个人没有干完活。","QTB");
        }while(futureList.size()>0);
        poolExecutors.shutdown();
        LogUtils.recordINFOLog("总计"+tableArray.length+"个表需要同步，本次一共"+ParamUtils.paramMap.get("dataPoolSize")+"只牛马给我干活，「数据同步」任务全部执行完毕,共有"+errorJobList.size()+"个人爆炸。","QTB");
        for(int i=0;i<errorJobList.size();i++){
            if(i==0){
                LogUtils.recordERRORLog("为了防止前面的错误信息刷没，我再统一告诉你一次吧。","QTB");
            }
            LogUtils.recordERRORLog("第"+(i+1)+"个工人跟我说："+errorJobList.get(i).toString(),"QTB");
        }
        LogUtils.calcTimeInMilliseconds(startTime,"程序总共用时：");
    }
    public static String[] initParam(String[] args){
        args=new String[32];
        args[0]="GaussDB";
        args[1]="com.huawei.gaussdb.jdbc.Driver";
        args[2]="jdbc:gaussdb://ip1:port1,ip2:port2,ip3:port3/database?targetServerType=master";
        args[3]="username";
        args[4]="password";

        args[5]="GBase8AMPP";
        args[6]="com.gbase.jdbc.Driver";
        args[7]="jdbc:gbase://ip1:port1/database?rewriteBatchedStatements=true&failoverEnable=true&hostList=ip2,ip3";
        args[8]="username";
        args[9]="password";

        args[10]="XXXparameter.conf's Path";
        args[11]="1";
        args[12]="1";
        args[13]="FileOutPutUrl";
        args[14]="200000";
        args[15]="50000";
        args[16]="10";
        args[17]="10";

        args[18]="Schema.Table.DeleteType";
        args[19]="1";
        args[20]="20250930";
        args[21]="0";

        return args;
    }
}
