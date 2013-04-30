package edu.columbia.cs.watson.newsframe.schema;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: chris
 * Date: 4/24/13
 * Time: 6:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class TaggedXmlInstance {

    private List<SentenceInstance> sentences = new ArrayList<SentenceInstance>();
    private Map<String,DBPediaEntryInstance> entryMap;

    public TaggedXmlInstance(Map<String, DBPediaEntryInstance> entryMap) {
        this.entryMap=entryMap;
    }

    public int size() {return sentences.size();}
    public void addSentence(SentenceInstance sentenceInstance) {sentences.add(sentenceInstance);}
    public List<SentenceInstance> getSentences() {return sentences;}
    public SentenceInstance getSentence(int index) {return sentences.get(index);}

    public Map<String,DBPediaEntryInstance> getDBPediaEntryMap() {return entryMap;}

}
