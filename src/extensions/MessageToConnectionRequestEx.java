package extensions;

import java.nio.ByteBuffer;

import net.sf.jinsim.PacketType;
import net.sf.jinsim.request.InSimRequest;

/**
 * @version 1.0
 * @author anrc
 * This class overrides the original MTCR Request and
 * extends it's functionality. It allows more characters
 * to be sent in one request.
 */
public class MessageToConnectionRequestEx extends InSimRequest
{

	private int connectionId;
	private String message;

	public MessageToConnectionRequestEx() {
		super(PacketType.MESSAGE_TO_CONNECTION, 128); //72
	}
	
	public MessageToConnectionRequestEx(int connectionId, String message) {
		this();
		this.connectionId = connectionId;
		this.message = message;
	}
	
	public void assemble(ByteBuffer buffer) {
        super.assemble(buffer);
        buffer.put((byte)0);
        buffer.put((byte)connectionId);
        buffer.put((byte)0);
        buffer.put((byte)0);
        buffer.put((byte)0);
        assembleString(buffer, message, 120);
    }

	public void setConnectionId(int connectionId) {
		this.connectionId = connectionId;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}