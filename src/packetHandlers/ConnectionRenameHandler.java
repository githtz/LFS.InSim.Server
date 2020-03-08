package packetHandlers;

import org.openbakery.jinsim.response.ConnectionPlayerRenameResponse;
import org.openbakery.jinsim.response.InSimResponse;

import Main.InSimWrapper;
import packetInterfaces.packetHandler;

/**
 * @version 1.0
 * @author anrc
 * Handles what to do when a user choses a new playername
 */
public class ConnectionRenameHandler implements packetHandler
{

	public void handlePacket(InSimResponse packet, InSimWrapper wrapper)
	{
		if (packet instanceof ConnectionPlayerRenameResponse)
		{
			ConnectionPlayerRenameResponse _packet = (ConnectionPlayerRenameResponse)packet;
			
			byte UCID = (byte)_packet.getConnectionId();
			String playerName = _packet.getNewName();
			String userName;
			boolean isMember = false;
			synchronized (wrapper.userManager)
			{
				wrapper.userManager.changePlayerName(UCID, wrapper.id, playerName);
				userName = wrapper.userManager.getUsername(UCID, wrapper.id);
				isMember = wrapper.userManager.isMember(UCID, wrapper.id);
			}
			
			/*
			 * 	Check playername for tofu team tag
			 */
			/*if (InSimWrapper.containsTofu(playerName) && !isMember)
			{
				wrapper.sendCommand(playerName + " (" + userName + ") ^1was auto-kicked for faking!");
				wrapper.sendMessage(UCID, "^1You are not a Tofu member! Remove '^7Tofu[^1�^7] ^1from your name!");
				wrapper.sendCommand("/kick " + userName);
			}*/
			if (InSimWrapper.containsCurb(playerName) && !isMember)
			{
				wrapper.sendCommand(playerName + " (" + userName + ") ^1was auto-kicked for faking!");
				wrapper.sendMessage(UCID, "^1You are not a Curb member! Remove \"^7Curb<^1*^7>\" from your name!");
				wrapper.sendCommand("/kick " + userName);
			}
		}

	}

}
