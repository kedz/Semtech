package edu.columbia.cs.watson.newsframe.schema;

import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: chris
 * Date: 4/24/13
 * Time: 6:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class SentenceInstance {

    private String[] sentence;
    private Set<DBPediaAnnotation> annotationSet;
    private Map<Integer,DBPediaAnnotation> indexToAnnotationMap = new HashMap<Integer,DBPediaAnnotation>();


    public SentenceInstance(String[] sentence) {
        this.sentence=sentence;
    }

    //public void setSentence(String[] sentence) {this.sentence=sentence;}
    public String getSentence() {
        StringBuilder buffer = new StringBuilder();
        for(String fragment:sentence) {
            if (fragment.startsWith("<A>:")) {


                String[] f = fragment.split("<A>:");
                buffer.append(f[2]);


            } else {
                buffer.append(fragment);
            }

        }

        return buffer.toString();
    }

    public DBPediaAnnotation getAnnotationFromIndex(Integer i) {
        return indexToAnnotationMap.get(i);
    }

    public void addAnnotationSet(Set<DBPediaAnnotation> annotationSet) {
        this.annotationSet=annotationSet;
        for(DBPediaAnnotation annotation : annotationSet) {
            for (int i = annotation.getStartIndex();i < annotation.getStopIndex();i++) {
                //System.out.println(i+" "+annotation.getEntryInstance().getName());
                indexToAnnotationMap.put(new Integer(i),annotation);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        for(String fragment:sentence) {
            if (fragment.startsWith("<A>:")) {

                String[] f = fragment.split("<A>:");
                buffer.append("<entity name=\""+f[1]+"\">"+f[2]+"</entity>");


            } else {
                buffer.append(fragment);
            }

        }

        return buffer.toString();
    }
}
