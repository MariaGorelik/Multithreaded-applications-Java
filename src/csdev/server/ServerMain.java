package csdev.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.Vector;


import csdev.*;


public class ServerMain {
	
	private static int MAX_USERS = 100;
	
	private static final int DAYS_SIZE = 6;
	private static final int TIMES_SIZE = 9;
	
	public static boolean isValidDay(String d)
	{
		if (d.equals("Monday") || d.equals("Tuesday") || d.equals("Wednesday") || d.equals("Thursday") || 
				d.equals("Friday") || d.equals("Saturday"))
		{
			return true;
		}
		return false;
	}
	
	public static boolean isValidTime(String t)
	{
		if (t.equals("10.00") || t.equals("11.00") || t.equals("12.00") || t.equals("13.00") || 
				t.equals("14.00") || t.equals("15.00") || t.equals("16.00") || t.equals("17.00") || t.equals("18.00"))
		{
			return true;
		}
		return false;
	}
	
	static TreeMap<String,Integer> daysToInt = new TreeMap<String,Integer>();
	static {
		daysToInt.put("Monday", 0);
		daysToInt.put("Tuesday", 1);
		daysToInt.put("Wednesday", 2);
		daysToInt.put("Thursday", 3);
		daysToInt.put("Friday", 4);
		daysToInt.put("Saturday", 5);
	}
	
	static String[] daysToStr = new String[DAYS_SIZE];
	static {
		daysToStr[0] = "Monday";
		daysToStr[1] = "Tuesday";
		daysToStr[2] = "Wednesday";
		daysToStr[3] = "Thursday";
		daysToStr[4] = "Friday";
		daysToStr[5] = "Saturday";
	}
	
	static TreeMap<String,Integer> timesToInt = new TreeMap<String,Integer>();
	static {
		timesToInt.put("10.00", 0);
		timesToInt.put("11.00", 1);
		timesToInt.put("12.00", 2);
		timesToInt.put("13.00", 3);
		timesToInt.put("14.00", 4);
		timesToInt.put("15.00", 5);
		timesToInt.put("16.00", 6);
		timesToInt.put("17.00", 7);
		timesToInt.put("18.00", 8);
	}
	
	static String[] timesToStr = new String[TIMES_SIZE];
	static {
		timesToStr[0] = "10.00";
		timesToStr[1] = "11.00";
		timesToStr[2] ="12.00";
		timesToStr[3] = "13.00";
		timesToStr[4] = "14.00";
		timesToStr[5] = "15.00";
		timesToStr[6] ="16.00";
		timesToStr[7] = "17.00";
		timesToStr[8] = "18.00";
	}
	
	static ServerThread records[][] = new ServerThread[DAYS_SIZE][TIMES_SIZE];
	static {
		for (int i = 0; i < DAYS_SIZE; i++)
		{
			for (int j = 0; j < TIMES_SIZE; j++)
			{
				records[i][j] = null;
			}
		}
	}
	
	public static Vector<String> availableOptions()
	{
		Vector<String> options = new Vector<String>();
		for (int i = 0; i < DAYS_SIZE; i++)
		{
			for (int j = 0; j < TIMES_SIZE; j++)
			{
				if (records[i][j] == null)
				{
					options.add(daysToStr[i] + " " + timesToStr[j]);
				}
			}
		}
		return options;
	}
	
	public static void putClientRecord(String day, String time, ServerThread client)
	{
		int d = daysToInt.get(day);
		int t = timesToInt.get(time);
		records[d][t] = client;
	}
	
	public static boolean isAvailable(String day, String time)
	{
		if (records[daysToInt.get(day)][timesToInt.get(time)] == null)
		{
			return true;
		}
		return false;
	}

	public static void main(String[] args) {

		try ( ServerSocket serv = new ServerSocket( Protocol.PORT  )) {
			System.err.println("initialized");
			ServerStopThread tester = new ServerStopThread();
			tester.start();
			while (true) {
				Socket sock = accept( serv );
				if ( sock != null ) {
					if ( ServerMain.getNumUsers() < ServerMain.MAX_USERS )
					{
						System.err.println( sock.getInetAddress().getHostName() + " connected" );
						ServerThread server = new ServerThread(sock);
						server.start();
					}
					else
					{
						System.err.println( sock.getInetAddress().getHostName() + " connection rejected" );
						sock.close();
					}
				} 
				if ( ServerMain.getStopFlag() ) {
					break;
				}
			}
		} catch (IOException e) {
			System.err.println(e);
		} finally {
			stopAllUsers();
			System.err.println("stopped");	
		}
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {		
		}
	}
	
	public static Socket accept( ServerSocket serv ) {	
		assert( serv != null );
		try {
			serv.setSoTimeout( 1000 );
			Socket sock = serv.accept();
			return sock;
		} catch (SocketException e) {
		} catch (IOException e) {
		}		
		return null;
	}
	
	private static void stopAllUsers() {
		String[] nic = getUsers();
		for (String user : nic ) {
			ServerThread ut = getUser( user );
			if ( ut != null ) {
				ut.disconnect();
			}
		}
	}
	
	private static Object syncFlags = new Object();
	private static boolean stopFlag = false;
	public static boolean getStopFlag() {
		synchronized ( ServerMain.syncFlags ) {
			return stopFlag;
		}
	}
	public static void setStopFlag( boolean value ) {
		synchronized ( ServerMain.syncFlags ) {
			stopFlag = value;
		}
	}
	
	private static Object syncUsers = new Object();
	private static TreeMap<String, ServerThread> users = 
			new TreeMap<String, ServerThread> ();
	
	public static ServerThread getUser( String userFullName ) {
		synchronized (ServerMain.syncUsers) {
			return ServerMain.users.get( userFullName );
		}		
	}

	public static ServerThread registerUser( String userFullName, ServerThread user ) {
		synchronized (ServerMain.syncUsers) {
			ServerThread old = ServerMain.users.get( userFullName );
			if ( old == null ) {
				ServerMain.users.put( userFullName, user );
			}
			return old;
		}		
	}

	public static ServerThread setUser( String userFullName, ServerThread user ) {
		synchronized (ServerMain.syncUsers) {
			ServerThread res = ServerMain.users.put( userFullName, user );
			if ( user == null ) {
				ServerMain.users.remove(userFullName);
			}
			return res;
		}		
	}
	
	public static String[] getUsers() {
		synchronized (ServerMain.syncUsers) {
			return ServerMain.users.keySet().toArray( new String[0] );
		}		
	}
	
	public static int getNumUsers() {
		synchronized (ServerMain.syncUsers) {
			return ServerMain.users.keySet().size();
		}		
	}
}

class ServerStopThread extends CommandThread {
	
	static final String cmd  = "q";
	static final String cmdL = "quit";
	
	Scanner fin; 
	
	public ServerStopThread() {		
		fin = new Scanner( System.in );
		ServerMain.setStopFlag( false );
		putHandler( cmd, cmdL, new CmdHandler() {
			@Override
			public boolean onCommand(int[] errorCode) {	return onCmdQuit(); }				
		});
		this.setDaemon(true);
		System.err.println( "Enter \'" + cmd + "\' or \'" + cmdL + "\' to stop server\n" );
	}
	
	public void run() {
		
		while (true) {			
			try {
				Thread.sleep( 1000 );
			} catch (InterruptedException e) {
				break;
			}
			if ( fin.hasNextLine()== false )
				continue;
			String str = fin.nextLine();
			if ( command( str )) {
				break;
			}
		}
	}
	
	public boolean onCmdQuit() {
		System.err.print("stop server...");
		fin.close();
		ServerMain.setStopFlag( true );
		return true;
	}
}

class ServerThread extends Thread {
	
	private Socket              sock;
	private ObjectOutputStream 	os;
	private ObjectInputStream 	is;
	private InetAddress 		addr;
	
	public final int RECORD_SIZE = 4;
	
	//private String userNic = null;
	private String userFullName;
	
	private Object syncRecords = new Object();
	private String day = null;
	private String time = null;
	private String phone = null;
	private String complaint = null;
	
	public boolean addRecord( String d, String t, String ph, String txt ) {	
		synchronized ( syncRecords ) {				
			if ( day == null ) {
				day = d;
				time = t;
				phone = ph;
				complaint = txt;
				return true;
			}
			else {
				//System.err.print("You are already have a record");
				return false;
			}
		}
	}
	
	public String[] getRecord() {
		synchronized ( syncRecords ) {				
			String[] rec = null;
			synchronized ( syncRecords ) {			
				if ( day != null ) {
					rec = new String[RECORD_SIZE];
					rec[0] = day;
					rec[1] = time;
					rec[2] = phone;
					rec[3] = complaint;
				}
			}
			return rec;
		}		
	}
	
	
	public ServerThread(Socket s) throws IOException {
		sock = s;
		s.setSoTimeout(1000);
		os = new ObjectOutputStream( s.getOutputStream() );
		is = new ObjectInputStream( s.getInputStream());
		addr = s.getInetAddress();
		this.setDaemon(true);
	}
	
	public void run() {
		try {
			while ( true ) {
				Message msg = null;
				try {
					msg = ( Message ) is.readObject();
				} catch (IOException e) {
				} catch (ClassNotFoundException e) {
				}
				if (msg != null) switch ( msg.getID() ) {
			
					case Protocol.CMD_CONNECT:
						if ( !connect( (MessageConnect) msg )) 
							return;
						break;
						
					case Protocol.CMD_DISCONNECT:
						return;
						
					case Protocol.CMD_OPTIONS:
						options(( MessageOptions ) msg);
						break;
						
					case Protocol.CMD_CHECK_RECORD:
						checkRecord(( MessageCheckRecord ) msg );
						break;
						
					case Protocol.CMD_RECORD:
						letter(( MessageRecord ) msg );
						break;					
				}
			}	
		} catch (IOException e) {
			System.err.print("Disconnect...");
		} finally {
			disconnect();
		}
	}
	
	boolean connect( MessageConnect msg ) throws IOException {
		
		ServerThread old = register( msg.userFullName );
		if ( old == null )
		{
			os.writeObject( new MessageConnectResult());
			return true;
		} else {
			os.writeObject( new MessageConnectResult( 
				"User " + old.userFullName + " already connected" ));
			return false;
		}
	}

	void letter( MessageRecord msg ) throws IOException {
		if (!ServerMain.isValidDay(msg.day) || !ServerMain.isValidTime(msg.time))
		{
			os.writeObject( new MessageRecordResult("Incorrect day or(and) time type"));
		}
	else
	{
		if (this.addRecord(msg.day, msg.time, msg.phone, msg.complaint))
		{
		    if (ServerMain.isAvailable(this.day, this.time))
		    {
			ServerMain.putClientRecord(this.day, this.time, this);
			//os.writeObject("You have successfully signed up");
			os.writeObject( new MessageRecordResult("You have successfully signed up"));
		    }
		    else
		    {
			this.day = null;
			this.time = null;
			this.phone = null;
			this.complaint = null;
			os.writeObject( new MessageRecordResult("This time isn't available, try another option"));
		    }
		}
		else
		{
			os.writeObject( new MessageRecordResult("You are already have a record"));
		}
	}
	}
	
	void options( MessageOptions msg ) throws IOException {
		Vector<String> options = ServerMain.availableOptions();
		if (options != null)
		{
			os.writeObject( new MessageOptionsResult(options));
		}
		else
			os.writeObject( new MessageOptionsResult( "there is no free appointment" ));
	}
	
	void checkRecord( MessageCheckRecord msg ) throws IOException {

		String[] rec = getRecord(); 
		if ( rec != null )
			os.writeObject( new MessageCheckRecordResult( rec[0], rec[1], rec[2], rec[3] ));
		else
			os.writeObject( new MessageCheckRecordResult("Unable to get record"));		
	}
	
	private boolean disconnected = false;
	public void disconnect() {
		if ( ! disconnected )
		try {			
			System.err.println( addr.getHostName() + " disconnected" );
			unregister();
			os.close();
			is.close();
			sock.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			this.interrupt();
			disconnected = true;
		}
	}
	
	private void unregister() {
		if ( userFullName != null ) {
			ServerMain.setUser( userFullName, null );			
			userFullName = null;
		}		
	}
	
	private ServerThread register( String fullName ) {
		ServerThread old = ServerMain.registerUser( fullName, this );
		if ( old == null ) {
				userFullName = fullName;
				System.err.println("User \'"+ fullName+ "\' is registered");
		}
		return old;
	}
}

