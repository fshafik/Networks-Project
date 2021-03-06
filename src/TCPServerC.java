import java.io.BufferedReader;import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.StringTokenizer;
public class TCPServerC implements  Runnable
{
	static ArrayList<String> members = new ArrayList<>();
	static ArrayList<String> Allmembers = new ArrayList<>();
	static ArrayList<Socket> sockets= new ArrayList<>();
	String serverInput = ""; //input to server from client 
	String clientOutput = ""; //output to client
	DataOutputStream outToClient;
	Socket connectionSocket;
	static ServerSocket welcomeSocket;
	static String ServerMessage;
	static ArrayList<Thread> t = new ArrayList<>();
	boolean joinFlag = false;
	static DataOutputStream outToServer;
	static Socket clientSocket;
	String inFromA;
	static BufferedReader inFromServer;
	

	public TCPServerC(Socket conn)
	{
		connectionSocket = conn;
	}
	public static void main(String argv[]) throws Exception
	{
	
		clientSocket = new Socket("Habiibas-MacBook-Pro.local", 6001);
		outToServer = new DataOutputStream(clientSocket.getOutputStream());
		BufferedReader inFromUser =  new BufferedReader(new InputStreamReader(System.in));	

		outToServer.writeBytes("join(serverC)"+ "\n");
		welcomeSocket=new ServerSocket(6002);
			
		 Thread th  =	new Thread(new Runnable(){
			 public void run()
			 {
				 String Message = null;
				 String inFromB = null;
				 while(true)
				 { 
					 try 
					 {
						
						  inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
						 inFromB = inFromServer.readLine();
						 
					
						  

					 } catch (Exception e) 
					 {
						 // TODO Auto-generated catch block
						 return;
					 }
					 if(inFromB.length()>0)
					 {
						
						 System.out.println("ServerB: " + inFromB + '\n' );

									if(inFromB.contains("Chat(")){
										
										
											StringTokenizer st = new StringTokenizer(inFromB, ",");
											String d = st.nextToken();
											String d1 = d.substring(5);
											String m = st.nextToken();
											String t = st.nextToken();
											int ttl = Integer.parseInt(t.substring(0, t.length()-1));
											String x = "";
											String y = " "+m;
											
											try {
												Route(y,d1,ttl);
											} catch (IOException e) {
												// TODO Auto-generated catch block
												e.printStackTrace();
											}
										
									}
									else if(inFromB.contains("join(") && !inFromB.contains("serverC")){
										System.out.println("in" + inFromB);
										String x = inFromB.substring(5);
										Allmembers.add(x);
										System.out.println("HA?");
										for(int i=0; i<members.size();i++){
											if(members.get(i).equals("serverD")){
												try {
													OutputStream os = (sockets.get(i)).getOutputStream();
													DataOutputStream outToOtherClient = new DataOutputStream(os);
													outToOtherClient.writeBytes("join("+x+"\n");
												} catch (IOException e) {
													// TODO Auto-generated catch block
													e.printStackTrace();
												}

											}
										}
									}
									else if(inFromB.contains("RE:")){
										String x = inFromB.substring(3);
										for(int i=0; i<Allmembers.size();i++){
											if(Allmembers.get(i).equals(x))
												Allmembers.remove(i);
										}
										for(int i=0; i<members.size();i++){
											if(members.get(i).equals("serverD")){
												try {
													OutputStream os = (sockets.get(i)).getOutputStream();
													DataOutputStream outToOtherClient = new DataOutputStream(os);
													outToOtherClient.writeBytes(inFromB+"\n");
												} catch (IOException e) {
													// TODO Auto-generated catch block
													e.printStackTrace();
												}

											}
										}
										
									}
									
								}
						 
					 }
				 
				 
				 
				 
			 }

		 
				public void Route(String Message, String Destination, int TTL) throws IOException
				{

					if(!(members.contains(Destination)))
					{
						for(int i=0; i<members.size();i++){
							if(members.get(i).equals("serverD")){
								try {
									OutputStream os = (sockets.get(i)).getOutputStream();
									DataOutputStream outToOtherClient = new DataOutputStream(os);
									outToOtherClient.writeBytes("Chat("+Destination+","+Message+","+(TTL-1)+")\n");
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}

							}
						}
						
					}
					else{
						for(int i=0; i<members.size();i++)
						{
							if(members.get(i).equals(Destination))
							{
								System.out.println("SERVER: To "+Destination+" Message "+Message);
								OutputStream os = (sockets.get(i)).getOutputStream();
								DataOutputStream outToOtherClient = new DataOutputStream(os);
								outToOtherClient.writeBytes(Message + '\n');
							

							}
						}
					}
					
				}
			 
			 

		 });
		 th.start();
		 while(true)
		 {
			
			 Socket connectionSocket=welcomeSocket.accept();
			 
			 Thread t1=new Thread(new TCPServerC(connectionSocket));
			 sockets.add(connectionSocket);
			 t.add(t1);
			 t1.start();

			 
		 }
		
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

		while(true) {

			try
			{
				BufferedReader inFromClient = 
						new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
				outToClient = 
						new DataOutputStream(connectionSocket.getOutputStream());

				if(serverInput!=null){
					serverInput = inFromClient.readLine();//this line often causes an exception to be thrown so i surrounded it with a try and catch

					System.out.println("CLIENT: "+serverInput);
					if(serverInput.startsWith("join(") && serverInput.endsWith(")"))
						JoinResponse();
					else
						if(serverInput.contains("GetMemberList()")){
								MemberListResponse();
						}

						else if(serverInput.contains("GetMembers()")){
								MemberResponse();
											}

						else {
							if(serverInput.contains("Chat(")){
								if(joinFlag==false){
									clientOutput = "SERVER: You are not signed in yet";
									outToClient.writeBytes(clientOutput);
								}
								else{
									StringTokenizer st = new StringTokenizer(serverInput, ",");
									String d = st.nextToken();
									String d1 = d.substring(5);
									String m = st.nextToken();
									String t = st.nextToken();
									int ttl = Integer.parseInt(t.substring(0,t.length()-1));
									String x = "";
									for(int i=0; i<sockets.size();i++){
										if(sockets.get(i) == connectionSocket){
											x = members.get(i);
										}
									}
									if(x.equals("serverD")){
										Route(m,d1,ttl);
									}
									else{
									String y = " "+m+" From: "+x;
									System.out.println("goo");
									Route(y,d1,ttl); 
									}
								}
							}
						}

					String serverInput1 = serverInput.toUpperCase();
					if((serverInput1.contains("QUIT")||serverInput1.contains("BYE")) && !serverInput.contains("Chat"))
					{
						for(Socket s: sockets)
						{
							DataOutputStream PendingChats = new DataOutputStream(s.getOutputStream());
							PendingChats.writeBytes(clientOutput);

						}

						clientOutput= "TERMINATED";
						String x = "";
						System.out.println("SERVER: "+clientOutput);
						for(int i=0; i<sockets.size();i++){
							if(connectionSocket.equals(sockets.get(i))){
								x = members.get(i);
								members.remove(i);
								sockets.remove(i);
								
							}	
						}
						for(int i =0; i<Allmembers.size();i++){
							if(Allmembers.get(i).equals(x))
								Allmembers.remove(i);
						}
						
						outToServer.writeBytes("RE:"+x+"\n");

						connectionSocket = welcomeSocket.accept();
						}
					else if(serverInput.contains("RE:")){
						String x = serverInput.substring(3);
						for(int i=0; i<Allmembers.size();i++){
							if(Allmembers.get(i).equals(x))
								Allmembers.remove(i);
						}
						outToServer.writeBytes(serverInput+"\n");
					}
					else
					{

						clientOutput =  "" + '\n';
						System.out.print(""+clientOutput);

					}

					outToClient.writeBytes(clientOutput + '\n');

				}
			}
			catch(Exception e){
				
			}
		}
	}
				

	public void MemberListResponse() throws IOException
	{
		
		clientOutput="Members: ";
		for(int i=0; i<Allmembers.size();i++){
			if(!(Allmembers.get(i).contains("server")))
				clientOutput+=Allmembers.get(i)+", ";
				
		}
		outToClient.writeBytes(clientOutput+"\n");
		System.out.println(clientOutput);
	}
	public void MemberResponse() throws IOException
	{
		clientOutput="Members: ";
		for(int i=0; i<members.size();i++){
			if(!(members.get(i).equals("serverD")))
				clientOutput+=members.get(i)+", ";
		
		}
		outToClient.writeBytes(clientOutput+"\n");
		System.out.println(clientOutput);

	}
	public void JoinResponse() throws IOException
	{
		String x = serverInput.substring(5,serverInput.length()-1);

			if(x.contains(",")){
				clientOutput = "Not joined";
				outToClient.writeBytes(clientOutput);
				System.out.println(clientOutput);

			}
			
			if(x.contains("FromServer"))
				{
					x=x.substring(10);

					Allmembers.add(x);
					
				}
				else
			{
				for(int i=0; i<Allmembers.size(); i++){
					if(Allmembers.get(i).equals(x)){
						clientOutput = "Not joined";
						outToClient.writeBytes(clientOutput+"\n");
						System.out.println(clientOutput);
						return;
					}
				}
				Allmembers.add(x);
				members.add(x);
				joinFlag = true;
				clientOutput = "joined";
				outToClient.writeBytes(clientOutput+"\n");
			}
				if(!serverInput.contains("FromServer")){
					outToServer.writeBytes("join(FromServer"+x+")\n");
					
					for(int i=0; i<members.size();i++){
						if(members.get(i).equals("serverD")){
							OutputStream os = (sockets.get(i)).getOutputStream();
							DataOutputStream outToOtherClient = new DataOutputStream(os);
							outToOtherClient.writeBytes("join("+x+"\n");
							
						}
					}
				}
				else{
					outToServer.writeBytes(serverInput+"\n");
					
				}
				
			}
		
	
	

			
	public void Route(String Message, String Destination, int TTL) throws IOException
	{

		if(!(Allmembers.contains(Destination)))
		{
				ServerMessage="SERVER: Member doesn't exist or is currently offline";
				System.out.println(ServerMessage);
				outToClient.writeBytes(ServerMessage+"\n");				
			}
			else {if(!members.contains(Destination)){
				
				outToServer.writeBytes("Chat("+Destination+","+Message+","+(TTL-1)+")\n");
			}

		else{
			for(int i=0; i<members.size();i++)
			{
				if(members.get(i).equals(Destination))
				{
					System.out.println("SERVER: To "+Destination+" Message "+Message);
					OutputStream os = (sockets.get(i)).getOutputStream();
					DataOutputStream outToOtherClient = new DataOutputStream(os);
					outToOtherClient.writeBytes(Message+"\n");

				}
			}
		}
	}
}
}






