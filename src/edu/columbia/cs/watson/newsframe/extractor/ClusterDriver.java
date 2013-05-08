package edu.columbia.cs.watson.newsframe.extractor;

import edu.columbia.cs.watson.newsframe.db.ConnectionFactory;
import edu.smu.tspell.wordnet.Synset;
import edu.smu.tspell.wordnet.SynsetType;
import edu.smu.tspell.wordnet.VerbSynset;
import edu.smu.tspell.wordnet.WordNetDatabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


/**
 * Created with IntelliJ IDEA.
 * User: chris
 * Date: 5/8/13
 * Time: 2:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class ClusterDriver {


    public static void main(String[] args) {


        System.setProperty("wordnet.database.dir", "/home/chris/WordNet-3.0/dict");
        WordNetDatabase database = WordNetDatabase.getFileInstance();




        System.out.print("Connecting to Newsframe DB...");

        Connection connection = ConnectionFactory.getConnection();
        System.out.println("Yahtzee!");

        try {
            System.out.println("Querying DB...");

            PreparedStatement statement = connection.prepareStatement("SELECT entity1, entity2, word, ngram FROM newsframe.raw_frame WHERE ngram > 3 AND score > 2");
            ResultSet resultSet = statement.executeQuery();

            while(resultSet.next()) {

                System.out.println("____\n\n");
                //System.out.println(ent1+":"ent2path);
                String ent1 = resultSet.getString(1);
                String ent2 = resultSet.getString(2);
                String path = resultSet.getString(3);
                int ngram   = resultSet.getInt(4);
                System.out.println(ent1+":"+ent2+":"+ngram+" -- "+path);

                for(String token : path.split(" ")) {
                    if (!token.equals("_ent1_") && !token.equals("_ent2_")) {
                        Synset[] synsets = database.getSynsets(token, SynsetType.VERB);
                        for (Synset synset : synsets) {

                            VerbSynset[] verbSynsets = ((VerbSynset) synset).getHypernyms();
                            for( VerbSynset vsyn : verbSynsets) {


                                System.out.println(vsyn);

                            }

                        }

                    }
                }



            }

        //selectStatement.setString(1,resource);




            connection.close();

        } catch (SQLException sqle) {
            System.out.println("Oh geez, an SQL Exception was thrown. Bummer. :(");
            sqle.printStackTrace();
        }




    }


}
