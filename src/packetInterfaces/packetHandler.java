package packetInterfaces;

import org.openbakery.jinsim.response.InSimResponse;

import Main.*;

public interface packetHandler
{
	public void handlePacket(InSimResponse packet, InSimWrapper wrapper);
}
