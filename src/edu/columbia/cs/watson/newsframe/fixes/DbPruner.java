package edu.columbia.cs.watson.newsframe.fixes;

import edu.columbia.cs.watson.newsframe.db.ConnectionFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created with IntelliJ IDEA.
 * User: chris
 * Date: 5/10/13
 * Time: 5:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class DbPruner {

    public static void main(String[] args) {


        try {
            Connection connection = ConnectionFactory.getConnection();

            PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT entity1, entity2, word, score, ngram FROM newsframe.raw_frame WHERE ngram > 0 AND ngram < 4");

            ResultSet selectResult = preparedStatement.executeQuery();
            while(selectResult.next()) {


                String entity1 = selectResult.getString(1);
                String entity2 = selectResult.getString(2);
                String path = selectResult.getString(3);
                int count = selectResult.getInt(4);
                int ngram = selectResult.getInt(5);

                String[] pathSplit = path.split("_ent._");


                System.out.println("\n+\n"+path+"\n"+pathSplit[1].trim()+"\n+");

                PreparedStatement insertStatement = connection.prepareStatement(
                        "INSERT INTO sem_frame (entity1, entity2, path, count, ngram)\n" +
                                "VALUES (?, ?, ?, ?, ?)\n" +
                                "ON DUPLICATE KEY UPDATE count = count + " + count);
                insertStatement.setString(1, entity1);
                insertStatement.setString(2, entity2);
                insertStatement.setString(3, pathSplit[1].trim());
                insertStatement.setInt(4, count);
                insertStatement.setInt(5,ngram);
                insertStatement.executeUpdate();

            }




        } catch (SQLException sqle) {

            sqle.printStackTrace();

        }

    }


}
