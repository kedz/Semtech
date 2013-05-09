package edu.columbia.cs.watson.newsframe.extractor;

import edu.columbia.cs.watson.newsframe.cluster.Cluster;
import edu.columbia.cs.watson.newsframe.cluster.CountsLookup;
import edu.columbia.cs.watson.newsframe.schema.DBPediaCategory;
import edu.columbia.cs.watson.newsframe.schema.DBPediaEntryInstance;
import edu.smu.tspell.wordnet.Synset;
import edu.smu.tspell.wordnet.SynsetType;
import edu.smu.tspell.wordnet.VerbSynset;
import edu.smu.tspell.wordnet.WordNetDatabase;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;


/**
 * Created with IntelliJ IDEA.
 * User: chris
 * Date: 5/8/13
 * Time: 2:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class ClusterDriverBad<K,V> {

    private CountsLookup<V> globalQCounts;
    private LinkedList<K> keyList;
    private HashMap<K,CountsLookup<V>> keyToValueSetMap;
    private int clusterSize = 100;



    private ArrayList<Cluster<K>> clusterList;

    private ArrayList<Double> alphaParameters;
    private ArrayList<HashMap<V,Double>> qParameters;

    public ClusterDriverBad(LinkedList<K> keyList, HashMap<K, CountsLookup<V>> keyToValueSetMap, CountsLookup<V> globalQCounts) {

        this.keyList=keyList;
        this.keyToValueSetMap=keyToValueSetMap;
        this.globalQCounts=globalQCounts;

    }

    public void setClusterSize(int size) {this.clusterSize=size;}
    public int getClusterSize() {return clusterSize;}

    public void init() {

        clusterList = new ArrayList<Cluster<K>>(clusterSize);
        alphaParameters = new ArrayList<Double>(clusterSize);
        qParameters = new ArrayList<HashMap<V,Double>>(clusterSize);



        for(int i = 0; i <clusterSize;i++) {
            clusterList.add(new Cluster<K>());
            alphaParameters.add(1.0/(double)clusterSize);

        }





        for (int i = 0; i < keyList.size(); i++){
            K item = keyList.get(i);

            for(int j = 0; j < clusterSize; j++) {

                clusterList.get(j).assignResponsibility(item, 1.0/(double)clusterSize);

            }


        }


        System.out.println("KEYS: "+keyList.size());
        System.out.println("CLUSTERS: "+clusterSize);



        maximizationStep();

    }




    public void maximizationStep() {


        maximizeAlphaParameters();
        maximizeQParameters();
        System.out.println("ALPHAS: ");
        for(Double d: alphaParameters)
            System.out.println(d);

        System.out.println("Qs");
        for(int i=0; i < clusterSize;i++) {
            for(V value : qParameters.get(i).keySet()) {
                System.out.println(i+" : "+ value+" : "+qParameters.get(i).get(value) );


            }


        }

    }

    private void maximizeAlphaParameters() {

        ArrayList<Double> rSums = new ArrayList<Double>(clusterSize);
        for(int i = 0; i < clusterSize;i++) {
            rSums.add(0.0);
        }



        for(int i = 0; i < keyList.size(); i++) {

            K item = keyList.get(i);

            for (int j = 0; j < clusterSize; j++) {

                double r = clusterList.get(j).getResponsibility(item);

                double oldR = rSums.get(j);
                System.out.println(r);

                rSums.set(j,r+oldR);


            }

        }

        for(int i = 0; i < clusterSize;i++) {
            double newAlpha = rSums.get(i)/keyList.size();
            System.out.println(newAlpha);
            alphaParameters.set(i,newAlpha);

        }

        rSums = null;

    }

    private void maximizeQParameters() {

        ArrayList<HashMap<V,Double>> newQParams = new ArrayList<HashMap<V,Double>>(clusterSize);

        for (int i = 0; i < clusterSize; i++) {

            Cluster cluster = clusterList.get(i);
            HashMap<V,Double> qKParams = new HashMap<V, Double>();


            for (K item : keyList) {

                double r = cluster.getResponsibility(item);

                for(V value : keyToValueSetMap.get(item).keySet()) {

                    if (qKParams.containsKey(value)) {
                        double oldR = qKParams.get(value);
                        qKParams.put(value,oldR+r);
                    } else {
                        qKParams.put(value,r);
                    }



                }

            }

            newQParams.add(i, qKParams);

        }

        for (V value : globalQCounts.keySet()) {


            for(int i = 0; i < clusterSize; i++) {

                double numerator = newQParams.get(i).get(value);
                newQParams.get(i).put(value, numerator/clusterList.get(i).getTotalResponsibility());

            }

        }

        qParameters = null;
        qParameters = newQParams;


    }

    public void expectationStep() {

        ArrayList<Double> precomputedAllMissing = precomputeNoValuesProb();


        for (int i =0; i < keyList.size();i++) {

            K item = keyList.get(i);
            ArrayList<Double> newRespList = new ArrayList<Double>(clusterSize);
            double denominator = 0.0;

            for(int j = 0; j < clusterSize;j++) {

                double numerator = precomputedAllMissing.get(j);

                for(V value : keyToValueSetMap.get(item).keySet()) {

                    double qParam = qParameters.get(j).get(value);

                    numerator = numerator + Math.log(qParam) - Math.log(1-qParam);


                }

                numerator = numerator + Math.log(alphaParameters.get(j));
                newRespList.add(j, numerator);
                denominator += Math.exp(numerator);
            }

            for(int j = 0; j <clusterSize;j++) {

                double newResp = newRespList.get(j);

                clusterList.get(j).assignResponsibility(item,Math.exp(newResp)/denominator);

            }


        }


    }

    private ArrayList<Double> precomputeNoValuesProb() {

        ArrayList<Double> precomputed = new ArrayList<Double>(clusterSize);
        for(int i = 0; i < clusterSize;i++) {

            double sum = 0.0;

            for(V value : globalQCounts.keySet()) {


                sum += Math.log(1-qParameters.get(i).get(value));


            }

            precomputed.add(i, sum);

        }


        return precomputed;

    }

    private void incrementDoubleListEntry(ArrayList<Double> arrList, int index, double inc) {

        if (index >= arrList.size() || arrList.get(index)==null)
            arrList.add(index, inc);
        else {
            double oldVal = arrList.get(index);
            arrList.set(index,oldVal+inc);
        }

    }


    public ArrayList<HashMap<K,Double>> getClusterAssignments() {

        ArrayList<HashMap<K,Double>> assignments = new ArrayList<HashMap<K, Double>>(clusterSize);
        for (int i=0;i<clusterSize;i++) {
            assignments.add(i, new HashMap<K, Double>());


        }


        for(K item : keyList) {

            int maxCluster = 0;
            double maxR = 0;

            for (int j = 0; j < clusterSize;j++) {

                double r = clusterList.get(j).getResponsibility(item);

                if (r > maxCluster) {
                    maxR = r;
                    maxCluster = j;

                }

            }

            assignments.get(maxCluster).put(item,maxR);


        }

        return assignments;
    }

    public static void main(String[] args) {


        System.setProperty("wordnet.database.dir", "/home/chris/WordNet-3.0/dict");
        WordNetDatabase database = WordNetDatabase.getFileInstance();
        //HashSet<VerbSynset> uniqueVerbSynsets = new HashSet<VerbSynset>();
        CountsLookup<VerbSynset> globalSynsetCounts = new CountsLookup();
        HashMap<PairwiseEntityPath,CountsLookup<VerbSynset>> pathToSynsetsMap= new HashMap<PairwiseEntityPath,CountsLookup<VerbSynset>>();
        LinkedList<PairwiseEntityPath> pathList = new LinkedList<PairwiseEntityPath>();



        try {

            BufferedReader reader = new BufferedReader(new FileReader(new File("/home/chris/corpora/newsblaster_watson/smaller_paths.txt")));

            while(reader.ready()) {


                String[] line = reader.readLine().split("_-_-_");
                PairwiseEntityPath aPath = new PairwiseEntityPath(  new DBPediaEntryInstance(line[0], new LinkedList<DBPediaCategory>()),
                                                                    new DBPediaEntryInstance(line[1], new LinkedList<DBPediaCategory>()),
                                                                    line[3],Integer.parseInt(line[2]));
                pathList.add(aPath);
                CountsLookup<VerbSynset> pathSynsetCounts = new CountsLookup<VerbSynset>();
                for(String word : aPath.getWordList())  {

                    Synset[] synsets = database.getSynsets(word, SynsetType.VERB);
                    for (Synset synset : synsets) {

                        VerbSynset[] verbSynsets = ((VerbSynset) synset).getHypernyms();
                        for( VerbSynset vsyn : verbSynsets) {

                            globalSynsetCounts.increment(vsyn);
                            pathSynsetCounts.increment(vsyn);
                        }

                    }


                }
                pathToSynsetsMap.put(aPath,pathSynsetCounts);


            }

            //System.out.println("Total unique words: "+globalWordCounts.numUniqueWords());
            //System.out.println("Total words: "+globalWordCounts.getTotalWords());
            System.out.println("Initializing clusters...");
            ClusterDriverBad<PairwiseEntityPath,VerbSynset> clusterDriver = new ClusterDriverBad<PairwiseEntityPath,VerbSynset>(pathList,pathToSynsetsMap,globalSynsetCounts);
            clusterDriver.setClusterSize(3);
            clusterDriver.init();

            for (int i = 1; i <= 2;i++) {
                System.out.println("Iteration "+i);
                System.out.println("\tE-STEP");
                clusterDriver.expectationStep();
                System.out.println("\tM-STEP");
                clusterDriver.maximizationStep();

            }

            ArrayList<HashMap<PairwiseEntityPath,Double>> assignments = clusterDriver.getClusterAssignments();

            for(int i = 0; i < assignments.size(); i++) {
                System.out.println("Cluster "+i);
                HashMap<PairwiseEntityPath,Double> map = assignments.get(i);

                for(PairwiseEntityPath path : map.keySet()) {

                    System.out.println(map.get(path) +" : " +path.getRawPath());

                }

                System.out.println("--------\n");

            }

        } catch (Exception e) {

            System.out.println("Ex");
            e.printStackTrace();

        }
        /*
        System.out.print("Connecting to Newsframe DB...");

        Connection connection = ConnectionFactory.getConnection();
        System.out.println("Yahtzee!");

        long numResults = 0;
        try {
            System.out.println("Querying DB...");

            PreparedStatement statement = connection.prepareStatement("SELECT entity1, entity2, word, ngram FROM newsframe.raw_frame WHERE ngram > 3 AND score > 2");
            ResultSet resultSet = statement.executeQuery();

            while(resultSet.next()) {
                numResults++;

                //System.out.println("____\n\n");
                //System.out.println(ent1+":"ent2path);
                String ent1 = resultSet.getString(1);
                String ent2 = resultSet.getString(2);
                String path = resultSet.getString(3);
                int ngram   = resultSet.getInt(4);
                System.out.println(ent1+":"+ent2+":"+ngram+":"+path);
                /*
                for(String token : path.split(" ")) {
                    if (!token.equals("_ent1_") && !token.equals("_ent2_")) {
                        Synset[] synsets = database.getSynsets(token, SynsetType.VERB);
                        for (Synset synset : synsets) {

                            VerbSynset[] verbSynsets = ((VerbSynset) synset).getHypernyms();
                            for( VerbSynset vsyn : verbSynsets) {


                                System.out.println(vsyn);
                                uniqueVerbSynsets.add(vsyn);

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
        */

        //for(VerbSynset syn : uniqueVerbSynsets)
        //    System.out.println(syn);
        //System.out.println("Total unique verb synsets: "+ uniqueVerbSynsets.size());
        //System.out.println("Total paths: "+numResults);

    }


}
