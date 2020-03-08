package packetHandlers.hiddenMessageHandlers;

import org.openbakery.jinsim.response.HiddenMessageResponse;
import org.openbakery.jinsim.response.InSimResponse;

import Main.InSimWrapper;
import packetInterfaces.packetHandler;

/**
 * @version 1.0
 * @author anrc
 * This handler handles team messages and team-admin commands
 */
public class TeamHandler implements packetHandler
{

	public void handlePacket(InSimResponse packet, InSimWrapper wrapper)
	{
		if (packet instanceof HiddenMessageResponse)
		{
			HiddenMessageResponse _packet = (HiddenMessageResponse)packet;
			String msg = _packet.getMessage();
			byte UCID = (byte)_packet.getConnectionId();
			
			
			if (msg.startsWith("team"))
			{
				String[] split = msg.split(" ", 2);
				if (split.length == 2)
				{
					String message = split[1];
					
					synchronized (wrapper.crossChat)
					{
						wrapper.crossChat.sendTeamMessage(UCID, wrapper.id, message);
					}	
				}
				else
				{
					wrapper.createTeamchatButton(UCID);
				}
			}
			else if (msg.startsWith("report"))
			{
				String[] split = msg.split(" ", 2);
				if (split.length < 2)
				{
					wrapper.createReportButton(UCID);
				}
				else
				{
					String message = split[1];
					
					synchronized (wrapper.crossChat)
					{
						wrapper.crossChat.reportToAdmin(UCID, wrapper.id, message);
					}
				}
				
			}
			else if (msg.startsWith("kick "))
			{
				boolean isMember = false;
				String userName;
				String nickName;
				
				synchronized (wrapper.userManager)
				{
					isMember = wrapper.userManager.isMember(UCID, wrapper.id);
					userName = wrapper.userManager.getUsername(UCID, wrapper.id);
					nickName = wrapper.userManager.getNickName(UCID, wrapper.id);
				}
				
				
				if (isMember)
				{
					String name = msg.split(" ", 2)[1];
					short OUCID = -1;
					try
					{
						OUCID =  Short.parseShort(name);
					}
					catch (NumberFormatException e)
					{
						
					}
					
					boolean cankick = false;
					
					if (OUCID > 0)
					{
						synchronized (wrapper.userManager)
						{
							cankick = wrapper.userManager.isKickable(OUCID);
							name = wrapper.userManager.getUserName(OUCID);
						}
					}
					else
					{
						synchronized (wrapper.userManager)
						{
							cankick = wrapper.userManager.isKickable(name);
						}
					}
					
					if (cankick)
					{
						String message = nickName + " ^7(" + userName + ") attempts to kick " + name;
						synchronized (wrapper.crossChat)
						{
							wrapper.crossChat.sendTeamMessage(message);
							wrapper.crossChat.sendCommand("/kick " + name);
						}
					}
					else
					{
						String message = "^1You can't kick members, admins or users that don't exist.";
						wrapper.sendMessage(UCID, message);
					}
					
				}
			}
			
			else if (msg.startsWith("ban "))
			{
				boolean isMember = false;
				String userName;
				String nickName;
				
				synchronized (wrapper.userManager)
				{
					isMember = wrapper.userManager.isMember(UCID, wrapper.id);
					userName = wrapper.userManager.getUsername(UCID, wrapper.id);
					nickName = wrapper.userManager.getNickName(UCID, wrapper.id);
				}
				
				if (isMember)
				{
					String[] split = msg.split(" ", 2);
					if (split.length == 2)
					{
						String name = split[1];
						short OUCID = -1;
						try
						{
							OUCID = Short.parseShort(name);
						}
						catch(NumberFormatException e)
						{
							
						}
						
						boolean canBan = false;
						
						if (OUCID > 0)
						{
							synchronized (wrapper.userManager)
							{
								canBan = wrapper.userManager.isKickable(OUCID);
								name = wrapper.userManager.getUserName(OUCID);
							}
						}
						else
						{
							synchronized (wrapper.userManager)
							{
								canBan = wrapper.userManager.isKickable(name);
							}
						}
						
						if (canBan)
						{
							String message = nickName + " ^7(" + userName + ") attempts to ban " + name;
							synchronized (wrapper.crossChat)
							{
								wrapper.crossChat.sendTeamMessage(message);
								wrapper.crossChat.sendCommand("/ban " + name + " 0");
							}
						}
						else
						{
							String message = "^1You can't ban members, admins or users that don't exist.";
							wrapper.sendMessage(UCID, message);
						}
					}
				}
			}
			
			else if (msg.startsWith("spec "))
			{
				boolean isMember = false;
				String userName;
				String nickName;
				
				synchronized (wrapper.userManager)
				{
					isMember = wrapper.userManager.isMember(UCID, wrapper.id);
					userName = wrapper.userManager.getUsername(UCID, wrapper.id);
					nickName = wrapper.userManager.getNickName(UCID, wrapper.id);
				}
				
				
				if (isMember)
				{
					String[] split = msg.split(" ", 2);
					if (split.length == 2)
					{
						String name = split[1];
						short OUCID = -1;
						try
						{
							OUCID = Short.parseShort(name);
						}
						catch (NumberFormatException e)
						{
							
						}
						
						boolean canSpec = false;
						
						if (OUCID > 0)
						{
							synchronized (wrapper.userManager)
							{
								canSpec = wrapper.userManager.isKickable(OUCID);
								name = wrapper.userManager.getUserName(OUCID);
							}
						}
						else
						{
							synchronized (wrapper.userManager)
							{
								canSpec = wrapper.userManager.isKickable(name);
							}
						}
						
						if (canSpec)
						{
							String message = nickName + " ^7(" + userName + ") attempts to spectate " + name;
							synchronized (wrapper.crossChat)
							{
								wrapper.crossChat.sendTeamMessage(message);
								wrapper.crossChat.sendCommand("/spec " + name);
							}
						}
						else
						{
							String message = "^1You can't spectate members, admins or users that don't exist.";
							wrapper.sendMessage(UCID, message);
						}
					}
				}
			}
		}

	}

}
