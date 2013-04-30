package edu.columbia.cs.watson.newsframe.util;

import edu.columbia.cs.watson.newsframe.schema.DBPediaCategory;

/**
 * Created with IntelliJ IDEA.
 * User: chris
 * Date: 4/28/13
 * Time: 10:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class PathTupletCount {

    private DBPediaCategory cat1;
    private DBPediaCategory cat2;
    private String path;
    private long count;
    private double wCount;
    private boolean isRaw = false;


    public PathTupletCount(DBPediaCategory cat1, DBPediaCategory cat2, String path, long count, double wCount, boolean isRaw) {
        this.cat1=cat1;
        this.cat2=cat2;
        this.path=path;
        this.count=count;
        this.wCount=wCount;
        this.isRaw = isRaw;

    }

    public DBPediaCategory getCategory1() {return cat1;}
    public DBPediaCategory getCategory2() {return cat2;}
    public String getPath() {return path;}
    public long getCount() {return count;}
    public double getWeightedCount() {return wCount;}
    public boolean isRaw() {return isRaw;}


    public void incrementCount(long inc) {count += inc;}
    public void incrementWeightedCount(double inc) {wCount += inc;}


    @Override
    public int hashCode() {

        return (cat1.getCategory()+cat2.getCategory()+getPath()).hashCode();

    }

    @Override
    public boolean equals(Object aThing) {

        if (aThing == null)
            return false;
        if (this == aThing)
            return true;
        if (!(aThing instanceof PathTupletCount))
            return false;
        PathTupletCount aTuplet = (PathTupletCount) aThing;

        return this.hashCode() == hashCode();


    }

}
