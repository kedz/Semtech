package edu.columbia.cs.watson.newsframe.schema;

/**
 * Created with IntelliJ IDEA.
 * User: chris
 * Date: 4/24/13
 * Time: 7:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class DBPediaAnnotation {

    private DBPediaEntryInstance entryInstance;
    private int startIndex;
    private int stopIndex;

    public DBPediaAnnotation(DBPediaEntryInstance entryInstance, int startIndex, int stopIndex) {
        this.entryInstance = entryInstance;
        this.startIndex = startIndex;
        this.stopIndex = stopIndex;
    }

    public void setStartIndex(int startIndex) {this.startIndex=startIndex;}
    public int getStartIndex() {return startIndex;}

    public void setStopIndex(int stopIndex) {this.stopIndex=stopIndex;}
    public int getStopIndex() {return stopIndex;}

    public void setEntryInstance(DBPediaEntryInstance entryInstance) {this.entryInstance=entryInstance;}
    public DBPediaEntryInstance getEntryInstance() {return entryInstance;}

}
