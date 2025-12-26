package ink.qicq.utils;

import javax.sql.rowset.serial.SerialBlob;
import javax.sql.rowset.serial.SerialClob;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

public class ResultSetUtils {
    public static PreparedStatement preparedStatementSetting(List<String> columnList, Map<String, String> columnMapping, ResultSet sourceRS, PreparedStatement targetPs) throws SQLException, ParseException {
        for(int j = 0;j<columnList.size();j++){
            //当前列值
            Object object = sourceRS.getObject(columnList.get(j));
            //当前列类型
            String columnType = columnMapping.get(columnList.get(j)).toString();
            if("VARCHAR".equals(columnType) || "VARCHAR2".equals(columnType) || "TEXT".equals(columnType)){
                if(object instanceof Clob){
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
                    targetPs.setDouble(j+1, Double.parseDouble(String.valueOf(object)));
                }
            }else if("INTEGER".equals(columnType) || "BIGINT".equals(columnType) || "INT".equals(columnType) || "INT4".equals(columnType) || "INT8".equals(columnType)){
                if(object==null){
                    targetPs.setNull(j+1, Types.INTEGER);
                }else{
                    targetPs.setInt(j+1, Integer.parseInt(String.valueOf(object)));
                }
            }else if("DATE".equals(columnType)){
                if(object==null){
                    targetPs.setNull(j+1, Types.DATE);
                }else{
                    String temp = String.valueOf(object);
                    if(object instanceof Timestamp){
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
}
