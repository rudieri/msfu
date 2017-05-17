package br.com.msfu.h2;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author rudieri
 */
public class H2BD {
    static {
        org.h2.Driver.load();
    }
    
    public static Connection getConnection() throws SQLException{
        return DriverManager.getConnection("jdbc:h2:tcp://localhost:9092/sonar");
    }
}
