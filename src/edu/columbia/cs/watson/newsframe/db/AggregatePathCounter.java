package edu.columbia.cs.watson.newsframe.db;

import org.joda.time.DateTime;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created with IntelliJ IDEA.
 * User: chris
 * Date: 5/12/13
 * Time: 10:37 AM
 * To change this template use File | Settings | File Templates.
 */
public class AggregatePathCounter {


    public static void main(String[] args) {

        System.out.println("Selecting paths from 'sem_frame' table and creating aggregate counts in 'path_agg_counts' table.");

        long totalRowsRetrieved = 0;

        try {


            Connection connection = ConnectionFactory.getConnection();

            PreparedStatement selectStatement = connection.prepareStatement("SELECT path, count FROM sem_frame WHERE ngram > 0");

            ResultSet pathRows = selectStatement.executeQuery();

            while(pathRows.next()) {

                if (totalRowsRetrieved%10000 == 0) {
                    System.out.println(totalRowsRetrieved + " total rows retrieved @ "+ new DateTime(System.currentTimeMillis()));
                }

                String path = pathRows.getString(1);
                int count = pathRows.getInt(2);

                //System.out.println(path+":"+count);

                PreparedStatement insertUpdateStatement = connection.prepareStatement(
                        "INSERT INTO path_agg_counts (path, count)\n" +
                        "VALUES (?, ?)\n" +
                        "ON DUPLICATE KEY UPDATE count = count + " + count);

                insertUpdateStatement.setString(1,path);
                insertUpdateStatement.setInt(2,count);
                insertUpdateStatement.executeUpdate();

                totalRowsRetrieved++;

            }



        } catch (SQLException sqle) {
            sqle.printStackTrace();

        }


    }



}
