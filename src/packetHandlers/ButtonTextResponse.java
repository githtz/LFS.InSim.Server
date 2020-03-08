package packetHandlers;

import java.util.ArrayList;

import org.openbakery.jinsim.response.ButtonTypeResponse;
import org.openbakery.jinsim.response.InSimResponse;

import Main.InSimWrapper;
import packetInterfaces.packetHandler;

/**
 * @version 1.0
 * @author anrc
 * Determines what happens when a specific button used for text-messages sends the user input
 * Example: pm-button, teamchat-button
 */
public class ButtonTextResponse implements packetHandler
{

	public void handlePacket(InSimResponse packet, InSimWrapper wrapper)
	{
		if (packet instanceof ButtonTypeResponse)
		{
			ButtonTypeResponse _packet = (ButtonTypeResponse)packet;
			byte UCID = (byte)_packet.getConnectionId();
			String message = _packet.getTypeInText();
			byte reqID = (byte)_packet.getRequestInfo();
			
			if (reqID == wrapper.TEAMCHATID)
			{
				synchronized (wrapper.crossChat)
				{
					wrapper.crossChat.sendTeamMessage(UCID, wrapper.id, message);
				}
			}
			else if (reqID == wrapper.PMID)
			{
				ArrayList<Short> userList = null;
				short OUCID = 0;
				
				synchronized (wrapper.userManager)
				{
					userList = wrapper.userManager.getAllUserOUCIDs();
					OUCID = userList.get( (_packet.getClickId() - wrapper.CMD_OFFSET) / 4);
				}
				
				if (OUCID != 0)
				{
					synchronized (wrapper.crossChat)
					{
						wrapper.crossChat.sendPrivateMessage(UCID, wrapper.id, OUCID, _packet.getTypeInText());
					}
				}
			}
			else if (reqID == wrapper.REPORTID)
			{
				synchronized (wrapper.crossChat)
				{
					wrapper.crossChat.reportToAdmin(UCID, wrapper.id, message);
				}
			}
		}
	}

}
