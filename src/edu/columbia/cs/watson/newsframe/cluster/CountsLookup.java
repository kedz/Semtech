package edu.columbia.cs.watson.newsframe.cluster;

import java.util.HashMap;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: chris
 * Date: 5/8/13
 * Time: 4:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class CountsLookup<E> {

    private HashMap<E, Integer> lookup = new HashMap<E,Integer>();
    private int totalItems = 0;

    public CountsLookup() {}


    public void increment(E item) {

        if (lookup.containsKey(item)) {
            int oldCount = lookup.get(item);
            lookup.put(item,oldCount+1);
        } else {
            lookup.put(item,1);
        }
        totalItems++;
    }

    public int getCount(E item) {
        if (lookup.containsKey(item))
            return lookup.get(item);
        return 0;
    }

    public int numUniqueItems() { return lookup.size(); }
    public int getTotalItems() { return totalItems;}
    public Set<E> keySet() { return lookup.keySet(); }

}
