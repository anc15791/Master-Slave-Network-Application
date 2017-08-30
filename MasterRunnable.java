/*
 * This Runnable class handles the Master Server operations like starting the serverSocekt.accept()
 * Listen for incoming slave connection request message. 
 * Adding a new slave to the slave list of Master.
 * */

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;

public class MasterRunnable implements Runnable {

	private Socket clientSocket = null;
	private ObjectInputStream ois = null;
	private ObjectOutputStream oos = null;
	private MasterSlaveProtocol inMessage=null;
	private MasterSlaveProtocol outMessage=null;

	//constructors
	public MasterRunnable(){
		MasterBot.isServerActive=true;
	}

	
	public void run(){

		try{
			
			while(true){
				
				clientSocket=MasterBot.serverSocket.accept();
				
				ois = new ObjectInputStream(clientSocket.getInputStream());
				oos = new ObjectOutputStream(clientSocket.getOutputStream());

				inMessage = (MasterSlaveProtocol)ois.readObject();
				
				//Handle the incoming message 
				if (inMessage!=null){
					IncommingMessageHandler(inMessage);
				}
				
				
				//Sending acknowledge to slave for successful inmessage received.
				outMessage=new MasterSlaveProtocol() ;
				if(inMessage!=null){
					outMessage.SetMessageType(1);
					outMessage.SetMessage(inMessage.GetMessageType(inMessage.GetMessageType())+ " Message recieved at master");					
					oos.writeObject(outMessage);
				}
				
				// If user have exited console, then shutdown the server.
				if(!MasterBot.isServerActive){
					System.out.println("server...Exiting");
					Thread.currentThread().interrupt();
					clientSocket.close();
					return;
				}
				
				inMessage=null;

			}// END of while
			
		}catch(IOException e){
			System.err.print("\n server...Closing\n>");
			//e.printStackTrace();
		} catch (ClassNotFoundException e) {	
			System.err.print("\n ****** Master is unable to read the message sent by the slave: \n       Please check check MasterSlaveProtocol.java class is compiled and stored in same directory. \n>");
			
		}
		
	}//END of RUN

	//Method to handle incoming Slave messages
	private static void IncommingMessageHandler(MasterSlaveProtocol slaveMessage) {
		
		if(slaveMessage.GetMessageType()==5){
			
			AddSlave(slaveMessage);
			//System.out.print("\nserver...Slave Created and added to slaveList in master\n>");		
		}
		
		if(slaveMessage.GetMessageType()==7){
			//System.out.println("\nserver...ipscan Reply recieved.");
			ipScanReply(slaveMessage);			
		}
		
		if(slaveMessage.GetMessageType()==9){
			//System.out.println("\nserver...tcp port scan Reply recieved.");
			TCPPortScanReply(slaveMessage);			
		}
		
		if(slaveMessage.GetMessageType()==12){
			//System.out.println("\nserver...ipscan Reply recieved.");
			geoIpScanReply(slaveMessage);			
		}
		
		if(slaveMessage.GetMessageType()==0){
			//System.out.println("\nserver...ipscan Reply recieved.");
			updateSlave(slaveMessage);			
		}
		
	}//END of IncommingMessageHandler
		
	private static void updateSlave(MasterSlaveProtocol slaveMessage) {
		
		for(HostInfo h: MasterBot.slaveList){
			if(h.GetIpAddr().equals(slaveMessage.GetSlave().GetIpAddr()) && h.GetPortNumber() == slaveMessage.GetSlave().GetPortNumber() ){
				h.SetIpAddr(slaveMessage.GetHost().GetIpAddr());
				h.SetPortNumber(slaveMessage.GetHost().GetPortNumber());
				break;
			}
			
		}
		
	}


	private static void geoIpScanReply(MasterSlaveProtocol slaveMessage) {
		System.out.println("");
		if(!slaveMessage.scan.isEmpty()){
			for(int i=0;i<slaveMessage.scan.size()-1;i++){
				System.out.print(slaveMessage.scan.get(i));
				System.out.println("");
				if(i%5==0 && i>0)
					System.out.print("\n");
			}
			System.out.print(slaveMessage.scan.get(slaveMessage.scan.size()-1));		
			}
		else
			System.out.print("...no ip responded in the range.");
		System.out.print("\n>");
		
	}


	//Print TCP Port Scan Reply from slave
	private static void TCPPortScanReply(MasterSlaveProtocol slaveMessage) {
		
		/*
		System.out.println("********** TCP Port Scan Reply from "+slaveMessage.GetSlave().getHostName()+":"+slaveMessage.GetSlave().GetPortNumber()+" **********");
		//String ip[] = slaveMessage.GetMessage().split("\\s,");
		System.out.println("Following Ports are alive : ");
		if((slaveMessage.GetMessage().contains(",")))
			System.out.println(slaveMessage.GetMessage());
		else
			System.out.println("No ip responded to ICMP: "+slaveMessage.GetMessage());
		
		
		System.out.print("*****************************END******************************\n>");
		*/
		
		System.out.println("");
		if(!slaveMessage.scan.isEmpty()){
			for(int i=0;i<slaveMessage.scan.size()-1;i++){
				System.out.print(slaveMessage.scan.get(i));
				System.out.print(", ");
				if(i%5==0 && i>0)
					System.out.print("\n");
			}
			System.out.print(slaveMessage.scan.get(slaveMessage.scan.size()-1));
			
		}
		else
			System.out.println("...no ip responded in the range");
		System.out.print("\n>");
		
	}


	//Print ipScanReply recieved from a slave.
	private static void ipScanReply(MasterSlaveProtocol slaveMessage) {
		
		//System.out.println("********** ip Scan Reply from "+slaveMessage.GetSlave().getHostName()+":"+slaveMessage.GetSlave().GetPortNumber()+" **********");
		//String ip[] = slaveMessage.GetMessage().split("\\s,");
		//System.out.println("Following IPs responed to ICMP echo:");
		System.out.println("");
		if(!slaveMessage.scan.isEmpty()){
			for(int i=0;i<slaveMessage.scan.size()-1;i++){
				System.out.print(slaveMessage.scan.get(i));
				System.out.print(", ");
				if(i%5==0 && i>0)
					System.out.print("\n");
			}
			System.out.print(slaveMessage.scan.get(slaveMessage.scan.size()-1));		
			}
		else
			System.out.print("...no ip responded in the range.");
		System.out.print("\n>");
		
		
		
		//if((slaveMessage.GetMessage().contains(",")))
			//System.out.println(slaveMessage.GetMessage());
		//else
			//System.out.println("No IPs alive: "+slaveMessage.GetMessage());
		
		
		//System.out.print("****************************END******************************\n>");
	}


	//Method to add a new slave to the slaveList
	private static void AddSlave(MasterSlaveProtocol slaveMessage) {			
		MasterBot.slaveList.add(slaveMessage.GetSlave());			
	}// END of  AddSlave method
	
}//END of MasterRunnable class
