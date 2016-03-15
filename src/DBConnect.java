import java.sql.*;

public class DBConnect {
    public static String DEFAULT_DB = "c421_a3";
    public static String DEFAULT_USER = "c421";
    public static String DEFAULT_PASS = "c421";
    private String DB, USER, PASS;
    private Connection c = null;

    public DBConnect() {
        this.DB = this.DEFAULT_DB;
        this.USER = this.DEFAULT_USER;
        this.PASS = this.DEFAULT_PASS;
    }

    public DBConnect(String DB, String user, String pass) {
        this.DB = DB;
        this.USER = user;
        this.PASS = pass;
    }

    public Connection getConnection() throws SQLException {
        if (this.c == null || this.c.isClosed()) {
            String dburl = "jdbc:postgresql://localhost:5432/" + this.DB;
            this.c = DriverManager
                    .getConnection(dburl, this.USER, this.PASS);
        }
        return this.c;
    }
}
