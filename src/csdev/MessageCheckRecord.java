package csdev;

import java.io.Serializable;


public class MessageCheckRecord extends Message implements Serializable {

	private static final long serialVersionUID = 1L;

	public MessageCheckRecord() {
		super( Protocol.CMD_CHECK_RECORD );
	}
}
