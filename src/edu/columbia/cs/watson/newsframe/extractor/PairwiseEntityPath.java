package edu.columbia.cs.watson.newsframe.extractor;

import edu.columbia.cs.watson.newsframe.schema.DBPediaEntryInstance;
import edu.stanford.nlp.ling.CoreLabel;

import java.util.ArrayList;
import java.util.LinkedList;
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
    //private List<String> depPath;
    //private List<CoreLabel> rawPath;
    private String rawPath;
    private int nGram;
    private ArrayList<String> wordList;


    public PairwiseEntityPath(DBPediaEntryInstance ent1, DBPediaEntryInstance ent2, String rawPath, int nGram) {
        this.ent1=ent1;
        this.ent2=ent2;
        this.rawPath=rawPath;
        this.nGram=nGram;

        String[] words = rawPath.split(" ");

        wordList = new ArrayList<String>(words.length);
        for(String word : words) {
            if (!word.equals("_ent1_") && !word.equals("_ent2_")) {
                wordList.add(word);
            }
        }

        //this.depPath=depPath;
    }

    public ArrayList<String> getWordList() {return wordList;}

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

    //public List<String> getDependencyPath() {return depPath;}
    public String getRawPath() {return rawPath;}
    public int getNGram() {return nGram;}


}
