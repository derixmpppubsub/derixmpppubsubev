package org.deri.xmpppubsubev;

import org.apache.log4j.Logger;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.deri.xmpppubsub.Publisher;
import org.deri.xmpppubsub.Subscriber;
import org.jivesoftware.smack.XMPPException;

/**
 * @author Julia Anaya
 *
 */
public class InitializePubsSubs {
    public String xmppServer;
    public static int numberOfPublishers;
    public static int numberOfSubscribers;
    public static String nameTest;
    static Logger logger = Logger.getLogger(InitializePubsSubs.class);
    
    
    public InitializePubsSubs(String xmppServer, int numberOfPub, 
            int numberOfSub, String nameTest) {
        numberOfPublishers = numberOfPub;
        numberOfSubscribers = numberOfSub;
        this.xmppServer = xmppServer;
        this.nameTest = nameTest;
    }
    
    public void initialize() throws XMPPException, InterruptedException {
        String pubName = "";
        String pubPass = "";
        String nodeName = "";
        Publisher p = null;
        for (int i=1; i<=numberOfPublishers; i++) {
            pubName = "pub" + nameTest + i;
            pubPass = pubName + "pass";
            p = new Publisher(pubName, pubPass , xmppServer);
            nodeName = "node" + i;
            p.getOrCreateNode(nodeName);
            p.disconnect();
            p = null;
            Runtime.getRuntime().gc();
        }
        String subName = "";
        String subPass = "";
        Subscriber s = null;
        for (int j=1; j<=numberOfSubscribers; j++) {  
//            if(Runtime.getRuntime().freeMemory()<1024*1024) System.gc();
            subName = "sub" + nameTest  + j;
            subPass = subName + "pass";  
            s = new Subscriber(subName, subPass, xmppServer);
            for (int i=1; i<=numberOfPublishers; i++) {
                nodeName = "node" + i;
                s.subscribeIfNotSubscribedTo(nodeName);
            }
            s.disconnect();
            s = null;
            Runtime.getRuntime().gc();
        }
    }
    
    public static void main(String[] args) {
        String domain="localhost";
        int port=5222;

        try {
//            XMPPConnection.DEBUG_ENABLED = true;
            BasicConfigurator.configure();
            logger.setLevel(Level.DEBUG);
            logger.debug("Entering application.");
            InitializePubsSubs ips = new InitializePubsSubs(domain, Integer.parseInt(args[0]), 
                    Integer.parseInt(args[1]), args[2]);
            ips.initialize();
        } catch (InterruptedException e) {
            logger.error(e);
        } catch (XMPPException e) {
            logger.error(e);
        }
    }
}
