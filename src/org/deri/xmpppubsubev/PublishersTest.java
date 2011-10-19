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
    HashMap<String, Publisher> publishers;
    public String xmppServer;
    public String endpoint;
    public static String pubNameTemplate = "pub%s";
    public static String userPassTemplate = "pass";
    public static String nodeNameTemplate = "node%s";
    public static String subNameTemplate = "sub%s";
    public static String postNameTemplate = "post%s";
    public static String postTemplate = "<http://ecp-alpha/semantic/post/%s> "
            + "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type> "
            + "<http://rdfs.org/sioc/ns#Post> .";
    public static String postCreatorTemplate =
            "<http://ecp-alpha/semantic/post/%s> "
            + "<http://purl.org/dc/elements/1.1/creator> "
            + "<http://ecp-alpha/semantic/employee/%s> .";
    public static String fileHeadersTemplate = "nTests,nTest,nSubs,nPubs,nTriples,"
            + "subName,pubName,tPubStore,tPushMsg,tSubStore,tTotal\n";
    public static String msgIdTemplate = "%s,%s,%s,%s,%s,%s,%s";
            //nTests, nTest, nSubs, nPubs, nTriples,pubName, tPubStore
    public static String fileNameTemplate = "results/nTests%snSubs%snPubs%snTriples%s.csv";
    public static int MAXTRIPLES = 8200;

    static Logger logger = Logger.getLogger(PublishersTest.class);

    /**
     *
     * @param xmppServer
     * @param endpoint
     * @throws IOException
     */
    public PublishersTest(String xmppServer, String endpoint) throws IOException {
        this.xmppServer = xmppServer;
        this.endpoint = endpoint;
        publishers = new HashMap<String, Publisher>();
    }

    /**
     *
     * @param nPubs
     * @param nTriples
     * @throws UnsupportedEncodingException
     * @throws IOException
     */
    public void initializeStore(int nPubs, int nTriples)
            throws UnsupportedEncodingException, IOException {
        String queryString = "";
        String triples = "";
//        SPARQLWrapper sw = new SPARQLWrapper();
        for (int i=1; i<=nPubs; i++) {
            for (int k=1; k<=nTriples; k++) {
                triples += String.format(postTemplate
                        + postCreatorTemplate,
                        String.format(postNameTemplate, k),
                        String.format(postNameTemplate, k),
                        String.format(pubNameTemplate, i));
            }
        }
        logger.debug(triples);
        queryString = "INSERT DATA {" + triples + "}";
//        String result = SPARQLWrapper.runQuery(queryString, endpoint, true);
//        Object[] ret = SPARQLWrapper.runQuery(queryString, endpoint, true);
        SPARQLWrapper.runQuery(queryString, endpoint, true);
    }

    /**
     *
     * @param pubName
     * @param postName
     * @return
     */
    public String queryPost(String pubName, String postName) {
        String queryString = "CONSTRUCT {"
          + String.format(postTemplate, postName)
          + "}  WHERE {"
          + String.format(postTemplate, postName)
          + String.format(postCreatorTemplate, postName, pubName)
          +"}";
          return queryString;
    }

    /**
     *
     * @param pubName
     * @param nTriples
     * @return
     */
    public String queryPosts(String pubName, int nTriples) {
        String queryString = "CONSTRUCT {"
          + "?post <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> "
                + "<http://rdfs.org/sioc/ns#Post> . "
          + "}  WHERE {"
          + "?post <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> "
                + "<http://rdfs.org/sioc/ns#Post> . "
          + String.format("?post <http://purl.org/dc/elements/1.1/creator> "
                + "<http://ecp-alpha/semantic/employee/%s> . ", pubName)
//          + String.format("FILTER (REGEX(str(?post),
//                '^http://ecp-alpha/semantic/post/%spost')) . ", pubName)
          +"}"
          + String.format(" LIMIT %s", nTriples);
          return queryString;
    }

    /**
     *
     * @param nTests
     * @param nSubs
     * @param nPubs
     * @param nTriples
     * @param pubName
     * @param tPubStore
     * @return
     */
    public String createMsgId(int nTests, int nTest, int nSubs, int nPubs, int nTriples,
            String pubName, Long tPubStore) { //, Long tStartMsg
          String msgId = String.format(msgIdTemplate, nTests, nTest, nSubs, nPubs, nTriples,
                  pubName, tPubStore.toString()); //, tStartMsg
        return msgId;
    }

    /**
     *
     * @param nSubs
     * @param nPubs
     * @param nTriples
     * @throws XMPPException
     * @throws IOException
     * @throws QueryTypeException
     * @throws InterruptedException
     */
    public void runPublishers(int nTests, int nTest, int nSubs, int nPubs, int nTriples)
            throws XMPPException, IOException, QueryTypeException,
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
            Long tPubStore = null;
            for (int nPub=1; nPub<=nPubs; nPub++) {
                pubName = String.format(pubNameTemplate, nPub);
                p = publishers.get(pubName);
                if (p == null) {
                    pubPass = String.format(userPassTemplate);
                    p = new Publisher(pubName, pubPass , xmppServer);
                    nodeName = String.format(nodeNameTemplate, nPub);
//                    p.getOrCreateNode(nodeName);
                    p.getNode(nodeName);
                    publishers.put(pubName,p);
                }
                String queryString = queryPosts(pubName, nTriples);
//                logger.debug(queryString);
                Object[] ret = SPARQLWrapper.runQuery(queryString, endpoint, false);
                tPubStore = (Long)ret[1];
//                logger.debug("tpubstore: " + tPubStore);
                triples = (String)ret[0];
//                logger.debug(triples);
//                if (triples != null) {
//                    logger.debug("returned triples");
//                } else {
//                    logger.debug("no triples");
//                }
                String msgId = this.createMsgId(nTests, nTest, nSubs, nPubs, nTriples,
                        pubName, tPubStore);
//                logger.debug(msgId);
                SPARQLQuery query = new SPARQLQuery();
                query.wrapTriples(triples);
//                logger.debug(query.toXML());
                p.publishQuery(query.toXML(), msgId);
                logger.debug("Published query.");
            }
        } catch(OutOfMemoryError e){
            System.gc();
            logger.error(e);
        }
    }

    /**
     *
     * @param nSubs
     * @param nPubs
     * @param nTriples
     * @throws XMPPException
     * @throws IOException
     * @throws QueryTypeException
     * @throws InterruptedException
     */
    public void initializeRunPublishers(int nTests, int nTest, int nSubs, int nPubs, int nTriples)
            throws XMPPException, IOException, QueryTypeException,
            InterruptedException {

        //init
        // mysqldump xmppserver db
        // mysql create new db
        InitializePubsSubs ips = new InitializePubsSubs(xmppServer);
        ips.initialize(nPubs, nSubs, pubNameTemplate, userPassTemplate,
                nodeNameTemplate, subNameTemplate);

        // cp /var/lib/4store/runningdb
        // 4s-backend-setup db
        // 4s-backend db
        // 4s-httpd db
        initializeStore(nPubs, nTriples);

        this.runPublishers(nTests, nTest,  nSubs, nPubs, nTriples);

        this.publishers = null;
    }

    /**
     *
     * @param nSubs
     * @param nPubs
     * @param nTriples
     * @throws XMPPException
     * @throws IOException
     * @throws QueryTypeException
     * @throws InterruptedException
     */
    public void tests(int nTests, int nSubs, int nPubs, int nTriples)
            throws XMPPException, IOException, QueryTypeException,
            InterruptedException {

//        int nTests = 1;
//        double nSubs = java.lang.Math.pow(10, Integer.parseInt(t)-1);
//        if ((nSubs == 1) && (nPubs == 1)) {
//            nTests = 30;
//        }
        logger.debug(nTests);
        for(int nTest=1; nTest<=nTests; nTest++) {
            logger.debug("nTest: " + nTest);

            for(int nSub=1; nSub<=nSubs; nSub=nSub*10) {
                logger.debug("nSub: " + nSub);

                for(int nPub = 1; nPub<=nPubs; nPub=nPub*10) {
                    logger.debug("nPub : " + nPub);
                    //init

                    for(int nT=1; nT<=nTriples; nT=nT*10) {
                        logger.debug("nTriples: " + nT);

                        this.runPublishers(nTests, nTest, nSub, nPub, nT);
                    }
                }
            }
        }

        this.publishers = null;
    }

//    public void manualTests(String t) throws XMPPException, IOException, QueryTypeException, InterruptedException {
//        String nameTest = "test" + 4;
//        int nS, nP, nT;
//        nS = 100;
//        nP = 1;
//        nT = 1;
//        this.runPublishers(nP, nT, nameTest);
//        Thread.sleep(5000);
//        nT = 10;
//        this.runPublishers(nP, nT, nameTest);
//        Thread.sleep(5000);
//        nT = 100;
//        this.runPublishers(nP, nT, nameTest);
//        Thread.sleep(5000);
//        nP = 10;
//        nT = 1;
//        this.runPublishers(nP, nT, nameTest);
//        Thread.sleep(5000);
//        nT = 10;
//        this.runPublishers(nP, nT, nameTest);
//        Thread.sleep(5000);
//        nT = 100;
//        this.runPublishers(nP, nT, nameTest);
//        Thread.sleep(5000);
//        nP = 100;
//        nT = 1;
//        this.runPublishers(nP, nT, nameTest);
//        Thread.sleep(5000);
//        nT = 10;
//        this.runPublishers(nP, nT, nameTest);
//        Thread.sleep(5000);
//        nT = 100;
//        this.runPublishers(nP, nT, nameTest);
//        Thread.sleep(5000);
//
//        publishers = null;
//
//    }

    public static void main(String[] args) {
        try {
            long start = System.currentTimeMillis();
            BasicConfigurator.configure();
            logger.setLevel(Level.DEBUG);
            Logger.getRootLogger().setLevel(Level.DEBUG);

//            XMPPConnection.DEBUG_ENABLED = true;

            String usage = "PublishersTest xmppserver nTests nSubs nPubs nTriples";
            String usageExample = "PublishersTest 192.168.1.2 1 1 1 1";
            if (args.length < 5) {
               System.out.println("Incorrect number of arguments");
               System.out.println("Usage: " + usage);
               System.out.println("Example: " + usageExample);
               System.exit(0);
            }

            String endpoint = "http://localhost:8001/sparql/";

            String xmppServer = args[0];
            int nTests = Integer.parseInt(args[1]);
            int nSubs  = Integer.parseInt(args[2]);
            int nPubs = Integer.parseInt(args[3]);
            int nTriples = Integer.parseInt(args[4]);

            logger.info("The file header created by the subscribers will be: "
                    + fileHeadersTemplate);
            logger.info("The file name created by the subscribers will be: "
                    + String.format(fileNameTemplate, nTests, nSubs, nPubs, nTriples));

            PublishersTest st = new PublishersTest(xmppServer,endpoint);

            st.tests(nTests, nSubs, nPubs, nTriples);

            long end = System.currentTimeMillis();
            long total = end -start;
            logger.info("Total time publishers running: " + total);
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
