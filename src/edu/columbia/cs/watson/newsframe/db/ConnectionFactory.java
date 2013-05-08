package edu.columbia.cs.watson.newsframe.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created with IntelliJ IDEA.
 * User: chris
 * Date: 5/8/13
 * Time: 2:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class ConnectionFactory {

    private static Connection connection = null;
    private static final String PORT = "1234";
    private static final String HOST = "localhost";
    private static final String PROTOCOL = "jdbc:mysql://";
    private static final String DBNAME = "newsframe";
    private static final String USERNAME = "newsframe";
    private static final String PASSWORD = "newsframe";



    private ConnectionFactory() {}

    public static Connection getConnection() {

        try {

            if (connection == null || connection.isClosed()) {
                connection = DriverManager
                    .getConnection(PROTOCOL+HOST+":"+PORT+"/"+DBNAME+"?"
                            + "user=" + USERNAME + "&password=" + PASSWORD);
            }
        } catch (SQLException sqle) {
            System.out.println("Could not connect to database. The following exception was raised: ");
            sqle.printStackTrace();
            System.exit(1);
        }

        return connection;

    }




}
