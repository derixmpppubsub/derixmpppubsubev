package org.deri.xmpppubsubev;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import org.deri.any23.extractor.ExtractionException;
import org.deri.xmpppubsub.*;
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

    static Logger logger = Logger.getLogger(ScalabilityTest.class);
    
    public ScalabilityTest() {
        numberOfPublishers = 1;
        numberOfSubscribers = 2;
        publishers = new ArrayList<Publisher>();
        subscribers = new ArrayList<Subscriber>();
    }
    public static void main(String[] args) throws XMPPException, IOException, ExtractionException, QueryTypeException{

        // Set up a simple configuration that logs on the console.
        BasicConfigurator.configure();
        
        //logger.setLevel(Level.DEBUG);
        logger.debug("Entering application.");
        
        String xmppServer = "vmuss12.deri.ie";
        for (int i=1; i<=numberOfPublishers; i++) {
            Publisher p = new Publisher("sca" + i, "sca" + i + "pass", xmppServer);
            LeafNode node = p.getOrCreateNode("node" + i);
            publishers.add(p);
            for (int j=1; j<=numberOfSubscribers; j++) {
                Subscriber s = new Subscriber("sca" + j, "sca" + j + "pass", xmppServer);
                subscribers.add(s);
                s.subscribeIfNotSubscribedTo(node);
            }
            SPARQLQuery query = new SPARQLQuery(String.format(postTemplate, i, i , i));
            p.publishQuery(query.toXMLDecodingEntitiesCDATA());
        }

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
