package csdev;

import java.io.Serializable;


public class MessageRecord extends Message implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public String day;
	public String time;
	public String phone;
	public String complaint;
	
	public MessageRecord( String d, String t, String ph, String txt ) {
		
		super(Protocol.CMD_RECORD);
		this.day = d;
		this.time = t;
		this.phone = ph;
		this.complaint = txt;
	}
	
}