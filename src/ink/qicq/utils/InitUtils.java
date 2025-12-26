package ink.qicq.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public class InitUtils {
    private InitUtils(){

    }
    private static InitUtils initUtils = null;

    public static InitUtils getInstance(){
        if(initUtils == null){
            initUtils = new InitUtils();
        }
        return initUtils;
    }

    public Map<String,String> init(String[] args){
        if(args.length==0){
            LogUtils.recordERRORLog("传入参数有误，请检查。。。","QTB");
            System.exit(1);
        }
        LogUtils.recordINFOLog("正在初始化参数。。。","QTB");
        Map<String,String> paramMap = new HashMap<String,String>();
        paramMap.put("first_dbName",args[0]);//第一个数据库的名字
        paramMap.put("first_driver",args[1]);//第一个数据库的驱动类
        paramMap.put("first_jdbcUrl",args[2]);//第一个数据库的jdbc串
        paramMap.put("first_username",args[3]);//第一个数据库的用户名
        paramMap.put("first_password",args[4]);//第一个数据库的密码

        paramMap.put("second_dbName",args[5]);//第二个数据库的名字
        paramMap.put("second_driver",args[6]);//第二个数据库的驱动类
        paramMap.put("second_jdbcUrl",args[7]);//第二个数据库的jdbc串
        paramMap.put("second_username",args[8]);//第二个数据库的用户名
        paramMap.put("second_password",args[9]);//第二个数据库的密码

        paramMap.put("propertiesUrl",args[10]);//数据库配置文件地址，用来设置特殊的数据库session参数
        paramMap.put("sync_direction",args[11]);//同步方向，1从一号到二号，2从二号到一号
        paramMap.put("output_type",args[12]);//结果运行方式。1直接入库，2生成脚本，3既入库又生成脚本
        paramMap.put("output_url",args[13]);//生成文件的目录地址（最后一定要加一个目录符号，如\\或/）
        paramMap.put("bufferSize",args[14]);//数据同步分页大小（如设置成1234567890即为无缓冲区，直接全量读全量写）
        paramMap.put("bufferSwitch","1");
        if("1234567890".equals(args[14])){
            paramMap.put("bufferSwitch","0");
        }
        paramMap.put("commitCount",args[15]);//多少条提交一次
//        if(Integer.parseInt(args[14])<Integer.parseInt(args[13]) && "1".equals(paramMap.get("bufferSwitch"))){
//            paramMap.put("commitCount",args[13]);
//        }
        paramMap.put("tablePoolSize",args[16]);//线程池大小
        paramMap.put("dataPoolSize",args[17]);//线程池大小
        paramMap.put("tableList",args[18]);//需要同步的表，如果多个用逗号拼接。标准格式：schema.tableName.deleteType
        paramMap.put("errorHandingType",args[19]);//错误以后如何处理。1终止并卡死，2继续丢数但不卡死
        paramMap.put("datadate",args[20]);//数据日期yyyyMMdd格式，用来创建或删除分区使用，如果本次跃迁没有智能建删分区，则这个值传什么都可以，但不能不传
        paramMap.put("evaluationId",args[21]);//评估ID，用来创建或删除分区使用，如果本次跃迁没有智能建删分区，则这个值传什么都可以，但不能不传
        return paramMap;
    }

    public void initDB(DBUtils sourceDB,DBUtils targetDB,Map<String,String> paramMap){
        //初始化数据连接
        if("1".equals(paramMap.get("sync_direction"))){
            //从一号到二号进行同步
            sourceDB.init(paramMap.get("first_dbName"), paramMap.get("first_driver"), paramMap.get("first_jdbcUrl"), paramMap.get("first_username") ,paramMap.get("first_password"));
            //sourceDB = new DBUtils(paramMap.get("first_dbName"), paramMap.get("first_driver"), paramMap.get("first_jdbcUrl"), paramMap.get("first_username") ,paramMap.get("first_password"));
            targetDB.init(paramMap.get("second_dbName"), paramMap.get("second_driver"), paramMap.get("second_jdbcUrl"), paramMap.get("second_username") ,paramMap.get("second_password"));
            //targetDB = new DBUtils(paramMap.get("second_dbName"), paramMap.get("second_driver"), paramMap.get("second_jdbcUrl"), paramMap.get("second_username") ,paramMap.get("second_password"));
        }else if("2".equals(paramMap.get("sync_direction"))){
            //从二号到一号进行同步
            targetDB.init(paramMap.get("first_dbName"), paramMap.get("first_driver"), paramMap.get("first_jdbcUrl"), paramMap.get("first_username") ,paramMap.get("first_password"));
            //targetDB = new DBUtils(paramMap.get("first_dbName"), paramMap.get("first_driver"), paramMap.get("first_jdbcUrl"), paramMap.get("first_username") ,paramMap.get("first_password"));
            sourceDB.init(paramMap.get("second_dbName"), paramMap.get("second_driver"), paramMap.get("second_jdbcUrl"), paramMap.get("second_username") ,paramMap.get("second_password"));
            //sourceDB = new DBUtils(paramMap.get("second_dbName"), paramMap.get("second_driver"), paramMap.get("second_jdbcUrl"), paramMap.get("second_username") ,paramMap.get("second_password"));
        }else{
            //这是什么玩意？？？
            LogUtils.recordERRORLog("sync_direction参数配置错误，请检查sync_direction参数值。","QTB");
            System.exit(1);
        }
        //设置特殊的数据库session参数
        this.initDBParameter(sourceDB,paramMap.get("propertiesUrl"));
        this.initDBParameter(targetDB,paramMap.get("propertiesUrl"));
    }
    private void initDBParameter(DBUtils currentDB,String propertiesUrl){
        String propertiesPath = propertiesUrl + currentDB.getDBName() + "parameter.conf";
        File file = new File(propertiesPath);
        if(file.exists()){
            FileInputStream fis = null;
            InputStreamReader isr = null;
            BufferedReader br = null;
            Connection conn = null;
            try {
                fis = new FileInputStream(file);
                isr = new InputStreamReader(fis);
                br = new BufferedReader(isr);
                String line;
                while((line = br.readLine())!=null){
                    conn = currentDB.getConnectionIfExistTryCreated();
                    if(conn != null){
                        if(!"".equals(line.trim())){
                            conn.prepareStatement(line.replace(";","")).execute();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                LogUtils.recordERRORLog(currentDB.getDBName()+"初始化数据连接session参数失败："+e.getMessage(),null);
            } finally {
                try {
                    if(br != null){
                        br.close();
                    }
                    if(isr != null){
                        isr.close();
                    }
                    if(fis != null){
                        fis.close();
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                    LogUtils.recordERRORLog(currentDB.getDBName()+"初始化数据连接session参数失败："+e.getMessage(),null);
                }
            }
        }
    }
}
