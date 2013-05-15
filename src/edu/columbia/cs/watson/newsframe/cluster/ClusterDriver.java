package edu.columbia.cs.watson.newsframe.cluster;


import java.util.*;
import java.util.concurrent.*;

/**
 * Created with IntelliJ IDEA.
 * User: chris
 * Date: 5/9/13
 * Time: 1:11 AM
 * To change this template use File | Settings | File Templates.
 */
public class ClusterDriver<K,V> {

    public static final int UNDERFLOW_CUTOFF = 10;
    private ArrayList<Cluster<K>> clusterList;
    private ArrayList<K> keyList;
    private HashMap<K,HashSet<V>> keyValueSetMap;
    private int numClusters = 2;

    private static ExecutorService executorService = Executors.newFixedThreadPool(10);


    private double[][] cachedResponsibilities;
    private int cacheSize = 0;

    private double[][] responsibilities;
    private double[][] qParameters;
    private double[] alphaParameters;
    private HashMap<V,Integer> valueToIndexMap;
    private HashMap<Integer,V> indexToValueMap;

    private int numValues;

    public ClusterDriver(ArrayList<K> keyList, HashMap<K, HashSet<V>> keyValueSetMap) {
        this.keyList = keyList;
        this.keyValueSetMap = keyValueSetMap;

    }

    public void init() {


        valueToIndexMap = new HashMap<V, Integer>();
        indexToValueMap = new HashMap<Integer, V>();

        int valueId = 0;

        System.out.print("CLUSTER INIT: Indexing values...");
        for (K key : keyList) {
            for(V value : keyValueSetMap.get(key)) {
                if (!valueToIndexMap.containsKey(value)) {
                    //System.out.println(value+":"+valueId);
                    valueToIndexMap.put(value,valueId);
                    indexToValueMap.put(valueId++,value);
                }
            }
        }
        System.out.println("\tradical!");

        numValues = valueToIndexMap.size();
        System.out.println(numValues+" total unqiue values.");

        System.out.print("CLUSTER INIT: Initializing "+numClusters+" clusters...");
        clusterList = new ArrayList<Cluster<K>>(numClusters);
        for(int i = 0; i < numClusters; i++)
            clusterList.add(new Cluster<K>());
        System.out.println("\tradical!");

        System.out.print("CLUSTER INIT: Initializing "+numClusters+"x"+keyList.size()+" responsibility table and uniformly setting alpha parameters...");
        responsibilities = new double[numClusters][keyList.size()];
        alphaParameters = new double[numClusters];
        for(int c = 0; c < numClusters; c++) {
            alphaParameters[c] = 1.0/(double)numClusters;
            for(int k = 0; k < keyList.size(); k++) {
                responsibilities[c][k] = 0.0;
            }
        }
        System.out.println("\tradical!");

        System.out.print("CLUSTER INIT: Initializing "+numClusters+"x"+numValues+" q parameters table...");
        qParameters = new double[numClusters][numValues];
        for(int c = 0; c < numClusters; c++) {
            for(int m = 0; m < numValues; m++) {
                qParameters[c][m] = 0.0;
            }
        }
        System.out.println("\tradical!");


        //alter this
        //responsibilities[1][5] = 1.0;
        //responsibilities[0][6] = 1.0;


        System.out.print("CLUSTER INIT: Randomly generating starting cluster assignments...");

        Random generator = new Random(System.currentTimeMillis());
        Random randomClusterAssignment = new Random(numClusters);

        for (int n = 0; n < keyList.size(); n++) {

            double randNum = generator.nextDouble();

            if (randNum > .2) {

                int clusterAssignment = randomClusterAssignment.nextInt(numClusters);
                responsibilities[clusterAssignment][n] = 1.0;


            }



        }
        System.out.println("\ttubular!");




        ExpectationCalculatorThread.initializeTablesandParameters(responsibilities,alphaParameters,qParameters,UNDERFLOW_CUTOFF,numClusters,numValues,indexToValueMap);
        LogLikelihoodCalculatorThread.initializeTablesandParameters(responsibilities, alphaParameters, qParameters, UNDERFLOW_CUTOFF, numClusters, numValues, indexToValueMap);


        maximizeQParameters();
        //maximizationStep();
        System.out.println("CLUSTER INIT: Initialization is complete. Let's do some clustering dawg.");

    }

    public void maximizationStep() {

        maximizeAlphas();
        maximizeQParameters();

    }

    private void maximizeAlphas() {

        for (int c = 0; c < numClusters; c++) {
            double sum = 0.0;

            for (int k = 0; k < keyList.size(); k++) {
                sum += responsibilities[c][k];
            }
            sum += .0001;
            sum = sum/ ( (double)keyList.size() + (double)numClusters * .0001          );
            alphaParameters[c] = sum;

        }


    }

    private void maximizeQParameters() {


        for(int m = 0; m < numValues; m++) {

            V value = indexToValueMap.get(m);

            for(int c = 0; c < numClusters; c++) {

                double numerator = 0.0;
                double denominator = 0.0;


                for(int n = 0; n < keyList.size(); n++) {

                    K key = keyList.get(n);
                    if (keyValueSetMap.get(key).contains(value)) {
                        numerator += responsibilities[c][n]+0.0001;
                    }

                    denominator += responsibilities[c][n]+ (double) numValues * 0.0001;

                }

                qParameters[c][m] = numerator/denominator;

            }
        }

    }


    public void expectationStep() {


        Collection<Future<?>> futures = new LinkedList<Future<?>>();

        for(int n = 0; n < keyList.size(); n++) {


            K key = keyList.get(n);
            HashSet<V> valueSet = keyValueSetMap.get(key);

            futures.add(executorService.submit(new ExpectationCalculatorThread<V>(valueSet, n)));


        }

        try {
            for (Future<?> future:futures) {
                future.get();
        }

        } catch (ExecutionException ee) {
            ee.printStackTrace();
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }

    }


    public void printAlphas() {
        for(int i = 0; i < numClusters; i++) {
            System.out.println("Alpha "+i+ ": "+alphaParameters[i]);
        }
    }

    public void printQParameters() {
        for (int m = 0; m < numValues; m++) {

            System.out.println(indexToValueMap.get(m)+":");
            for(int c = 0; c < numClusters; c++)
                System.out.println("\t"+qParameters[c][m]);


        }
    }

    public void printResponsibilities() {
        for (int n = 0; n < keyList.size(); n++) {

            System.out.print(n + ": ");
            for(int c = 0; c < numClusters; c++)
                System.out.print("\t" + c + ": " + responsibilities[c][n]);

            System.out.println();
        }
    }

    public void printMostLikelyAssignments() {

        System.out.println("Cluster assignments:");

        for (int n = 0; n < keyList.size(); n++) {

            double maxR = 0.0;
            int maxCluster = 0;

            for(int c = 0; c < numClusters; c++) {

                if (responsibilities[c][n] > maxR) {
                    maxR = responsibilities[c][n];
                    maxCluster = c;
                }


            }

            System.out.println(n+": "+ maxCluster +" : "+maxR);

        }


    }

    public int getAssignment(K key) {

        int index = keyList.indexOf(key);

        double maxR = 0.0;
        int maxCluster = 0;

        for(int c = 0; c < numClusters; c++) {

            if (responsibilities[c][index] > maxR) {
                maxR = responsibilities[c][index];
                maxCluster = c;
            }

        }

        return maxCluster;

    }

    public double getResponsibility(int clusterIndex, K key) {

        int keyIndex = keyList.indexOf(key);
        return responsibilities[clusterIndex][keyIndex];

    }

    public void setNumClusters(int numClusters) {this.numClusters = numClusters;}

    public double getLogLikelihood() {

        double logLikelihood = 0.0;
        LinkedList<LogLikelihoodCalculatorThread<V>> threadList = new LinkedList<LogLikelihoodCalculatorThread<V>>();

        Collection<Future<?>> futures = new LinkedList<Future<?>>();

        for(int n = 0; n < keyList.size(); n++) {


            K key = keyList.get(n);
            HashSet<V> valueSet = keyValueSetMap.get(key);

            LogLikelihoodCalculatorThread<V> llCalc = new LogLikelihoodCalculatorThread<V>(valueSet,n);

            threadList.add(llCalc);
            futures.add(executorService.submit(llCalc));


        }

        try {
            for (Future<?> future:futures) {
                future.get();
            }

        } catch (ExecutionException ee) {
            ee.printStackTrace();
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }

        for(LogLikelihoodCalculatorThread<V> calc : threadList) {
            logLikelihood += calc.logLikelihood;
        }

        return logLikelihood;
    }

    public double[][] getResponsibilityTable() { return responsibilities; }

    public void addCurrentResultsToCache() {
        if (cachedResponsibilities == null) {
            cachedResponsibilities = responsibilities;
        } else {

            for (int c = 0; c < numClusters; c++) {
                for (int n = 0; n < keyList.size(); n++) {
                    cachedResponsibilities[c][n] += responsibilities[c][n];

                }
            }
        }
        cacheSize++;
    }

    public double[][] getAveragedResponsibilities() {

        double[][] avgResp = new double[numClusters][keyList.size()];

        for (int c = 0; c < numClusters; c++) {
            for (int n = 0; n < keyList.size(); n++) {
                avgResp[c][n] = cachedResponsibilities[c][n]/(double)cacheSize;

            }
        }
        return avgResp;
    }

    public void shutdown() {
        executorService.shutdown();
    }


    public static void main(String[] args) {


        ArrayList<Integer> docIdList = new ArrayList<Integer>();
        Integer one = new Integer(1);
        docIdList.add(one);
        Integer two = new Integer(2);
        Integer three = new Integer(3);
        Integer four = new Integer(4);
        Integer five = new Integer(5);
        Integer six = new Integer(6);
        Integer seven = new Integer(7);
        Integer eight = new Integer(8);
        Integer nine = new Integer(9);
        Integer ten = new Integer(10);
        Integer eleven = new Integer(11);
        docIdList.add(two);
        docIdList.add(three);
        docIdList.add(four);
        docIdList.add(five);
        docIdList.add(six);
        docIdList.add(seven);
        docIdList.add(eight);
        docIdList.add(nine);
        docIdList.add(ten);
        docIdList.add(eleven);


        HashSet<String> oneWords = new HashSet<String>();
        oneWords.add("hot");
        oneWords.add("chocolate");
        oneWords.add("cocoa");
        oneWords.add("beans");

        HashSet<String> twoWords = new HashSet<String>();
        twoWords.add("cocoa");
        twoWords.add("ghana");
        twoWords.add("africa");

        HashSet<String> threeWords = new HashSet<String>();
        threeWords.add("beans");
        threeWords.add("harvest");
        threeWords.add("ghana");

        HashSet<String> fourWords = new HashSet<String>();
        fourWords.add("cocoa");
        fourWords.add("butter");

        HashSet<String> fiveWords = new HashSet<String>();
        fiveWords.add("butter");
        fiveWords.add("truffles");

        HashSet<String> sixWords = new HashSet<String>();
        sixWords.add("sweet");
        sixWords.add("chocolate");

        HashSet<String> sevenWords = new HashSet<String>();
        sevenWords.add("sweet");
        sevenWords.add("sugar");

        HashSet<String> eightWords = new HashSet<String>();
        eightWords.add("sugar");
        eightWords.add("cane");
        eightWords.add("brazil");

        HashSet<String> nineWords = new HashSet<String>();
        nineWords.add("sweet");
        nineWords.add("sugar");
        nineWords.add("beet");

        HashSet<String> tenWords = new HashSet<String>();
        tenWords.add("sweet");
        tenWords.add("cake");
        tenWords.add("icing");


        HashSet<String> elevenWords = new HashSet<String>();
        elevenWords.add("cake");
        elevenWords.add("black");
        elevenWords.add("forest");

        HashMap<Integer,HashSet<String>> keyValueSetMap = new HashMap<Integer,HashSet<String>>();
        keyValueSetMap.put(one, oneWords);
        keyValueSetMap.put(two, twoWords);
        keyValueSetMap.put(three, threeWords);
        keyValueSetMap.put(four, fourWords);
        keyValueSetMap.put(five, fiveWords);
        keyValueSetMap.put(six, sixWords);
        keyValueSetMap.put(seven, sevenWords);
        keyValueSetMap.put(eight, eightWords);
        keyValueSetMap.put(nine, nineWords);
        keyValueSetMap.put(ten, tenWords);
        keyValueSetMap.put(eleven, elevenWords);

        ClusterDriver<Integer,String> cluster = new ClusterDriver<Integer, String>(docIdList,keyValueSetMap);

        cluster.init();
        cluster.printAlphas();
        cluster.printQParameters();

        cluster.expectationStep();

        for (int i = 2; i <= 25; i++) {
            cluster.maximizationStep();
            cluster.expectationStep();
            cluster.printResponsibilities();
            System.out.println("Log Likelihood: " + cluster.getLogLikelihood());
        }

        cluster.printAlphas();
        cluster.printResponsibilities();
        cluster.printMostLikelyAssignments();

        cluster.shutdown();


        /*
        System.setProperty("wordnet.database.dir", "/home/chris/WordNet-3.0/dict");
        WordNetDatabase database = WordNetDatabase.getFileInstance();


        ArrayList<PairwiseEntityPath> pathList = new ArrayList<PairwiseEntityPath>();
        HashMap<PairwiseEntityPath,HashSet<VerbSynset>> pathToSynsetsMap = new HashMap<PairwiseEntityPath, HashSet<VerbSynset>>();

        try {

            BufferedReader reader = new BufferedReader(new FileReader(new File("/home/chris/corpora/newsblaster_watson/subset_paths.txt")));

            while(reader.ready()) {


                String[] line = reader.readLine().split("_-_-_");
                PairwiseEntityPath aPath = new PairwiseEntityPath(  new DBPediaEntryInstance(line[0], new LinkedList<DBPediaCategory>()),
                        new DBPediaEntryInstance(line[1], new LinkedList<DBPediaCategory>()),
                        line[3],Integer.parseInt(line[2]));
                pathList.add(aPath);
                HashSet<VerbSynset> verbSynsetsCollection = new HashSet<VerbSynset>();
                for(String word : aPath.getWordList())  {

                    Synset[] synsets = database.getSynsets(word, SynsetType.VERB);
                    for (Synset synset : synsets) {

                        VerbSynset[] verbSynsets = ((VerbSynset) synset).getHypernyms();
                        for( VerbSynset vsyn : verbSynsets) {
                            verbSynsetsCollection.add(vsyn);

                        }

                    }


                }
                pathToSynsetsMap.put(aPath,verbSynsetsCollection);


            }

            int clusters = 100;
            ClusterDriver<PairwiseEntityPath,VerbSynset> clusterDriver2 = new ClusterDriver<PairwiseEntityPath, VerbSynset>(pathList,pathToSynsetsMap);
            clusterDriver2.setNumClusters(clusters);
            clusterDriver2.init();

            for(int i = 1; i <= 40; i++) {
                System.out.println("Iteration "+i);
                System.out.println("M-STEP");
                clusterDriver2.maximizationStep();
                System.out.println("E-STEP");
                clusterDriver2.expectationStep();
            }

            ArrayList<ArrayList<String>> assignments = new ArrayList<ArrayList<String>>();
            for(int i =0; i < clusters;i++) {
                assignments.add(new ArrayList<String>());
            }

            for(PairwiseEntityPath path : pathList) {

                int clusterAssignment = clusterDriver2.getAssignment(path);
                double resp = clusterDriver2.getResponsibility(clusterAssignment,path);
                //System.out.println(path.getEntity1Name()+":"+path.getEntity2Name()+":"+path.getRawPath()+": "+clusterAssignment+" -- "+resp);
                assignments.get(clusterAssignment).add(path.getEntity1Name()+":"+path.getEntity2Name()+":"+path.getRawPath()+": "+clusterAssignment+" -- "+resp);


            }

            for(int i = 0; i < clusters; i++) {
                System.out.println("Cluster "+i);
                for(String assign : assignments.get(i)) {
                    System.out.println(assign);

                }
                System.out.println();

            }




        } catch (IOException ioe) {
            System.out.println("!");
            ioe.printStackTrace();
        }

        */

    }

}
