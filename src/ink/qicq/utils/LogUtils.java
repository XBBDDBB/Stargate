package ink.qicq.utils;

public class LogUtils {
    public static void calcTimeInMilliseconds(Long startTime,String head){
        System.out.println(head+(System.currentTimeMillis()-startTime)+"ms");
    }
    public static void calcTimeInSeconds(Long startTime,String head){
        System.out.println(head+(System.currentTimeMillis()-startTime)/1000+"s");
    }
    public static void recordINFOLog(String msg){
        System.out.println("「数据自动化同步工程」"+msg);
    }
    public static void recordERRORLog(String msg){
        System.err.println("「数据自动化同步工程」"+msg);
    }
}
