package ink.qicq.utils;

import java.sql.*;

public class DBUtils {
    private Connection conn=null;
    private PreparedStatement pst=null;
    private ResultSet rs=null;
    private String DBName;
    private String driver;
    private String jdbcUrl;
    private String username;
    private String password;

    public DBUtils(String DBName, String driver, String jdbcUrl, String username, String password) {
        this.DBName = DBName;
        this.driver = driver;
        this.jdbcUrl = jdbcUrl;
        this.username = username;
        this.password = password;
    }
    public DBUtils(){
    }
    public void init(String DBName, String driver, String jdbcUrl, String username, String password){
        this.DBName = DBName;
        this.driver = driver;
        this.jdbcUrl = jdbcUrl;
        this.username = username;
        this.password = password;
    }

    public String getDBName() {
        return DBName;
    }
    public String getCurrentUserName() {
        return username;
    }

    private Connection getConnection() throws ClassNotFoundException, SQLException {
        Class.forName(this.driver);
        this.conn = DriverManager.getConnection(this.jdbcUrl, this.username, this.password);
        return this.conn;
    }

    public Connection getConnectionIfExistTryCreated(){
        if(this.conn == null){
            try {
                this.getConnection();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return this.conn;
    }

    public void closeAllExceptConnection() throws SQLException {
        if(this.rs != null) {
            this.rs.close();
        }
        if(this.pst != null) {
            this.pst.close();
        }
    }

    public void closeAll() throws SQLException {
        this.closeAllExceptConnection();
        if(this.conn != null) {
            this.conn.close();
        }
    }

    public PreparedStatement getNewPst(String sql) throws SQLException, ClassNotFoundException {
        if(this.conn == null || this.conn.isClosed()) {
            this.getConnection();
        }
        this.pst = this.conn.prepareStatement(sql);
        return this.pst;
    }

    public ResultSet getNewRs() throws SQLException {
        if(this.pst == null) {
            throw new SQLException("PrepareStatement is null and SQL can not be null,please check your code!");
        }
        this.rs = this.pst.executeQuery();
        return this.rs;
    }

    public ResultSet getNewRs(String sql) throws SQLException, ClassNotFoundException {
        this.getNewPst(sql);
        this.rs = this.pst.executeQuery();
        return this.rs;
    }
}
