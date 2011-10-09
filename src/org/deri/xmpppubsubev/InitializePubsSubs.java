package org.deri.xmpppubsubev;

//import java.util.logging.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.BasicConfigurator;
import org.deri.xmpppubsub.Publisher;
import org.deri.xmpppubsub.Subscriber;
//import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
//import org.jivesoftware.smackx.pubsub.AccessModel;
//import org.jivesoftware.smackx.pubsub.ConfigureForm;
//import org.jivesoftware.smackx.pubsub.FormType;
//import org.jivesoftware.smackx.pubsub.LeafNode;
//import org.jivesoftware.smackx.pubsub.PubSubManager;
//import org.jivesoftware.smackx.pubsub.PublishModel;

public class InitializePubsSubs {
    public String xmppServer;
    public static int numberOfPublishers;
    public static int numberOfSubscribers;
    static Logger logger = Logger.getLogger(InitializePubsSubs.class);
    
    
    public InitializePubsSubs(String xmppServer, int numberOfPub, int numberOfSub) {
        numberOfPublishers = numberOfPub;
        numberOfSubscribers = numberOfSub;
        this.xmppServer = xmppServer;
    }
    
    public void initialize() throws XMPPException, InterruptedException {
//            ConnectionConfiguration config = new ConnectionConfiguration(domain,port);
//            connection = new XMPPConnection(config);
//            connection.connect();
//            //publishers
//            for (int i=1; i<=1000; i++) {
//                userName = "pub"+i;
//                password = userName+"pass";

//                try {
//                    connection.getAccountManager().createAccount(userName, password);
//                    logger.info("User " + userName + " created " 
//                            + domain);
//                } catch(XMPPException e) {
//                    logger.info("User " + connection.getUser() + " already created ");
//                }        
//                try {
//                    connection.login(userName, password);
//                    logger.info("User " + connection.getUser() + " login ");
//                } catch(IllegalStateException e) {
//                    logger.info("User " + connection.getUser() + " already login ");
//                }
//                String nodeName = "node"+i;
//                PubSubManager mgr = new PubSubManager(connection);
//                try {
//                    LeafNode node = (LeafNode) mgr.getNode(nodeName);
//                } catch (Exception e){
//                    ConfigureForm form = new ConfigureForm(FormType.submit);
//                    form.setAccessModel(AccessModel.open);
//                    form.setDeliverPayloads(true);
//                    form.setNotifyRetract(true);
//                    form.setPersistentItems(true);
//                    form.setPublishModel(PublishModel.open);
//                    LeafNode node = (LeafNode) mgr.createNode(nodeName, form);
//                    logger.info("node " + nodeName  + " created");
//                }
//            }
//            //subscribers
//            for (int i=1; i<=1000; i++) {
//                userName = "sub"+i;
//                password = userName+"pass";
//                try {
//                    connection.getAccountManager().createAccount(userName, password);
//                    logger.info("User " + userName + " logged in to the server " 
//                            + domain);
//                } catch(XMPPException e) {
//                }
//            }
//        for (int i=1; i<=numberOfPublishers; i++) {
//            //if(Runtime.getRuntime().freeMemory()<1024*1024) System.gc();
//            String pubName = "pub" + i;
//            String pubPass = pubName + "pass";
//
//            Publisher p = new Publisher(pubName, pubPass , xmppServer);
//            String nodeName = "node" + i;
//            p.getOrCreateNode(nodeName);
//            p.disconnect();
//            p = null;
//            Runtime.getRuntime().gc();
//        }
        for (int j=762; j<=numberOfSubscribers; j++) {  
//            if(Runtime.getRuntime().freeMemory()<1024*1024) System.gc();
            String subName = "sub" + j;
            String subPass = subName + "pass";  
            Subscriber s = new Subscriber(subName, subPass, xmppServer);
            for (int i=1; i<=numberOfPublishers; i++) {
                String nodeName = "node" + i;
                s.subscribeIfNotSubscribedTo(nodeName);
            }
            s.disconnect();
            s = null;
            Runtime.getRuntime().gc();
        }
    }
    
    public static void main(String[] args) {
//        String userName;
//        String password;
        String domain="localhost";
        int port=5222;

        try {
//            XMPPConnection.DEBUG_ENABLED = true;
            BasicConfigurator.configure();
            //logger.setLevel(Level.DEBUG);
            logger.info("Entering application.");
            InitializePubsSubs ips = new InitializePubsSubs(domain, Integer.parseInt(args[0]), 
                    Integer.parseInt(args[1]));
            ips.initialize();
        } catch (InterruptedException e) {
            logger.error(e);
        } catch (XMPPException e) {
            logger.error(e);
        }
    }
}
