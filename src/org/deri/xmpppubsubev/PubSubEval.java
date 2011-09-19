package org.deri.xmpppubsubev;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
import org.deri.any23.extractor.ExtractionException;
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
import org.jivesoftware.smackx.pubsub.Item;
import org.jivesoftware.smackx.pubsub.LeafNode;
import org.jivesoftware.smackx.pubsub.Subscription;

import org.deri.xmpppubsub.*;

/**
 * @author Maciej Dabrowski
 * @author Julia Anaya
 *
 */
public class PubSubEval {
	private String xmppServer;
	private int xmppPort;
	public ArrayList<Publisher> publishers;
	public ArrayList<Subscriber> subscribers;
	
    static Namespace nm = new Namespace();
    // TODO use Junit
    static Logger logger = Logger.getLogger(PubSubEval.class);
    
    public PubSubEval(String xmppServer, int xmppPort) {
    	this.xmppServer = xmppServer;
    	this.xmppPort = xmppPort;
    	this.publishers = new ArrayList<Publisher>();
    	this.subscribers = new ArrayList<Subscriber>();
    }
    
    /**
     * @param username the username.
     * @param password the password.
     * @throws XMPPException if an error occurs creating the account.
     */
//    public void createAccount(String username, String password) throws XMPPException {
//        logger.debug("in createaccount");
//        if (!connection.isConnected())
//            connection.connect();
//        try {
//            connection.login(username, password);
//            logger.debug("already logged in");
//        } catch (XMPPException e) {
//            connection.getAccountManager().getAccountAttributes();
//            
//            connection.getAccountManager().createAccount(username, password);
//            connection.login(username, password,"PubSubEval");
//            logger.debug("logged in");
//        } catch (IllegalStateException e) {
//            
//        }
    public void createAccount(String userName, String password) throws XMPPException {
        ConnectionConfiguration config = new ConnectionConfiguration(xmppServer);  
    	XMPPConnection connection = new XMPPConnection(config);
		connection.connect();
		try {
			connection.getAccountManager().createAccount(userName, password);
		} catch(XMPPException e) {
			// account already created
		}
		connection.disconnect();
    }
    
    public void createAccounts(HashMap<String, String> jids) throws XMPPException  {
        for (String userName : jids.keySet()) {
        	this.createAccount(userName, jids.get(userName));
        }
    }
    
    public Model extractNTriples(String fileName) {
        Model model = ModelFactory.createDefaultModel();
        InputStream in = FileManager.get().open( fileName );
        if (in == null) {
            throw new IllegalArgumentException(
                                         "File: " + fileName + " not found");
        }
        // FIXME: check serialization?
        model.read(in, null,"N-TRIPLE");
        logger.debug("NTriples in file " + fileName);
        model.write(System.out);   
        return model;
        
    }
        
    public String extractJID(String employeeURI) {
//        String jid = employeeURI.split(nm.namespace("ert"))[1];
//        logger.debug(employeeURI.split("http://www.cisco.com/ert/"));
        String jid = "u" + employeeURI.replace("http://ecp-alpha/semantic/employee/", "");
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


    public ArrayList<String> extractEmployees(Model model) {
        ArrayList<String> employees = new ArrayList<String>();
        String prolog = "PREFIX rdf: <"+RDF.getURI()+"> \n" ;
//        prolog += nm.prefix("cisco")+"\n";
        prolog += "PREFIX cisco: <http://www.cisco.com/ert/> \n" ;
        String queryString = prolog +
                "SELECT ?emp WHERE {" +
                "?emp a cisco:Employee ." +
                "}" ;         
        logger.debug("Execute query=\n"+queryString) ;
        Query query = QueryFactory.create(queryString) ;
        QueryExecution qexec = QueryExecutionFactory.create(query, model) ;
        try {
          ResultSet results = qexec.execSelect() ;
          for ( ; results.hasNext() ; ) {
            QuerySolution soln = results.nextSolution() ;
            Resource r = soln.getResource("emp") ; // Get a result variable - must be a resource
            employees.add(r.getURI());
            logger.debug("Employee URI");
            logger.debug(r.getURI());
          }
        } finally { qexec.close() ; }
        return employees;
    }
    
    public HashMap<String, String> createJIDsHash(ArrayList<String> employees) {
    	HashMap<String, String> jids = new HashMap<String, String>();
        for (String employee: employees) {
            String jid = this.extractJID(employee);
            String pass = jid+"pass";
            jids.put(jid, pass);
            logger.debug("The employee " + employee + "jid is " + jid);
        }
		return jids;
    }
    
    public ArrayList<String> extractPosts(Model model) {
        ArrayList<String> posts = new ArrayList<String>();
        String prolog = "PREFIX rdf: <"+RDF.getURI()+"> \n" ;
        prolog += nm.prefix("sioc");
        String queryString = prolog +
                "SELECT ?post WHERE {" +
                "?post a sioc:Post .}" ;        
        logger.debug("Execute query=\n"+queryString) ;
        Query query = QueryFactory.create(queryString) ;
        QueryExecution qexec = QueryExecutionFactory.create(query, model) ;
        try {
          ResultSet results = qexec.execSelect() ;
//          logger.debug("Posts query result");
//          ResultSetFormatter.out(System.out, results, query) ;
          for ( ; results.hasNext() ; )
          {
            QuerySolution soln = results.nextSolution() ;
            Resource r = soln.getResource("post") ; // Get a result variable - must be a resource
            posts.add(r.getURI());
            logger.debug(r.getURI());
          }
        } finally { qexec.close() ; }
        logger.debug(posts);
        return posts;
    }

    
    public String extractPostJID(Model model, String post) {
        String emp = "";
        String jid;
        String prolog = "PREFIX rdf: <"+RDF.getURI()+"> \n" ;
        prolog += "PREFIX dc: <"+DC.getURI()+"> \n" ;
        prolog += nm.prefix("sioc")+" \n";
        String queryString = prolog +
                "SELECT ?emp WHERE {" +
                "<" + post + "> a sioc:Post ;" +
                		"dc:creator ?emp" +
                "}" ;      
        logger.debug("Executed query=\n"+queryString) ;  
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
    public HashMap<String, String> createNodesHash (Model postsModel, ArrayList<String> posts) {
    	HashMap<String, String> postsHash = new HashMap<String, String>();
	    for (String post: posts) {
	        //FIXME: get tags
	        //String[] tags = getTagsFromPost(post);
	        String jid = this.extractPostJID(postsModel, post);
	        String postData = this.extractPostData(postsModel, post);
	        logger.debug("jid " + jid + " post data" + postData);
	        postsHash.put(jid, postData);
	    }
	    return postsHash;
    	
    }
    
    public void publishPosts(HashMap<String, String> postsHash) throws 
            XMPPException, IOException, ExtractionException, 
            QueryTypeException, InterruptedException{
        for (String nodeName : postsHash.keySet()) {
	        String pass = nodeName+"pass";
	        Publisher p = new Publisher(nodeName, pass, xmppServer);
	        LeafNode node = p.getOrCreateNode(nodeName);
	        //FIXME get tags from post and add tags to node
	        logger.debug("node " + nodeName + " got or created");
		    SPARQLQuery query = new SPARQLQuery(postsHash.get(nodeName));
	        p.publishQuery(query.toXML());
	        logger.debug("sent post " + query.toXML());
//	        p.disconnect();
	    }
}
    public String extractPostData(Model model, String post) {
        String postData;
        StringWriter dataWriter = new StringWriter();
        String prolog = "PREFIX rdf: <"+RDF.getURI()+"> \n" ;
        prolog += "PREFIX dc: <"+DC.getURI()+"> \n" ;
        prolog += nm.prefix("sioc")+" \n";
        String queryString = prolog +
                "CONSTRUCT   { <" + post + "> ?p ?o }" +
                "WHERE {" +
                "<" + post + "> a sioc:Post ;" +
                		"?p ?o" +
                "}" ;      
        logger.debug("Execute query=\n"+queryString) ;  
        Query query = QueryFactory.create(queryString) ;
        QueryExecution qexec = QueryExecutionFactory.create(query, model) ;
        Model resultModel = qexec.execConstruct() ;
        qexec.close() ; 
        resultModel.write(dataWriter, "N-TRIPLE");
        postData = dataWriter.toString();
        logger.debug("postData " + postData);
        return postData;
        
    }    
    
    public void subscribeToNodes(HashMap<String, String> jids,
            ArrayList<String> nodeNames) throws XMPPException, 
            InterruptedException{
        for (String jidaccount : jids.keySet()) {
            Subscriber s = new Subscriber(jidaccount, jids.get(jidaccount), xmppServer);
            //FIXME needed to get all nodes with the s connection?
            //s.mgr.discoverNodes()
            for (String nodeName: nodeNames) {
                LeafNode node = s.getNode(nodeName);
                
                //just to clean
                //s.deleteSubscriptions(node, jidaccount, xmppServer);
                
         	    if (!nodeName.equals(jidaccount) ) {
         	        s.subscribeIfNotSubscribedTo(node);
         	        
//         	        List<? extends Item> items = node.getItems(1);
//		    			
//         	        for(Item item : items){
//		    			   System.out.println(item.toString());
//		    		}
         	     }
            }
            //s.disconnect
        }
    }
    
    public static void main(String[] args){
        try {
            // Set up a simple configuration that logs on the console.
            BasicConfigurator.configure();
            
            //logger.setLevel(Level.DEBUG);
            logger.debug("Entering application.");
    
            // turn on the enhanced debugger
            XMPPConnection.DEBUG_ENABLED = true;
        
            // get configuration file data
            Properties prop = new Properties();
            File file = new File("config/xmpppubsub.properties");
            String filePath = file.getCanonicalPath();
            logger.debug(filePath);
            InputStream is = new FileInputStream(filePath);
            prop.load(is);
            
            String xmppServer = prop.getProperty("xmppserver");
            int xmppPort = Integer.parseInt(prop.getProperty("port")); 

            // parse input args
            String postsFileName = args[0];
            String employeesFileName = args[1];
            
            PubSubEval eval = new PubSubEval(xmppServer, xmppPort);
            
            // extract employees
            Model employeesModel = eval.extractNTriples(employeesFileName);           
            ArrayList<String> employees = eval.extractEmployees(employeesModel);
            HashMap<String, String> jids = eval.createJIDsHash(employees);
            // create Jabber accounts for the employees 
            //eval.createAccounts(jids);
            
            // extract posts
            Model postsModel = eval.extractNTriples(postsFileName);
            ArrayList<String> posts = eval.extractPosts(postsModel);
            
//            ArrayList<LeafNode> nodes = new ArrayList<LeafNode>();
//            ArrayList<String> nodenames = new ArrayList<String>();
            HashMap<String, String> postsHash = eval.createNodesHash(postsModel, posts); 
            // publish posts
            eval.publishPosts(postsHash);
                
            // subscribe
            ArrayList<String> nodeNames = new ArrayList<String>(postsHash.keySet());
            eval.subscribeToNodes(jids, nodeNames);
            
            
        } catch(Exception e) {
            e.printStackTrace();
            logger.debug(e);
        }
    }
}
