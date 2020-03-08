package packetInterfaces;

import org.openbakery.jinsim.response.InSimResponse;

import main.*;

public interface packetHandler
{
	public void handlePacket(InSimResponse packet, InSimWrapper wrapper);
}
