package csdev;

import java.io.Serializable;
import java.util.Vector;


public class MessageOptionsResult extends MessageResult implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public Vector<String> options = null; 
	
	public MessageOptionsResult( String errorMessage ) { // Error
		super( Protocol.CMD_OPTIONS, errorMessage );
	}
	
	public MessageOptionsResult( Vector<String> opt ) { // No errors
		super( Protocol.CMD_OPTIONS );
		this.options = opt;
	}
}
