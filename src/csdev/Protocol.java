package csdev;


interface CMD {
	static final int CMD_CONNECT 	= 1;
	static final int CMD_DISCONNECT= 2;
	static final int CMD_OPTIONS 	= 3;
	static final int CMD_CHECK_RECORD= 4;
	static final int CMD_RECORD	= 5;
}


interface RESULT {
	static final int RESULT_CODE_OK 	= 0;
	static final int RESULT_CODE_ERROR 	= -1;
}	


interface PORT {
	static final int PORT = 8071;
}


public class Protocol implements CMD, RESULT, PORT {
	private static final int CMD_MIN = CMD_CONNECT;
	private static final int CMD_MAX = CMD_RECORD;
	public static boolean validID( int id ) {
		return id >= CMD_MIN && id <= CMD_MAX; 
	}
}

