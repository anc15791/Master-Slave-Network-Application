/**
 * CMPE 206 : Project Part 1
 * MasterSlaveProtocol.java
 * Purpose: Create a MasterSlaveProtocol class that will be used by both master and slave to communicate and pass object of this class as message to each other.
 * @author Anurag Chowdhary
 * @version 1.0 03/03/2017
 */

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MasterSlaveProtocol implements Serializable{


	private static final long serialVersionUID = 1L;
	// 0-UPDATE: To be used when slave wants to update its information in the master.
	// 1-ACK:for the sequence number in ackOfSequence, Message has success message
	// 2-ERR : Message has error message, ackOfSequence has for which command sequence this error belongs to
	// 3-connect: Message has connect message, 
	// 4- disconnect:
	// 5- NewSlave REQ : slave has the obejct of HostInfo
	// 6- ipscan : scan the range of ip addresses using ICMP echo.
	// 7- ipscanReply : reply to ipscan , message contains the string that replied to ipscan and slave contains the information of which slave sent the message. 
	// 8- tcpportscan : Scan the range of tcp port for given host ip and return the ports that are open for tcp connection.
	// 9- tctportscanReply: Reply to tcpportscan, message contains the string of available ports and slave details that generated the list.
	
	private int messageType=0; 
	private HostInfo slave=null;
	private HostInfo host=null;
	private String message="";
	private static int sequence=0;
	private int ackOfSequence=0;

	List<String> scan = new ArrayList<String>();

	
	String GetMessageType(int type){
		String message="";
		if(messageType==0)
			message="UPDATE";
		if(messageType==1)
			message="ACK";
		if(messageType==2)
			message="ERR";
		if(messageType==3)
			message="CONNECT";
		if(messageType==4)
			message="DISCONNECT";
		if(messageType==5)
			message="New Slave REQ";	
		if(messageType==6)
			message="IP Scan";
		if(messageType==7)
			message="IP Scan Reply";
		if(messageType==8)
			message="TCP Port Scan";
		if(messageType==9)
			message="TCP Port Scan Reply";
		if(messageType==10)
			message="Exit";
		if(messageType==11)
			message="Geo IP scan  Scan";
		if(messageType==12)
			message="Geo IP scan Reply";
		return message;
	}
	
	//constructor
	MasterSlaveProtocol(){
		slave=new HostInfo();
		host = new HostInfo();
		sequence++;
	}
	
	//getters
	int GetMessageType(){return messageType;}
	HostInfo GetSlave(){return slave;}
	HostInfo GetHost(){return host;}
	String GetMessage(){return message;}
	int GetSequence(){return sequence;}
	int GetAckOfSequence(){return ackOfSequence;}

	
	//setters
	void SetMessageType(int type){messageType=type;}
	void SetSlave(HostInfo s){slave=s;}
	void SetHost(HostInfo h){host=h;}
	void SetMessage(String str){message=str;}
	void SetAckOfSequence(int seq){ackOfSequence=seq;}


	public String Print() {
		String str="";
		
		str+= "Message type:"+ messageType + "| "+GetMessageType(messageType)+"\nMessage: "+ message;
		if(slave!=null)
			str+="Slave Socket Info: "+slave.Print();
		if(host!=null)
			str+="Host Socket Info: "+host.Print();
		
		return str;
	}




}
