package org.deri.xmpppubsubev;

import org.apache.log4j.Logger;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.deri.xmpppubsub.Publisher;
import org.deri.xmpppubsub.Subscriber;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

/**
 * @author Julia Anaya
 *
 */
public class InitializePubsSubs {
    public String xmppServer;
    static Logger logger = Logger.getLogger(InitializePubsSubs.class);

    /**
     *
     * @param xmppServer
     */
    public InitializePubsSubs(String xmppServer) {
        this.xmppServer = xmppServer;
    }

    public void initialize(int nSubs, int nPubs, String pubNameTemplate,
            String userPassTemplate, String nodeNameTemplate,
            String subNameTemplate)
            throws XMPPException, InterruptedException {
        String pubName = "";
        String pubPass = "";
        String nodeName = "";
        Publisher p = null;
        logger.debug("nSubs: "+nSubs);
        logger.debug("nPubs: "+nPubs);
        for (int nPub=1; nPub<=nPubs; nPub++) {
            logger.debug("nPub: "+nPub);
            pubName = String.format(pubNameTemplate, nPub);
            pubPass = String.format(userPassTemplate);
            p = new Publisher(pubName, pubPass , xmppServer);
            nodeName = String.format(nodeNameTemplate, nPub);
            p.getOrCreateNode(nodeName);
//            p.disconnect();
//            p = null;
//            Runtime.getRuntime().gc();
        }
        String subName = "";
        String subPass = "";
        Subscriber s = null;
        for (int nSub=1; nSub<=nSubs; nSub++) {
            logger.debug("nSub: "+nSub);
            subName = String.format(subNameTemplate, nSub);
            subPass = String.format(userPassTemplate, subName);
            s = new Subscriber(subName, subPass, xmppServer);
            for (int i=1; i<=nPubs; i++) {
                nodeName = String.format(nodeNameTemplate, i);
                s.subscribeIfNotSubscribedTo(nodeName);
//                s.subscribeTo(nodeName);
                logger.debug("subscribed to " + nodeName);
            }
//            s.disconnect();
//            s = null;
//            Runtime.getRuntime().gc();
        }
    }

    public static void main(String[] args) {
        String domain="localhost";
        int port=5222;

        String pubNameTemplate = "pub%s";
        String userPassTemplate = "pass";
        String nodeNameTemplate = "node%s";
        String subNameTemplate = "sub%s";

        try {
            BasicConfigurator.configure();
            logger.setLevel(Level.DEBUG);

//            XMPPConnection.DEBUG_ENABLED = true;

            logger.debug("Entering application.");
            String usage = "InitializePubsSubs xmppserver nSubs nPubs";
            String usageExample = "InitializePubsSubs 192.168.1.2 1 1";
            if (args.length < 3) {
               System.out.println("Incorrect number of arguments");
               System.out.println("Usage: " + usage);
               System.out.println("Example: " + usageExample);
               System.exit(0);
            }

            domain = args[0];
            int nSubs = Integer.parseInt(args[1]);
            int nPubs = Integer.parseInt(args[2]);

            InitializePubsSubs ips = new InitializePubsSubs(domain);
            ips.initialize(nSubs, nPubs, pubNameTemplate, userPassTemplate,
                    nodeNameTemplate, subNameTemplate);
        } catch (InterruptedException e) {
            logger.error(e.getMessage());
        } catch (XMPPException e) {
            logger.error(e.getMessage());
        }
    }
}
