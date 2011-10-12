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
//    public static int nPubs;
//    public static int nSubs;
//    public static String nameTest;
    static Logger logger = Logger.getLogger(InitializePubsSubs.class);
    
    
    public InitializePubsSubs(String xmppServer) {
//        nPubs = nPubs;
//        nSubs = nSubs;
        this.xmppServer = xmppServer;
//        this.nameTest = nameTest;
    }
    
    public void initialize(int nPubs, int nSubs, String nameTest) 
            throws XMPPException, InterruptedException {
        String pubName = "";
        String pubPass = "";
        String nodeName = "";
        Publisher p = null;
        for (int i=1; i<=nPubs; i++) {
            pubName = nameTest + "pub" +  i;
            pubPass = pubName + "pass";
            p = new Publisher(pubName, pubPass , xmppServer);
            nodeName = nameTest + "node" + i;
            p.getOrCreateNode(nodeName);
            p.disconnect();
            p = null;
            Runtime.getRuntime().gc();
        }
        String subName = "";
        String subPass = "";
        Subscriber s = null;
        for (int j=1; j<=nSubs; j++) {  
//            if(Runtime.getRuntime().freeMemory()<1024*1024) System.gc();
            subName = nameTest  + "sub" + j;
            subPass = subName + "pass";  
            s = new Subscriber(subName, subPass, xmppServer);
            for (int i=1; i<=nPubs; i++) {
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
            int nPubs = Integer.parseInt(args[0]);
            int nSubs = Integer.parseInt(args[1]);
            String nameTest = "test" + args[2];
            InitializePubsSubs ips = new InitializePubsSubs(domain);
            ips.initialize(nPubs, nSubs, nameTest);
        } catch (InterruptedException e) {
            logger.error(e);
        } catch (XMPPException e) {
            logger.error(e);
        }
    }
}
