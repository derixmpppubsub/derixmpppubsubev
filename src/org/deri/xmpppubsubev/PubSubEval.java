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
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.XSD;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.deri.xmpppubsub.*;
import org.jivesoftware.smack.AccountManager;
import org.jivesoftware.smack.ConnectionConfiguration;
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

    public void connect(String xmppServer, int port) throws XMPPException {
        ConnectionConfiguration config = new ConnectionConfiguration(xmppServer,port);
        connection = new XMPPConnection(config);
        connection.connect();
    }
    
    /**
     * @param username the username.
     * @param password the password.
     * @throws XMPPException if an error occurs creating the account.
     */
    public void createAccount(String username, String password) throws XMPPException {
        logger.info("in createaccount");
        if (!connection.isConnected())
            connection.connect();
        try {
            connection.login(username, password);
            logger.info("already logged in");
        } catch (XMPPException e) {
            connection.getAccountManager().createAccount(username, password);
            connection.login(username, password,"PubSubEval");
            logger.info("logged in");
        } catch (IllegalStateException e) {
            
        }
    }
        
    public String extractJID(String employeeURI) {
//        String jid = employeeURI.split(nm.namespace("ert"))[1];
//        logger.info(employeeURI.split("http://www.cisco.com/ert/"));
        logger.info("in extractjid");
        String jid = employeeURI.replace("http://ecp-alpha/semantic/employee/", "");
        logger.debug("Employee JID");
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
        logger.info(fileName);
        Model model = ModelFactory.createDefaultModel();
        InputStream in = FileManager.get().open( fileName );
        if (in == null) {
            throw new IllegalArgumentException(
                                         "File: " + fileName + " not found");
        }
        // FIXME: check serialization?
        model.read(in, null,"N-TRIPLE");
        model.write(System.out);   
        return model;
        
    }

    public ArrayList<String> extractEmployees(Model model) {
        ArrayList<String> employees = new ArrayList<String>();
        String prolog = "PREFIX rdf: <"+RDF.getURI()+"> \n" ;
//        prolog += nm.prefix("cisco")+"\n";
        prolog += "PREFIX cisco: <http://www.cisco.com/ert/> \n" ;
        String queryString = prolog +
                "SELECT ?emp WHERE {" +
                "?emp a cisco:Employee ." +
                "}" ;         
        logger.info("Execute query=\n"+queryString) ;
        Query query = QueryFactory.create(queryString) ;
        QueryExecution qexec = QueryExecutionFactory.create(query, model) ;
        try {
          ResultSet results = qexec.execSelect() ;
          logger.info("Employees query result");
//          ResultSetFormatter.out(System.out, results, query) ;
          for ( ; results.hasNext() ; ) {
            QuerySolution soln = results.nextSolution() ;
            Resource r = soln.getResource("emp") ; // Get a result variable - must be a resource
            employees.add(r.getURI());
            logger.info("Employee URI");
            logger.info(r.getURI());
          }
        } finally { qexec.close() ; }
        return employees;
    }
    
    public ArrayList<String> extractPosts(Model model) {
        ArrayList<String> posts = new ArrayList<String>();
        String prolog = "PREFIX rdf: <"+RDF.getURI()+"> \n" ;
        prolog += nm.prefix("sioc");
        String queryString = prolog +
                "SELECT ?post WHERE {" +
                "?post a sioc:Post .}" ;        
        logger.info("Execute query=\n"+queryString) ;
        Query query = QueryFactory.create(queryString) ;
        QueryExecution qexec = QueryExecutionFactory.create(query, model) ;
        try {
          ResultSet results = qexec.execSelect() ;
          logger.info("Posts query result");
//          ResultSetFormatter.out(System.out, results, query) ;
          for ( ; results.hasNext() ; )
          {
            QuerySolution soln = results.nextSolution() ;
            Resource r = soln.getResource("post") ; // Get a result variable - must be a resource
            posts.add(r.getURI());
            logger.info(r.getURI());
          }
        } finally { qexec.close() ; }
        logger.info(posts);
        return posts;
    }

    
    public String extractPostJID(Model model, String post) {
        String emp = "";
        String jid;
        String prolog = "PREFIX rdf: <"+RDF.getURI()+"> \n" ;
        prolog += "PREFIX dc: <"+DC.getURI()+"> \n" ;
        prolog += nm.prefix("sioc")+"> \n";
        String queryString = prolog +
                "SELECT ?emp WHERE {" +
                post + " a sioc:Post ;" +
                		"dc:creator ?emp" +
                "}" ;        
        Query query = QueryFactory.create(queryString) ;
        QueryExecution qexec = QueryExecutionFactory.create(query, model) ;
        try {
          ResultSet results = qexec.execSelect() ;
          logger.info("Post JID query result");
          ResultSetFormatter.out(System.out, results, query) ;
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
            logger.info(filePath);
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
            eval.connect(xmppserver, port);
            
            // create accounts for all employees
            Model employeesModel = eval.extractN3(employeesFileName);
            ArrayList<String> employees = eval.extractEmployees(employeesModel);
            logger.info("Employees parsed");
            for (String employee: employees) {
                logger.info("Extracting jid for employee");
                logger.info(employee);
                String jid = eval.extractJID(employee);
                logger.info(jid);
                String pass = jid+"pass";
                eval.createAccount(jid, pass);
            }
            
            // create nodes
            Model postsModel = eval.extractN3(postsFileName);
            ArrayList<String> posts = eval.extractPosts(postsModel);
            ArrayList<LeafNode> allNodes = new ArrayList<LeafNode>();
            logger.info("Posts parsed");
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
               //Subscriber s = new Subscriber(jidem, passem, xmppserver, port);
               //FIXME needed to get all nodes with the s connection?
               //s.mgr.discoverNodes()
               //node = s.getNode(nodeName);
               for (LeafNode nodeem: allNodes) {
                   nodeem.subscribe(jid + "@" + xmppserver);
               }
           }
                
                
            }
        } catch(Exception e) {
            e.printStackTrace();
            logger.debug(e);
        }
    }
}
