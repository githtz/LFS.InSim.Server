package packetHandlers.hiddenMessageHandlers;

import java.util.ArrayList;

import net.sf.jinsim.response.HiddenMessageResponse;
import net.sf.jinsim.response.InSimResponse;
import Main.InSimWrapper;
import Main.Main;
import Routines.DirectionControl.Direction;
import packetInterfaces.packetHandler;

/**
 * @version 1.0
 * @author anrc
 * Handles admin command messages
 */
public class AdminHandler implements packetHandler
{

	@Override
	public void handlePacket(InSimResponse packet, InSimWrapper wrapper)
	{
		if (packet instanceof HiddenMessageResponse)
		{
			HiddenMessageResponse _packet = (HiddenMessageResponse)packet;
			String msg = _packet.getMessage();
			byte UCID = (byte)_packet.getConnectionId();
			
			if (msg.startsWith("direction "))
			{
				boolean isAdmin = false;
				synchronized (wrapper.userManager)
				{
					isAdmin = wrapper.userManager.isAdmin(UCID, wrapper.id);
				}
				
				if (isAdmin)
				{
					String[] split = msg.split(" ", 2);
					if (split[1].equals("normal"))
					{
						wrapper.direction = Direction.NORMAL;
						wrapper.sendMessage(UCID, "^7Direction set to ^6normal^7.");
					}
					else if (split[1].equals("reverse"))
					{
						wrapper.direction = Direction.REVERSE;
						wrapper.sendMessage(UCID, "^7Direction set to ^1reverse^7.");
					}
					else if (split[1].equals("cruise"))
					{
						wrapper.direction = Direction.CRUISE;
						wrapper.sendMessage(UCID, "^7Direction set to ^2cruise^7.");
					}
					else
					{
						wrapper.sendMessage(UCID, "^1Can't set direction. Valid directions are ^7normal^1, ^7reverse^1, ^7cruise^1!");
					}
				}
			}
			//	TODO NEEDS TESTING
			else if(msg.startsWith("addmember ") || msg.startsWith("removemember "))
			{
				String admin = "sonic chao";
				boolean hasright = false;
				boolean add_action = msg.startsWith("addmember ");
				synchronized (wrapper.userManager)
				{
					String name = wrapper.userManager.getUsername(UCID, wrapper.id).toLowerCase();
					hasright = name.equals(admin);
				}

				String[] split = msg.split(" ", 2);
				String name = split[1];
				System.out.println(name);//DEBUG
				
				hasright = hasright && (!name.equals(admin));
				
				if(hasright)
				{
					synchronized (wrapper.userManager)
					{
						short OUCID = wrapper.userManager.getOUCIDofUser(name);
						if(OUCID != -1)
						{
							wrapper.userManager.setMemberStatus(OUCID, add_action);
							wrapper.userManager.saveMemberList(Main.config.memberlist);
						}
						else
							wrapper.sendMessage(UCID, "^1You can only add players that are playing on the server.");
					}

					String action = add_action ? " added " : " removed ";
					wrapper.sendMessage(UCID, "^6Member ^3" + name + " ^7was sucsessfully" + action);
					//	TODO THIS NEEDS TO GO SOMEWHERE ELSE
					
				}
				else
					wrapper.sendMessage(UCID, "You can not execute this command");
			}
		}
	}

}
