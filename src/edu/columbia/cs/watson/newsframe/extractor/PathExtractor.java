package edu.columbia.cs.watson.newsframe.extractor;

import edu.columbia.cs.watson.newsframe.db.ConnectionFactory;
import edu.columbia.cs.watson.newsframe.db.NewsFrameConn;
import edu.columbia.cs.watson.newsframe.schema.DBPediaAnnotation;
import edu.columbia.cs.watson.newsframe.schema.DBPediaEntryInstance;
import edu.columbia.cs.watson.newsframe.schema.SentenceInstance;
import edu.columbia.cs.watson.newsframe.schema.TaggedXmlInstance;
import edu.columbia.cs.watson.newsframe.util.PathTupletCount;
import edu.columbia.cs.watson.newsframe.xml.TaggedXmlReader;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import org.apache.commons.cli.*;

import java.io.File;
import java.io.FilenameFilter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: chris
 * Date: 4/24/13
 * Time: 6:04 PM
 * To change this template use File | Settings | File Templates.
 */
public class PathExtractor {

    private static HashSet<String> punctuationTagSet = new HashSet<String>();

    static {

        punctuationTagSet.add("#");
        punctuationTagSet.add("$");
        punctuationTagSet.add("''");
        punctuationTagSet.add("(");
        punctuationTagSet.add(")");
        punctuationTagSet.add(",");
        punctuationTagSet.add(".");
        punctuationTagSet.add(":");
        punctuationTagSet.add("``");


    }

    public static List<PairwiseEntityPath> extractPairwiseEntityPaths(SentenceInstance sentenceInstance, Annotation annotation) {



        List<PairwiseEntityPath> paths = new LinkedList<PairwiseEntityPath>();

        HashMap<CoreLabel,DBPediaAnnotation> tokensAnnotationMap = new HashMap<CoreLabel,DBPediaAnnotation>();
        ArrayList<CoreLabel> entityTokens = new ArrayList<CoreLabel>();


        List<CoreLabel> tokens = annotation.get(CoreAnnotations.TokensAnnotation.class);

        for (CoreLabel token : tokens) {


            DBPediaAnnotation dbPediaAnnotation = sentenceInstance.getAnnotationFromIndex(token.beginPosition());
            if (dbPediaAnnotation!= null) {
                tokensAnnotationMap.put(token, dbPediaAnnotation);
                entityTokens.add(token);
            }

        }





        Collection<CoreMap> sentences =  annotation.get(CoreAnnotations.SentencesAnnotation.class);

        if (sentences.size() > 0) {
            CoreMap sentence = sentences.iterator().next();

            List<CoreLabel> words = sentence.get(CoreAnnotations.TokensAnnotation.class);


            ArrayList<CoreLabel> wordStack = new ArrayList<CoreLabel>();

            for(int i = 0; i < words.size();i++) {

                CoreLabel word = words.get(i);
                if (tokensAnnotationMap.containsKey(word)) {

                    DBPediaAnnotation dbAnn1 = tokensAnnotationMap.get(word);

                    if (i+1 < words.size()) {

                        int j = i + 1;
                        CoreLabel word2 = words.get(j);

                        while(j < words.size() && tokensAnnotationMap.containsKey(word2) && tokensAnnotationMap.get(word2).getEntryInstance().getName().equals(dbAnn1.getEntryInstance().getName())) {
                            j++;
                            if (j < words.size())
                                word2 = words.get(j);


                        }
                        i = j-1;


                    }

                    wordStack.add(word);

                } else {

                    if (!punctuationTagSet.contains(word.tag()) && !word.tag().equals("POS") )
                        wordStack.add(word);
                }



            }


            for(int i = 0; i < wordStack.size(); i++) {
                CoreLabel word1 = wordStack.get(i);
                if (tokensAnnotationMap.containsKey(word1)) {
                    int ngram = 0;



                    StringBuilder buffer = new StringBuilder();
                    for(int j = i +1; j < wordStack.size() && j < i +5;j++ ) {

                        CoreLabel word2 = wordStack.get(j);
                        if (tokensAnnotationMap.containsKey(word2)) {
                            //System.out.println(tokensAnnotationMap.get(word1).getEntryInstance().getName()+": "+buffer.toString()+" :"+tokensAnnotationMap.get(word2).getEntryInstance().getName() + " : "+ngram);
                            if (ngram <= 3)
                                paths.add(new PairwiseEntityPath(tokensAnnotationMap.get(word1).getEntryInstance(),
                                    tokensAnnotationMap.get(word2).getEntryInstance(), buffer.toString().trim(), ngram));

                            buffer.append(" "+tokensAnnotationMap.get(word2).getEntryInstance().getName());

                        } else {
                            buffer.append(" "+word2.lemma()) ;
                        }
                        ngram++;


                    }



               }

            }

            /*
            for(int i=0;i<words.size();i++) {

                CoreLabel word = words.get(i);


                if (tokensAnnotationMap.containsKey(word)) {

                    DBPediaAnnotation ann1 = tokensAnnotationMap.get(word);
                    StringBuilder buffer = new StringBuilder();









                    int nGram = 0;
                    int slack = 8;
                    for (int j=i+1;j<words.size() && j < i+slack;j++  ) {


                        CoreLabel word2 = words.get(j);


                        if (tokensAnnotationMap.containsKey(word2) ) {





                            DBPediaAnnotation ann2 = tokensAnnotationMap.get(word2);

                            if (ann1 != ann2) {

                                String path = "_ent1_ " + buffer.toString() + "_ent2_";

                                String prefix = findWordBeforeIndex(i,words);
                                path = prefix + path;

                                path = path + findWordAfterIndex(j,words);

                                paths.add(new PairwiseEntityPath(ann1.getEntryInstance(), ann2.getEntryInstance(), path, nGram));



                            }

                        }

                        if (!punctuationTagSet.contains(word2.tag()) && !word2.tag().equals("POS") ) {
                            buffer.append(word2.lemma().toLowerCase()+" ");
                            nGram++;

                        } else {
                            slack++;
                        }


                    }




                }


            }
            */

            /*
            List<IndexedWord> vertexSet = dGraph.vertexListSorted();

            for(int i = 0; i < vertexSet.size();i++) {

                CoreLabel token1 = tokens.get(vertexSet.get(i).index()-1);


                if (tokensAnnotationMap.containsKey(token1)) {

                    CoreLabel parToken = null;

                    if (dGraph.getParent(vertexSet.get(i))!= null)
                        parToken = tokens.get(dGraph.getParent(vertexSet.get(i)).index() - 1);

                    DBPediaAnnotation tokenAnnotation = tokensAnnotationMap.get(token1);
                    DBPediaAnnotation parentTokenAnnotation = tokensAnnotationMap.get(parToken);

                    if (tokenAnnotation == null
                            || parentTokenAnnotation == null
                            || tokenAnnotation != parentTokenAnnotation) {


                        for(int j = i+1; j < vertexSet.size();j++) {
                            CoreLabel token2 = tokens.get(vertexSet.get(j).index()-1);


                            if (tokensAnnotationMap.containsKey(token2)) {
                                //System.out.println(vertexSet.get(i)+ "+"+tokens.get(vertexSet.get(i).index()-1 ).word()+":"+vertexSet.get(j));

                                LinkedList<String> depPath = new LinkedList<String>();
                                LinkedList<String> rawPath = new LinkedList<String>();

                                DBPediaAnnotation ann1 = tokensAnnotationMap.get(token1);
                                DBPediaAnnotation ann2 = tokensAnnotationMap.get(token2);

                                if (ann1 != ann2) {

                                    List<String> depPaths;
                                    List<CoreLabel> rawPaths;

                                    IndexedWord root = dGraph.getFirstRoot();


                                    List<SemanticGraphEdge> edges = dGraph.getShortestUndirectedPathEdges(vertexSet.get(i),vertexSet.get(j));

                                    if (edges!= null && edges.size() > 1) {
                                        //System.out.println(ann1.getEntryInstance() +":::"+ann2.getEntryInstance());


                                        depPaths = new ArrayList<String>(edges.size());
                                        rawPaths = getRawPathsList(vertexSet.get(i).index()-1, vertexSet.get(j).index()-1, tokens);

                                        for(SemanticGraphEdge edge : edges) {
                                            if (edge.getGovernor().index() == root.index()) {
                                                depPaths.add(    edge.getRelation()+":ROOT->"+edge.getGovernor().lemma()+"-"+edge.getGovernor().tag()+":"+edge.getDependent().lemma()+"-"+edge.getDependent().tag());
                                            } else {
                                                depPaths.add(    edge.getRelation()+":"+edge.getGovernor().lemma()+"-"+edge.getGovernor().tag()+":"+edge.getDependent().lemma()+"-"+edge.getDependent().tag());
                                            }
                                            //System.out.println(token1.word()+":"+token2.word()+"  |   "+    edge.getRelation()+":"+edge.getGovernor().lemma()+"-"+edge.getGovernor().tag()+":"+edge.getDependent().lemma()+"-"+edge.getDependent().tag());
                                        }


                                        paths.add(new PairwiseEntityPath(ann1.getEntryInstance(), ann2.getEntryInstance(), depPaths, rawPaths));


                                    }

                                }

                            }
                        }
                    }
                }
            }
            */

        }

        return paths;

    }

    private static String findWordBeforeIndex(int index, List<CoreLabel> words){

        String output = "";

        while(--index>=0) {

            CoreLabel word = words.get(index);
            if (!punctuationTagSet.contains(word.tag()) && !word.tag().equals("POS"))
                return word.lemma().toLowerCase() +" ";

        }

        return output;
    }

    private static String findWordAfterIndex(int index, List<CoreLabel> words){

        String output = "";

        while(++index<words.size()) {

            CoreLabel word = words.get(index);
            if (!punctuationTagSet.contains(word.tag()) && !word.tag().equals("POS"))
                return " "+word.lemma().toLowerCase();

        }

        return output;
    }


    private static List<CoreLabel> getRawPathsList(int token1Index, int token2Index, List<CoreLabel> tokens) {

        LinkedList<CoreLabel> rawPath = new LinkedList<CoreLabel>();

        CoreLabel token1 = tokens.get(token1Index);
        CoreLabel token2 = tokens.get(token2Index);

        //System.out.print("<|"+token1.word()+":" +token2.word()+"|>" );
        for(int i = token1Index + 1; i < token2Index && i < tokens.size();i++) {
            if (!punctuationTagSet.contains(tokens.get(i).tag())) {
                rawPath.add(tokens.get(i));
                //System.out.print("<-->"+tokens.get(i).word()+"-"+tokens.get(i).tag()+"<-->" );

            }

        }

        //System.out.println();


        return rawPath;
    }


    public static void main(String[] args) {

        /* Get command line arguments and set up shop/print usage/otherwise complain */
        CommandLine cmd = null;

        /* Boolean Options */
        Option help = new Option("h", "help", false, "display usage message");

        /* Argument Options */
        Option batchOpt = OptionBuilder.withArgName("Tagged XML Directory")
                .hasArg()
                .withDescription("Process all XML files tagged with DBPedia entries in specified directory.")
                .create("d");

        Option singletonOpt = OptionBuilder.withArgName("Tagged XML File")
                .hasArg()
                .withDescription("Process a single XML file tagged with DBPedia entries.")
                .create("f");


        /* Add Options */
        Options options = new Options();
        options.addOption(help);
        options.addOption(batchOpt);
        options.addOption(singletonOpt);

        /* Parse command line */
        try {
            CommandLineParser parser = new GnuParser();
            cmd = parser.parse(options, args, false);
        } catch (ParseException pe) {
            System.out.println("Command line options parsing failed. Reason: " + pe.getStackTrace());
            System.exit(-1);
        }

        File inputDir = null;
        File inputFile = null;

        Stack<File> fileStack = new Stack<File>();

        if (cmd.hasOption("d")) {
            inputDir = new File(cmd.getOptionValue("d"));
            File[] files = inputDir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    if (name.endsWith(".xml"))
                        return true;
                    return false;  //To change body of implemented methods use File | Settings | File Templates.
                }
            });
            Collections.addAll(fileStack, files);

        } else if (cmd.hasOption("f")) {
            inputFile = new File(cmd.getOptionValue("f"));
            fileStack.push(inputFile);
        }


        /* Handle command line options */
        if (cmd.hasOption("h") || (inputDir == null && inputFile == null)) {
          /* Help - print usage and exit */
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( "PathExtractor", options );
            System.exit(0);


        }


        StanfordCoreNLP pipeline;
        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma");
        pipeline = new StanfordCoreNLP(props);



        while (!fileStack.isEmpty()) {

            HashMap<String, PathTupletCount> countsMap = new HashMap<String, PathTupletCount>();
            //HashMap<String, PairwiseEntityPath> pathsSet = new HashMap<String, PairwiseEntityPath>();
            //HashMap<String, Integer> depCounts = new HashMap<String, Integer>();
            //HashMap<String, Double>  rawCounts = new HashMap<String, Double>();

            File taggedXmlFile = fileStack.pop();
            System.out.println("Loading Sentences From Document: "+taggedXmlFile);
            TaggedXmlInstance xmlInstance = TaggedXmlReader.parseXmlFile(taggedXmlFile);


            System.out.println("\n\nPROCESSING FILE: "+taggedXmlFile);
            System.out.println("TOTAL SENTENCES: "+xmlInstance.size());
            System.out.println("TOTAL DBPedia Entries: "+xmlInstance.getDBPediaEntryMap().size());

            int n = 0;
            for (SentenceInstance sentenceInstance : xmlInstance.getSentences()) {

                int length = sentenceInstance.getSentence().length();

                if (length < 1000) {

                    if (length> 30)
                        System.out.println("Processing: "+(++n) + ": "+sentenceInstance.getSentence().substring(0,30).trim()+"...");
                    else
                        System.out.println("Processing: "+(++n) + ": "+sentenceInstance.getSentence().trim());

                    Annotation annotation = new Annotation(sentenceInstance.getSentence());
                    pipeline.annotate(annotation);

                    //annotation.System.out.println(annotation.toString());


                    List<PairwiseEntityPath> paths = PathExtractor.extractPairwiseEntityPaths(sentenceInstance,annotation);


                    for(PairwiseEntityPath path : paths) {



                        String rawPathKey = path.getEntity1Name() + " : "+ path.getRawPath() + " : "+path.getEntity2().getName();


                        if (countsMap.containsKey(rawPathKey)) {
                            countsMap.get(rawPathKey).incrementCount(1);

                            //int pathSize = path.getRawPath().size();
                            //countsMap.get(rawPathKey).incrementWeightedCount( (pathSize > 0) ? 1.0/(float) pathSize : 0.0 );

                        } else {

                            countsMap.put(rawPathKey, new PathTupletCount(path.getEntity1(),path.getEntity2(),path.getRawPath(),1,path.getNGram()));

                        }



                        /*
                        if (path.getDependencyPath().size() > 0) {

                            HashSet<DBPediaCategory> entOrCatSet1 = new HashSet<DBPediaCategory>();
                            HashSet<DBPediaCategory> entOrCatSet2 = new HashSet<DBPediaCategory>();

                            entOrCatSet1.add(DBPediaCategory.getSingletonCategory(path.getEntity1Name()));
                            //entOrCatSet1.addAll(path.getEntity1().getCategories());

                            entOrCatSet2.add(DBPediaCategory.getSingletonCategory(path.getEntity2Name()));
                            //entOrCatSet2.addAll(path.getEntity2().getCategories());

                            for(DBPediaCategory entCat1 : entOrCatSet1) {

                                for(DBPediaCategory entCat2 : entOrCatSet2) {

                                    StringBuilder depBuffer = new StringBuilder();
                                    //depBuffer.append("<D>" + entCat1);
                                    for(String depPath : path.getDependencyPath()) {
                                        depBuffer.append(depPath+" : ");

                                    }
                                    //depBuffer.append(" : " + entCat2 + "</D>");
                                    String depPath = depBuffer.toString();
                                    if (depPath.length() > 1)
                                        depPath = depPath.substring(0, depPath.length()-1);

                                    String depPathKey = entCat1.toString()+entCat2.toString()+depPath;
                                    if (countsMap.containsKey(depPathKey)) {
                                        countsMap.get(depPathKey).incrementCount(1);
                                        int pathSize = path.getDependencyPath().size();
                                        countsMap.get(depPathKey).incrementWeightedCount( (pathSize > 0) ? 1.0/(float) pathSize : 0.0 );
                                    } else {
                                        countsMap.put(depPathKey, new PathTupletCount(entCat1,entCat2,depPath,1,1.0,false));
                                    }



                                    //System.out.print("<R>"+entCat1);
                                    //for(CoreLabel rawPath : path.getRawPath()) {
                                      //  System.out.print(" : "+rawPath.lemma());

                                    //}
                                    //System.out.println(" : "+entCat2+"</R>");

                                    StringBuilder rawBuffer = new StringBuilder();
                                    //depBuffer.append("<D>" + entCat1);
                                    for(CoreLabel rawPath : path.getRawPath()) {
                                        rawBuffer.append(rawPath.lemma()+" : ");

                                    }
                                    //depBuffer.append(" : " + entCat2 + "</D>");
                                    String rawPath = rawBuffer.toString();
                                    if (rawPath.length() > 1)
                                        rawPath = rawPath.substring(0, rawPath.length()-1);

                                    String rawPathKey = entCat1.toString()+entCat2.toString()+rawPath;
                                    if (countsMap.containsKey(rawPathKey)) {
                                        countsMap.get(rawPathKey).incrementCount(1);
                                        int pathSize = path.getRawPath().size();
                                        countsMap.get(rawPathKey).incrementWeightedCount( (pathSize > 0) ? 1.0/(float) pathSize : 0.0 );

                                    } else {

                                        countsMap.put(rawPathKey, new PathTupletCount(entCat1,entCat2,rawPath,1,1.0, true));

                                    }


                                }

                            }


                        }
                        */


                        path = null;
                    }

                }

            }

            System.out.println("Finished collecting counts.\nUpdating MYSQL tables...");



            NewsFrameConn newsFrameConn = new NewsFrameConn();
            newsFrameConn.connect();

            int largestPath = 0;
            String longestPathStr = null;
            int largestCat = 0;
            String longestCatStr = null;


            long bucket = countsMap.keySet().size() / 50;
            long counter = 0;
            System.out.println("Progress [--------------------------------------------------]");
            System.out.print(  "          ");
            for(String key : countsMap.keySet()) {

                if (counter++ >= bucket) {
                    System.out.print("#");
                    counter = 0;
                }

                PathTupletCount counts = countsMap.get(key);

                if (counts.getPath().length() > largestPath) {
                    largestPath = counts.getPath().length();
                    longestPathStr = counts.getPath();
                }



                //System.out.println(counts.getPath());
                //newsFrameConn.updateRaw(counts.getEntity1().getName(), counts.getEntity2().getName(), counts.getPath(), counts.getCount(), counts.getNGram());

                try {
                Connection connection = ConnectionFactory.getConnection();
                PreparedStatement insertStatement = connection.prepareStatement(
                        "INSERT INTO sem_frame (entity1, entity2, path, count, ngram)\n" +
                                "VALUES (?, ?, ?, ?, ?)\n" +
                                "ON DUPLICATE KEY UPDATE count = count + " + counts.getCount());
                insertStatement.setString(1, counts.getEntity1().getName());
                insertStatement.setString(2, counts.getEntity2().getName());
                insertStatement.setString(3, counts.getPath());
                insertStatement.setLong(4, counts.getCount());
                insertStatement.setInt(5,counts.getNGram());
                insertStatement.executeUpdate();


                } catch (SQLException sqle) {
                    sqle.printStackTrace();
                }


            }
            System.out.println("#\nClosing connection to database.");
            newsFrameConn.closeConnection();

            System.out.println("Finished processing file "+taggedXmlFile);
            System.out.println("Longest path is " + largestPath + " chars: "+longestPathStr);


        }



    }


}
