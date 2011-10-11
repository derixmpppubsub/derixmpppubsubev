package org.deri.xmpppubsubev;

import java.io.IOException;
import java.util.HashMap;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import org.deri.xmpppubsub.*;
import org.jivesoftware.smack.XMPPException;

/**
 * @author Julia Anaya
 *
 */
public class SubscribersTest {
    public static int numberOfSubscribers;
    public HashMap<String, Subscriber> subscribers;
    //public static int MAX_PUBLISHERS = 100;
    public static String postTemplate = "<http://ecp-alpha/semantic/post/%s> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://rdfs.org/sioc/ns#Post> .";
    public String xmppServer;
    public String fileName;
    public String endpoint;
    public String nameTest;
    
    static Logger logger = Logger.getLogger(SubscribersTest.class);
    
    public SubscribersTest(String xmppServer, int numberOfSub, //int numberOfPub,
            String fileName, String endpoint, String nameTest) {
        numberOfSubscribers = numberOfSub;
        subscribers = new HashMap<String, Subscriber>();
        this.xmppServer = xmppServer;
        this.fileName = fileName;
        this.endpoint = endpoint;
        this.nameTest = nameTest;
    }
    
    public void run() throws XMPPException, QueryTypeException, 
            InterruptedException, IOException {
        logger.debug("number of subscribers " + Integer.toString(numberOfSubscribers));
        String subName = "";
        String subPass = "";
        Subscriber s = null;
        try {
            for (int j=1; j<=numberOfSubscribers; j++) {  
                subName = "sub" + nameTest + j;
                subPass = subName + "pass";  
                s = new Subscriber(subName, subPass, xmppServer);
                subscribers.put(subName, s);
                s.addListenerToAllNodes(subName+ "of" + numberOfSubscribers, 
                        fileName, endpoint);
            }
        } catch(OutOfMemoryError e){
            System.gc();
            System.out.println("out of memory");
        }
    }
    
    public static void main(String[] args) {
        try {
            BasicConfigurator.configure();
            logger.setLevel(Level.DEBUG);
            Logger.getRootLogger().setLevel(Level.DEBUG);
            logger.debug("Entering application.");
            // turn on the enhanced debugger
            //XMPPConnection.DEBUG_ENABLED = true;
//            String xmppServer = "localhost";
            String xmppServer = args[2];
            String endpoint = "http://localhost:8000/update/";
            int numberSubs = Integer.parseInt(args[0]);
            String nameTest = args[1];
            String fileName = "results/pubssubs/"+nameTest+".csv";
            SubscribersTest st = new SubscribersTest(xmppServer, numberSubs, 
                    fileName, endpoint, nameTest);
            st.run();
            while (true) {
                Thread.sleep(100);
            }
        
        } catch(XMPPException e) {
            logger.error(e);
        } catch(IOException e) {
            logger.error(e);
        } catch (QueryTypeException e) {
            logger.error(e);
        } catch (InterruptedException e) {
            logger.error(e);
        }
        
    }
}
