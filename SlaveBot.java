/*
 * Test for command line arguments
 * create a socket to Master and start Slave server socket thread.
 * */

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class SlaveBot {

	static List<HostInfo> hostList=null;
	static ServerSocket slaveServer =null; 
	static List<HostInfo> hostConnections=null;
	static HostInfo master = new HostInfo();
	static HostInfo thisSlave = new HostInfo();
	public static void main(String[] args) {
		
    	int errCode=0;
    	String firstArg="";
    	String secondArg="";
    	String masterIP = "";
    	int portNumber=10000;
	
    	//Test for correct args values -p portNumber  	 
    	try {
    		if(args.length==4) {
    			
    			firstArg=args[0];
    			if(firstArg.equals("-h")){
    				masterIP = args[1];
    			}
    			else{
    				System.err.println("Exiting... \n******* Wrong 1st argument");
    				System.exit(-1);
    			}
    			
    			secondArg = args[2];   	
    			if(secondArg.equals("-p")) {   	   				
    				portNumber = Integer.parseInt(args[3]);// Throws NumberFormatException if 4th argument is not a valid number string.   				
    		    } 
    			else{
    				System.err.println("Exiting... \n******* Wrong 3rd argument");	
    				System.exit(-1);
    			}
    		}
    		else
    		{
    			System.err.println("Exiting... \n******* Correct arguments are:-h hostName|IP -p portNumber"); // if args[] does not contain 2 arguments.
    			System.exit(-1);
    		}
    	}   	
    	catch (NumberFormatException e) {   
	        System.err.println("Exiting... \n******* 2nd argument '" + args[1] + "' must be a valid port number.");// If user entered non integer value for second argument.	       
	        System.exit(errCode++);
	    }
    	
    	
    	
    	try{
    		
    		master.SetIpAddr(InetAddress.getByName(masterIP).getHostAddress());
    		master.setHostName(InetAddress.getByName(masterIP).getHostName());
    		master.SetPortNumber(portNumber);
    		
    		//System.out.println(master.Print());
    		
    		Socket slave = new Socket(InetAddress.getByName(master.GetIpAddr()),master.GetPortNumber()); // create socket to connect to master for the given IP and port number.
    		MasterSlaveProtocol slaveMessage=null;
        	
    		//HostInfo slaveHost=new HostInfo(); // to store slave information to be sent to master.

    		thisSlave.SetPortNumber(slave.getLocalPort());
    		thisSlave.SetIpAddr(slave.getLocalAddress().getHostAddress());
    		//SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    		//Date date = new Date();
    		thisSlave.setHostName(slave.getLocalAddress().getHostName()) ;

    		//Creating outMessage to send to Master.
    		slaveMessage = new MasterSlaveProtocol();    		
    		slaveMessage.SetMessageType(5); // 5= request for connection
    		slaveMessage.SetSlave(thisSlave);
    		slaveMessage.SetMessage("CONNREQ");
    		slaveMessage.SetHost(null);
      		
    		//Sending outMessage to Master.
        	ObjectOutputStream oos = null;
    		oos = new ObjectOutputStream(slave.getOutputStream());
    		oos.writeObject(slaveMessage);
 		
    		//To accept Master's ACK message for the New Slave REQ message sent by slave.
    		ObjectInputStream ois = new ObjectInputStream(slave.getInputStream());
    		MasterSlaveProtocol inMessage=(MasterSlaveProtocol)ois.readObject();
    		System.out.println(inMessage.GetMessage());
    		
    		int slavePort = slave.getLocalPort();
			slave.close(); // close the slave socket to master
			
			//If successful connection has been setup with the master start the slave server socket thread.
			if(inMessage.GetMessageType()==1){
				Thread t = new Thread(new SlaveRunnable(slavePort,thisSlave.GetIpAddr()));
				t.start();
			}
			else{
				System.out.println("****Exiting slave: Unable to connect to master");
			}
    		
    	}catch(IOException e){
    		e.printStackTrace();
    	} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
    	 
    	

	}

}
