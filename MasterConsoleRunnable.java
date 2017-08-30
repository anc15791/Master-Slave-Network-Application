/*
 * This runnable class runs the console thread.
 * Accept user commands and process them according to logic.
 * */

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class MasterConsoleRunnable implements Runnable{
	
	public void run(){
		Scanner sc = new Scanner(System.in);
		while(true){
			System.out.print(">");	
			String command = sc.nextLine();
			
			//If user wants to exit from server.
			if(command.contains("exit"))
			{
				if(command.equals("exit all")){
					MasterSlaveProtocol exit = new MasterSlaveProtocol();
					exit.SetMessageType(10);
					exit.GetSlave().SetIpAddr("all");
					SendMessageToSlave(exit);
					
				}
				
				try{
				System.out.println("Exiting console...");				
				MasterBot.isServerActive=false;
				if(!MasterBot.serverSocket.isClosed())
					MasterBot.serverSocket.close();
				sc.close();
				Thread.currentThread().interrupt();
				return;
				}catch(IOException e){
					System.out.println("******* Unable to close Master Server Socket.\n");					
				}				
			}
			
			if(command.contains("list") ||command.contains("connect") || command.contains("geoipscan")
					|| command.contains("disconnect") || command.contains("ipscan") || command.contains("tcpportscan") ){
				ExecuteCommand(command);
			}
			else{
				command="";
				System.out.println("**** Invalid command. Use list, connect, disconnect, ipscan or exit command only.");
			}
		}
		
	}
	
	//Method to handle incoming commands.
	private static void ExecuteCommand(String input) {
		
		//Remove all slaves which have their ip="" - Those slaves with whom the master is unable to create a connection.
		for(int i=0;i<MasterBot.slaveList.size();i++){
			if(MasterBot.slaveList.get(i).GetIpAddr().equals("")){
				MasterBot.slaveList.remove(i);
				//System.out.println("removing "+i);
			}
			
		}
		
		//List
		if(input.equals("list")){
			ExecuteList();
			return;
		}	
		
		
		String[] command=input.split("\\s+");		
		
		//Check- if user supplied incorrect connect or disconnect command.
		if(command.length<2){
			
			System.out.println("**** Invalid command.");
			return;
		}
			
		//If user supplied the slave name instead of IP address, get the ip address of slave first.
		String ip = command[1];
		if(ip.contains(".") ){
	        String[] parts = ip.split( "\\." );
	        if ( parts.length != 4 ) {
	        	ip=getIPFromName(ip);
	        }
		}
		else{
			ip=getIPFromName(ip);
		}
		
		
		// Creating message to be sent as connect or disconnect.
    	MasterSlaveProtocol outMessage=new MasterSlaveProtocol();    	
    	outMessage.GetSlave().SetIpAddr(ip); // set IP address field of command to outMessage slave IP
		
		
		//Connect
    	if(command[0].equals("connect"))
    	{	
    		if(command.length<4 || command.length>=7){
    			System.out.println("**** Invalid connect command.");
    			return;
    		}
    		
    		outMessage.GetHost().SetIpAddr(command[2]); // set host IP.
    		outMessage.SetMessageType(3); // set message type, connect=3
    		outMessage.GetHost().createList();
    		outMessage.GetHost().SetPortNumber(Integer.parseInt(command[3]));//assign host portNumber
    		
    		//for connect 127.0.0.1 www.google.com 80
    		if(command.length==4){
    			outMessage.GetHost().setNumberOfConnections(1);
    			outMessage.SetMessage("No option given");
    		}
    		else if(command.length>4){
    			try
    		    {
    		        Integer.parseInt(command[4]);
    		        outMessage.GetHost().setNumberOfConnections(Integer.parseInt(command[4])); // This will execute if number of connections is given.
    		        if(command.length==6)
    		        	outMessage.SetMessage(command[5]);
    		        else
    		        	outMessage.SetMessage("No option given");
    		        
    		    } catch (NumberFormatException ex) // This will execute if number of connection is not given but keepalive or url is given.
    		    {
        			outMessage.GetHost().setNumberOfConnections(1);
        			outMessage.SetMessage(command[4]);
    		    }
    		}

    		
    		SendMessageToSlave(outMessage);
     		return;
    		
    	}//END of if connect
    	
    	//Disconnect
    	if(command[0].equals("disconnect"))
    	{
    		if(command.length<3 || command.length>4){
    			System.out.println("**** Invalid disconnect command.");
    			return;
    		}
    		
    		outMessage.GetHost().SetIpAddr(command[2]); // set host IP.
    		outMessage.SetMessageType(4); // 4=disconnect
    		//TargetPort, all if no port specified(-1)  	
    		if(command.length==4)
    			outMessage.GetHost().SetPortNumber(Integer.parseInt(command[3]));
    		else
    			outMessage.GetHost().SetPortNumber(-1);
    		

    		SendMessageToSlave(outMessage);
    	    return;
    		
    	}//END of if disconnect
    	
    	//IPScan
    	if(command[0].equals("ipscan")){

    		if(command.length<3 || command.length>3){
    			System.out.println("**** Invalid ipscan command.");
    			return;
    		}
    		outMessage.GetHost().SetIpAddr(command[2]);// sets the IP address range.
    		outMessage.SetMessageType(6); // 6=ipscan
    		SendMessageToSlave(outMessage);
    		return;
    	}
    	
    	//TctPortScan
    	if(command[0].equals("tcpportscan")){
    		
    		if(command.length<4 || command.length>4){
    			System.out.println("**** Invalid tcpportscan command.");
    			return;
    		}
    		outMessage.GetHost().SetIpAddr(command[2]);
    		outMessage.SetMessage(command[3]);
    		outMessage.SetMessageType(8); // 8=tcp port scan
    		SendMessageToSlave(outMessage);
    		return;
    	}
	
    	//Geo Ip scan
    	if(command[0].equals("geoipscan")){

    		if(command.length<3 || command.length>3){
    			System.out.println("**** Invalid ipscan command.");
    			return;
    		}
    		outMessage.GetHost().SetIpAddr(command[2]);// sets the IP address range.
    		outMessage.SetMessageType(11); // 6=ipscan
    		SendMessageToSlave(outMessage);
    		return;
    	}
    	
	}//END of ExecuteCommand
	
	

	/*
	 * Method to send connect or disconnect message to a given slave IP or all slaves.
	 * */
	private static void SendMessageToSlave(MasterSlaveProtocol outMessage) {
		
		try{
			//Send message to all slaves connected to master if "all" is given in command else send to specific slave in the master slave list.
			if(outMessage.GetSlave().GetIpAddr().equals("all")){
				
				for(int i=0;i< MasterBot.slaveList.size() && MasterBot.slaveList.get(i)!=null ;i++){
					outMessage.GetSlave().SetIpAddr(MasterBot.slaveList.get(i).GetIpAddr()); // set slave IP in outMessage.
					outMessage.GetSlave().SetPortNumber(MasterBot.slaveList.get(i).GetPortNumber()); // set slave port in outMessage
					CreateSocket(MasterBot.slaveList.get(i).GetIpAddr(),MasterBot.slaveList.get(i).GetPortNumber(),outMessage);								
				}
			}
			else{		
				
				for(HostInfo temp:MasterBot.slaveList){	
					if(temp!=null && temp.GetIpAddr().equals(outMessage.GetSlave().GetIpAddr())){
						
						outMessage.GetSlave().SetPortNumber(temp.GetPortNumber()); // Set slave port in outMessage. 
						CreateSocket(temp.GetIpAddr(),temp.GetPortNumber(),outMessage);						
					}
				}						
			}		
		
		}catch (IOException e) {
			e.printStackTrace();
		}
		catch(IndexOutOfBoundsException e){
			
			e.printStackTrace();
		}
		
	}//END of SendMessageToSlave
	
	
	//Create a socket for the given IP and port number to send outMessage and close the socket. 
	private static void CreateSocket(String ip, int port, MasterSlaveProtocol outMessage) throws IOException{
	   	ObjectOutputStream oos = null;
	   	Socket slaveSocekt;
	   	try{
	   		slaveSocekt= new Socket(InetAddress.getByName(ip),port);
	   	
	   		oos = new ObjectOutputStream(slaveSocekt.getOutputStream());
			//System.out.println("creating socket to "+InetAddress.getByName(outMessage.GetSlave().GetIpAddr()).toString()+" :"+port
				//+" and sending "+outMessage.GetMessageType(outMessage.GetMessageType())+" message.");
			oos.writeObject(outMessage);
			slaveSocekt.close();
			oos.close();
	   	}catch(ConnectException e){
	   		System.out.println("A slave is down, removing from list :"+ip+"::"+port);
	   		SetToDefault( ip,  port);
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
	}//END of CreateSocket.
	
	//Set values of a slave in the list to its default values. This is used so that this slave can be removed from the list during next command execution.
	private static void SetToDefault(String ip, int port) {
			
		for(int i=0;i<MasterBot.slaveList.size();i++){
			if(MasterBot.slaveList.get(i).GetIpAddr().equals(ip) && MasterBot.slaveList.get(i).GetPortNumber()==port){
				MasterBot.slaveList.get(i).setHostName("");
				MasterBot.slaveList.get(i).setNumberOfConnections(0);
				MasterBot.slaveList.get(i).SetIpAddr("");
				MasterBot.slaveList.get(i).SetPortNumber(0);
				
			}
		}
	}

	//Method to print the list of slaves
	private static void ExecuteList() {
		
		if(!MasterBot.slaveList.isEmpty())
			for(HostInfo h: MasterBot.slaveList){
				System.out.println(h.getHostName()+" "+  h.GetIpAddr()+" "+h.GetPortNumber()+" "+h.GetDate());
		}
		else
			System.out.println("No slave added");
		
	}// END of ExecuteList
	
	//Method to return Ip address for the given hostname of slave
	private static String getIPFromName(String nameToIp) {
		if(nameToIp.equals("all"))
			return nameToIp;
		
		for(HostInfo h:MasterBot.slaveList){
			if(h.getHostName().equals(nameToIp)){
				nameToIp = h.GetIpAddr();
				break;
			}
				
		}		
		return nameToIp;
	}

	
}//END of MasterConsoleRunnable class
