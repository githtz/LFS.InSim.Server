package packetHandlers;

import org.openbakery.jinsim.response.ButtonFunctionResponse;
import org.openbakery.jinsim.response.InSimResponse;

import Main.InSimWrapper;
import packetInterfaces.packetHandler;

/**
 * @version 1.0
 * @author anrc
 * Changes the values in the UserManagement if a user removes
 * all buttons from his screen (shift+i)
 */
public class ButtonRemoveHandler implements packetHandler
{

	public void handlePacket(InSimResponse packet, InSimWrapper wrapper)
	{
		if (packet instanceof ButtonFunctionResponse)
		{
			ButtonFunctionResponse _packet = (ButtonFunctionResponse)packet;
			byte UCID = (byte)_packet.getConnectionId();
			String username = "", nickname = ""; 
			boolean accept = true;
			
			/*
			 * Disables all buttons in the UserManagement
			 */
			synchronized (wrapper.userManager)
			{
				wrapper.userManager.setListStatus(UCID, wrapper.id, false);
				wrapper.userManager.setReportStatus(UCID, wrapper.id, false);
				wrapper.userManager.setTeamchatStatus(UCID, wrapper.id, false);
				accept = wrapper.userManager.getAccepted(UCID, wrapper.id);
				if (!accept)
				{
					username = wrapper.userManager.getUsername(UCID, wrapper.id);
					nickname = wrapper.userManager.getNickName(UCID, wrapper.id);
				}
			}
			
			/*
			 * 	Removes player from the server when he used shift+i and did not accept the rules
			 */
			if (!accept)
			{
				wrapper.sendMessage(UCID, "^1You can not stay here without accepting the rules");
				wrapper.sendCommand("/msg " + nickname + " ^1did not accept the rules.");
				try
				{
					Thread.sleep(1000);
				}
				catch(InterruptedException e)
				{
					
				}
				wrapper.sendCommand("/kick " + username);
			}
		}

	}

}
