package ink.qicq.utils;

public class LogUtils {
    private static String getSystemName(String systemId){
        if("QTB".equals(systemId)){
            return "「量子跃迁」";
        }else if("MAS".equals(systemId)){
            return "「记忆锚点」";
        }else{
            return "「星际之门」";
        }
    }
    public static void calcTimeInMilliseconds(Long startTime,String head){
        System.out.println(head+(System.currentTimeMillis()-startTime)+"ms");
    }
    public static void calcTimeInSeconds(Long startTime,String head){
        System.out.println(head+(System.currentTimeMillis()-startTime)/1000+"s");
    }
    public static void recordINFOLog(String msg,String systemId){
        String systemName = getSystemName(systemId);
        System.out.println(systemName+msg);
    }
    public static void recordERRORLog(String msg,String systemId){
        String systemName = getSystemName(systemId);
        System.err.println(systemName+msg);
    }
}
