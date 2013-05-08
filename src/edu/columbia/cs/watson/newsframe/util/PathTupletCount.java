package edu.columbia.cs.watson.newsframe.util;

import edu.columbia.cs.watson.newsframe.schema.DBPediaCategory;
import edu.columbia.cs.watson.newsframe.schema.DBPediaEntryInstance;

/**
 * Created with IntelliJ IDEA.
 * User: chris
 * Date: 4/28/13
 * Time: 10:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class PathTupletCount {

    private DBPediaEntryInstance cat1;
    private DBPediaEntryInstance cat2;
    private String path;
    private long count;
    private int nGram;


    public PathTupletCount(DBPediaEntryInstance cat1, DBPediaEntryInstance cat2, String path, long count, int nGram) {
        this.cat1=cat1;
        this.cat2=cat2;
        this.path=path;
        this.count=count;
        this.nGram=nGram;

    }

    public DBPediaEntryInstance getEntity1() {return cat1;}
    public DBPediaEntryInstance getEntity2() {return cat2;}
    public String getPath() {return path;}
    public long getCount() {return count;}
    public int getNGram() {return nGram;}

    public void incrementCount(long inc) {count += inc;}

    @Override
    public int hashCode() {

        return (cat1.getName()+":"+cat2.getName()+":"+getPath()).hashCode();

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
