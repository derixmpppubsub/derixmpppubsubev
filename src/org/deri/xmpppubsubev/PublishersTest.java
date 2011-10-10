package org.deri.xmpppubsubev;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import org.deri.xmpppubsub.*;
import org.deri.xmpppubsubev.InitializePubsSubs;
import org.jivesoftware.smack.XMPPException;

public class PublishersTest {
    public static int numberOfPublishers;
    HashMap<String, Publisher> publishers; 
    public static String postTemplate = "<http://ecp-alpha/semantic/post/%s> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://rdfs.org/sioc/ns#Post> .";
    public static String postCreatorTemplate = "<http://ecp-alpha/semantic/post/%s> <http://purl.org/dc/elements/1.1/creator> <http://ecp-alpha/semantic/employee/%s> .";
    public String xmppServer;
    public static int numberOfTriples;
//    public static int numberOfMsgs;
    public String endpoint;
    public static String nameTest;
    public static int MAXTRIPLES = 8200;
    
    static Logger logger = Logger.getLogger(PublishersTest.class);
    
    public PublishersTest(String xmppServer, String endpoint) throws IOException {
//        numberOfPublishers = numberOfPub;
        this.xmppServer = xmppServer;
//        this.numberOfTriples = numberOfTriples;
//        this.numberOfMsgs = numberOfMsgs;
        this.endpoint = endpoint;
//        this.nameTest = nameTest;
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
        logger.debug(triples);
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
    
    public static String createQueryPosts(String pubName) {
//        String postName = pubName  + "post" + k;
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
    
    public static String createMsgId(int i,Long time) {
//        String msgId = "pub" + i + "of" + numberOfPublishers 
//                + ",triples" + numberOfTriples + ",ctime" + time.toString();
          String msgId = "pub" + nameTest + i + "of" + numberOfPublishers 
                + "," + numberOfTriples + "," + time.toString();
        return msgId;
    }
    
    public void run() throws XMPPException, IOException, QueryTypeException, 
            InterruptedException {
        logger.debug("number of publishers " + Integer.toString(numberOfPublishers));
//        logger.debug("number of triples " + Integer.toString(numberOfTriples));
        try {
            String triples = "";
            SPARQLWrapper sw = new SPARQLWrapper();       
            String pubName = "";
            String pubPass = "";
            String nodeName = "";
            Publisher p = null;
            for (int i=1; i<=numberOfPublishers; i++) {
                pubName = "pub" + nameTest + i;
                p = publishers.get(pubName);
                if (p == null) {
                    pubPass = pubName + "pass";
                    p = new Publisher(pubName, pubPass , xmppServer);
                    nodeName = "node" + i;
                    p.getOrCreateNode(nodeName);
                    publishers.put(pubName,p);
                }
                String queryString = createQueryPosts(pubName.replace(nameTest, ""));
//                logger.debug(queryString);
                triples = sw.runQuery(queryString, endpoint, false);
                //logger.debug(triples);
//                if (triples != null) {
//                    logger.debug("returned triples");
//                } else {
//                    logger.debug("no triples");
//                }
                String msgId = this.createMsgId(i, sw.time); 
                SPARQLQuery query = new SPARQLQuery();
                query.wrapTriples(triples);
                //logger.debug(query.toXML());
                p.publishQuery(query.toXML(), msgId);
                logger.debug("Published query.");
            }
        } catch(OutOfMemoryError e){
            System.gc();
            logger.error(e);
        }
    }
    
    public void test1() throws XMPPException, IOException, QueryTypeException, InterruptedException {
        this.numberOfPublishers = 1;
        this.nameTest = "test1";
        int numberSubs = 1;
        int numberRun = 30;
        for (int j=1;j<=MAXTRIPLES; j=j*10) {
            this.numberOfTriples = j;
            for (int i=1;i<=numberRun;i++) {
                this.run();
            }
        }
        this.numberOfTriples = MAXTRIPLES;
        for (int i=1;i<=numberRun;i++) {
            this.run();
        }
        publishers = null;
        //Thread.sleep(100*numberOfPublishers*numberSubs);
    }
    
    public void test2() throws XMPPException, IOException, QueryTypeException, InterruptedException {
        int numberSubs = 10;
//        for (int nS = 10; nS<=10000; nS=nS*10) {
//        for(int t=2; t<=4; t++) {
            int t = 2;
            this.nameTest = "test" + t;
            for(int nP = 1; nP<=100; nP=nP*10) {
                this.numberOfPublishers = nP;
                //init
//                InitializePubsSubs ips = InitializePubsSubs(xmppServer, numberOfPublishers, 
//                numberSubs, nameTest);
//                ips.initialize;
                for(int nT=1; nT<=100; nT=nT*10) {
                    this.numberOfTriples = nT;
                    for(int nR=1; nR <= 30; nR++) {
                        this.run();
                    }
                }
            }
//        }
       publishers = null;
    }
    
    public static void main(String[] args) {
        try {
            // Set up a simple configuration that logs on the console.
            BasicConfigurator.configure();
            
            logger.setLevel(Level.DEBUG);
            Logger.getRootLogger().setLevel(Level.DEBUG);
            logger.debug("Entering application.");
            // turn on the enhanced debugger
//            XMPPConnection.DEBUG_ENABLED = true;
            String xmppServer = "localhost";
            String endpoint = "http://localhost:8001/sparql/";
//            int numberPubs  = Integer.parseInt(args[0]);
//            int numberSubs = Integer.parseInt(args[1]);
//            int numberTriples = Integer.parseInt(args[2]);
//            String nameTest = args[3];
//            int numberMsgs = Integer.parseInt(args[3]);
            PublishersTest st = new PublishersTest(xmppServer,endpoint);
            //st.run();
            //st.test1();
            st.test2();

            // give time to all the messages to send
            //Thread.sleep(100*numberPubs*numberSubs);
            //insertTestTriples(1, 1000, "http://localhost:8000/update/");
        
        } catch(IOException e) {
            logger.error(e);  
        } catch(XMPPException e) {
            logger.error(e);        
        } catch (QueryTypeException e) {
            logger.error(e);
        } catch (InterruptedException e) {
            logger.error(e);
        }
        
    }
}
