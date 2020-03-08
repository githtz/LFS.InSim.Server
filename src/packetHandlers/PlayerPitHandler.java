package packetHandlers;

import org.openbakery.jinsim.response.InSimResponse;
import org.openbakery.jinsim.response.PlayerPitsResponse;

import main.InSimWrapper;
import packetInterfaces.packetHandler;

/**
 * @version 1.0
 * @author anrc
 * This class handles what happens if a player goes to the pits
 */
public class PlayerPitHandler implements packetHandler 
{

	public void handlePacket(InSimResponse packet, InSimWrapper wrapper)
	{
		if (packet instanceof PlayerPitsResponse)
		{
			PlayerPitsResponse _packet = (PlayerPitsResponse)packet;
			byte PLID = (byte)_packet.getPlayerId();
			@SuppressWarnings("unused")
			byte UCID = -1;
			synchronized (wrapper.playerList)
			{
				for (int i = 0 ; i < wrapper.playerList.size(); i++)
				{
					if (PLID == wrapper.playerList.get(i).PLID)
					{
						UCID = wrapper.playerList.get(i).UCID;
						break;
					}
				}
			}
			
			/*String userName;
			synchronized (wrapper.userManager)
			{
				userName = wrapper.userManager.getUsername(UCID, wrapper.id);
			}*/
			/*if (userName != null)
			{
				OnlineLogWrapper w = OnlineLogWrapper.getWrapper();
				w.leaveRace(userName);
			}*/
		}
	}
}
