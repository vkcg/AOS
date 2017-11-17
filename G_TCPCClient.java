import java.net.*;  
import java.io.*;
import java.util.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
class Item implements java.io.Serializable {
	public String name;
	public int id,q;
	public double price;
}
public class G_TCPCClient {
	public static void main (String args[]) {
		// arguments supply message and hostname of destination  
		System.out.println(args[0]);
		Socket s = null;
		InetAddress ip;
		String hostip = null;
		/*try {
            ip = InetAddress.getLocalHost();
            //hostname = ip.getHostName();
            System.out.println("Your current IP address : " + ip);
            hostip = ip.getHostAddress() ;
            System.out.println("Your current Hostip : " + hostip);
 
        } catch (UnknownHostException e) {
 
            e.printStackTrace();
        }*/
		Scanner sc = new Scanner(System.in);
		String usr,user,pwd;
		String p = null;
		int points;
		//int flag=0;
		//user and password input
		System.out.print("Enter User:");
		usr = sc.nextLine();
		System.out.print("Enter Password:");
		pwd = sc.nextLine();
		//System.out.println(usr);
		try{
			int serverPort = Integer.parseInt(args[0]);
			int epoints;
			s = new Socket(args[1], serverPort);
			DataInputStream in = new DataInputStream( s.getInputStream());  
			DataOutputStream out = new DataOutputStream( s.getOutputStream());
			//using synchronized block for locking
			synchronized (G_TCPCClient.class)
			{
				//finding user and displaying points from text file
				FileReader f = new FileReader("users.txt");
				Scanner input = new Scanner(f);
				while(input.hasNext()){
					user = input.next();
					points = Integer.parseInt(input.next());
					if(usr.equals(user)){
						//flag=1;
						p = Integer.toString(points);
						System.out.println("User found");
						System.out.println("points: " + points);
					}
				}
				
				f.close();
					
			}  
			//send user, password and points to middle server
			out.writeUTF(usr);	// UTF is a string encoding 
			out.writeUTF(pwd);
			if(p==null)
			{
				p="0";
			}
			out.writeUTF(p);
			//receiving from middle server and displaying login message
			String data = in.readUTF();
			if(data.equals("yes"))
			{
				System.out.println("Login Successfull") ;
				//
				String shopip = in.readUTF();
				int shopport = Integer.parseInt(in.readUTF());
				System.out.println("shopip:"+shopip+" shop port:"+shopport);
				DatagramSocket aSocket = null;
				//udp connection with shop server
				try{
					aSocket = new DatagramSocket();
					String s1 = "hey";
					byte [] m = s1.getBytes(); 
					InetAddress aHost = InetAddress.getByName(shopip);
					System.out.println("aHost:"+aHost);
					DatagramPacket request = new DatagramPacket(m, m.length, aHost, shopport);
					aSocket.send(request);
					//
					
					byte[] buffer = new byte[1000];
					String choice = "yes";
					do{
						DatagramPacket que = new DatagramPacket(buffer, buffer.length);
						aSocket.receive(que);
						String q1 = new String(buffer, 0, que.getLength());
						System.out.println("file received : "+q1);
						//
						//shopping code
						ArrayList<Item> itm = new ArrayList<Item>();
						Item i1 = new Item();
						//for locking
						synchronized (G_TCPCClient.class)
						{
							try {
							FileInputStream fileIn = new FileInputStream(q1);
							ObjectInputStream sin = new ObjectInputStream(fileIn);
							itm = (ArrayList)sin.readObject();
							sin.close();
							fileIn.close();
							for (Item x : itm) {
								System.out.println("id: " + x.id +" Name: " + x.name +" quantity: " + x.q +" Price: " + x.price);
							} 
							}
							catch(IOException e) {
								e.printStackTrace();
								return;
							}
							catch(ClassNotFoundException c) {
								System.out.println("class not found");
								c.printStackTrace();
								return;
							}
						}
						//select item and quantity
						System.out.println("enter id");
						String id = sc.nextLine();
						System.out.println("enter quantity");
						String quantity = sc.nextLine();
						System.out.println("item id: "+id+" of quantity - "+quantity);
						//send id
						m = new byte[1000];
						m = id.getBytes();
						request = new DatagramPacket(m, m.length, aHost, shopport);
						aSocket.send(request);
						//send quantity
						m = new byte[1000];
						m = quantity.getBytes();
						request = new DatagramPacket(m, m.length, aHost, shopport);
						aSocket.send(request);
						//check success or not
						buffer = new byte[1000];
						DatagramPacket msg = new DatagramPacket(buffer, buffer.length);
						aSocket.receive(msg);
						String op = new String(msg.getData(), 0, msg.getLength());
						System.out.println("Shopping is : "+ op);
						if(op.equals("valid"))
						{
							//display total price and poins
							buffer = new byte[1000];
							DatagramPacket pricemsg = new DatagramPacket(buffer, buffer.length);
							aSocket.receive(pricemsg);
							String totalprice = new String(pricemsg.getData(), 0, pricemsg.getLength());
							System.out.println("Price is : "+ totalprice);
							buffer = new byte[1000];
							DatagramPacket pointmsg = new DatagramPacket(buffer, buffer.length);
							aSocket.receive(pointmsg);
							String extrapoints = new String(pointmsg.getData(), 0, pointmsg.getLength());
							System.out.println("Extra points : "+ extrapoints);
							epoints = Integer.parseInt(extrapoints);//for locking
							synchronized (G_TCPCClient.class)
							{
								//update new points
								FileReader f1 = new FileReader("users.txt");
								Scanner input1 = new Scanner(f1);
								BufferedWriter writer = new BufferedWriter(new FileWriter("users2.txt"));
								while(input1.hasNext()){
									user = input1.next();
									points = Integer.parseInt(input1.next());
									if(usr.equals(user)){
										points= points + epoints;
										System.out.println("new points :" +points);
									}
									writer.write(user +" "+ points + "\r\n");
								}
								writer.close();
								f1.close();
								File inputFile = new File("users.txt");
								File outFile = new File("users2.txt");
									
								if(inputFile.delete()){
									  outFile.renameTo(inputFile);
								}  
								}
							
						}
						//end of shop code
						buffer = new byte[1000];
						que = new DatagramPacket(buffer, buffer.length);
						aSocket.receive(que);
						q1 = new String(que.getData(), 0, que.getLength());
						System.out.println("Continue: "+ q1);
						System.out.println("enter yes or no ");
						choice = sc.nextLine();
						System.out.println("choice is "+choice);
						m = new byte[1000];
						m = choice.getBytes();
						request = new DatagramPacket(m, m.length, aHost, shopport);
						aSocket.send(request);
					}while(choice.equals("yes"));
							
							
				}
				catch (SocketException e){System.out.println("Socket: " + e.getMessage());
				}catch (IOException e){System.out.println("IO: " + e.getMessage());}
				finally {if(aSocket != null) System.out.println("closing shop socket");aSocket.close();}
		}
		else{
				System.out.println("Login failed") ;
		}
		}
		catch (UnknownHostException e){
			System.out.println("Sock:"+e.getMessage());
		}
		catch (EOFException e){
			System.out.println("EOF:"+e.getMessage());
		}
		catch (IOException e){
			System.out.println("IO:"+e.getMessage());
		}
		finally {
			if(s!=null) 
				try {
					System.out.println("closing group socket");
					s.close();
				}
				catch (IOException  e){
					System.out.println("close:"+e.getMessage());
				}
		}
	}
}
