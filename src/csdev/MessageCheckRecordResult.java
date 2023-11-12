package csdev;

import java.io.Serializable;


public class MessageCheckRecordResult extends MessageResult implements
		Serializable {

	private static final long serialVersionUID = 1L;
	
	public String day = null;
	public String time = null;
	public String phone = null;
	public String complaint = null;
	
	public MessageCheckRecordResult( String errorMessage ) { //Error
		super( Protocol.CMD_CHECK_RECORD, errorMessage );
	}

	public MessageCheckRecordResult( String d, String t, String ph, String txt ) { // No errors
		super( Protocol.CMD_CHECK_RECORD );
		this.day = d;
		this.time = t;
		this.phone = ph;
		this.complaint = txt;
	}

}
