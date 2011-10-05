package org.deri.xmpppubsubev;

import java.io.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

//import org.deri.any23.extractor.ExtractionException;
import org.deri.xmpppubsub.*;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.pubsub.LeafNode;

public class PublishersTest {
    public static int numberOfPublishers;
    //public static int numberOfSubscribers;
    //public static ArrayList<Publisher> publishers;
    //public static ArrayList<Subscriber> subscribers;
    //public static int MAX_PUBLISHERS = 100;
    public static String postTemplate = "<http://ecp-alpha/semantic/post/%s> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://rdfs.org/sioc/ns#Post> .";
    public String xmppServer;
    public String fileName;
    public static int numberOfTriples;
    public static boolean separatedPosts;
    public String endpoint;
    
    static Logger logger = Logger.getLogger(PublishersTest.class);
    static Logger loggerp = Logger.getLogger(Publisher.class);
    //static Logger loggers = Logger.getLogger(Subscriber.class);
    
    public PublishersTest(String xmppServer, int numberOfPub, //int numberOfSub,
            String fileName, int numberOfTriples, boolean separatedPosts, 
            String endpoint) {
        numberOfPublishers = numberOfPub;
        //numberOfSubscribers = numberOfSub;
        //publishers = new ArrayList<Publisher>();
        //subscribers = new ArrayList<Subscriber>();
        this.xmppServer = xmppServer;
        this.fileName = fileName;
        this.numberOfTriples = numberOfTriples;
        this.separatedPosts = separatedPosts;
        this.endpoint = endpoint;
    }
    
    public static void insertTestTriples(int numPubs, int numTriples, String endpoint) 
            throws UnsupportedEncodingException, IOException {
        Writer outputWriter = null;
        File outputFile = new File("pub" + numPubs + "post" + numTriples 
                + "insert-times.txt");
        outputWriter = new BufferedWriter(new FileWriter(outputFile));
        String queryString = "";
        String triples = "";
        for (int i=1; i<=numPubs; i++) {
            for (int k=1; k<=numTriples; k++) {
                triples += String.format(postTemplate, "pub" + i + "post" +k );
            }
        }
        logger.info(triples);
        queryString = "INSERT DATA {" + triples + "}";
        SPARQLWrapper sw = new SPARQLWrapper();
        String result = sw.executeQuery(queryString, endpoint, true);
        System.out.println(result);
        outputWriter.write("pub" + numPubs + "post" + numTriples + ", " 
                    + sw.usedtime_cpu + ", " +  sw.usedtime_sys + "\n");
    }
    
    public void run() throws XMPPException, IOException, QueryTypeException, 
            InterruptedException {
        logger.info("number of publishers " + Integer.toString(numberOfPublishers));
        //logger.info("number of subscribers " + Integer.toString(numberOfSubscribers));
        logger.info("number of triples " + Integer.toString(numberOfTriples));
        logger.info("separated files " + Boolean.toString(separatedPosts));
        Writer outputWriter = null;
        File outputFile = new File("pub" + numberOfPublishers + "post" + numberOfTriples
                + "select-times.txt");
        outputWriter = new BufferedWriter(new FileWriter(outputFile));
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
                if (separatedPosts) {
                    for (int k=1; k<=numberOfTriples; k++) {  
                        //String triples = String.format(postTemplate, k);
                        String postName = pubName  + "post" + k;
                        String queryString = "CONSTRUCT {"
                          + String.format("<http://ecp-alpha/semantic/post/%s>", 
                                          postName) + " ?p ?o"
                          + "}  WHERE {"
                          + String.format("<http://ecp-alpha/semantic/post/%s> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://rdfs.org/sioc/ns#Post> .", 
                                          postName)
                          +"}";
                        logger.info(queryString);
                        triples = sw.executeQuery(queryString, endpoint, false);
                        logger.info(triples);
                        outputWriter.write(postName + ", " 
                                    + sw.usedtime_cpu + ", " +  sw.usedtime_sys + "\n");
                        
                        SPARQLQuery query = new SPARQLQuery(triples);
                        logger.info(query.toXML());
                        p.publishQuery(query.toXML());
                        logger.debug("Published query.");
                    }
                } else {
//                    for (int k=1; k<=numberOfTriples; k++) { 
//                        triples += "\n"+String.format(postTemplate, k);
//                    }
                    
                    String queryString = "CONSTRUCT {"
                      + String.format("<http://ecp-alpha/semantic/post/%s>", 
                                      pubName) + " ?p ?o"
                      + "}  WHERE {"
                      + "?post <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://rdfs.org/sioc/ns#Post> ."
                      + String.format("FILTER regex(?post, '^<http://ecp-alpha/semantic/post/%s') ", pubName)
                      +"}";
                    logger.info(queryString);
                    triples = sw.executeQuery(queryString, endpoint, false);
                    logger.info(triples);
                    outputWriter.write(pubName + ", " 
                                + sw.usedtime_cpu + ", " +  sw.usedtime_sys + "\n");
                    
                    SPARQLQuery query = new SPARQLQuery(triples);
                    p.publishQuery(query.toXML());
                    logger.debug("Published query.");
                }
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
            //XMPPConnection.DEBUG_ENABLED = true;
            /*
            String xmppServer = "192.168.1.4";
//            String xmppServer = "vmuss12.deri.ie";
            int numberPubs = 1;
            int numberSubs = 1;
            String fileName = numberPubs + "pub" + numberSubs + "sub.csv" ;
            PublishersTest st = new PublishersTest(xmppServer, numberPubs,
                    numberSubs, fileName, 100, false);
            st.run();
            // give time to all the messages to send
            Thread.sleep(100*numberPubs*numberSubs);
            */
            
            insertTestTriples(1000, 1000, "http://localhost:8000/update/");
        
        } catch(IOException e) {
            e.printStackTrace();
            logger.debug(e);   
        }
//        } catch (ExtractionException e) {
//            e.printStackTrace();
//            logger.debug(e);
            
//        } catch(XMPPException e) {
//            e.printStackTrace();
//            logger.debug(e);        
//        } catch (QueryTypeException e) {
//            e.printStackTrace();
//            logger.debug(e.getMessage());
//        } catch (InterruptedException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
        
    }
}
