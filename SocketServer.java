/*This class creates and runs a thread to handle console input output operations.
 * A second thread is created to run the master socket server.
 * 
 * */

import java.io.*;
import java.net.*;


public class SocketServer {

	int portNumber=0;
	Thread masterServer=null;
	Thread console = null;
	
	//constructors
	public SocketServer(){
		MasterBot.isServerActive=true;
	}
	public SocketServer(int port){
		MasterBot.isServerActive=true;
	}
	
	
	public void runServer(int port){
		
		//Create server socket for which master will listen for connections
		try{
			MasterBot.serverSocket=new ServerSocket(port);
			
		
		}catch(IOException e){
			e.printStackTrace();
		}
		
		//Creating separate thread for running console provided to the user.
		console = new Thread(new MasterConsoleRunnable());
		console.start();

		//Creating Separate thread for accepting connection over server socket.
		try{
			masterServer=new Thread(new MasterRunnable());
			masterServer.start();
			Thread.sleep(2000);
				
		}catch(Exception e){
			e.printStackTrace();
			}
		
		
		
		
		}// End of runServer method
	
}
