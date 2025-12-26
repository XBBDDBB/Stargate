package ink.qicq.utils;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ParamUtils {

    //量子跃迁桥主参数
    public static Map<String,String> paramMap = null;

    //量子跃迁桥任务队列
    public static Queue<TaskUtils> queue = new ConcurrentLinkedQueue<TaskUtils>();
}
