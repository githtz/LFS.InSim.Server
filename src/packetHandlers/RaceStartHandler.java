package packetHandlers;

import org.openbakery.jinsim.Tiny;
import org.openbakery.jinsim.request.TinyRequest;
import org.openbakery.jinsim.response.InSimResponse;
import org.openbakery.jinsim.response.RaceStartResponse;

import main.InSimWrapper;
import packetInterfaces.packetHandler;

public class RaceStartHandler implements packetHandler
{

	public void handlePacket(InSimResponse packet, InSimWrapper wrapper)
	{
		if(packet instanceof RaceStartResponse)
		{
			TinyRequest req = new TinyRequest(Tiny.MULTI_CAR_INFO);
			wrapper.sendRequest(req);
		}		
	}

}