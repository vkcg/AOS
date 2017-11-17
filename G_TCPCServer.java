import java.net.*;  
import java.io.*;
import java.util.*;
public class G_TCPCServer {
	
	public static void main (String args[]) {
		//arg[0] is server port arg[1] is shop host address
		try{
			//port of server
			int serverPort = Integer.parseInt(args[0]);
			ServerSocket listenSocket = new ServerSocket(serverPort);  
				while(true) {
					Socket clientSocket = listenSocket.accept();  
					Connection c = new Connection(clientSocket,args);
				}
		} 
		catch(IOException e){
						System.out.println("Listen :"+e.getMessage());
		}
	}
}

class Connection extends Thread {  
	DataInputStream in;  
	DataOutputStream out; 
	Socket clientSocket;
	String arg1[];
	public Connection (Socket aClientSocket, String arg[]) {  
		try {
			clientSocket = aClientSocket;
			arg1 = arg;
			in = new DataInputStream( clientSocket.getInputStream());
			out =new DataOutputStream( clientSocket.getOutputStream());  
			this.start();
			//clientSocket.close();
		} 
		catch(IOException e) {
			System.out.println("Connection:"+e.getMessage());
		}
	}
	public void run(){
		try {	// an echo server
			String pass,user;
			int id,points;
			String usr = in.readUTF();
			System.out.println("Username received: " + usr );
			String pwd = in.readUTF();
			System.out.println("Password received: " + pwd);
			int poi = Integer.parseInt(in.readUTF());
			System.out.println("Points received: " + poi);
			int flag = 0;
			String status = null;
			//for locking
			synchronized (G_TCPCServer.class)
			{
				//authenticate user
				FileReader file = new FileReader("login.txt");
				BufferedWriter writer = new BufferedWriter(new FileWriter("login2.txt"));
				Scanner input=new Scanner(file);
				input.useDelimiter(" |\r\n"); //delimitor is one or more spaces
				while(input.hasNext()){
					id = Integer.parseInt(input.next());
					user = input.next();
					pass = input.next();
					points = Integer.parseInt(input.next());
					//writer.write(id +" "+ user + " " + pwd + " " + points + "\r\n");
	 				//System.out.println(id +"\n"+ user + "\n" + pwd + "\n" + points);
	 				if(usr.equals(user) && pwd.equals(pass)){
						flag=1;
						//p = Integer.toString(points);
						System.out.println("User Authenticated");
						points = poi;
					}
					writer.write(id +" "+ user + " " + pwd + " " + points + "\r\n");
				}
				writer.close();
				file.close();
				File inputFile = new File("login.txt");
				File outFile = new File("login2.txt");
					
				if(inputFile.delete()){
					  outFile.renameTo(inputFile);
				}
			}
			
			if(flag == 1){
				out.writeUTF("yes");
				Socket s = null;
				int port=1111;
				if(poi>0 && poi<=499)
				{
					port = 1111;
					status = "silver";
				}
				else if(poi>=500 && poi<1000)
				{
					port = 2222;
					status = "gold";
					System.out.println("port :" +port);
				}
				else if(poi>= 1000)
				{
					status = "platinum";
					port = 3333;
				}
				try{
					//tcp connecion with shop server
					//arg[1] is shopserver ip address
					s = new Socket(arg1[1], 4444);
					DataOutputStream out1 = new DataOutputStream( s.getOutputStream());
					DataInputStream in1 = new DataInputStream( s.getInputStream());
					//write to shop if client is silver gold or platinumn 
					out1.writeUTF(status);
					String shopip = in1.readUTF();
					System.out.println("Shop ip: "+shopip);
					out.writeUTF(shopip);
					out.writeUTF(Integer.toString(port));
				}
				catch (UnknownHostException e){  
					System.out.println("Sock:"+e.getMessage());
				}
				catch (EOFException e){
					System.out.println("EOF:"+e.getMessage());
				}
				catch (IOException e){
					System.out.println("IO:"+e.getMessage());}
				finally {
					if(s!=null) {
						try {
							System.out.println("Group Server: closing Shop socket");
							s.close();
						}
						catch (IOException  e){
							System.out.println("close:"+e.getMessage());
						}
					}
						
				}
				
			}
			else
			{
				out.writeUTF("no");
			}
			
		} 
		catch(EOFException e) {
			System.out.println("EOF:"+e.getMessage());
		} 
		catch(IOException e) {
			System.out.println("IO:"+e.getMessage());
		}
		finally{ 
			try {
				System.out.println("closing socket");
				clientSocket.close();
			}
			catch (IOException e){/*close failed*/}
		}
	}
}


