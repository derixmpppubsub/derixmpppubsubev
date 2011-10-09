package org.deri.xmpppubsubev;

//import java.io.*;
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.FileReader;
//import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
//import java.util.ArrayList;
//import java.util.StringTokenizer;

import java.util.ArrayList;
import java.util.HashMap;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

//import org.deri.any23.extractor.ExtractionException;
import org.deri.xmpppubsub.*;
//import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
//import org.jivesoftware.smackx.pubsub.LeafNode;

public class PublishersTest {
    public static int numberOfPublishers;
//    public static int numberOfSubscribers;
//    public static ArrayList<Publisher> publishers;
    HashMap<String, Publisher> publishers; 
    //public static ArrayList<Subscriber> subscribers;
    //public static int MAX_PUBLISHERS = 100;
    public static String postTemplate = "<http://ecp-alpha/semantic/post/%s> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://rdfs.org/sioc/ns#Post> .";
    public static String postCreatorTemplate = "<http://ecp-alpha/semantic/post/%s> <http://purl.org/dc/elements/1.1/creator> <http://ecp-alpha/semantic/employee/%s> .";
    public String xmppServer;
    public static int numberOfTriples;
    public static int numberOfMsgs;
//    public static boolean separatedPosts;
    public String endpoint;
//    public String fileName;
//    public FileWriter writer;
    
    static Logger logger = Logger.getLogger(PublishersTest.class);
    static Logger loggerp = Logger.getLogger(Publisher.class);
    //static Logger loggers = Logger.getLogger(Subscriber.class);
    
    public PublishersTest(String xmppServer, int numberOfPub, //int numberOfSub,
            //String fileName, 
            int numberOfTriples, //boolean separatedPosts, 
            int numberOfMsgs, String endpoint) throws IOException {
        numberOfPublishers = numberOfPub;
//        numberOfSubscribers = numberOfSub;
        //publishers = new ArrayList<Publisher>();
        //subscribers = new ArrayList<Subscriber>();
        this.xmppServer = xmppServer;
//        this.fileName = fileName;
        this.numberOfTriples = numberOfTriples;
        this.numberOfMsgs = numberOfMsgs;
//        this.separatedPosts = separatedPosts;
        this.endpoint = endpoint;
//        writer = new FileWriter(fileName, true);
        publishers = new HashMap<String, Publisher>();
    }
    
    public static void insertTestTriples(int numPubs, int numTriples, String endpoint) 
            throws UnsupportedEncodingException, IOException {
//        Writer outputWriter = null;
//        File outputFile = new File("pub" + numPubs + "post" + numTriples 
//                + "insert-times.txt");
//        outputWriter = new BufferedWriter(new FileWriter(outputFile));
        String queryString = "";
        String triples = "";
        for (int i=1; i<=numPubs; i++) {
            for (int k=1; k<=numTriples; k++) {
                triples += String.format(postTemplate 
                        + "<http://ecp-alpha/semantic/post/%s> <http://purl.org/dc/elements/1.1/creator> <http://ecp-alpha/semantic/employee/%s> . "
                        , "pub" + i + "post" +k, "pub" + i + "post" +k, "pub" + i);
            }
        }
        logger.info(triples);
        queryString = "INSERT DATA {" + triples + "}";
        SPARQLWrapper sw = new SPARQLWrapper();
        String result = sw.runQuery(queryString, endpoint, true);
        System.out.println(result);
//        outputWriter.write("pub" + numPubs + "post" + numTriples + ", " 
////                + sw.usedtime_cpu + ", " +  sw.usedtime_sys 
//                + "\n");
        //sw.writer.close();
    }
    
    public static String createQueryPost(String pubName, int k) {
        String postName = pubName  + "post" + k;
        String queryString = "CONSTRUCT {"
          + String.format(postTemplate, postName)
          + "}  WHERE {"
          + String.format(postTemplate, postName)
          + String.format(postCreatorTemplate, postName, pubName)
          +"}";
          return queryString;
    }
    
    public static String createQueryPosts(String pubName, int k) {
        String postName = pubName  + "post" + k;
        String queryString = "CONSTRUCT {"
          + "?post <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://rdfs.org/sioc/ns#Post> . "
          + "}  WHERE {"
          + "?post <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://rdfs.org/sioc/ns#Post> . "       
          + String.format("?post <http://purl.org/dc/elements/1.1/creator> <http://ecp-alpha/semantic/employee/%s> . ", pubName)
          + String.format("FILTER (REGEX(str(?post), '^http://ecp-alpha/semantic/post/%spost')) . ", pubName)
          +"}"
          + String.format(" LIMIT %s", numberOfTriples);
          return queryString;
    }
    
    public static String createMsgId(int i, int k, Long time) {
        String msgId = "pub" + i + "of" + numberOfPublishers 
                + ",msg" + k + "of" + numberOfTriples 
                + ",triples" + 1 + ",ctime" + time.toString();
        return msgId;
    }
    
    public void run() throws XMPPException, IOException, QueryTypeException, 
            InterruptedException {
        logger.info("number of publishers " + Integer.toString(numberOfPublishers));
        //logger.info("number of subscribers " + Integer.toString(numberOfSubscribers));
        logger.info("number of triples " + Integer.toString(numberOfTriples));
//        logger.info("separated files " + Boolean.toString(separatedPosts));
//        Writer outputWriter = null;
//        File outputFile = new File(fileName);
//        outputWriter = new BufferedWriter(new FileWriter(outputFile));
        try {
            
           
            for (int i=1; i<=numberOfPublishers; i++) {
                if(Runtime.getRuntime().freeMemory()<1024*1024) System.gc();
                String pubName = "pub" + i;
                String pubPass = pubName + "pass";
                
                Publisher p = new Publisher(pubName, pubPass , xmppServer);
                String nodeName = "node" + i;
                p.getOrCreateNode(nodeName);
                //publishers.add(p);
                
                String triples = "";
                //diferent queries
                //get query from repo
                SPARQLWrapper sw = new SPARQLWrapper();
//                if (separatedPosts) {
                    for (int k=1; k<=numberOfTriples; k++) {  
                        //String triples = String.format(postTemplate, k);
                        String queryString = createQueryPost(pubName, k);
                        logger.info(queryString);
                        triples = sw.runQuery(queryString, endpoint, false);
                        logger.info(triples);
                        
                        String msgId = this.createMsgId(i, k, sw.time); 
                        SPARQLQuery query = new SPARQLQuery();
                        query.wrapTriples(triples);
                        logger.info(query.toXML());
                        p.publishQuery(query.toXML(), msgId);
                        logger.debug("Published query.");
                    }
//                } else {
//                    String queryString = createQueryPosts(pubName, k);
//                    logger.info(queryString);
//                    triples = sw.runQuery(queryString, endpoint, false);
//                    logger.info(triples);
//                    
//                    String msgId = this.createMsgId(i, i, sw.time); 
//                    SPARQLQuery query = new SPARQLQuery();
//                    query.wrapTriples(triples);
//                    logger.debug(query.toXML());
//                    p.publishQuery(query.toXML(), msgId);
//                }
            }
        } catch(OutOfMemoryError e){
            System.gc();
            System.out.println("out of memory");
        }
    }
    
    public static void main(String[] args) {
        try {
            // Set up a simple configuration that logs on the console.
            BasicConfigurator.configure();
            
            logger.setLevel(Level.DEBUG);
            logger.debug("Entering application.");
            // turn on the enhanced debugger
//            XMPPConnection.DEBUG_ENABLED = true;
            String xmppServer = "localhost";
            String endpoint = "http://localhost:8001/sparql/";
            int numberPubs  = Integer.parseInt(args[0]);
            int numberSubs = Integer.parseInt(args[1]);
            int numberTriples = Integer.parseInt(args[2]);
            int numberMsgs = Integer.parseInt(args[3]);
//            boolean separatedPosts = Boolean.parseBoolean(args[2]);
//            String fileName = "pub" + numberPubs + "post" + numberTriples 
//                    + "separatedPosts" + String.valueOf(separatedPosts)
//                    + "sub" + numberSubs  + ".csv";
            PublishersTest st = new PublishersTest(xmppServer, numberPubs,
                    numberTriples, numberMsgs, endpoint);
            st.run();
            // give time to all the messages to send
            Thread.sleep(100*numberPubs*numberSubs);
//            st.writer.close();           
            //insertTestTriples(1, 1000, "http://localhost:8000/update/");
        
        } catch(IOException e) {
            e.printStackTrace();
            logger.debug(e);  
//        } catch (ExtractionException e) {
//            e.printStackTrace();
//            logger.debug(e);
            
        } 
        catch(XMPPException e) {
            e.printStackTrace();
            logger.debug(e);        
        } catch (QueryTypeException e) {
            e.printStackTrace();
            logger.debug(e.getMessage());
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }
}
