package extensions;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import org.openbakery.jinsim.Encoding;
import org.openbakery.jinsim.PacketType;
import org.openbakery.jinsim.response.InSimResponse;

/**
 * @version 1.0
 * @author anrc
 * This class implements AdminCommandReports.
 * Represents a Response that informs about the usage
 * of admin commands.
 * Find out more in the InSim documentation at
 * @see <LFS INSTALL DIR>\docs\Insim.txt
 */
public class AdminCommandReport extends InSimResponse
{
	private byte connectionID;
	
	private boolean admin;
	
	private byte result;
	
	private String message;

	public AdminCommandReport()
	{
		super(PacketType.ADMIN_COMMAND_REPORT);
	}
	
	public void construct(ByteBuffer buffer) throws BufferUnderflowException
	{
		super.construct(buffer);
		buffer.position(buffer.position() +1);
		connectionID = buffer.get();
		admin = (buffer.get() > 0);
		result = buffer.get();
		byte[] rawMessage = getBytes(buffer, 64);
		message = Encoding.decodeString(rawMessage);
	}
	
	public String toString()
	{
		String retVal = super.toString();
		retVal += getMessage() + "\n";
		return retVal;
	}
	
	public String getMessage()
	{
		return this.message;
	}
	
	public void setMessage(String message)
	{
		this.message = message;
	}
	
	
	public int getConnectionID()
	{
		return connectionID & 0xFF;
	}
	
	public boolean isAdmin()
	{
		return admin;
	}
	
	public int getResult()
	{
		return result & 0xFF;
	}
}