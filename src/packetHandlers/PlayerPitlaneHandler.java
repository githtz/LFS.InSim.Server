package packetHandlers;

import net.sf.jinsim.response.InSimResponse;
import net.sf.jinsim.response.PitLaneResponse;
import Main.InSimWrapper;
import packetInterfaces.packetHandler;

/**
 * @version 1.0
 * @author anrc
 * This class handels when a player leaves the pitlane
 */
public class PlayerPitlaneHandler implements packetHandler
{

	@Override
	public void handlePacket(InSimResponse packet, InSimWrapper wrapper)
	{
		if(packet instanceof PitLaneResponse)
		{
			PitLaneResponse _packet = (PitLaneResponse)packet;
			if (_packet.isPitsExit())
			{
				//	Get the users credentials
				byte PLID = (byte)_packet.getPlayerId();
				String userName = null;
				byte UCID = (byte)255;
				synchronized (wrapper.playerList)
				{
					for (int i = 0; i < wrapper.playerList.size(); i++)
					{
						if (wrapper.playerList.get(i).PLID == PLID)
						{
							UCID = wrapper.playerList.get(i).UCID;
							break;
						}
					}
				}
				
				synchronized (wrapper.userManager)
				{
					userName = wrapper.userManager.getUsername(UCID,  wrapper.id);
				}
				
				//	Clear penalties and remove wrong way message
				if (userName != null)
				{
					wrapper.sendCommand("/p_clear " + userName);
					wrapper.sendCommand("/rcc_ply " + userName);
				}
			}
		}
	}

}
