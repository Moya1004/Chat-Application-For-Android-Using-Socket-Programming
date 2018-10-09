
import com.github.jmsoft.socketclient.ChatMessage;
import java.io.*;
import java.net.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;

// the server that can be run as a console
public class Server {
	// a unique ID for each connection
	private static int uniqueId;
	// an ArrayList to keep the list of the Client
	private ArrayList<ClientThread> al;
        
        private ArrayList<ArrayList> groupNames;
	// to display time
	private SimpleDateFormat sdf;
	// the port number to listen for connection
	private int port;
	// to check if server is running
	private boolean keepGoing;
	// notification
	private String notif = " *** ";
	
	//constructor that receive the port to listen to for connection as parameter
	
	public Server(int port) {
		// the port
		this.port = port;
		// to display hh:mm:ss
		sdf = new SimpleDateFormat("HH:mm:ss");
		// an ArrayList to keep the list of the Client
		al = new ArrayList<ClientThread>();
                groupNames = new ArrayList();
	}
	
	public void start() {
		keepGoing = true;
		//create socket server and wait for connection requests 
		try 
		{
			// the socket used by the server
			ServerSocket serverSocket = new ServerSocket(port);

			// infinite loop to wait for connections ( till server is active )
			while(keepGoing) 
			{
				display("Server waiting for Clients on port " + port + ".");
				
				// accept connection if requested from client
				Socket socket = serverSocket.accept();
				// break if server stoped
				if(!keepGoing)
					break;
				// if client is connected, create its thread
				ClientThread t = new ClientThread(socket);
				//add this client to arraylist
				al.add(t);
				
				t.start();
			}
			// try to stop the server
			try {
				serverSocket.close();
				for(int i = 0; i < al.size(); ++i) {
					ClientThread tc = al.get(i);
					try {
					// close all data streams and socket
					tc.sInput.close();
					tc.sOutput.close();
					tc.socket.close();
					}
					catch(IOException ioE) {
					}
				}
			}
			catch(Exception e) {
				display("Exception closing the server and clients: " + e);
			}
		}
		catch (IOException e) {
            String msg = sdf.format(new Date()) + " Exception on new ServerSocket: " + e + "\n";
			display(msg);
		}
	}
	
	// to stop the server
	protected void stop() {
		keepGoing = false;
		try {
			new Socket("localhost", port);
		}
		catch(Exception e) {
		}
	}
	
	// Display an event to the console
	private void display(String msg) {
		String time = sdf.format(new Date()) + " " + msg;
		System.out.println(time);
	}
	
	// to broadcast a message to all Clients
	private synchronized boolean broadcast(String message) {
		// add timestamp to the message
		String time = sdf.format(new Date());
		
		// to check if message is private i.e. client to client message
		String[] w = message.split(" ",3);
		
		boolean isPrivate = false;
                boolean isGroup = false;
		if(w[1].charAt(0)=='@') 
			isPrivate=true;
                if(w[1].charAt(0)=='=') 
			isGroup=true;
		
		
		// if private message, send message to mentioned username only
		String tocheck = "";
                if(isPrivate==true)
		{
			
                        tocheck=w[1].substring(1, w[1].length());
			message=w[2];
			String messageLf = time + " " + message + "\n";
			boolean found=false;
                        DB db = new DB();
                        if (!db.loginUser(tocheck, "pass"))
                            return false;
                        else
                        {
                            String sender = w[0].substring(0,w[0].length() - 1);
                            db.insertMessage(message, db.getUserIDByUsername(sender), db.getUserIDByUsername(tocheck),2);
                        }
                        
			// we loop in reverse order to find the mentioned username
			for(int y=al.size(); --y>=0;)
			{
				ClientThread ct1=al.get(y);
				String check=ct1.getUsername();
				if(check.equals(tocheck))
				{
					// try to write to the Client if it fails remove it from the list
					if(!ct1.writeMsg(messageLf)) {
						al.remove(y);
						display("Disconnected Client " + ct1.username + " removed from list.");
					}
					// username found and delivered the message
					found=true;
					break;
				}
				
				
				
			}
			// mentioned user not found, return false
			if(found!=true)
			{
				return false; 
			}
		}
                /*
                else if(isGroup==true)
		{
			
                        tocheck=w[1].substring(1, w[1].length());
			message=w[2];
			String messageLf = time + " " + message + "\n";
			boolean found=false;
                        DB db = new DB();
                        if (!db.groupExists(tocheck))
                            return false;
                        else
                        {
                            db.insertMessage(message, db.getUserIDByUsername(w[0].substring(0,w[0].length() -1)), db.getUserIDByUsername(tocheck),1);
                        }
                        
			// we loop in reverse order to find the mentioned username
			for(int y=al.size(); --y>=0;)
			{
				ClientThread ct1=al.get(y);
				String check=ct1.getUsername();
				DataResult users = db.getUsersByGroupID(db.getGroupIDByGroupname(tocheck));
                                
                                if(check.equals(tocheck))
				{
					// try to write to the Client if it fails remove it from the list
					if(!ct1.writeMsg(messageLf)) {
						al.remove(y);
						display("Disconnected Client " + ct1.username + " removed from list.");
					}
					// username found and delivered the message
					found=true;
					break;
				}
				
				
				
			}
			// mentioned user not found, return false
			if(found!=true)
			{
				return false; 
			}
		}*/
		// if message is a broadcast message
		else
		{
			String messageLf = time + " " + message + "\n";
			// display message
			System.out.print(messageLf);
                        DB db = new DB();
                        String sender = w[0].replace(":", "");
			db.insertMessage(w[1], db.getUserIDByUsername(sender), 0 ,0);
			// we loop in reverse order in case we would have to remove a Client
			// because it has disconnected
			for(int i = al.size(); --i >= 0;) {
				ClientThread ct = al.get(i);
				// try to write to the Client if it fails remove it from the list
				if(!ct.writeMsg(messageLf)) {
					al.remove(i);
					display("Disconnected Client " + ct.username + " removed from list.");
				}
			}
		}
                //DB db = new DB();
                //int id = db.getUserIdByUsername(tocheck);
                //db.insertMessage(message,1,id,2);
		return true;
		
		
	}

	// if client sent LOGOUT message to exit
	synchronized void remove(int id) {
		
		String disconnectedClient = "";
		// scan the array list until we found the Id
		for(int i = 0; i < al.size(); ++i) {
			ClientThread ct = al.get(i);
			// if found remove it
			if(ct.id == id) {
				disconnectedClient = ct.getUsername();
				al.remove(i);
				break;
			}
		}
		broadcast(notif + disconnectedClient + " has left the chat room." + notif);
	}
	
	/*
	 *  To run as a console application
	 * > java Server
	 * > java Server portNumber
	 * If the port number is not specified 1500 is used
	 */ 
	public static void main(String[] args) {
		// start server on port 1500 unless a PortNumber is specified 
		int portNumber = 1500;
		switch(args.length) {
			case 1:
				try {
					portNumber = Integer.parseInt(args[0]);
				}
				catch(Exception e) {
					System.out.println("Invalid port number.");
					System.out.println("Usage is: > java Server [portNumber]");
					return;
				}
			case 0:
				break;
			default:
				System.out.println("Usage is: > java Server [portNumber]");
				return;
				
		}
		// create a server object and start it
		Server server = new Server(portNumber);
		server.start();
	}

	// One instance of this thread will run for each client
	class ClientThread extends Thread {
		// the socket to get messages from client
		Socket socket;
		ObjectInputStream sInput;
		ObjectOutputStream sOutput;
		// my unique id (easier for deconnection)
		int id;
		// the Username of the Client
		String username;
		// message object to recieve message and its type
		ChatMessage cm;
		// timestamp
		String date;

		// Constructor
		ClientThread(Socket socket) {
			// a unique id
			id = ++uniqueId;
			this.socket = socket;
                        String hostAddress = socket.getInetAddress().getHostAddress();
                        //DB db = new DB();
                        //db.insertLog(hostAddress, new SimpleDateFormat("YYYY-MM-dd HH:mm:ss").format(new Date()));
			//Creating both Data Stream
			System.out.println("Thread trying to create Object Input/Output Streams");
			try
			{
				sOutput = new ObjectOutputStream(socket.getOutputStream());
				sInput  = new ObjectInputStream(socket.getInputStream());
				// read the username
				username = (String) sInput.readObject();
				broadcast(notif + username + " has joined the chat room." + notif);
                                DB db = new DB();
                                if (!db.loginUser(username, "pass"))
                                    if (db.insertUser(username,"pass", null))
                                        return;
                                db.insertGroupUser(1, db.getUserIDByUsername(username));
			}
			catch (IOException e) {
				display("Exception creating new Input/output Streams: " + e);
				return;
			}
			catch (ClassNotFoundException e) {
			}
            date = new Date().toString() + "\n";
		}
		
		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		// infinite loop to read and forward message
		public void run() {
			// to loop until LOGOUT
			boolean keepGoing = true;
			while(keepGoing) {
				// read a String (which is an object)
				try {
                                        //System.out.println(sInput.readObject().toString());
                                        String className = ChatMessage.class.getName();
                                        Class.forName(className);
					Object obj = sInput.readObject();
                                        cm = (ChatMessage)obj;
				}
				catch (IOException e) {
					display(username + " Exception reading Streams: " + e.getMessage());
					break;				
				}
				catch(ClassNotFoundException e2) {
                                    display(username + " Class Not Found: " + e2.getMessage());
                                    break;
                                }
				// get the message from the ChatMessage object received
				String message = cm.getMessage();

				// different actions based on type message
				switch(cm.getType()) {

				case ChatMessage.MESSAGE:
					boolean confirmation =  broadcast(username + ": " + message);
					if(confirmation==false){
						String msg = notif + "Sorry. No such user exists." + notif;
						writeMsg(msg);
					}
					break;
				case ChatMessage.LOGOUT:
					display(username + " disconnected with a LOGOUT message.");
					keepGoing = false;
					break;
				case ChatMessage.WHOISIN:
					writeMsg("List of the users connected at " + sdf.format(new Date()) + "\n");
					// send list of active clients
					for(int i = 0; i < al.size(); ++i) {
						ClientThread ct = al.get(i);
						writeMsg((i+1) + ") " + ct.username + " since " + ct.date);
					}
					break;
                                case ChatMessage.CREATE_GROUP:
					//writeMsg("List of the users connected at " + sdf.format(new Date()) + "\n");
					// send list of active clients
                                        DB db = new DB();
                                        db.insertGroup(message);
                                        int userid = db.getUserIDByUsername(username);
                                        int groupid = db.getGroupIDByGroupname(message);
                                        db.insertGroupUser(groupid, userid);
					break;
                                case ChatMessage.REGISTER_FOR_GROUP:
					//writeMsg("List of the users connected at " + sdf.format(new Date()) + "\n");
					// send list of active clients
					db = new DB();
                                        userid = db.getUserIDByUsername(username);
                                        groupid = db.getGroupIDByGroupname(message);
                                        db.insertGroupUser(groupid,userid);
                                        break;

                                case ChatMessage.MESSAGE_GROUP:
					db = new DB();
                                        int groupID = db.getGroupIDByGroupname(message);
                                        ResultSet rs = db.getUsersByGroupID(groupID);
                                        
                                {
                                    try {
                                        while(rs.next()) {
                                            String user = rs.getString("Username");
                                            broadcast(this.username + ": " +"@" + user  +" "+  message);
                                        }
                                    } catch (SQLException ex) {
                                        Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
					break;
                                case ChatMessage.EVERYONE:
					//DB db = new DB();
                                        //int groupID = db.getGroupIDByGroupName();
                                        //ResultSet rs = getUsers(groupID);
                                        
					//while(rs.next()) {
					//	String user = rs.getString("Username");
					//	broadcast(this.username + ": " +"@" + user +  message);
					//}
					break;
				}
			}
			// if out of the loop then disconnected and remove from client list
			remove(id);
			close();
		}
		
		// close everything
		private void close() {
			try {
				if(sOutput != null) sOutput.close();
			}
			catch(Exception e) {}
			try {
				if(sInput != null) sInput.close();
			}
			catch(Exception e) {};
			try {
				if(socket != null) socket.close();
			}
			catch (Exception e) {}
		}

		// write a String to the Client output stream
		private boolean writeMsg(String msg) {
			// if Client is still connected send the message to it
			if(!socket.isConnected()) {
				close();
				return false;
			}
			// write the message to the stream
			try {
				sOutput.writeObject(msg);
			}
			// if an error occurs, do not abort just inform the user
			catch(IOException e) {
				display(notif + "Error sending message to " + username + notif);
				display(e.toString());
			}
			return true;
		}
	}
}

