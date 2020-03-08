package packetHandlers.hiddenMessageHandlers;

import org.openbakery.jinsim.response.HiddenMessageResponse;
import org.openbakery.jinsim.response.InSimResponse;

import main.InSimWrapper;
import packetInterfaces.packetHandler;

/**
 * @version 1.0
 * @author anrc
 * Handles normal commands send with /i 
 */
public class GeneralHandler implements packetHandler
{

	public void handlePacket(InSimResponse packet, InSimWrapper wrapper)
	{
		if (packet instanceof HiddenMessageResponse)
		{
			HiddenMessageResponse _packet = (HiddenMessageResponse)packet;
			String msg = _packet.getMessage();
			byte UCID = (byte)_packet.getConnectionId();
			
			if (msg.startsWith("help"))
			{
				boolean isMember = false;
				boolean isAdmin = false;
				synchronized (wrapper.userManager)
				{
					isMember = wrapper.userManager.isMember(UCID, wrapper.id);
					isAdmin = wrapper.userManager.isAdmin(UCID, wrapper.id);
				}
				
				wrapper.sendMessage(UCID, "^3The following commands can be used on this server:");
				wrapper.sendMessage(UCID, "^3/i help - ^7shows this help");
				wrapper.sendMessage(UCID, "^3/i list - ^7shows a list of all connected users and their ^1<chatID>");
				wrapper.sendMessage(UCID, "^3/i pm ^1<chat ID> ^2<message> ^3- ^7sends a private message to the user");
				wrapper.sendMessage(UCID, "^3/i report ^2<message> ^7- sends a message to all admins. Use it to report players.");
				if (isMember)
				{
					wrapper.sendMessage(UCID, "^3/i team ^2<message> ^7- sends a message to all connected team members");
					wrapper.sendMessage(UCID, "^3/i kick ^6<userName> ^7OR ^3/i kick ^1<chatID> ^7- kicks the player");
					wrapper.sendMessage(UCID,"^3/i ban ^6<userName> ^7 OR ^3/i ban ^1<chatID> ^7- bans the player for 12 hours");
					wrapper.sendMessage(UCID,"^3/i spec ^6<userName> ^7 OR ^3/i spec ^1<chatID> ^7- spectates the player");
				}
				if (isAdmin)
				{
					wrapper.sendMessage(UCID, "^3/i direction ^5<direction> ^7- set direction start message (reverse, normal, cruise)");
				}
			}
			else if (msg.startsWith("list"))
			{
				wrapper.createConnectionList(UCID);
			}
			else if(msg.startsWith("pm "))
			{
				String[] split = msg.split(" ", 3);
				if (split.length < 3)
					return;
				
				try
				{
					byte OUCID = Byte.parseByte(split[1]);
					String message = split[2];	
					synchronized (wrapper.crossChat)
					{
						wrapper.crossChat.sendPrivateMessage(UCID, wrapper.id, OUCID, message);
					}
				}
				catch(NumberFormatException e)
				{
					wrapper.sendMessage(UCID, "^1You need to enter a valid ID");
				}
				// TODO add support for usernames additional to the OUCID
			}
		}
	}

}
