package org.deri.xmpppubsubev;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import org.deri.any23.extractor.ExtractionException;
import org.deri.xmpppubsub.*;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.pubsub.LeafNode;

public class ScalabilityTest {
    public static int numberOfPublishers;
    public static int numberOfSubscribers;
    public static ArrayList<Publisher> publishers;
    public static ArrayList<Subscriber> subscribers;
    public static int MAX_PUBLISHERS = 100;
    public static String postTemplate = "<http://ecp-alpha/semantic/post/%s> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://rdfs.org/sioc/ns#Post> ."
        + "<http://ecp-alpha/semantic/post/%s> <http://purl.org/dc/elements/1.1/creator> <http://ecp-alpha/semantic/employee/%s> .";
    public String xmppServer;
    
    static Logger logger = Logger.getLogger(ScalabilityTest.class);
    static Logger loggerp = Logger.getLogger(Publisher.class);
    static Logger loggers = Logger.getLogger(Subscriber.class);
    
    public ScalabilityTest(String xmppServer) {
        numberOfPublishers = 1;
        numberOfSubscribers = 2;
        publishers = new ArrayList<Publisher>();
        subscribers = new ArrayList<Subscriber>();
        this.xmppServer = xmppServer;
    }
    
    public void run() throws XMPPException, IOException, ExtractionException, QueryTypeException {
        System.out.print("number of publishers" + numberOfPublishers);
        for (int i=1; i<=numberOfPublishers; i++) {
            System.out.print("" + i);
            Publisher p = new Publisher("pub" + i, "pub" + i + "pass", xmppServer);
            logger.debug("Created publisher.");
            String nodeName = "node" + i;
            p.getOrCreateNode(nodeName);
            publishers.add(p);
            for (int j=1; j<=numberOfSubscribers; j++) {    
                Subscriber s = new Subscriber("sub" + j, "sub" + j + "pass", xmppServer);
                logger.debug("Created subscriber.");
                subscribers.add(s);
                LeafNode node = s.getNode(nodeName);
                node.addItemEventListener(new ItemEventCoordinator());
                s.subscribeIfNotSubscribedTo(node);
            }
            SPARQLQuery query = new SPARQLQuery(String.format(postTemplate, i, i , i));
            p.publishQuery(query.toXML());
            logger.debug("Published query.");
        }
    }
    
    public static void main(String[] args) throws XMPPException, IOException, ExtractionException, QueryTypeException{

        // Set up a simple configuration that logs on the console.
        BasicConfigurator.configure();
        
        logger.setLevel(Level.DEBUG);
        logger.debug("Entering application.");
        // turn on the enhanced debugger
        XMPPConnection.DEBUG_ENABLED = true;
        String xmppServer = "vmuss12.deri.ie";
        
        ScalabilityTest st = new ScalabilityTest(xmppServer);
        st.run();
        
        // 1. 
        // init publishers and subscribers
        // for jid in jids

            // init publisher
            // Publisher p = new Publisher(jid, jid+"pass");
            // publisherNodes.add(p.getOrCreateNode("node" + jid));
        
            // init subscriber
            // Subscriber s = new Subscriber(jid, jid+"pass");
            // for publisherNode in publisherNodes
                // s.subscribeIfNotSubscribedTo(publisherNode)
        
        // send posts
        // nodesHash = createNodesHash();
        // for nodename in nodesHash
        
        
            // SPARQLQuery query = new SPARQLQuery(nodename.get("data"));
            // p.get(node).publishQuery(query.toXMLDecodingEntitiesCDATA());

        
        // 2. 
        // init publishers and subscribers
        // for jid in jids
        
        
            // init subscriber
            // Subscriber s = new Subscriber(jid, jid+"pass");
            // for publisherNode in publisherNodes
                // s.subscribeIfNotSubscribedTo(publisherNode)
        
        // send posts
        // nodesHash = createNodesHash();
        // for nodename in nodesHash
        
            // init publisher
            // Publisher p = new Publisher(node, node+"pass");
            // publisherNodes.add(p.getOrCreateNode("node" + jid));
        
            // SPARQLQuery query = new SPARQLQuery(nodename.get("data"));
            // p.publishQuery(query.toXMLDecodingEntitiesCDATA());

        
    }
}
