package ink.qicq.utils;

import javax.sql.rowset.serial.SerialBlob;
import javax.sql.rowset.serial.SerialClob;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

public class ResultSetUtils {
    public static PreparedStatement preparedStatementSetting(List<String> columnList, Map<String, String> columnMapping, ResultSet sourceRS, PreparedStatement targetPs, String sourceDBName) throws SQLException, ParseException {
        for(int j = 0;j<columnList.size();j++){
            //当前列值
            Object object = sourceRS.getObject(columnList.get(j));
            //当前列类型
            String columnType = columnMapping.get(columnList.get(j)).toString();
            if("VARCHAR".equals(columnType) || "VARCHAR2".equals(columnType) || "TEXT".equals(columnType)){
                if(object instanceof Clob){
                    //如果后续其他数据中instanceof判断实效，可用sourceDBName强制根据数据库名字判断
                    object = ((Clob) object).getSubString(1, (int) ((Clob) object).length());
                }
                if(object==null){
                    targetPs.setNull(j+1, Types.VARCHAR);
                }else{
                    targetPs.setString(j+1, String.valueOf(object));
                }
            }else if("CHAR".equals(columnType) || "BPCHAR".equals(columnType)){
                if(object==null){
                    targetPs.setNull(j+1, Types.CHAR);
                }else{
                    targetPs.setString(j+1, String.valueOf(object));
                }
            }else if("NUMBER".equals(columnType) || "NUMERIC".equals(columnType) || "DECIMAL".equals(columnType) || "DEC".equals(columnType) || "DOUBLE".equals(columnType)){
                if(object==null){
                    targetPs.setNull(j+1, Types.DECIMAL);
                }else{
                    targetPs.setBigDecimal(j+1, new BigDecimal(String.valueOf(object)));
                }
            }else if("INTEGER".equals(columnType) || "INT".equals(columnType) || "INT4".equals(columnType)){
                if(object==null){
                    targetPs.setNull(j+1, Types.INTEGER);
                }else{
                    targetPs.setInt(j+1, Integer.parseInt(String.valueOf(object)));
                }
            }else if("BIGINT".equals(columnType) || "INT8".equals(columnType)){
                if(object==null){
                    targetPs.setNull(j+1, Types.BIGINT);
                }else{
                    targetPs.setLong(j+1, Long.parseLong(String.valueOf(object)));
                }
            }else if("DATE".equals(columnType)){
                if(object==null){
                    targetPs.setNull(j+1, Types.DATE);
                }else{
                    String temp = String.valueOf(object);
                    if(object instanceof Timestamp){
                        //如果后续其他数据中instanceof判断实效，可用sourceDBName强制根据数据库名字判断
                        //GaussDB不支持Date类型，他会返回Timestamp格式是yyyy-MM-dd HH:ii:ss.0
                        SimpleDateFormat sourceSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                        java.util.Date date = sourceSdf.parse(temp);
                        SimpleDateFormat targetSdf = new SimpleDateFormat("yyyy-MM-dd");
                        temp = targetSdf.format(date);
                    }
                    targetPs.setDate(j+1, Date.valueOf(temp));
                }
            }else if("TIMESTAMP".equals(columnType) || "DATETIME".equals(columnType)){
                if(object==null){
                    targetPs.setNull(j+1, Types.TIMESTAMP);
                }else{
                    String temp = String.valueOf(object);
                    if(object instanceof Date){
                        //如果后续其他数据中instanceof判断实效，可用sourceDBName强制根据数据库名字判断
                        //GaussDB不支持Date类型，需要把所有的yyyy-MM-dd转换成Timestamp
                        temp = temp+" 00:00:00";
                    }
                    targetPs.setTimestamp(j+1, Timestamp.valueOf(temp));
                }
            }else if("TIME".equals(columnType)){
                if(object==null){
                    targetPs.setNull(j+1, Types.TIME);
                }else{
                    targetPs.setTime(j+1, Time.valueOf(String.valueOf(object)));
                }
            }else if("BLOB".equals(columnType)){
                if(object==null){
                    targetPs.setNull(j+1, Types.BLOB);
                }else{
                    try{
                        object = new SerialBlob((byte[]) object);
                    }catch (ClassCastException e){
                        //这干点啥呢？？？
                    }
                    targetPs.setBlob(j+1, (Blob) object);
                }
            }else if("CLOB".equals(columnType)){
                if(object instanceof String){
                    //如果后续其他数据中instanceof判断实效，可用sourceDBName强制根据数据库名字判断
                    object = new SerialClob(object.toString().toCharArray());
                }
                if(object==null){
                    targetPs.setNull(j+1, Types.CLOB);
                }else{
                    targetPs.setClob(j+1, (Clob) object);
                }
            }
        }
        return targetPs;
    }

    public static String preparedStatementAppending(List<String> columnList, Map<String, String> columnMapping, ResultSet sourceRS, String sourceDBName) throws SQLException, IOException {
        StringBuffer sb = new StringBuffer("");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for(int j = 0;j<columnList.size();j++){
            //当前列值
            Object object = sourceRS.getObject(columnList.get(j));
            //当前列类型
            String columnType = columnMapping.get(columnList.get(j)).toString();
            if("VARCHAR".equals(columnType) || "VARCHAR2".equals(columnType) || "TEXT".equals(columnType)){
                if(object instanceof Clob){
                    //如果后续其他数据中instanceof判断实效，可用sourceDBName强制根据数据库名字判断
                    object = ((Clob) object).getSubString(1, (int) ((Clob) object).length());
                }
                if(object==null){
                    sb.append("NULL");
                }else{
                    sb.append("'"+object+"'");
                }
            }else if("CHAR".equals(columnType) || "BPCHAR".equals(columnType)){
                if(object==null){
                    sb.append("NULL");
                }else{
                    sb.append("'"+object+"'");
                }
            }else if("NUMBER".equals(columnType) || "NUMERIC".equals(columnType) || "DECIMAL".equals(columnType) || "DEC".equals(columnType) || "DOUBLE".equals(columnType)){
                if(object==null){
                    sb.append("NULL");
                }else{
                    sb.append(new BigDecimal(String.valueOf(object)));
                }
            }else if("INTEGER".equals(columnType) || "INT".equals(columnType) || "INT4".equals(columnType)){
                if(object==null){
                    sb.append("NULL");
                }else{
                    sb.append(Integer.parseInt(String.valueOf(object)));
                }
            }else if("BIGINT".equals(columnType) || "INT8".equals(columnType)){
                if(object==null){
                    sb.append("NULL");
                }else{
                    sb.append(Long.parseLong(String.valueOf(object)));
                }
            }else if("DATE".equals(columnType)){
                if(object==null){
                    sb.append("NULL");
                }else{
                    sb.append("'"+sdf.format(Date.valueOf(String.valueOf(object)))+"'");
                }
            }else if("TIMESTAMP".equals(columnType) || "DATETIME".equals(columnType)){
                if(object==null){
                    sb.append("NULL");
                }else{
                    sb.append("'"+sdf.format(Timestamp.valueOf(String.valueOf(object)))+"'");
                }
            }else if("TIME".equals(columnType)){
                if(object==null){
                    sb.append("NULL");
                }else{
                    sb.append("'"+sdf.format(Time.valueOf(String.valueOf(object)))+"'");
                }
            }else if("BLOB".equals(columnType)){
                if(object==null){
                    sb.append("NULL");
                }else{
                    try{
                        object = new SerialBlob((byte[]) object);
                    }catch (ClassCastException e){
                        //这干点啥呢？？？
                    }
                    InputStream is = ((Blob) object).getBinaryStream();
                    byte[] bytes = new byte[is.available()];
                    is.read(bytes);
                    String content = new String(bytes, "UTF-8");
                    is.close();
                    sb.append("'"+content+"'");
                }
            }else if("CLOB".equals(columnType)){
                if(object instanceof String){
                    //如果后续其他数据中instanceof判断实效，可用sourceDBName强制根据数据库名字判断
                    object = new SerialClob(object.toString().toCharArray());
                }
                if(object==null){
                    sb.append("NULL");
                }else{
                    Reader characterStream = ((Clob) object).getCharacterStream();
                    BufferedReader br = new BufferedReader(characterStream);
                    StringBuilder temp = new StringBuilder();
                    String line;
                    while((line = br.readLine())!=null){
                        temp.append(line);
                    }
                    String content = temp.toString();
                    sb.append("'"+content+"'");
                }
            }
            sb.append(",");
        }
        return sb.toString();
    }
}
