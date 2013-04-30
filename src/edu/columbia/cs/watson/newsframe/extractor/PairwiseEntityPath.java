package edu.columbia.cs.watson.newsframe.extractor;

import edu.columbia.cs.watson.newsframe.schema.DBPediaEntryInstance;
import edu.stanford.nlp.ling.CoreLabel;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: chris
 * Date: 4/25/13
 * Time: 9:36 PM
 * To change this template use File | Settings | File Templates.
 */
public class PairwiseEntityPath {

    private DBPediaEntryInstance ent1;
    private DBPediaEntryInstance ent2;
    private List<String> depPath;
    private List<CoreLabel> rawPath;

    public PairwiseEntityPath(DBPediaEntryInstance ent1, DBPediaEntryInstance ent2, List<String> depPath, List<CoreLabel> rawPath) {
        this.ent1=ent1;
        this.ent2=ent2;
        this.rawPath=rawPath;
        this.depPath=depPath;
    }


    public DBPediaEntryInstance getEntity1() {
        return ent1;
    }

    public String getEntity1Name() {
        return ent1.getName();
    }

    public DBPediaEntryInstance getEntity2() {
        return ent2;
    }

    public String getEntity2Name() {
        return ent2.getName();
    }

    public List<String> getDependencyPath() {return depPath;}
    public List<CoreLabel> getRawPath() {return rawPath;}



}
