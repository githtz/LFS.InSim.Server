package packetHandlers.SpecialHandlers;

import org.openbakery.jinsim.PacketType;
import org.openbakery.jinsim.response.InSimResponse;

import Main.InSimWrapper;
import extensions.AdminCommandReport;
import packetInterfaces.packetHandler;

/**
 * @version 1.0
 * @author anrc
 * Uses commands like kick and ban on other servers as well 
 */
public class CommandExecutorHandler implements packetHandler
{

	public void handlePacket(InSimResponse packet, InSimWrapper wrapper)
	{
		if (packet.getPacketType() == PacketType.ADMIN_COMMAND_REPORT)
		{
			AdminCommandReport _packet = (AdminCommandReport)packet;
			
			String message = _packet.getMessage();
			
			boolean send = _packet.isAdmin();
			boolean result = _packet.getResult() == 1;
			boolean notHost = _packet.getConnectionID() > 0;
			boolean unBanKick = (message.startsWith("/ban ") || message.startsWith("/kick ") || message.startsWith("/unban "));
			
			if (send && result && notHost && unBanKick)
			{
				String command = _packet.getMessage();
				synchronized (wrapper.crossChat)
				{
					wrapper.crossChat.sendCommand(command);
				}
				System.out.println(command);
				wrapper.sendMessage((byte)_packet.getConnectionID(), "^1Command executed for user on all servers: " + message);
			}
		}
	}

}
