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

    public Map<String,String> init(String[] args,String systemId){
        if(args.length==0){
            LogUtils.recordERRORLog("传入参数有误，请检查。。。",null);
            System.exit(1);
        }
        Map<String,String> paramMap = null;
        if("QTB".equals(systemId)){
            paramMap = this.initQTB(args);
        }else if("MAS".equals(systemId)){
            paramMap = this.initMAS(args);
        }
        return paramMap;
    }
    private Map<String,String> initMAS(String[] args){
        LogUtils.recordINFOLog("正在初始化参数。。。","MAS");
        Map<String,String> paramMap = new HashMap<String,String>();
        if("MASCreateFile".equals(args[0])){
            paramMap.put("taskName",args[0]);
            paramMap.put("MASBackupUrl",args[1]);
            paramMap.put("backupDBName",args[2]);
            paramMap.put("outputUrl",args[3]);
        }else{
            paramMap.put("DBName",args[0]);//数据库的名字
            paramMap.put("driver",args[1]);//数据库的驱动类
            paramMap.put("JDBCUrl",args[2]);//数据库的jdbc串
            paramMap.put("userName",args[3]);//数据库的用户名
            paramMap.put("password",args[4]);//数据库的密码
            paramMap.put("backupUser",args[5]);//需要备份的用户
            paramMap.put("backupData",args[6]);//是否打开备份数据的功能
            paramMap.put("bufferSize",args[7]);//数据同步分页大小（如设置成1234567890即为无缓冲区，直接全量读全量写）
            paramMap.put("bufferSwitch","1");
            if("1234567890".equals(args[7])){
                paramMap.put("bufferSwitch","0");
            }
            paramMap.put("propertiesUrl",args[8]);//配置文件的路径
            paramMap.put("outputUrl",args[9]);//备份文件生成的路径
        }
        return paramMap;
    }
    private Map<String,String> initQTB(String[] args){
        LogUtils.recordINFOLog("正在初始化参数。。。","QTB");
        Map<String,String> paramMap = new HashMap<String,String>();
        paramMap.put("firstDBName",args[0]);//第一个数据库的名字
        paramMap.put("firstDriver",args[1]);//第一个数据库的驱动类
        paramMap.put("firstJDBCUrl",args[2]);//第一个数据库的jdbc串
        paramMap.put("firstUserName",args[3]);//第一个数据库的用户名
        paramMap.put("firstPassword",args[4]);//第一个数据库的密码

        paramMap.put("secondDBName",args[5]);//第二个数据库的名字
        paramMap.put("secondDriver",args[6]);//第二个数据库的驱动类
        paramMap.put("secondJDBCUrl",args[7]);//第二个数据库的jdbc串
        paramMap.put("secondUserName",args[8]);//第二个数据库的用户名
        paramMap.put("secondPassword",args[9]);//第二个数据库的密码

        paramMap.put("propertiesUrl",args[10]);//数据库配置文件地址，用来设置特殊的数据库session参数
        paramMap.put("syncDirection",args[11]);//同步方向，1从一号到二号，2从二号到一号
        paramMap.put("outputType",args[12]);//结果运行方式。1直接入库，2生成脚本，3既入库又生成脚本
        paramMap.put("outputUrl",args[13]);//生成文件的目录地址（最后一定要加一个目录符号，如\\或/）
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
        paramMap.put("dataDate",args[20]);//数据日期yyyyMMdd格式，用来创建或删除分区使用，如果本次跃迁没有智能建删分区，则这个值传什么都可以，但不能不传
        paramMap.put("evaluationId",args[21]);//评估ID，用来创建或删除分区使用，如果本次跃迁没有智能建删分区，则这个值传什么都可以，但不能不传
        return paramMap;
    }
    public void initDB(DBUtils sourceDB,DBUtils targetDB,Map<String,String> paramMap){
        //初始化数据连接
        if("1".equals(paramMap.get("syncDirection"))){
            //从一号到二号进行同步
            sourceDB.init(paramMap.get("firstDBName"), paramMap.get("firstDriver"), paramMap.get("firstJDBCUrl"), paramMap.get("firstUserName") ,paramMap.get("firstPassword"));
            targetDB.init(paramMap.get("secondDBName"), paramMap.get("secondDriver"), paramMap.get("secondJDBCUrl"), paramMap.get("secondUserName") ,paramMap.get("secondPassword"));
        }else if("2".equals(paramMap.get("syncDirection"))){
            //从二号到一号进行同步
            targetDB.init(paramMap.get("firstDBName"), paramMap.get("firstDriver"), paramMap.get("firstJDBCUrl"), paramMap.get("firstUserName") ,paramMap.get("firstPassword"));
            sourceDB.init(paramMap.get("secondDBName"), paramMap.get("secondDriver"), paramMap.get("secondJDBCUrl"), paramMap.get("secondUserName") ,paramMap.get("secondPassword"));
        }else{
            //这是什么玩意？？？
            LogUtils.recordERRORLog("syncDirection参数配置错误，请检查syncDirection参数值。","QTB");
            System.exit(1);
        }
        //设置特殊的数据库session参数
        this.initDBParameter(sourceDB,paramMap);
        this.initDBParameter(targetDB,paramMap);
    }
    public void initDB(DBUtils dbUtils,Map<String,String> paramMap){
        //初始化数据连接
        dbUtils.init(paramMap.get("DBName"), paramMap.get("driver"), paramMap.get("JDBCUrl"), paramMap.get("userName"), paramMap.get("password"));
        //设置特殊的数据库session参数
        this.initDBParameter(dbUtils,paramMap);
    }
    private void initDBParameter(DBUtils currentDB,Map<String,String> paramMap){
        String propertiesUrl = paramMap.get("propertiesUrl");
        String propertiesPath = propertiesUrl + currentDB.getDBName() + "parameter.conf";
        File file = new File(propertiesPath);
        if(file.exists()){
            FileInputStream fis = null;
            InputStreamReader isr = null;
            BufferedReader br = null;
            Connection conn = null;
            String dbName = currentDB.getDBName();
            StringBuffer sb = new StringBuffer("");
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
                            sb.append(line.replace(";",""));
                            sb.append(";");
                            sb.append("\n");
                        }
                    }
                }
                paramMap.put(dbName+"_parameter",sb.toString());
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
