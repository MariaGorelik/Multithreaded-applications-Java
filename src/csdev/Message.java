package csdev;

import java.io.Serializable;


public class Message implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private int id;
	public int getID() {
		return id;
	}
	
	protected Message() {
		assert( false );
	}
	
	protected Message( int id ) {
		
		assert( Protocol.validID( id )== true );
		
		this.id = id;
	}
}
