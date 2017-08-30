/**
 * CMPE 206 : Project Part 1
 * HostInfo.java
 * Purpose: Create a HostInfo class to store the Host info like IP address, port number, date created.
 * @author Anurag Chowdhary
 * @version 1.0 03/03/2017
 */

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

 


public class HostInfo implements Serializable{
 

	private static final long serialVersionUID = 1L;
	private String ipAddr="";
	private String hostName="NotGiven";
	private int portNumber=0;
	private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	private Date date =null;
	private int numberOfConnections=0;
	List<Socket> numOfConnections =null;

	//constructor
	HostInfo(){
		date= new Date();
	}

	//Initialize the socket list containing the connections to this host.
	void createList(){ numOfConnections = new ArrayList<Socket>();}
	
	//If a socket is closed then remove the socket object from the list.
	void RemoveClosedConnections(){
		Iterator<Socket> temp = numOfConnections.iterator();
		while(temp.hasNext()){
			if(temp.next().isClosed())
				temp.remove();
		}
	}
	
	//Close the socket connection if the list contains a socket object with the given IP, port combination. 
	//Clear the list of closed connections.
	void CloseConnection(String ip,int port) throws IOException{

		for(Socket temp: this.numOfConnections){
			
			if(temp.getInetAddress().getHostAddress().equals(InetAddress.getByName(ip).getHostAddress()) && temp.getPort()==port){
				System.out.println("-closing "+temp.getInetAddress().getHostName()+"::"+temp.getInetAddress().getHostAddress()+":"+temp.getPort()+" at local port "+temp.getLocalPort());
				if(!temp.isClosed())
					temp.close();		
				
			}
		}
		this.RemoveClosedConnections();
		
	}

	//Getters
	String GetIpAddr(){return ipAddr;}
	int GetPortNumber(){return portNumber;}
	String GetDate(){return dateFormat.format(date);}
	String getHostName() {return hostName;}
	int getNumberOfConnections() {return numberOfConnections;}
	
	//Setters
	void SetIpAddr(String addr){ipAddr=addr;}
	void SetPortNumber(int num){portNumber=num;}
	public void setHostName(String hostName) {this.hostName = hostName;}
	void setNumberOfConnections(int numOfConn) {numberOfConnections = numOfConn;	}

	
	public String Print() {
		String str="";
		str +="\nHOSTNAME: "+ hostName +"\nIP:"+ipAddr+ "\nPORT: "+portNumber+"\nDate Created: "+this.GetDate()+"\nNumber ofConnections:"+numberOfConnections+"\n";
		
		return str;
		
	}
	
	//Return a string containing all active connections for this host.
	public String GetActivePorts() throws SocketException{
		String activeConn="";
		if(!numOfConnections.isEmpty())
			for(Socket s : numOfConnections){
				if(!s.isClosed()){
					activeConn+= s.getInetAddress().getHostName()+"::"+ s.getInetAddress().getHostAddress()+":"+s.getPort()+" ,from Local port: "
							+s.getLocalPort()+" keepalive="+s.getKeepAlive()+"\n* ";
				}
			}
		
		return activeConn;
	}
		
}
