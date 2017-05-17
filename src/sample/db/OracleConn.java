package sample.db;

import org.apache.commons.dbcp.BasicDataSourceFactory;

import javax.sql.DataSource;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.util.Properties;

/**
 * 连接测试库
 * Created by belong on 2017/4/12.
 */
public class OracleConn {
    private Connection conn;
    private InputStream is;
    private DataSource ds;
    private Properties pro;

    public Connection getConnection() {
        String path = OracleConn.class.getClassLoader().getResource("sample/resources/db/dbcpT.ini").getPath();
        pro = new Properties();
        try {
            is = new FileInputStream(path);
            pro.load(is);
            ds = BasicDataSourceFactory.createDataSource(pro);
            conn = ds.getConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return conn;
    }
}
