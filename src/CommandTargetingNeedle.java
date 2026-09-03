import ink.qicq.utils.DBUtils;
import ink.qicq.utils.InitUtils;
import ink.qicq.utils.LogUtils;
import ink.qicq.utils.ParamUtils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

public class CommandTargetingNeedle {
    public static void main(String[] args) throws ClassNotFoundException {
        long startTime = System.currentTimeMillis();
        //开发测试专用，生产需要注释掉本代码
        //args=initParam(args);
        //初始化程序
        InitUtils initUtils = InitUtils.getInstance();
        ParamUtils.paramMap = initUtils.init(args,"CTN");
        DBUtils dbUtils = new DBUtils();
        initUtils.initDB(dbUtils,ParamUtils.paramMap);
        //正式开始
        LogUtils.recordINFOLog("准备执行："+ParamUtils.paramMap.get("execSql"),"CTN");
        try (PreparedStatement ps = dbUtils.getNewPst(ParamUtils.paramMap.get("execSql"))) {
            StringBuilder executionResult = new StringBuilder();
            boolean isResultSet = ps.execute();
            boolean hasExecutionResult = false;
            int resultSetIndex = 0;

            while (true) {
                if (isResultSet) {
                    hasExecutionResult = true;
                    resultSetIndex++;
                    int rowCount = 0;
                    try (ResultSet rs = ps.getResultSet()) {
                        ResultSetMetaData metaData = rs.getMetaData();
                        int columnCount = metaData.getColumnCount();
                        executionResult.append("第").append(resultSetIndex).append("个结果集：\n");
                        while (rs.next()) {
                            rowCount++;
                            executionResult.append("第").append(rowCount).append("行：");
                            for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
                                if (columnIndex > 1) {
                                    executionResult.append("，");
                                }
                                Object value = rs.getObject(columnIndex);
                                executionResult.append(metaData.getColumnLabel(columnIndex))
                                        .append("=")
                                        .append(value == null ? "NULL" : String.valueOf(value));
                            }
                            executionResult.append("\n");
                        }
                    }
                    executionResult.append("第").append(resultSetIndex)
                            .append("个结果集共").append(rowCount).append("行。\n");
                } else {
                    int updateCount = ps.getUpdateCount();
                    if (updateCount == -1) {
                        break;
                    }
                    hasExecutionResult = true;
                    executionResult.append("执行成功，受影响行数：")
                            .append(updateCount).append("。\n");
                }
                isResultSet = ps.getMoreResults(Statement.CLOSE_CURRENT_RESULT);
            }

            if (!hasExecutionResult) {
                executionResult.append("执行成功，语句未返回结果集或受影响行数。");
            }
            LogUtils.recordINFOLog(executionResult.toString(),"CTN");
            LogUtils.calcTimeInMilliseconds(startTime,"指令执行耗时：");
        } catch (SQLException e) {
            LogUtils.recordERRORLog("SQL执行失败，SQLState=" + e.getSQLState()
                    + "，错误码=" + e.getErrorCode() + "，错误信息=" + e.getMessage(),"CTN");
            System.exit(1);
        } finally {
            try {
                dbUtils.closeAll();
            } catch (SQLException e) {
                LogUtils.recordERRORLog("关闭数据库资源失败：" + e.getMessage(),"CTN");
                System.exit(1);
            }
        }
    }
    public static String[] initParam(String[] args){
        args=new String[32];
        args[0]="OushuDB";
        args[1]="org.postgresql.Driver";
        args[2]="jdbc:postgresql://ip:port/database";
        args[3]="username";
        args[4]="password";
        args[5]="XXXparameter.conf's Path";
        args[6]="sql";
        return args;
    }
}
