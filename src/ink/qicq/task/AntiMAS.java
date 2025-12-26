package ink.qicq.task;

import ink.qicq.utils.LogUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;

public class AntiMAS {
    public void MASCreateSqlFile(Map<String,String> paramMap) throws IOException {
        File rootFile = new File(paramMap.get("MASBackupUrl"));
        File[] backupDatabaseFiles = rootFile.listFiles(pathname ->
                pathname.isDirectory() && pathname.getName().equals(paramMap.get("backupDBName"))
        );
        if(backupDatabaseFiles == null || backupDatabaseFiles.length>1 || backupDatabaseFiles.length==0){
            LogUtils.recordERRORLog("备份文件或目录获取异常，请检查参数！","MAS");
        }
        LogUtils.recordINFOLog("初始化参数完毕！本次读取的锚点为"+paramMap.get("MASBackupUrl")+"的"+paramMap.get("backupDBName")+"，解析完锚点后会将SQL文件输出至"+paramMap.get("outputUrl")+"。","MAS");
        rootFile = backupDatabaseFiles[0];
        File[] timestameFiles = rootFile.listFiles();
        Arrays.sort(timestameFiles,(file1, file2)->{
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
            path = Paths.get(paramMap.get("outputUrl"), paramMap.get("backupDBName")+"_MASSqlFile", currentSchemaFile.getName()+".sql");
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
    private int getFileWeight(File file){
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
