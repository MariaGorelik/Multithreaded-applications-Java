package csdev;

import java.io.Serializable;


public class MessageOptions extends Message implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public MessageOptions() {
		super( Protocol.CMD_OPTIONS );
	}
}