package edu.columbia.cs.watson.newsframe.xml;

import edu.columbia.cs.watson.newsframe.db.DBPediaAnnotator;
import edu.columbia.cs.watson.newsframe.schema.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: chris
 * Date: 4/24/13
 * Time: 5:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class TaggedXmlReader {

    private static DocumentBuilder documentBuilder;
    static {
        try {
            documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException pce) {
            System.out.println("XML Parser config failed!");
            pce.printStackTrace();
        }
    }

    private static double PROBABILITY_CUTOFF = .00005;

    //private static DBPediaAnnotator dbPediaAnnotator = new DBPediaAnnotator();

    public static TaggedXmlInstance parseXmlFile(File xml) {

        Map<String,DBPediaEntryInstance> entryMap = new HashMap<String,DBPediaEntryInstance>();

        try {


            TaggedXmlInstance taggedXmlInstance = new TaggedXmlInstance(entryMap);
            Document document = documentBuilder.parse(xml);

            NodeList sentenceNodes = document.getElementsByTagName("sentence");
            for(int i=0; i <sentenceNodes.getLength();i++) {



                Element sentenceElement = (Element) sentenceNodes.item(i);

                int cutoff = 30;
                if (sentenceElement.getTextContent().length()> cutoff)
                    System.out.println(i+": "+sentenceElement.getTextContent().substring(0,cutoff ).trim()+"...");
                else
                    System.out.println(i+": "+sentenceElement.getTextContent().trim());

                cutoff = sentenceElement.getTextContent().length();

                taggedXmlInstance.addSentence(createSentenceFromXmlElement(sentenceElement,entryMap));


            }

            DBPediaAnnotator.closeConnection();

            return taggedXmlInstance;

        } catch(IOException ioe) {
            ioe.printStackTrace();
            System.exit(-1);
        } catch(SAXException saxe) {
            saxe.printStackTrace();
            System.exit(-1);
        }

        return null;

    }

    private static SentenceInstance createSentenceFromXmlElement(Element sentence, Map<String,DBPediaEntryInstance> entryMap) {

        if (sentence.getTagName().equals("sentence")) {

            Set<DBPediaAnnotation> annotationSet = new HashSet<DBPediaAnnotation>();

            int offset = 0;

            NodeList entityElements = sentence.getChildNodes();


            String[] fragments = null;
            if (entityElements.getLength() - 1 > 0)
                fragments = new String[entityElements.getLength()-1];
            else
                fragments = new String[1];

            for(int i=1;i<entityElements.getLength();i++) {
                if (entityElements.item(i).getNodeName().equals("#text")) {
                    String text = ((Text) entityElements.item(i)).getTextContent();
                    offset += text.length();
                    fragments[i-1] = text;


                } else {

                    Element entity = (Element) entityElements.item(i);
                    String text = entity.getTextContent();
                    String entityName = entity.getAttributes().getNamedItem("name").getNodeValue();
                    double prob = Double.parseDouble(entity.getAttributes().getNamedItem("prob").getNodeValue());

                    int start = offset;
                    offset += text.length();
                    int end = offset;

                    if (prob < PROBABILITY_CUTOFF) {
                        fragments[i-1] = text;
                    } else {


                        DBPediaEntryInstance dbPediaEntry;

                        if (entryMap.containsKey(entityName) && entryMap.get(entityName).getCategories().size()>0) {
                            dbPediaEntry = entryMap.get(entityName);
                            annotationSet.add(new DBPediaAnnotation(dbPediaEntry, start, end));
                            fragments[i-1] = "<A>:"+entityName + "<A>:"+text;


                        } else if (entryMap.containsKey(entityName)){
                            fragments[i-1] = text;
                        } else {

                            List<DBPediaCategory> categories = null;

                            try {
                                DBPediaAnnotator dbPediaAnnotator = new DBPediaAnnotator();
                                categories = dbPediaAnnotator.annotate(entityName);

                                dbPediaEntry = new DBPediaEntryInstance(entityName,categories);
                                entryMap.put(dbPediaEntry.getName(), dbPediaEntry);


                                if (categories.size()>0) {


                                    fragments[i-1] = "<A>:"+entityName + "<A>:"+text;




                                    annotationSet.add(new DBPediaAnnotation(dbPediaEntry, start, end));


                                } else {
                                    fragments[i-1] = text;
                                    System.out.println("Not a person or place!");

                                }





                            } catch (Exception e) {
                                //categories = new ArrayList<DBPediaCategory>();
                                System.out.println("Not a person or place!");

                            }

                            //String[] categories = entity.getAttributes().getNamedItem("categories").getNodeValue().split(":");

                        }



                    }

                }


            }

            SentenceInstance sentenceInstance = new SentenceInstance(fragments);
            sentenceInstance.addAnnotationSet(annotationSet);

            return  sentenceInstance;

        } else {
            return null;
        }
    }

}
