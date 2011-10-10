package org.deri.xmpppubsubev;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import org.deri.xmpppubsub.*;
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
    
    static Logger logger = Logger.getLogger(PublishersTest.class);
    
    public PublishersTest(String xmppServer, int numberOfPub, 
            int numberOfTriples, //int numberOfMsgs, 
            String endpoint) throws IOException {
        numberOfPublishers = numberOfPub;
        this.xmppServer = xmppServer;
        this.numberOfTriples = numberOfTriples;
//        this.numberOfMsgs = numberOfMsgs;
        this.endpoint = endpoint;
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
    
    public static String createMsgId(int i,Long time) {
        String msgId = "pub" + i + "of" + numberOfPublishers 
                + ",triples" + numberOfTriples + ",ctime" + time.toString();
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
                pubName = "pub" + i;
                p = publishers.get(pubName);
                if (p == null) {
                    pubPass = pubName + "pass";
                    p = new Publisher(pubName, pubPass , xmppServer);
//                        nodeName = "node" + i;
//                        p.getOrCreateNode(nodeName);
                    publishers.put(pubName,p);
                }
                String queryString = createQueryPost(pubName, k);
                logger.debug(queryString);
                triples = sw.runQuery(queryString, endpoint, false);
                logger.debug(triples);

                String msgId = this.createMsgId(i, k, sw.time); 
                SPARQLQuery query = new SPARQLQuery();
                query.wrapTriples(triples);
                logger.debug(query.toXML());
                p.publishQuery(query.toXML(), msgId);
                logger.debug("Published query.");
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
