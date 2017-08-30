/*
 * Test for Command line arguments. 
 * Call the SocketServer class object which handles servr and console threads.
 * */

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

public class MasterBot {

	static List<HostInfo> slaveList=null; // To maintain list of slaves connected to master.
	static ServerSocket serverSocket =null; // To store the Master ServerSocket object
	static boolean isServerActive=true; // True if server is active else false.
	
	 
	
	//start of MasterBot main method
	public static void main(String[] args) {
		
    	int errCode=0;
    	String firstArg="";
    	int portNumber=10000;

    	
    	/* Test for correct args values -p portNumber
    	 */
    	try {
    		if(args.length==2) {
    			firstArg = args[0];
    			if(firstArg.equals("-p")) {   	   				
    				portNumber = Integer.parseInt(args[1]);// Throws NumberFormatException if second argument is not a valid number string.   				
    		    } 
    			else{
    				System.err.println("Exiting... \n******* Wrong 1st argument");
    				System.exit(-1);
    			}
    		}
    		else
    		{
    			System.err.println("Exiting... \n******* Correct arguments are: -p portNumber"); // if args[] does not contain 2 arguments.
    			System.exit(-1);
    		}
    	}   	
    	catch (NumberFormatException e) {   
	        System.err.println("Exiting... \n******* 2nd argument '" + args[1] + "' must be a valid port number.");// If user entered non integer value for second argument.	       
	        System.exit(errCode++);
	    }// End of Try - catch block of checking arguments	
    	
    	
    	// check to see if port number is available
    	checkPortNumber(portNumber); 

    	
    	System.out.println("Examples of correct commands:\n*******************");
    	System.out.println("* list");
    	System.out.println("* connect 127.0.0.1 www.google.com 80 2 || connect 127.0.0.1 www.google.com 80 ||\n* "
    			+ "connect 127.0.0.1 www.google.com 80 2 url=https://www.google.com/#q=YowurRandomString || connect 127.0.0.1 www.google.com 80 2 keepalive" );
    	System.out.println("* disconnect 127.0.0.1 www.sjsu.edu 80 || disconnect 127.0.0.1 www.sjsu.edu");
    	System.out.println("* ipscan localhost 127.0.0.1-127.0.0.5 || ipscan localhost 216.58.216.130-216.58.216.135");
    	System.out.println("* tcpportscan all www.google.com 1-100");
    	System.out.println("* geoipscan localhost 127.0.0.1-127.0.0.5 || geoipscan all 216.58.216.130-216.58.216.135");
    	System.out.println("* exit - This command will stop the server and exit MasterBot.\n * exit all - This command closes the master server as well as all slave servers\n*******************");
    	
    	slaveList = new ArrayList<HostInfo>();
    	
    	
    	SocketServer server = new SocketServer(); 
    	server.runServer(portNumber);

	}// End of MasterBot Main method
	
	
	/*Start of checkPortNumber method.
	 *This method will try to create a temporary server socket with the given port. 
	 *If unable to create a server socket, means the port is unavailable.
	 */
	private static void checkPortNumber(int portNumber){
    	boolean portTaken = false;
        ServerSocket portTest = null;
        
        try {
        	portTest = new ServerSocket(portNumber); // Throw IO exception if port is in use.
        } catch (IOException e) {
            portTaken = true;
            
        } finally {
            if (portTest != null) // close the testPort socket
                try {
                	portTest.close();	      
                } catch (IOException e) { 
                	System.err.println("******* Unable to close test PortNumber Socket");
                	System.exit(-1);
            	        
                }
            if(portTest == null && portTaken)
            {
            	System.err.println("\nExiting... \n******* Port Number '"+portNumber+"' is in use by another process");
            	System.exit(-1);
            }
        }// End of Try-Catch-Finally block
        
    }// End of checkPortNumber method
	


}// End of BasterBot class
