package packetHandlers;

import net.sf.jinsim.Tiny;
import net.sf.jinsim.request.TinyRequest;
import net.sf.jinsim.response.InSimResponse;
import Main.InSimWrapper;
import packetInterfaces.packetHandler;
import net.sf.jinsim.response.RaceStartResponse;

public class RaceStartHandler implements packetHandler
{

	@Override
	public void handlePacket(InSimResponse packet, InSimWrapper wrapper)
	{
		if(packet instanceof RaceStartResponse)
		{
			TinyRequest req = new TinyRequest(Tiny.MULTI_CAR_INFO);
			wrapper.sendRequest(req);
		}		
	}

}