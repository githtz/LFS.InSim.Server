package packetHandlers;

import org.openbakery.jinsim.response.InSimResponse;
import org.openbakery.jinsim.response.PlayerLeavingResponse;

import Main.InSimWrapper;
import Main.Player;
import packetInterfaces.packetHandler;

public class PlayerLeaveRaceHandler implements packetHandler
{
	public void handlePacket(InSimResponse packet, InSimWrapper wrapper)
	{
		if (packet instanceof PlayerLeavingResponse)
		{
			PlayerLeavingResponse _packet = (PlayerLeavingResponse)packet;
			Player temp = null;
			@SuppressWarnings("unused")
			byte UCID = -1;
			synchronized (wrapper.playerList)
			{
				int size = wrapper.playerList.size();
				for (int i = 0; i < size; i++)
				{
					temp = wrapper.playerList.get(i);
					//System.out.println("\tUCID: " + temp.UCID + " PLID: "+ temp.PLID);
					if (temp.PLID == _packet.getPlayerId())
					{
						UCID = temp.UCID;
						wrapper.playerList.remove(i);
						break;
					}
				}
			}	
		}
	}

}
