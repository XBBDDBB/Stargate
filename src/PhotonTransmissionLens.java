import ink.qicq.utils.DBUtils;
import ink.qicq.utils.InitUtils;
import ink.qicq.utils.LogUtils;
import ink.qicq.utils.ParamUtils;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class PhotonTransmissionLens {
    private static final int FETCH_SIZE = 1000;
    private static final long MAX_DATA_ROWS_PER_CSV = 1048575L;

    public static void main(String[] args) throws ClassNotFoundException {
        long startTime = System.currentTimeMillis();
        // 开发测试专用，生产需要注释掉本代码
        //args = initParam(args);
        //初始化程序
        InitUtils initUtils = InitUtils.getInstance();
        ParamUtils.paramMap = initUtils.init(args, "PTL");
        String tableName = ParamUtils.paramMap.get("tableName");
        File outputPath = new File(tableName + ".csv.zip");
        DBUtils dbUtils = new DBUtils();
        initUtils.initDB(dbUtils, ParamUtils.paramMap);
        try {
            exportCsv(dbUtils, tableName, outputPath);
            LogUtils.recordINFOLog("CSV导出完成，文件：" + outputPath.getAbsolutePath(), "PTL");
            LogUtils.calcTimeInMilliseconds(startTime, "CSV导出耗时：");
        } catch (SQLException e) {
            LogUtils.recordERRORLog("CSV导出失败，SQLState=" + e.getSQLState()
                    + "，错误码=" + e.getErrorCode() + "，错误信息=" + e.getMessage(), "PTL");
            System.exit(1);
        } catch (IOException e) {
            LogUtils.recordERRORLog("CSV文件写入失败：" + e.getMessage(), "PTL");
            System.exit(1);
        } finally {
            try {
                dbUtils.closeAll();
            } catch (SQLException e) {
                LogUtils.recordERRORLog("关闭数据库资源失败：" + e.getMessage(), "PTL");
                System.exit(1);
            }
        }
    }

    private static void exportCsv(DBUtils dbUtils, String tableName, File outputPath)
            throws SQLException, IOException, ClassNotFoundException {
        String queryTableName = validateTableName(tableName);
        String sql = "SELECT * FROM " + queryTableName;
        LogUtils.recordINFOLog("准备导出表：" + tableName + "，文件：" + outputPath, "PTL");
        Connection connection = dbUtils.getConnectionIfExistTryCreated();
        if (connection == null) {
            throw new SQLException("数据库连接创建失败。");
        }
        // 导出连接只用于读取，关闭自动提交可让支持服务端游标的驱动真正分批取数。
        connection.setAutoCommit(false);
        PreparedStatement ps = null;
        ResultSet rs = null;
        ZipOutputStream zip = null;
        BufferedWriter writer = null;
        boolean entryOpen = false;
        try {
            try {
                ps = connection.prepareStatement(
                        sql,
                        ResultSet.TYPE_FORWARD_ONLY,
                        ResultSet.CONCUR_READ_ONLY);
            } catch (SQLException e) {
                ps = connection.prepareStatement(sql);
            }
            zip = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(outputPath)));
            writer = new BufferedWriter(new OutputStreamWriter(zip, "UTF-8"));
            try {
                ps.setFetchSize(FETCH_SIZE);
            } catch (SQLException e) {
                LogUtils.recordINFOLog("当前JDBC驱动不支持fetchSize，使用默认读取方式。", "PTL");
            }
            //ps.setFetchDirection(ResultSet.FETCH_FORWARD);
            LogUtils.recordINFOLog("已提交查询，开始读取结果集。", "PTL");
            rs = ps.executeQuery();
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            long rowCount = 0;
            long partRowCount = 0;
            int partNumber = 0;
            long lastProgressTime = System.currentTimeMillis();
            startCsvPart(zip, writer, metaData, columnCount, ++partNumber, tableName);
            entryOpen = true;
            while (rs.next()) {
                if (partRowCount >= MAX_DATA_ROWS_PER_CSV) {
                    writer.flush();
                    zip.closeEntry();
                    entryOpen = false;
                    startCsvPart(zip, writer, metaData, columnCount, ++partNumber, tableName);
                    entryOpen = true;
                    partRowCount = 0;
                }
                for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
                    if (columnIndex > 1) {
                        writer.write(',');
                    }
                    writeCsvValue(writer, rs.getObject(columnIndex));
                }
                writer.write("\r\n");
                rowCount++;
                partRowCount++;
                if (rowCount % 10000 == 0 || System.currentTimeMillis() - lastProgressTime >= 1000) {
                    writer.flush();
                    printProgress(rowCount, outputPath);
                    lastProgressTime = System.currentTimeMillis();
                }
            }
            writer.flush();
            if (entryOpen) {
                zip.closeEntry();
                entryOpen = false;
            }
            System.out.println();
            LogUtils.recordINFOLog("共导出" + rowCount + "行，生成" + partNumber + "个CSV文件。", "PTL");
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    // 资源关闭失败不覆盖原始导出异常。
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    // 资源关闭失败不覆盖原始导出异常。
                }
            }
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    // 资源关闭失败不覆盖原始导出异常。
                }
            } else if (zip != null) {
                try {
                    zip.close();
                } catch (IOException e) {
                    // 资源关闭失败不覆盖原始导出异常。
                }
            }
        }
    }

    private static void startCsvPart(ZipOutputStream zip, BufferedWriter writer,
                                     ResultSetMetaData metaData, int columnCount,
                                     int partNumber, String tableName) throws IOException, SQLException {
        String entryName = tableName.replace('.', '_') + "_part_"
                + String.format("%04d", partNumber) + ".csv";
        zip.putNextEntry(new ZipEntry(entryName));
        writeHeader(writer, metaData, columnCount);
    }

    private static void printProgress(long rowCount, File outputPath) throws IOException {
        long fileSize = outputPath.exists() ? outputPath.length() : 0;
        String progress = "\r已导出：" + rowCount + " 行，已写入："
                + formatBytes(fileSize) + "，文件：" + outputPath;
        System.out.print(progress);
        System.out.flush();
    }

    private static String formatBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        if (bytes < 1024 * 1024) {
            return String.format("%.2f KB", bytes / 1024.0);
        }
        if (bytes < 1024L * 1024L * 1024L) {
            return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
        }
        return String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }

    private static String validateTableName(String tableName) throws SQLException {
        String[] parts = tableName.split("\\.", -1);
        if (parts.length < 1 || parts.length > 2) {
            throw new SQLException("表名格式不正确，只支持 table 或 schema.table：" + tableName);
        }
        for (String part : parts) {
            if (!part.matches("[A-Za-z_][A-Za-z0-9_$]*")) {
                throw new SQLException("表名包含不支持的字符：" + tableName);
            }
        }
        return tableName;
    }

    private static void writeHeader(BufferedWriter writer, ResultSetMetaData metaData, int columnCount)
            throws SQLException, IOException {
        for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
            if (columnIndex > 1) {
                writer.write(',');
            }
            writeCsvValue(writer, metaData.getColumnLabel(columnIndex));
        }
        writer.write("\r\n");
    }

    private static void writeCsvValue(BufferedWriter writer, Object value) throws IOException {
        if (value == null) {
            return;
        }
        String text = String.valueOf(value);
        boolean quoted = text.indexOf(',') >= 0 || text.indexOf('"') >= 0
                || text.indexOf('\r') >= 0 || text.indexOf('\n') >= 0;
        if (quoted) {
            writer.write('"');
            writer.write(text.replace("\"", "\"\""));
            writer.write('"');
        } else {
            writer.write(text);
        }
    }

    public static String[] initParam(String[] args) {
        args = new String[32];
        args[0]="OushuDB";
        args[1]="org.postgresql.Driver";
        args[2]="jdbc:postgresql://ip:port/database";
        args[3]="username";
        args[4]="password";
        args[5]="XXXparameter.conf's Path";
        args[6]="tableName";
        return args;
    }
}
