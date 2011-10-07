package org.deri.xmpppubsubev;
import java.util.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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

public class SubscribersTest {
    public static int numberOfPublishers;
    public static int numberOfSubscribers;
    //public static ArrayList<Publisher> publishers;
    //public static ArrayList<Subscriber> subscribers;
    //public static int MAX_PUBLISHERS = 100;
    public static String postTemplate = "<http://ecp-alpha/semantic/post/%s> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://rdfs.org/sioc/ns#Post> .";
    public String xmppServer;
    public String fileName;
    public static int numberOfTriples;
    public static boolean separatedPosts;
    
    static Logger logger = Logger.getLogger(SubscribersTest.class);
    //static Logger loggerp = Logger.getLogger(Publisher.class);
    static Logger loggers = Logger.getLogger(Subscriber.class);
    
    public SubscribersTest(String xmppServer, int numberOfPub, int numberOfSub,
            String fileName, int numberOfTriples, boolean separatedPosts) {
        numberOfPublishers = numberOfPub;
        numberOfSubscribers = numberOfSub;
        //publishers = new ArrayList<Publisher>();
        //subscribers = new ArrayList<Subscriber>();
        this.xmppServer = xmppServer;
        this.fileName = fileName;
        this.numberOfTriples = numberOfTriples;
        this.separatedPosts = separatedPosts;
    }
    
    public void calculateAverage(String inputFileName, String numberClients, 
            String outputFileName) throws IOException {
//        ArrayList<String> numbers = new ArrayList<String>();
        long totalTime = 0;
        int numberReq = 0;
        File file = new File(inputFileName);
        BufferedReader bufRdr  = new BufferedReader(new FileReader(file));
        String line = null;
        while((line = bufRdr.readLine()) != null)        {
//            StringTokenizer st = new StringTokenizer(line,",");
            String[] columns = line.split(",");
            String timeElapsed = columns[columns.length -1];
            logger.info(timeElapsed);
//            numbers.add(timeElapsed);
            totalTime += Long.parseLong(timeElapsed);
            numberReq++;
        }
        bufRdr.close();
        long avgTime = totalTime/numberReq;
        logger.info("average: " + avgTime);
        FileWriter writer = new FileWriter(outputFileName, true);
        writer.append(numberClients);
        writer.append(',');
        writer.append(Long.toString(avgTime));
        writer.append('\n');
        writer.flush();
        writer.close();
        
    }
    
    public void run() throws XMPPException, IOException, QueryTypeException, 
            InterruptedException {
        logger.info("number of publishers " + Integer.toString(numberOfPublishers));
        logger.info("number of subscribers " + Integer.toString(numberOfSubscribers));
    try {
        for (int i=1; i<=numberOfPublishers; i++) {
            if(Runtime.getRuntime().freeMemory()<1024*1024) System.gc();
            String nodeName = "node" + i;
            for (int j=1; j<=numberOfSubscribers; j++) {  
                String subName = "sub" + j;
                String subPass = subName + "pass";  
                Subscriber s = new Subscriber(subName, subPass, xmppServer);
                //subscribers.add(s);
                LeafNode node = s.getNode(nodeName);
                node.addItemEventListener(
                        new ItemEventCoordinator(fileName));
                s.subscribeIfNotSubscribedTo(node);
            }
        }
    } catch(OutOfMemoryError e){
        System.gc();
        System.out.println("out of memory");
    }
//        // give time to all the messages to arrive
//        Thread.sleep(100*numberOfPublishers*numberOfSubscribers);
    }
    
    public static void main(String[] args) {
        try {
            // Set up a simple configuration that logs on the console.
            BasicConfigurator.configure();
            
            logger.setLevel(Level.DEBUG);
            logger.debug("Entering application.");
            // turn on the enhanced debugger
            //XMPPConnection.DEBUG_ENABLED = true;
            String xmppServer = "192.168.1.7";
//            String xmppServer = "vmuss12.deri.ie";
//            int numberPubs = 1;
//            int numberSubs = 1;
            // parse input args
            int numberPubs  = Integer.parseInt(args[0]);
            int numberTriples = Integer.parseInt(args[1]);
            boolean separatedPosts = Boolean.parseBoolean(args[2]);
            int numberSubs = Integer.parseInt(args[3]);
//            String fileName = "subscriberpub" + numberPubs + "post" +numberTriples
//                    + "separatedPosts" + String.valueOf(separatedPosts)
//                    + "sub" + numberSubs + ".csv";
            String fileName = "allTests.csv";
            SubscribersTest st = new SubscribersTest(xmppServer, numberPubs,
                    numberSubs, fileName, numberTriples, separatedPosts);
            st.run();
            //st.sw.writer.close();
          // give time to all the messages to arrive
            //Thread.sleep(100*numberPubs*numberSubs);
            while (true) {
                Thread.sleep(100);
            }
            //st.calculateAverage(fileName, numberSubs+"", "averages.csv");
        
        } catch(XMPPException e) {
            e.printStackTrace();
            logger.debug(e);
            
        } catch(IOException e) {
            e.printStackTrace();
            logger.debug(e);
        } catch (QueryTypeException e) {
            e.printStackTrace();
            logger.debug(e.getMessage());
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
            
//        } catch (ExtractionException e) {
//            e.printStackTrace();
//            logger.debug(e);
//        }
        
    }
}
