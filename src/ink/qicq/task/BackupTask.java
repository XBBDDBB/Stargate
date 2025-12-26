package ink.qicq.task;

import ink.qicq.utils.DBUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class BackupTask {
    public DBUtils dbUtils;
    public Map<String,String> paramMap;
    public StringBuffer result;
    public abstract String getObjListByUser(String DBName,String user);
    public abstract String getSqlByUserAndTableName(String DBName,String user,String tableName);
    public abstract String getObjListColumnName();
    public abstract String getSqlColumnName();
    public abstract String getType();
    public abstract String specialTreatment(String tableName,String sql);
    public BackupTask(Map<String,String> paramMap){
        this.paramMap = paramMap;
        this.dbUtils = new DBUtils(paramMap.get("dbName"), paramMap.get("driver"), paramMap.get("jdbcUrl"), paramMap.get("username") ,paramMap.get("password"));
        String propertiesPath = paramMap.get("propertiesUrl") + paramMap.get("dbName") + "parameter.conf";
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
                    conn = this.dbUtils.getConnectionIfExistTryCreated();
                    if(conn != null){
                        if(!"".equals(line.trim())){
                            conn.prepareStatement(line.replace(";","")).execute();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
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
                }
            }
        }
    }

    public String backup(String backupUser){
        result = new StringBuffer("");
        List<String> needWriteList = new ArrayList<String>();
        String resSql = "";
        ResultSet resultSet;
        try{
            resultSet = dbUtils.getNewRs(getObjListByUser(dbUtils.getDBName(), backupUser));
            while (resultSet.next()) {
                needWriteList.add(resultSet.getString(getObjListColumnName()).toUpperCase().trim());
            }
            dbUtils.closeAllExceptConnection();
        }catch (Exception e){
            result.append("用户「"+backupUser+"」查询所有信息失败！"+e.getMessage()+"。");
        }
        for(String currentTableName:needWriteList){
            try {
                resultSet = dbUtils.getNewRs(getSqlByUserAndTableName(dbUtils.getDBName(), backupUser, currentTableName));
                while (resultSet.next()) {
                    resSql = resultSet.getString(getSqlColumnName());
                }
                dbUtils.closeAllExceptConnection();
                resSql = specialTreatment(currentTableName, resSql);
                writeToFile(currentTableName, resSql);
            }catch (Exception e){
                result.append("用户「"+backupUser+"」对象「"+currentTableName+"」"+e.getMessage()+"。");
            }
        }
        return result.toString();
    }

    public File createDirectorys(String type, String tableName) throws IOException {
        String rootPath = paramMap.get("backupCurrentPath");
        Path path = Paths.get(rootPath, type, tableName+".sql");
        Path parentPath = path.getParent();
        if(parentPath !=null && !Files.exists(parentPath)){
            Files.createDirectories(parentPath);
        }
        if(!Files.exists(path)){
            Files.createFile(path);
        }
        return path.toFile();
    }

    public void writeToFile(String tableName,String sql) throws IOException {
        File file = createDirectorys(getType(), tableName);
        FileOutputStream fos = new FileOutputStream(file);
        OutputStreamWriter osw = new OutputStreamWriter(fos);
        BufferedWriter bw = new BufferedWriter(osw);
        bw.write(sql);
        bw.flush();
        if(bw!=null)
            bw.close();
        if(osw!=null)
            osw.close();
        if(fos!=null)
            fos.close();
    }

    public String close(){
        result = new StringBuffer("");
        try {
            dbUtils.closeAll();
        } catch (SQLException e) {
            result.append("用户「"+dbUtils.getCurrentUserName()+"」关闭数据库连接失败！"+e.getMessage()+"。");
        }
        return result.toString();
    }
}
