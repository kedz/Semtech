package edu.columbia.cs.watson.newsframe.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;

/**
 * Created with IntelliJ IDEA.
 * User: chris
 * Date: 5/15/13
 * Time: 12:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class CategoryFrameTableBuilder {

    public static void main(String[] args) {


        long totalInserts = 0;

        try {

            Connection connection = ConnectionFactory.getConnection();

            PreparedStatement selectAll = connection.prepareStatement("SELECT entity1, entity2, path, count, ngram FROM sem_frame");
            ResultSet resultSet = selectAll.executeQuery();

            while(resultSet.next()) {

                String ent1 = resultSet.getString(1).intern();
                String ent2 = resultSet.getString(2).intern();
                String path = resultSet.getString(3).intern();
                int count = resultSet.getInt(4);
                int ngram = resultSet.getInt(5);

                HashSet<String> cat1Set = new HashSet<String>();
                HashSet<String> cat2Set = new HashSet<String>();


                PreparedStatement ent1CatsQuery = connection.prepareStatement("SELECT category FROM category_map where entity = ?");
                ent1CatsQuery.setString(1,ent1);
                ResultSet ent1Cats = ent1CatsQuery.executeQuery();
                while(ent1Cats.next()) {
                    cat1Set.add(ent1Cats.getString(1).intern());
                }
                ent1Cats = null;
                ent1CatsQuery = null;

                PreparedStatement ent2CatsQuery = connection.prepareStatement("SELECT category FROM category_map where entity = ?");
                ent2CatsQuery.setString(1,ent2);
                ResultSet ent2Cats = ent2CatsQuery.executeQuery();
                while(ent2Cats.next()) {
                    cat2Set.add(ent2Cats.getString(1).intern());
                }
                ent2Cats = null;
                ent1CatsQuery = null;


                for(String cat1 : cat1Set) {
                    for(String cat2 : cat2Set) {

                        PreparedStatement insertToCatFrame = connection.prepareStatement(
                                "INSERT INTO cat_frame (category1, category2, path, count, ngram)\n"+
                                "VALUES (?, ?, ?, ?, ?)\n"+
                                "ON DUPLICATE KEY UPDATE count = count + " + count);
                        insertToCatFrame.setString(1,cat1);
                        insertToCatFrame.setString(2,cat2);
                        insertToCatFrame.setString(3,path);
                        insertToCatFrame.setInt(4,count);
                        insertToCatFrame.setInt(5,ngram);
                        insertToCatFrame.executeUpdate();

                        totalInserts++;
                        if (totalInserts%1000 == 0)
                            System.out.println("Inserted/Updated "+totalInserts+" rows.");

                    }
                }




            }




        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }






    }


}
