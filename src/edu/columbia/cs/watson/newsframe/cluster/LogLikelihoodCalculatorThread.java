package edu.columbia.cs.watson.newsframe.cluster;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Created with IntelliJ IDEA.
 * User: chris
 * Date: 5/14/13
 * Time: 9:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class LogLikelihoodCalculatorThread<V> implements Runnable {

    private static double RESPONSIBILITY_TABLE[][] = null;
    private static double ALPHA_PARAMETERS[] = null;
    private static double Q_PARAMETERS[][] = null;
    private static int UNDERFLOW_CUTOFF;
    private static int NUM_CLUSTERS;
    private static int NUM_VALUES;
    private static HashMap<Integer,?> INDEX_TO_VALUE_MAP = null;

    public double logLikelihood = 0.0;

    private HashSet<V> valueSet;
    private int n;

    public LogLikelihoodCalculatorThread(HashSet<V> valueSet, int n) {
        this.valueSet = valueSet;
        this.n = n;
    }

    public static void initializeTablesandParameters(double[][] responsibilityTable,double[] alphaTable, double[][] qTable, int underflowCutoff, int numClusters, int numValues, HashMap<Integer,?> indexToValueMap) {
        RESPONSIBILITY_TABLE = responsibilityTable;
        ALPHA_PARAMETERS = alphaTable;
        Q_PARAMETERS = qTable;
        UNDERFLOW_CUTOFF = underflowCutoff;
        NUM_CLUSTERS = numClusters;
        NUM_VALUES = numValues;
        INDEX_TO_VALUE_MAP = indexToValueMap;

    }

    public void run() {



        double[] logLikelihoods = new double[NUM_CLUSTERS];
        double maxLogLikelihood = Double.NEGATIVE_INFINITY;


        for(int c = 0; c < NUM_CLUSTERS; c++) {

            double newLogLikelihood = Math.log(ALPHA_PARAMETERS[c]);

            for(int m = 0; m < NUM_VALUES; m++) {
                V value = (V) INDEX_TO_VALUE_MAP.get(m);
                double qParam = Q_PARAMETERS[c][m];

                if (valueSet.contains(value)) {
                    newLogLikelihood += Math.log(qParam);
                } else {
                    newLogLikelihood += Math.log(1.0-qParam);
                }
            }

            logLikelihoods[c] = newLogLikelihood;
            if (newLogLikelihood > maxLogLikelihood)
                maxLogLikelihood = newLogLikelihood;

        }




        double innerSum = 0.0;

        for (int c = 0; c < NUM_CLUSTERS; c++) {

            if (logLikelihoods[c] - maxLogLikelihood >= -UNDERFLOW_CUTOFF) {

                innerSum += Math.exp(logLikelihoods[c] - maxLogLikelihood);
            }

        }

        logLikelihood += maxLogLikelihood + Math.log(innerSum);



    }



}
