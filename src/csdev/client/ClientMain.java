package csdev.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;
import java.util.TreeMap;

import csdev.*;
import csdev.server.ServerMain;


public class ClientMain {
	// arguments: name surname patronymic [host]
	public static void main(String[] args)  {
		if (args.length < 3 || args.length > 4) {
			System.err.println(	"Invalid number of arguments\n" + "Use: name surname patronymic [host]" );
			waitKeyToStop();
			return;
		}
		try ( Socket sock = ( args.length == 3 ? 
				new Socket( InetAddress.getLocalHost(), Protocol.PORT ):
				new Socket( args[3], Protocol.PORT ) )) { 		
			System.err.println("initialized");
			session(sock, args[0], args[1], args[2] );
		} catch ( Exception e) {
			System.err.println(e);
		} finally {
			System.err.println("bye...");
		}
	}
	
	static void waitKeyToStop() {
		System.err.println("Press a key to stop...");
		try {
			System.in.read();
		} catch (IOException e) {
		}
	}
	
	static class Session {
		boolean connected = false;
		String fullName = null;
		Session( String name, String surname, String patronymic ) {
			fullName = name + " " + surname + " " + patronymic;
		}
	}
	static void session(Socket s, String name, String surname, String patronymic) {
		try ( Scanner in = new Scanner(System.in);
			  ObjectInputStream is = new ObjectInputStream(s.getInputStream());
			  ObjectOutputStream os = new ObjectOutputStream(s.getOutputStream())) {
			Session ses = new Session(name, surname, patronymic);
			if ( openSession( ses, is, os, in )) { 
				try {
					while (true) {
						Message msg = getCommand(ses, in);
						if (! processCommand(ses, msg, is, os)) {
							break;
						}				
					}			
				} finally {
					closeSession(ses, os);
				}
			}
		} catch ( Exception e) {
			System.err.println(e);
		}
	}
	
	static boolean openSession(Session ses, ObjectInputStream is, ObjectOutputStream os, Scanner in) 
			throws IOException, ClassNotFoundException {
		os.writeObject( new MessageConnect(ses.fullName));
		MessageConnectResult msg = (MessageConnectResult) is.readObject();
		if (msg.Error()== false ) {
			System.err.println("connected");
			ses.connected = true;
			return true;
		}
		System.err.println("Unable to connect: "+ msg.getErrorMessage());
		System.err.println("Press <Enter> to continue...");
		if( in.hasNextLine())
			in.nextLine();
		return false;
	}
	
	static void closeSession(Session ses, ObjectOutputStream os) throws IOException {
		if ( ses.connected ) {
			ses.connected = false;
			os.writeObject(new MessageDisconnect());
		}
	}

	static Message getCommand(Session ses, Scanner in) {	
		while (true) {
			printPrompt();
			if (in.hasNextLine()== false)
				break;
			String str = in.nextLine();
			int cmd = translateCmd(str);
			switch ( cmd ) {
				case -1:
					return null;
				case Protocol.CMD_CHECK_RECORD:
					return new MessageCheckRecord();
				case Protocol.CMD_OPTIONS:
					return new MessageOptions();
				case Protocol.CMD_RECORD:
					return inputRecord(in);
				case 0:
					continue;
				default: 
					System.err.println("Unknow command!");
					continue;
			}
		}
		return null;
	}
	
	static MessageRecord inputRecord(Scanner in) {
		String day, time, phone, complaint;
		System.out.print("Enter day: ");
		day = in.nextLine().trim();
		System.out.print("Enter time: ");
		time = in.nextLine().trim();
		System.out.print("Enter phone number: ");
		phone = in.nextLine().trim();
		System.out.print("Enter complaint: ");
		complaint = in.nextLine().trim();
		return new MessageRecord(day, time, phone, complaint);
	}
	
	static TreeMap<String,Integer> commands = new TreeMap<String,Integer>();
	static {
		commands.put("q", -1);
		commands.put("quit", -1);
		commands.put("my", Protocol.CMD_CHECK_RECORD);
		commands.put("myrecord", Protocol.CMD_CHECK_RECORD);
		commands.put("o", Protocol.CMD_OPTIONS);
		commands.put("options", Protocol.CMD_OPTIONS);
		commands.put("r", Protocol.CMD_RECORD);
		commands.put("record", Protocol.CMD_RECORD);
	}
	
	static int translateCmd(String str) {
		// returns -1-quit, 0-invalid cmd, Protocol.CMD_XXX
		str = str.trim();
		if (commands.containsKey(str))
		{
			return commands.get(str);
		}
		return 0;
	}
	
	static void printPrompt() {
		System.out.println();
		System.out.print("(q)uit/(my)record/(o)ptions/(r)ecord >");
		System.out.flush();
	}
	
	static boolean processCommand(Session ses, Message msg, 
			                      ObjectInputStream is, ObjectOutputStream os) 
            throws IOException, ClassNotFoundException {
		if ( msg != null )
		{
			os.writeObject(msg);
			MessageResult res = (MessageResult) is.readObject();
			if ( res.Error()) {
				System.err.println(res.getErrorMessage());
			} else {
				switch (res.getID()) {
					case Protocol.CMD_CHECK_RECORD:
						printRecord(( MessageCheckRecordResult ) res);
						break;
					case Protocol.CMD_OPTIONS:
						printOptions(( MessageOptionsResult ) res);
						break;
					case Protocol.CMD_RECORD:
						System.out.println("OK...");
						break;
					default:
						assert(false);
						break;
				}
			}
			return true;
		}
		return false;
	}
	
	static void printRecord(MessageCheckRecordResult m) {
		if ( m.day != null && m.time != null && m.phone != null && m.complaint != null) {
			System.out.println("Your record {");
				System.out.println("Day: ");
				System.out.print(m.day);
				System.out.println("\nTime: ");
				System.out.print(m.time);
				System.out.println("\nPhone number: ");
				System.out.print(m.phone);
				System.out.println("\nComplaint: ");
				System.out.print(m.complaint);
			    System.out.println("}");
		}
		else {
			System.out.println("No mail...");		
		}
	}
	
	static void printOptions(MessageOptionsResult m) { 
		if ( m.options != null ) {
			System.out.println("Options {");
			for (String str: m.options) {
				System.out.println("\t" + str);
			}	
			System.out.println("}");
		}
	}
}
