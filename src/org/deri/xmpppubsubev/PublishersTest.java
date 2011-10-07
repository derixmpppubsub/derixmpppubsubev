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
    public FileWriter writer;
    
    static Logger logger = Logger.getLogger(PublishersTest.class);
    static Logger loggerp = Logger.getLogger(Publisher.class);
    //static Logger loggers = Logger.getLogger(Subscriber.class);
    
    public PublishersTest(String xmppServer, int numberOfPub, //int numberOfSub,
            String fileName, int numberOfTriples, boolean separatedPosts, 
            String endpoint) throws IOException {
        numberOfPublishers = numberOfPub;
        //numberOfSubscribers = numberOfSub;
        //publishers = new ArrayList<Publisher>();
        //subscribers = new ArrayList<Subscriber>();
        this.xmppServer = xmppServer;
        this.fileName = fileName;
        this.numberOfTriples = numberOfTriples;
        this.separatedPosts = separatedPosts;
        this.endpoint = endpoint;
        writer = new FileWriter(fileName, true);
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
        String result = sw.executeQuery(queryString, endpoint, true);
        System.out.println(result);
//        outputWriter.write("pub" + numPubs + "post" + numTriples + ", " 
////                + sw.usedtime_cpu + ", " +  sw.usedtime_sys 
//                + "\n");
        //sw.writer.close();
    }
    
    public void run() throws XMPPException, IOException, QueryTypeException, 
            InterruptedException {
        logger.info("number of publishers " + Integer.toString(numberOfPublishers));
        //logger.info("number of subscribers " + Integer.toString(numberOfSubscribers));
        logger.info("number of triples " + Integer.toString(numberOfTriples));
        logger.info("separated files " + Boolean.toString(separatedPosts));
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
                if (separatedPosts) {
                    for (int k=1; k<=numberOfTriples; k++) {  
                        //String triples = String.format(postTemplate, k);
                        String postName = pubName  + "post" + k;
                        String queryString = "CONSTRUCT {"
                          + String.format("<http://ecp-alpha/semantic/post/%s> ", postName)
                          + "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://rdfs.org/sioc/ns#Post> . "
                          + "}  WHERE {"
                          + String.format("<http://ecp-alpha/semantic/post/%s> ", postName)
                          + "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://rdfs.org/sioc/ns#Post> . "
                          + String.format("<http://ecp-alpha/semantic/post/%s> ", postName)
                          + String.format("<http://purl.org/dc/elements/1.1/creator> <http://ecp-alpha/semantic/employee/%s> .", pubName)
                          +"}";
                        logger.info(queryString);
                        triples = sw.executeQuery(queryString, endpoint, false);
                        logger.info(triples);
//                        logger.info("writing to file " + postName + ", " 
////                                + sw.usedtime_cpu + ", " +  sw.usedtime_sys
//                                + sw.time + "\n");
////                        outputWriter.write(postName + ", " 
////                                    + sw.usedtime_cpu + ", " +  sw.usedtime_sys + "\n");       
//                        writer.append(postName);
//                        writer.append(',');
////                        writer.append(Double.toString(sw.usedtime_cpu));
////                        writer.append(',');
////                        writer.append(Double.toString(sw.usedtime_sys));
////                        writer.append(',');
//                        writer.append(sw.time.toString());
//                        writer.append('\n');
//                        writer.flush();
                        
                        String msgId = "pub" + i + "of" + numberOfPublishers 
                                + ",msg" + k + "of" + numberOfTriples 
                                + ",triples" + 1 + ",ctime" + sw.time.toString(); 
                        SPARQLQuery query = new SPARQLQuery(triples);
                        logger.info(query.toXML());
                        p.publishQuery(query.toXML(), msgId);
                        logger.debug("Published query.");
                    }
                } else {
                    String queryString = "CONSTRUCT {"
                      + "?post <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://rdfs.org/sioc/ns#Post> . "
                      + "}  WHERE {"
                      + "?post <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://rdfs.org/sioc/ns#Post> . "       
                      + String.format("?post <http://purl.org/dc/elements/1.1/creator> <http://ecp-alpha/semantic/employee/%s> . ", pubName)
                      + String.format("FILTER (REGEX(str(?post), '^http://ecp-alpha/semantic/post/%spost')) . ", pubName)
                      +"}"
                      + String.format(" LIMIT %s", numberOfTriples);
                    logger.info(queryString);
                    triples = sw.executeQuery(queryString, endpoint, false);
                    logger.info(triples);
//                    logger.info("writing to file " + pubName + ", " 
////                                + sw.usedtime_cpu + ", " +  sw.usedtime_sys + "\n");
//                                + sw.time + "\n");
////                    outputWriter.write(pubName + ", " 
////                                + sw.usedtime_cpu + ", " +  sw.usedtime_sys + "\n");
//                    writer.append(pubName);
//                    writer.append(',');
////                    writer.append(Double.toString(sw.usedtime_cpu));
////                    writer.append(',');
////                    writer.append(Double.toString(sw.usedtime_sys));
//                    writer.append(sw.time.toString());
//                    writer.append('\n');         
//                    writer.flush();
                    String msgId = "pub" + i + "of" + numberOfPublishers 
                            + ",msg" + 1 + "of" + 1 
                            + ",triples" + numberOfTriples + ",ctime" + sw.time.toString(); 
                    SPARQLQuery query = new SPARQLQuery(triples);
                    logger.debug(query.toXML());
                    p.publishQuery(query.toXML(), msgId);
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
//            XMPPConnection.DEBUG_ENABLED = true;
            String xmppServer = "192.168.1.7";
            String endpoint = "http://localhost:8000/sparql/";
            int numberPubs  = Integer.parseInt(args[0]);
            int numberTriples = Integer.parseInt(args[1]);
            boolean separatedPosts = Boolean.parseBoolean(args[2]);
            int numberSubs = Integer.parseInt(args[3]);
            String fileName = "pub" + numberPubs + "post" + numberTriples 
                    + "separatedPosts" + String.valueOf(separatedPosts)
                    + "sub" + numberSubs  + ".csv";
            PublishersTest st = new PublishersTest(xmppServer, numberPubs,
                    fileName, numberTriples, separatedPosts, endpoint);
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
