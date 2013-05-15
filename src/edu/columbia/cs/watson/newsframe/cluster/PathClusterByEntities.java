package edu.columbia.cs.watson.newsframe.cluster;

import edu.columbia.cs.watson.newsframe.db.ConnectionFactory;
import edu.columbia.cs.watson.newsframe.extractor.PairwiseEntityPath;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created with IntelliJ IDEA.
 * User: chris
 * Date: 5/12/13
 * Time: 11:14 AM
 * To change this template use File | Settings | File Templates.
 */
public class PathClusterByEntities {


    public static void main(String[] args) {

        String entityLabel = "entity1";
        if (args.length > 0 && args[0].equals("entity2")) {
            entityLabel = "entity2";
            System.out.println("Clustering on entity2");
        } else {
            System.out.println("Clustering on entity1");
        }


        try {

            System.out.println("Retrieving most common paths and their entities...");

            ArrayList<String> pathList = new ArrayList<String>(15000);
            HashMap<String,HashSet<String>> pathEntitySet = new HashMap<String, HashSet<String>>();
            HashSet<String> totalEntities = new HashSet<String>();

            Connection connection = ConnectionFactory.getConnection();
            PreparedStatement selectPaths = connection.prepareStatement("SELECT path FROM path_agg_counts WHERE count > 100 ORDER BY count DESC LIMIT 10000");

            ResultSet frequentPathRows = selectPaths.executeQuery();

            while(frequentPathRows.next()) {

                String path = frequentPathRows.getString(1).intern();

                System.out.println(path);



                PreparedStatement selectAllEntities = connection.prepareStatement("SELECT ? FROM sem_frame WHERE path = ? AND count > 1");
                selectAllEntities.setString(1,entityLabel);
                selectAllEntities.setString(2,path);
                ResultSet entitiesFromPathResults = selectAllEntities.executeQuery();

                HashSet<String> associatedEntities = new HashSet<String>();

                while(entitiesFromPathResults.next()) {

                    String entity1 = entitiesFromPathResults.getString(1).intern();
                    //String entity2 = entitiesFromPathResults.getString(2).intern();
                    associatedEntities.add(entity1);
                    //associatedEntities.add(entity2);
                    totalEntities.add(entity1);
                    //totalEntities.add(entity2);
                }

                pathList.add(path);
                pathEntitySet.put(path,associatedEntities);

                //for(String ent : associatedEntities)
                  //  System.out.println(ent);

               // System.out.println();




            }

            double avgEnts = 0;

            for(String path : pathEntitySet.keySet())
                avgEnts += pathEntitySet.get(path).size();
            avgEnts = avgEnts/pathEntitySet.size();


            System.out.println("Finished retrieving paths and associated entities.");
            System.out.println("Total paths: "+pathList.size());
            System.out.println("Total unique entities: "+totalEntities.size());
            System.out.println("Avg. entities per path: "+avgEnts);

            totalEntities = null; //GC
            System.out.println("Initializing clusterer...");

            int totalRuns = 25;
            int numClusters = 1000;


            ClusterDriver<String,String> clusterDriver = new ClusterDriver<String, String>(pathList,pathEntitySet);
            clusterDriver.setNumClusters(numClusters);

            for (int run = 1; run <= totalRuns; run++) {

                System.out.println("\nRun "+run);

                clusterDriver.init();

                int iter = 0;
                double epsilon = 0.0000001;
                double oldLogLikelihood = Double.NEGATIVE_INFINITY;
                double delta = Double.POSITIVE_INFINITY;

                while(iter < 50 && delta > epsilon) {
                    System.out.println("Iteration "+iter);
                    System.out.println("M-STEP");
                    clusterDriver.maximizationStep();
                    System.out.println("E-STEP");
                    clusterDriver.expectationStep();
                    double logLikelihood = clusterDriver.getLogLikelihood();

                    delta = logLikelihood - oldLogLikelihood;
                    oldLogLikelihood = logLikelihood;

                    System.out.println("Log Likelihood: "+logLikelihood+" (Δ"+delta+")");

                    iter++;
                }



                double[][] responsibilityTable = clusterDriver.getResponsibilityTable();

                for(int n = 0; n < pathList.size(); n++) {
                    StringBuilder buffer = new StringBuilder();

                    buffer.append(pathList.get(n)+"_-_");
                    for(int c = 0; c < numClusters; c++) {
                        buffer.append(responsibilityTable[c][n]);
                        if (c+1 < numClusters)
                            buffer.append(":");

                    }

                    System.out.println(buffer.toString());

                    //int clusterAssignment = clusterDriver.getAssignment(path);
                    //double resp = clusterDriver.getResponsibility(clusterAssignment,path);
                    //System.out.println(path.getEntity1Name()+":"+path.getEntity2Name()+":"+path.getRawPath()+": "+clusterAssignment+" -- "+resp);
                    //assignments.get(clusterAssignment).add(path+" -- "+resp);


                }

                clusterDriver.addCurrentResultsToCache();

            }

            ArrayList<ArrayList<String>> assignments = new ArrayList<ArrayList<String>>();
            for(int i =0; i < numClusters;i++) {
                assignments.add(new ArrayList<String>());
            }

            System.out.println("Averaged responsibilities: ");
            double[][] responsibilityTable = clusterDriver.getAveragedResponsibilities();

            for(int n = 0; n < pathList.size(); n++) {

                int maxCluster = 0;
                double maxResp = 0.0;

                StringBuilder buffer = new StringBuilder();

                buffer.append(pathList.get(n)+"_-_");
                for(int c = 0; c < numClusters; c++) {

                    if (responsibilityTable[c][n] > maxResp) {
                        maxResp = responsibilityTable[c][n];
                        maxCluster = c;
                    }


                    buffer.append(responsibilityTable[c][n]);
                    if (c+1 < numClusters)
                        buffer.append(":");

                }

                System.out.println(buffer.toString());

                //int clusterAssignment = clusterDriver.getAssignment(path);
                //double resp = clusterDriver.getResponsibility(clusterAssignment,path);
                //System.out.println(path.getEntity1Name()+":"+path.getEntity2Name()+":"+path.getRawPath()+": "+clusterAssignment+" -- "+resp);
                assignments.get(maxCluster).add(pathList.get(n));


            }





            System.out.println("Avg. Assignments:");
            for(int i = 0; i < numClusters; i++) {
                System.out.println("Cluster "+i);
                for(String assign : assignments.get(i)) {
                    System.out.println(assign);

                }
                System.out.println();

            }

        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }


    }


}
