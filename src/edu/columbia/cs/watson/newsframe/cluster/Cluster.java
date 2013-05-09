package edu.columbia.cs.watson.newsframe.cluster;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: chris
 * Date: 5/8/13
 * Time: 4:12 PM
 * To change this template use File | Settings | File Templates.
 */
public class Cluster<E> {

    private HashMap<E,Double> responsibilitiesMap = new HashMap<E, Double>();
    double totalResponsibility = 0.0;


    public Cluster() {}

    public void assignResponsibility(E item, double r) {
        if (responsibilitiesMap.containsKey(item)) {

            double oldR = responsibilitiesMap.get(item);
            responsibilitiesMap.put(item, r);
            totalResponsibility = totalResponsibility - oldR + r;

        } else {
            responsibilitiesMap.put(item, r);
            totalResponsibility = totalResponsibility + r;

        }

    }

    public double getResponsibility(E item) {
        if (responsibilitiesMap.containsKey(item))
            return responsibilitiesMap.get(item);
        else return 0.0;
    }

    public double getTotalResponsibility() {return totalResponsibility;}
    //public void


}
