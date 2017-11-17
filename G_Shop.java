import java.net.*;  
import java.io.*;
import java.util.*;
class Item implements java.io.Serializable {
	public String name;
	public int id,q;
	public double price;
}
public class G_Shop {
	
	public static void main (String args[]) {
		try{
			int serverPort = 4444;
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
	public Connection (Socket aClientSocket,String arg[]) {  
		try {
			clientSocket = aClientSocket;
			arg1 = arg;
			in = new DataInputStream( clientSocket.getInputStream());
			out =new DataOutputStream( clientSocket.getOutputStream());  
			this.start();
		} 
		catch(IOException e) {
			System.out.println("Connection:"+e.getMessage());
		}
	}
	public void run(){
		InetAddress ip;
			String myip = null;
			String hostname = null;
			try {
            	ip = InetAddress.getLocalHost();
            	System.out.println("Your current IP address : " + ip);
            	myip = ip.getHostAddress() ;
            	System.out.println("Your current ip : " + myip);
            	hostname = ip.getHostName();
            	System.out.println("Your current Hostname : " + hostname);
 
        	} catch (UnknownHostException e) {
 
            	e.printStackTrace();
        	}
		try {	// an echo server
			
			String msg = in.readUTF();
			System.out.println("msg = " + msg);
			double discount =1;
			
			if(msg.equals("silver"))
			{
				discount = 	0.9;
			}
			else if(msg.equals("gold"))
			{
				discount = 0.8;
			}
			else if(msg.equals("platinum"))
			{
				discount = 0.6;
			}
			else
			{
				discount = 1;
			}
			out.writeUTF(hostname);
			clientSocket.close();
			DatagramSocket aSocket = null;
			try{
				//aSocket = new DatagramSocket(1111);
				//aSocket.close();
				//arg1[0] is ort number
				aSocket = new DatagramSocket(Integer.parseInt(arg1[0]));
				//set timeout for datagram packets
				aSocket.setSoTimeout(10000);
				byte[] buffer = new byte[1000];
				byte[] m = new byte[1000];
				DatagramPacket request = new DatagramPacket(buffer, buffer.length);
				aSocket.receive(request);
				buffer = new byte[1000];
				String msg1 = new String(request.getData(),0,request.getLength());
				System.out.println("cli says: "+ msg1);
				int cport = request.getPort();
				System.out.println("client port: "+ cport);
				InetAddress cipaddr = request.getAddress();
				InetSocketAddress address = new InetSocketAddress(cipaddr, cport);
        		//aSocket.bind(address);
				String ch = null;
				do{
					String f1 = "menu.ser";
					m = new byte[1000];
					m = f1.getBytes();
					//send serilizable file to client
					DatagramPacket ques = new DatagramPacket(m,  m.length, cipaddr, cport);
					aSocket.send(ques);
					System.out.println("Sent file");
					//shop code
					//Receive id
					buffer = new byte[1000];
					request = new DatagramPacket(buffer, buffer.length);
					aSocket.receive(request);
					int itemid = Integer.parseInt(new String(request.getData(),0,request.getLength()));
					System.out.println("Receive id:"+itemid);
					//Receive quantity
					buffer = new byte[1000];
					request = new DatagramPacket(buffer, buffer.length);
					aSocket.receive(request);
					int itemquantity = Integer.parseInt(new String(request.getData(),0,request.getLength()));
					System.out.println("Received quantity:"+itemquantity);
					// update the inventory serializable file
					double totprice = 0;
					ArrayList<Item> itm = new ArrayList<Item>();
					Item i1 = new Item();
					int epoint = 0;
					String extrapoints = null;
					try {
						synchronized (G_Shop.class)
						{
							FileInputStream fileIn = new FileInputStream("menu.ser");
							ObjectInputStream in = new ObjectInputStream(fileIn);
							itm = (ArrayList)in.readObject();
							in.close();
							fileIn.close();
							int flag = 0;
							for (Item i : itm) {
								if(i.id==itemid)
								{
									flag = 1;
									System.out.println("id: " + i.id +" Name: " + i.name +" quantity: " + i.q +" Price: " + i.price);
									if(i.q>=itemquantity)
									{
										//open serializable file for quantity, caluculate price and points
										i.q = i.q-itemquantity;
										totprice = (double)(itemquantity * i.price * discount);
										epoint = (int)totprice/10;
										String val = "valid";
										m = new byte[1000];
										m = val.getBytes();
										request = new DatagramPacket(m, m.length, cipaddr, cport);
										aSocket.send(request);
										String tprice = Double.toString(totprice);
										m = new byte[1000];
										m = tprice.getBytes();
										request = new DatagramPacket(m, m.length, cipaddr, cport);
										aSocket.send(request);
										extrapoints = Integer.toString(epoint);
										m = new byte[1000];
										m = extrapoints.getBytes();
										request = new DatagramPacket(m, m.length, cipaddr, cport);
										aSocket.send(request);
									}
									else{
										String inval = "invalid";
										m = new byte[1000];
										m = inval.getBytes();
										request = new DatagramPacket(m, m.length, cipaddr, cport);
										aSocket.send(request);
									}
									
								}
								
							}
							if(flag == 0)
							{
								String inval = "invalid";
								m = new byte[1000];
								m = inval.getBytes();
								request = new DatagramPacket(m, m.length, cipaddr, cport);
								aSocket.send(request);
							}
							
						}
					}
					catch(IOException i) {
						i.printStackTrace();
						return;
					}
					catch(ClassNotFoundException c) {
						System.out.println("class not found");
						c.printStackTrace();
						return;
					}
					//updating ser file
					try {
						synchronized (G_Shop.class)
						{
							FileOutputStream fileOut = new FileOutputStream("menu.ser");
							ObjectOutputStream out = new ObjectOutputStream(fileOut);
							out.writeObject(itm);
							out.close();
							fileOut.close();
							System.out.printf("Serialized data is saved in menu.ser");
							
						}
					}
					catch(IOException i) {
						i.printStackTrace();
					}
					
					//question if you want to shop
					m = new byte[1000];
					String q1 = "do you want to shop?";
					m = q1.getBytes();
					ques = new DatagramPacket(m,  m.length, cipaddr, cport);
					aSocket.send(ques);
					System.out.println("waiting for reply yes or no ");
					DatagramPacket ans = new DatagramPacket(buffer, buffer.length);
					aSocket.receive(ans);
					ch = new String(ans.getData(),0,ans.getLength());
					System.out.println("choice is "+ch);
				}while(ch.equals("yes"));
				
			}catch (SocketTimeoutException e) {
                // timeout exception.
                System.out.println("Timeout reached!!! " + e);
                //break;
            }catch (SocketException e){System.out.println("Socket: " + e.getMessage());
			}catch (IOException e) {System.out.println("IO: " + e.getMessage());}
			finally {if(aSocket != null) System.out.println("closing client socket");aSocket.close();}


		} 
		catch(Exception e) {
			System.out.println("EOF:"+e.getMessage());
		} 
		/*catch(EOFException e) {
			System.out.println("EOF:"+e.getMessage());
		} 
		catch(IOException e) {
			System.out.println("IO:"+e.getMessage());
		}*/
		finally{ 
			try {
				if(clientSocket != null)
				{
				System.out.println("closing Group server socket");
				clientSocket.close();
				}
			}
			catch(Exception e) {
			System.out.println("EOF:"+e.getMessage());
		} 
			//catch (IOException e){
				//close failed
				//}
		}
	}
}