package edu.columbia.cs.watson.newsframe.cluster;

import edu.columbia.cs.watson.newsframe.db.ConnectionFactory;

import java.sql.Connection;

/**
 * Created with IntelliJ IDEA.
 * User: chris
 * Date: 5/9/13
 * Time: 2:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class ClusterPipeline {

    //public void getEntityLists


    public static void main(String args[]) {



        Connection connection = ConnectionFactory.getConnection();

        // Get path subsets from overlap
        //            |
        //            |
        //            |
        //           \|/
        //            +
        //
        // a Big Bag of Paths
        //
        //
        //
        // explode paths with synsets
        //
        // For each subset:
        //      LDA to assign to topics
        //
        //
        // EM clustering within topics










    }




}
