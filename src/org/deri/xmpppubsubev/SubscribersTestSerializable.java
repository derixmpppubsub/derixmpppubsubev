package org.deri.xmpppubsubev;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import org.deri.xmpppubsub.*;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

/**
 * @author Julia Anaya
 *
 */
public class SubscribersTestSerializable implements Serializable {
    public HashMap<String, Subscriber> subscribers;
    public String xmppServer;
    public String endpoint;
    public static String userPassTemplate = "pass";
    public static String subNameTemplate = "sub%s";
    public static String fileHeadersTemplate = "nTests,nSubs,nPubs,nTriples,"
            + "subName,pubName,tPubStore, tPushMsg, tSubStore, tTotal\n";
//    public static String msgIdTemplate = "%s,%s,%s,%s,%s,%s,%s";
//                    //nTests,nSubs,nPubs,nTriples,pubName,tPubStore,tStartMsg
    public static String fileNameTemplate = "results/nSubs%snPubs%snTriples%s.csv";

    static Logger logger = Logger.getLogger(SubscribersTestSerializable.class);

    /**
     *
     * @param xmppServer
     * @param endpoint
     */
    public SubscribersTestSerializable(String xmppServer, String endpoint) {
        subscribers = new HashMap<String, Subscriber>();
        this.xmppServer = xmppServer;
        this.endpoint = endpoint;
    }

    public SubscribersTestSerializable(String xmppServer, String endpoint,
            int nSubs) throws XMPPException, QueryTypeException,
            InterruptedException, IOException {
        subscribers = new HashMap<String, Subscriber>();
        this.xmppServer = xmppServer;
        this.endpoint = endpoint;
        this.runSubscribers(nSubs);
    }

    public void runSubscribers(int nSubs)
            throws XMPPException, QueryTypeException, InterruptedException,
            IOException {
        logger.debug("nSubs: " + Integer.toString(nSubs));

        String subName = "";
        String subPass = "";
        Subscriber s = null;
        try {
            for (int nSub=1; nSub<=nSubs; nSub++) {
                subName = String.format(subNameTemplate, nSub);
                subPass = String.format(userPassTemplate);
                s = new Subscriber(subName, subPass, xmppServer);
                subscribers.put(subName, s);
                s.addListenerToAllNodes(subName);
            }
        } catch(OutOfMemoryError e){
            System.gc();
            System.out.println("out of memory");
        }
    }

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        try {
            BasicConfigurator.configure();
            logger.setLevel(Level.DEBUG);
            Logger.getRootLogger().setLevel(Level.DEBUG);

//            XMPPConnection.DEBUG_ENABLED = true;

            String usage = "SubscribersTest xmppserver nSubs";
            String usageExample = "SubscribersTest 192.168.1.2 1";
            if (args.length < 2) {
               System.out.println("Incorrect number of arguments");
               System.out.println("Usage: " + usage);
               System.out.println("Example: " + usageExample);
               System.exit(0);
            }

            String endpoint = "http://localhost:8000/update/";

            String xmppServer = args[0];
            int nSubs = Integer.parseInt(args[1]);

            logger.info("The file header created will be: "
                    + fileHeadersTemplate);
            String fileName = String.format(fileNameTemplate, nSubs, "x", "y");
            logger.info("The file name created by the subscribers will be: "
                    + fileName);

            SubscribersTestSerializable st = new SubscribersTestSerializable(
                    xmppServer, endpoint, nSubs);
//                    st.runSubscribers(nSubs);

             FileOutputStream fos = null;
             ObjectOutputStream out = null;
             try
             {
               fos = new FileOutputStream("subscribers-dump");
               out = new ObjectOutputStream(fos);
               out.writeObject(st);
               out.close();
             }
             catch(IOException ex)
             {
               ex.printStackTrace();
             }

            while (true) {
                Thread.sleep(1000);
            }

        } catch(XMPPException e) {
            logger.error(e.getMessage());
        } catch(IOException e) {
            logger.error(e.getMessage());
        } catch (QueryTypeException e) {
            logger.error(e.getMessage());
        } catch (InterruptedException e) {
            logger.error(e.getMessage());
            long end = System.currentTimeMillis();
            long total = end -start;
            logger.info("Total time publishers running: " + total);

        } finally {
            long end = System.currentTimeMillis();
            long total = end -start;
            logger.info("Total time publishers running: " + total);
//            for(Subscriber s : st.subscribers.values()) {
//                s.disconnect();
//            }
        }

    }
}
