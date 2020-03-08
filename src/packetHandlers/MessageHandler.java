package packetHandlers;

import net.sf.jinsim.response.InSimResponse;
import net.sf.jinsim.response.MessageResponse;
import Main.InSimWrapper;
import packetInterfaces.packetHandler;

/**
 * @version 1.0
 * @author anrc
 * This class checks the count of swearwords a player used.
 * Penalties are given accordingly.
 */
public class MessageHandler implements packetHandler
{

	@Override
	public void handlePacket(InSimResponse packet, InSimWrapper wrapper)
	{
		if (packet instanceof MessageResponse)
		{
			MessageResponse _packet = (MessageResponse)packet;
			
			/*
			 * 	Get the users credentials
			 */
			byte UCID = (byte)_packet.getConnectionId();
			String userName;
			synchronized (wrapper.userManager)
			{
				userName = wrapper.userManager.getUsername(UCID, wrapper.id);
			}
			
			synchronized (wrapper.prevention)
			{
				if (userName != null)
				{
					//	Gets the number of swearwords the user has after this message
					int count = wrapper.prevention.bWordsPenalty(userName, _packet.getMessage().toLowerCase());
					switch(count)
					{
					case 0:
						break;
					case 1:
						wrapper.sendMessage(UCID, "^1Don't swear! This is your 1st time, so be more careful!");
						break;
					case 2:
						wrapper.sendMessage(UCID, "^1Don't swear! This is your 2nd time, you will be kicked next time!");
						break;
					case 3:
						wrapper.sendMessage(UCID, "^1Don't swear! This is your 3rd time, so byebye!");
						wrapper.sendCommand("/msg ^1" + userName + " gets kicked for swearing!");
						wrapper.sendCommand("/kick " + userName);
						wrapper.prevention.reset(userName);
						break;
					default:
						wrapper.sendMessage(UCID, "^1Byebye!");
						wrapper.sendCommand("/msg ^1" + userName + " gets kicked for swearing!");
						wrapper.sendCommand("/kick " + userName);
						wrapper.prevention.reset(userName);
						break;
					}
				}
			}
		}
	}

}
