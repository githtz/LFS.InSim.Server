package packetHandlers;

import net.sf.jinsim.response.InSimResponse;
import net.sf.jinsim.response.TakeOverCarResponse;
import Main.InSimWrapper;
import Main.Player;
import packetInterfaces.packetHandler;

public class PlayerCarTakeoverHandler implements packetHandler
{

	@Override
	public void handlePacket(InSimResponse packet, InSimWrapper wrapper)
	{
		if (packet instanceof TakeOverCarResponse)
		{			
			TakeOverCarResponse _packet = (TakeOverCarResponse)packet;
			
			byte O_UCID = (byte)_packet.getOldConnectionId();
			byte N_UCID = (byte)_packet.getNewConnectionId();
			
			synchronized (wrapper.playerList)
			{
				int size = wrapper.playerList.size();
				Player temp = null;
				for (int i = 0; i < size; i++)
				{
					temp = wrapper.playerList.get(i);
					if (temp.UCID == O_UCID)
					{
						wrapper.playerList.remove(i);
						temp.UCID = N_UCID;
						wrapper.playerList.add(temp);
						break;
					}
				}
			}
		}
	}

}
