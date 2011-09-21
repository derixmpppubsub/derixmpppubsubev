package org.deri.xmpppubsubev;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

public class CreateAccounts {
    
    public static void main(String[] args) {
        XMPPConnection connection;
        String userName;
        String password;
        String domain="localhost";
        int port=5222;

        Logger logger = Logger.getLogger(CreateAccounts.class);
        ConnectionConfiguration config = new ConnectionConfiguration(domain,port);
        connection = new XMPPConnection(config);
        try {
            connection.connect();
            //publishers
            for (int i=1; i<=1000; i++) {
                userName = "pub"+i;
                password = userName+"pass";
                try {
                    connection.getAccountManager().createAccount(userName, password);
                    logger.info("User " + userName + " logged in to the server " 
                            + domain);
                } catch(XMPPException e) {
                }
            }
            //subscribers
            for (int i=1; i<=1000; i++) {
                userName = "sub"+i;
                password = userName+"pass";
                try {
                    connection.getAccountManager().createAccount(userName, password);
                    logger.info("User " + userName + " logged in to the server " 
                            + domain);
                } catch(XMPPException e) {
                }
            }
        } catch (XMPPException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }
}