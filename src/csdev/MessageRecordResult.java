package csdev;

import java.io.Serializable;


public class MessageRecordResult extends MessageResult implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public MessageRecordResult( String errorMessage ) { //Error
		
		super( Protocol.CMD_RECORD, errorMessage );
	}
	
	public MessageRecordResult() { // No errors
		
		super( Protocol.CMD_RECORD );
	}
}