package org.deri.xmpppubsubev;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import org.deri.xmpppubsub.*;
//import org.deri.xmpppubsubev.InitializePubsSubs;
import org.jivesoftware.smack.XMPPException;

public class PublishersTest {
    HashMap<String, Publisher> publishers; 
    public static String postTemplate = "<http://ecp-alpha/semantic/post/%s> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://rdfs.org/sioc/ns#Post> .";
    public static String postCreatorTemplate = "<http://ecp-alpha/semantic/post/%s> <http://purl.org/dc/elements/1.1/creator> <http://ecp-alpha/semantic/employee/%s> .";
    public String xmppServer;
    public String endpoint;
//    public static int nPubs;
//    public static int nTriples;
//    public static int numberOfMsgs;
//    public static String nameTest;
    public static int MAXTRIPLES = 8200;
    public static int NRUNS = 1;
    
    static Logger logger = Logger.getLogger(PublishersTest.class);
    
    public PublishersTest(String xmppServer, String endpoint) throws IOException {
//        nPubs = numberOfPub;
        this.xmppServer = xmppServer;
//        this.nTriples = nTriples;
//        this.numberOfMsgs = numberOfMsgs;
        this.endpoint = endpoint;
//        this.nameTest = nameTest;
        publishers = new HashMap<String, Publisher>();
    }
    
    public void insertTestTriples(int nPubs, int nTriples, String nameTest) 
            throws UnsupportedEncodingException, IOException {
        String queryString = "";
        String triples = "";
//        SPARQLWrapper sw = new SPARQLWrapper();
        for (int i=1; i<=nPubs; i++) {
            for (int k=1; k<=nTriples; k++) {
                triples += String.format(postTemplate 
                        + "<http://ecp-alpha/semantic/post/%s> <http://purl.org/dc/elements/1.1/creator> <http://ecp-alpha/semantic/employee/%s> . "
                        ,  nameTest + "pub" + i + "post" +k, 
                        nameTest + "pub" + i + "post" +k, "pub" + i);
            }
        }
        logger.debug(triples);
        queryString = "INSERT DATA {" + triples + "}";
//        String result = SPARQLWrapper.runQuery(queryString, endpoint, true);
//        Object[] ret = SPARQLWrapper.runQuery(queryString, endpoint, true);
        SPARQLWrapper.runQuery(queryString, endpoint, true);
    }
    
    public String createQueryPost(String pubName, int k) {
        String postName = pubName  + "post" + k;
        String queryString = "CONSTRUCT {"
          + String.format(postTemplate, postName)
          + "}  WHERE {"
          + String.format(postTemplate, postName)
          + String.format(postCreatorTemplate, postName, pubName)
          +"}";
          return queryString;
    }
    
    public String createQueryPosts(String pubName, int nTriples) {
//        String postName = pubName  + "post" + k;
        String queryString = "CONSTRUCT {"
          + "?post <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://rdfs.org/sioc/ns#Post> . "
          + "}  WHERE {"
          + "?post <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://rdfs.org/sioc/ns#Post> . "       
          + String.format("?post <http://purl.org/dc/elements/1.1/creator> <http://ecp-alpha/semantic/employee/%s> . ", pubName)
//          + String.format("FILTER (REGEX(str(?post), '^http://ecp-alpha/semantic/post/%spost')) . ", pubName)
          +"}"
          + String.format(" LIMIT %s", nTriples);
          return queryString;
    }
    
    public String createMsgId(String pubName ,Long time, 
            int nTriples) {
//        String msgId = "pub" + i + "of" + nPubs 
//                + ",triples" + nTriples + ",ctime" + time.toString();
          String msgId = pubName 
                + "," + nTriples + "," + time.toString();
        return msgId;
    }
    
    public void runPublishers(int nPubs, int nTriples, 
            String nameTest) throws XMPPException, IOException, QueryTypeException, 
            InterruptedException {
        //logger.debug("number of publishers " + Integer.toString(nPubs));
//        logger.debug("number of triples " + Integer.toString(nTriples));
        try {
            String triples = "";
//            SPARQLWrapper sw = new SPARQLWrapper();       
            String pubName = "";
            String pubPass = "";
            String nodeName = "";
            Publisher p = null;
            Long time = null;
            for (int i=1; i<=nPubs; i++) {
                pubName = nameTest + "pub" + i;
                p = publishers.get(pubName);
                if (p == null) {
                    pubPass = pubName + "pass";
                    p = new Publisher(pubName, pubPass , xmppServer);
                    nodeName = nameTest + "node" + i;
                    p.getOrCreateNode(nodeName);
                    publishers.put(pubName,p);
                }
                String queryString = createQueryPosts(
                        pubName.replace(nameTest,""), nTriples);
//                logger.debug(queryString);
                Object[] ret = SPARQLWrapper.runQuery(queryString, endpoint, false);
                time = (Long)ret[1];
                triples = (String)ret[0];
//                logger.debug(triples);
//                if (triples != null) {
//                    logger.debug("returned triples");
//                } else {
//                    logger.debug("no triples");
//                }
                String msgId = this.createMsgId(pubName + "of" + nPubs, time,
                        nTriples); 
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
    
    public void test(String t) throws XMPPException, IOException, QueryTypeException, InterruptedException {
        double nSubs = java.lang.Math.pow(10, Integer.parseInt(t)-1);
//        for (int nS = 10; nS<=10000; nS=nS*10) {
//        for(int t=2; t<=4; t++) {
        String nameTest = "test" + t;
        if (!t.equals("1")) {
            
            //init
            InitializePubsSubs ips = new InitializePubsSubs(xmppServer);
            ips.initialize(100, (int)nSubs, nameTest);
            insertTestTriples(100, 100, nameTest);
                
            for(int nP = 1; nP<=100; nP=nP*10) {
//                this.nPubs = nP;
                logger.debug("Publisher : " + nP);
                
                for(int nT=1; nT<=100; nT=nT*10) {
//                    this.nTriples = nT;
                    logger.debug("Triples: " + nT);
                    for(int nR=1; nR <= NRUNS; nR++) {
                        this.runPublishers(nP, nT, nameTest);
                    }
                }
            }
//        }
        } else {
            int nPubs = 1;
            nSubs = 1;
            
            //init
            InitializePubsSubs ips = new InitializePubsSubs(xmppServer);
            ips.initialize(nPubs, (int)nSubs, nameTest);
            insertTestTriples(1, MAXTRIPLES, nameTest);
            
            int nRuns = NRUNS;
            int nTriples;
            for (int j=1;j<=MAXTRIPLES; j=j*10) {
                nTriples = j;
                for (int i=1;i<=nRuns;i++) {
                    this.runPublishers(nPubs, nTriples, "test" +1);
                }
            }
            nTriples = MAXTRIPLES;
            for (int i=1;i<=nRuns;i++) {
                this.runPublishers(nPubs, nTriples, "test" +1);
            }
        } 
        publishers = null;
    }
    
    public void testTest(String t) throws XMPPException, IOException, QueryTypeException, InterruptedException {
        double nSubs = java.lang.Math.pow(10, Integer.parseInt(t)-1);
//        for (int nS = 10; nS<=10000; nS=nS*10) {
//        for(int t=2; t<=4; t++) {
        String nameTest = "test" + 4;
            
            //init
//            InitializePubsSubs ips = new InitializePubsSubs(xmppServer);
//            ips.initialize(100, (int)nSubs, nameTest);
//            insertTestTriples(100, 100, nameTest);
                
            for(int nP = 1; nP<=1; nP=nP*10) {
//                this.nPubs = nP;
                logger.debug("Publisher : " + nP);
                
                for(int nT=1; nT<=100; nT=nT*10) {
//                    this.nTriples = nT;
                    logger.debug("Triples: " + nT);
                    for(int nR=1; nR <= 1; nR++) {
                        this.runPublishers(nP, nT, nameTest);
                    }
                }
            }
//        }
        publishers = null;
        
    }
    
    
    public void allTests(String t) throws XMPPException, IOException, QueryTypeException, InterruptedException {
        String nameTest = "test" + 4;
        int nS, nP, nT;
        nS = 100;
        nP = 1;
        nT = 1;
        this.runPublishers(nP, nT, nameTest);
        Thread.sleep(5000);
        nT = 10;
        this.runPublishers(nP, nT, nameTest);
        Thread.sleep(5000);
        nT = 100;
        this.runPublishers(nP, nT, nameTest);
        Thread.sleep(5000);
        nP = 10;
        nT = 1;
        this.runPublishers(nP, nT, nameTest);
        Thread.sleep(5000);
        nT = 10;
        this.runPublishers(nP, nT, nameTest);
        Thread.sleep(5000);
        nT = 100;
        this.runPublishers(nP, nT, nameTest);
        Thread.sleep(5000);
        nP = 100;
        nT = 1;
        this.runPublishers(nP, nT, nameTest);
        Thread.sleep(5000);
        nT = 10;
        this.runPublishers(nP, nT, nameTest);
        Thread.sleep(5000);
        nT = 100;
        this.runPublishers(nP, nT, nameTest);
        Thread.sleep(5000);
        
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
//            String xmppServer = "localhost";
            String xmppServer = args[0];
            String endpoint = "http://localhost:8001/sparql/";
//            int nPubs  = Integer.parseInt(args[0]);
//            int nSubs = Integer.parseInt(args[1]);
//            int numberTriples = Integer.parseInt(args[2]);
//            String nameTest = args[3];
//            int numberMsgs = Integer.parseInt(args[3]);
            PublishersTest st = new PublishersTest(xmppServer,endpoint);
            //st.run();
            //st.test1();
////            st.test(args[1]);
//            st.testTest(args[1]);
            st.allTests(args[1]);
//            if (args[1].equals("test2")) 
//                st.test2();

            // give time to all the messages to send
            //Thread.sleep(100*nPubs*nSubs);
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
