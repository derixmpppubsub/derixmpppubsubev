package org.deri.xmpppubsubev;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.FileManager;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.deri.xmpppubsub.*;
import org.jivesoftware.smack.AccountManager;
import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Registration;
import org.jivesoftware.smackx.pubsub.LeafNode;

import org.deri.xmpppubsub.*;

/**
 * @author Maciej Dabrowski
 * @author Julia Anaya
 *
 */
public class PubSubEval {
    private XMPPConnection connection;
    static Namespace nm = new Namespace();
    // TODO use Junit
    static Logger logger = Logger.getLogger(PubSubEval.class);

    /**
     * @param username the username.
     * @param password the password.
     * @throws XMPPException if an error occurs creating the account.
     */
    public void createAccount(String username, String password) throws XMPPException {
        
        if (!connection.isConnected())
            connection.connect();
        try {
            connection.login(username, password);
        } catch (XMPPException e) {
            connection.getAccountManager().createAccount(username, password);
            connection.login(username, password);
        }
//            AccountManager am = new AccountManager(this.connection);
//            am.createAccount(username, password);
    }
        
    public String extractJID(String employeeURI) {
        String jid = employeeURI.split(nm.namespace("cisco"))[1];
        logger.debug(jid);
        return jid;
    }
    
//    public static String[] getTagsFromPost(String data) {
//        String[] tags;
//        return tags;
//    }
//  
//    public String[] getTagsFromEmployee(String data) {
//        String[] tags;
//        return tags;
//    }

    public Model extractN3(String fileName) {
        Model model = ModelFactory.createDefaultModel();
        InputStream in = FileManager.get().open( fileName );
        if (in == null) {
            throw new IllegalArgumentException(
                                         "File: " + fileName + " not found");
        }
        // FIXME: check serialization?
        model.read(in, null,"N3");
        return model;
        
    }
    
    public ArrayList<String> extractPosts(Model model) {
        ArrayList<String> posts = new ArrayList<String>();
        String queryString = nm.prefix("siocT") + nm.prefix("rdf") +
                "SELECT ?post WHERE {" +
                "?post a siocT:Post .}" ;        
        Query query = QueryFactory.create(queryString) ;
        QueryExecution qexec = QueryExecutionFactory.create(query, model) ;
        try {
          ResultSet results = qexec.execSelect() ;
          for ( ; results.hasNext() ; )
          {
            QuerySolution soln = results.nextSolution() ;
            Resource r = soln.getResource("post") ; // Get a result variable - must be a resource
            posts.add(r.getURI());
          }
        } finally { qexec.close() ; }
        return posts;
    }

    
    public String extractPostJID(Model model, String post) {
        String emp = "";
        String jid;
        String queryString = nm.prefix("siocT") + nm.prefix("rdf") + nm.prefix("dc") +
                "SELECT ?emp WHERE {" +
                post + " a siocT:Post ;" +
                		"dc:creator ?emp" +
                "}" ;        
        Query query = QueryFactory.create(queryString) ;
        QueryExecution qexec = QueryExecutionFactory.create(query, model) ;
        try {
          ResultSet results = qexec.execSelect() ;
          for ( ; results.hasNext() ; )           {
            QuerySolution soln = results.nextSolution() ;
            Resource r = soln.getResource("emp") ; // Get a result variable - must be a resource
            emp = r.getURI();
          }
        } finally { qexec.close() ; }
        logger.debug(emp);
        jid = this.extractJID(emp);
        return jid;
        
    }
    
    public ArrayList<String> extractEmployees(Model model) {
        ArrayList<String> employees = new ArrayList<String>();
        String queryString = nm.prefix("cisco") + nm.prefix("rdf") +
                "SELECT ?emp WHERE {" +
                "?emp a cisco:Employee ." +
                "}" ;         
        Query query = QueryFactory.create(queryString) ;
        QueryExecution qexec = QueryExecutionFactory.create(query, model) ;
        try {
          ResultSet results = qexec.execSelect() ;
          for ( ; results.hasNext() ; )
          {
            QuerySolution soln = results.nextSolution() ;
            Resource r = soln.getResource("emp") ; // Get a result variable - must be a resource
            employees.add(r.getURI());
          }
        } finally { qexec.close() ; }
        return employees;
    }
    
    public static void main(String[] args){
        try {
            // Set up a simple configuration that logs on the console.
            BasicConfigurator.configure();
            
            //logger.setLevel(Level.DEBUG);
            logger.info("Entering application.");
    
            // turn on the enhanced debugger
            XMPPConnection.DEBUG_ENABLED = true;
        
            // open the file and read data
            Properties prop = new Properties();
            File file = new File("config/xmpppubsub.properties");
            String filePath = file.getCanonicalPath();
            logger.debug(filePath);
            InputStream is = new FileInputStream(filePath);
            prop.load(is);
            String xmppserver = prop.getProperty("xmppserver");
            int port = Integer.parseInt(prop.getProperty("port")); 
//            String xmppserver="foo";
//            String port="bar";
            // FIXME get file name from args?
            String postsFileName = args[0];
            String employeesFileName = args[1];
            
            PubSubEval eval = new PubSubEval();
            
            // create accounts for all employees
            Model employeesModel = eval.extractN3(employeesFileName);
            ArrayList<String> employees = eval.extractEmployees(employeesModel);
            for (String employee: employees) {
                String jid = eval.extractJID(employee);
                String pass = jid+"pass";
                eval.createAccount(jid, pass);
            }
            
            // create nodes
            Model postsModel = eval.extractN3(postsFileName);
            ArrayList<String> posts = eval.extractPosts(postsModel);
            ArrayList<LeafNode> allNodes = new ArrayList<LeafNode>();
            for (String post: posts) {
                //FIXME: get tags
                //String[] tags = getTagsFromPost(post);
                String jid = eval.extractPostJID(postsModel, post);
                String pass = jid+"pass";
                Publisher p = new Publisher(jid, pass, xmppserver);
                LeafNode node = p.getOrCreateNode(jid);
                allNodes.add(node);
                //FIXME get tags from post and add tags to node
//              node.send(post)?
            
           // create subscribers
           for (String employee: employees) {
               //FIXME: get tags
               String jidem = eval.extractJID(employee);
               String passem = jid+"pass";
               Subscriber s = new Subscriber(jidem, passem, xmppserver, port);
               for (LeafNode nodeem: allNodes) {
                   node.subscribe(jid + "@" + xmppserver);
               }
           }
                
                
            }
        } catch(Exception e) {
            e.printStackTrace();
            logger.debug(e);
        }
    }
}
