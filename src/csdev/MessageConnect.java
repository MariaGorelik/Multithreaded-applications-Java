package csdev;

import java.io.Serializable;


public class MessageConnect extends Message implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public String userFullName;
	
	public MessageConnect( String userFullName ) {
		super( Protocol.CMD_CONNECT );
		this.userFullName = userFullName;
	}
	
}