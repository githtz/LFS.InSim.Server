package packetHandlers;

import java.util.ArrayList;

import org.openbakery.jinsim.response.ButtonClickedResponse;
import org.openbakery.jinsim.response.InSimResponse;

import main.InSimWrapper;
import packetInterfaces.packetHandler;

/**
 * @version 1.0
 * @author anrc
 * Determines what happens if a user presses a button
 */
public class ButtonClickHandler implements packetHandler
{
	public void handlePacket(InSimResponse packet, InSimWrapper wrapper)
	{
		if (packet instanceof ButtonClickedResponse)
		{
			ButtonClickedResponse _packet = (ButtonClickedResponse)packet;
			byte UCID = (byte)_packet.getConnectionId();
			byte reqID = (byte)_packet.getRequestInfo();
			
			if (reqID >= wrapper.SPECID && reqID <= wrapper.BANID)
			{
				ArrayList<Short> userList = null;
				String userName_k = null;
				String userName_e = null;
				String nickName_e = null;
				boolean isKickable_k = false;
				boolean isMember_e = false;
				
				synchronized (wrapper.userManager)
				{
					userList = wrapper.userManager.getAllUserOUCIDs();
					short OUCID = 0;
					if (reqID == wrapper.SPECID)
						OUCID = userList.get( (_packet.getClickId() - 1 - wrapper.CMD_OFFSET) / 4 );
					else if (reqID == wrapper.KICKID)
						OUCID = userList.get( (_packet.getClickId() - 2 - wrapper.CMD_OFFSET) / 4 );
					else if (reqID == wrapper.BANID)
						OUCID = userList.get( (_packet.getClickId() - 3 - wrapper.CMD_OFFSET) / 4 );
					
					userName_k = wrapper.userManager.getUserName(OUCID);
					isKickable_k = wrapper.userManager.isKickable(OUCID);
					isMember_e = wrapper.userManager.isMember(UCID, wrapper.id);
					userName_e = wrapper.userManager.getUsername(UCID, wrapper.id);
					nickName_e = wrapper.userManager.getNickName(UCID, wrapper.id);
				}
				if (userName_k != null && isKickable_k && isMember_e)
				{				
					synchronized (wrapper.crossChat)
					{
						if (reqID == wrapper.SPECID)
						{
							String message = nickName_e + " ^7(" + userName_e + ") attempts to spec " + userName_k;
							wrapper.crossChat.sendTeamMessage(message);
							wrapper.crossChat.sendCommand("/spec " + userName_k);
						}
						else if (reqID == wrapper.KICKID)
						{
							String message = nickName_e + " ^7(" + userName_e + ") attempts to kick " + userName_k;
							wrapper.crossChat.sendTeamMessage(message);
							wrapper.crossChat.sendCommand("/kick "+ userName_k);
						}
						else if (reqID == wrapper.BANID)
						{
							String message = nickName_e + " ^7(" + userName_e + ") attempts to ban " + userName_k;
							wrapper.crossChat.sendTeamMessage(message);
							wrapper.crossChat.sendCommand("/ban " + userName_k + " 0");
						}
					}
				}
				else
				{
					String message = "^1You can't use commands on members, admins or users that don't exist.";
					wrapper.sendMessage(UCID, message);
				}
			}
			else if (reqID == wrapper.L11)
			{
				wrapper.removeButtons(UCID);
				
				synchronized (wrapper.userManager )
				{
					wrapper.sendMessage(UCID, "^1You can not stay here without accepting the rules");
					wrapper.sendCommand("/msg " + wrapper.userManager.getNickName(UCID, wrapper.id) + " ^1did not accept the rules.");
				}
				
				try
				{
					Thread.sleep(1000);
				}
				catch(InterruptedException e)
				{
					
				}
				wrapper.sendCommand("/kick " + wrapper.userManager.getUsername(UCID, wrapper.id));
			}
			else if (reqID == wrapper.L12)
			{
				boolean member;
				synchronized(wrapper.userManager)
				{
					member = wrapper.userManager.isMember(UCID, wrapper.id);
					wrapper.userManager.setAccepted(UCID, wrapper.id);
				}
				
				wrapper.sendMessage(UCID, "^1Please stick to the rules!");
				wrapper.removeButtons(UCID);
				if (!member)
				{
					wrapper.createReportButton(UCID);
					
				}
				wrapper.sendMessage(UCID, "^1INFO: ^7Press ^5shift+i ^7to hide the buttons!");
			}
		}
	}

}
