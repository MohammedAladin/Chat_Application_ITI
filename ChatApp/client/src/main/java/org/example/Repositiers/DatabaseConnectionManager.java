package org.example.Repositiers;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;
import com.mysql.cj.jdbc.MysqlDataSource;


public class DatabaseConnectionManager {
    static String propPath = "/media/mohamed/01D3829D52880A80/ChatApplication/ChatApp/Client/Resources/dp.properties";

    public  void generatePropFile() {
        Properties prop = new Properties();

        try (OutputStream oStream = new FileOutputStream(propPath)) {

            prop.setProperty("URL", "jdbc:mysql://localhost:3306/ChatApplicationDB");
            prop.setProperty("User", "root");
            prop.setProperty("Password", "password");

            prop.store(oStream, null);

        } catch (IOException e) {
            System.out.println("IOEXCEPTION: " + e.getMessage());
        }
    }
    public  DataSource createDataSource() {
        generatePropFile();
        Properties prop = new Properties();
        MysqlDataSource mysqlDS = new MysqlDataSource();

        try (InputStream iStream = new FileInputStream(propPath)) {

            prop.load(iStream);
            mysqlDS.setURL(prop.getProperty("URL"));
            mysqlDS.setUser(prop.getProperty("User"));
            mysqlDS.setPassword(prop.getProperty("Password"));

        } catch (IOException e) {
            System.out.println("IOEXCEPTION: " + e.getMessage());
        }

        return mysqlDS;
    }
    public  Connection getMyConnection() {
        DataSource ds = createDataSource();
        
        Connection con = null;

        try {
            con = ds.getConnection();
            return con;
        } catch (SQLException e) {
            System.out.println("Can not create connection... ");
            e.printStackTrace();
            return null;
        }
    }
}
