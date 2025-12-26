package ink.qicq.utils;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ParamUtils {

    //星际之门主参数
    public static Map<String,String> paramMap = null;

    //星际之门任务队列
    public static Queue<TaskUtils> queue = new ConcurrentLinkedQueue<TaskUtils>();
}
