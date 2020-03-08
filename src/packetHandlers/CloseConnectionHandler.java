package packetHandlers;

import org.openbakery.jinsim.response.ConnectionLeaveResponse;
import org.openbakery.jinsim.response.InSimResponse;

import Main.InSimWrapper;
import Main.Player;
import packetInterfaces.packetHandler;

/**
 * @version 1.0
 * @author anrc
 * Removes a users from the UserManagement when he disconnects
 */
public class CloseConnectionHandler implements packetHandler
{

	public void handlePacket(InSimResponse packet, InSimWrapper wrapper)
	{
		if (packet instanceof ConnectionLeaveResponse)
		{
			ConnectionLeaveResponse _packet = (ConnectionLeaveResponse)packet;
			byte UCID = (byte)_packet.getConnectionId();						
			synchronized (wrapper.playerList)
			{
				int size = wrapper.playerList.size();
				Player temp = null;
				
				for (int i = 0; i < size; i++)
				{
					temp = wrapper.playerList.get(i);
					if (temp.UCID == UCID)
					{
						wrapper.playerList.remove(i);
						break;
					}
				}
			}
			synchronized (wrapper.userManager)
			{
				wrapper.userManager.remUser(UCID, wrapper.id);
			}
			
			synchronized (wrapper.crossChat)
			{
				wrapper.crossChat.refreshConnectionList();
			}
		}
	}

}
