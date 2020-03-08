package packetHandlers;

import org.openbakery.jinsim.response.InSimResponse;
import org.openbakery.jinsim.response.NewPlayerResponse;

import Main.InSimWrapper;
import Main.Player;
import Routines.DirectionControl.Direction;
import packetInterfaces.packetHandler;

public class PlayerJoinRaceHandler implements packetHandler
{

	public void handlePacket(InSimResponse packet, InSimWrapper wrapper)
	{
		if (packet instanceof NewPlayerResponse)
		{
			NewPlayerResponse _packet = (NewPlayerResponse)packet;
			Player p = new Player();
			Player temp = null;
			p.PLID = (byte)_packet.getPlayerId();
			p.UCID = (byte)_packet.getConnectionId();
			//p.x_pos = p.y_pos = 0;
			
			//System.out.println("START: "+this.toString());
			synchronized (wrapper.playerList)
			{
				int size = wrapper.playerList.size();
				for(int i = 0; i < size; i++)
				{
					temp = wrapper.playerList.get(i);
					//System.out.println("\tUCID: " + temp.UCID + " PLID: "+ temp.PLID);
					if(temp.PLID == p.PLID || temp.UCID == p.UCID)
					{
						wrapper.playerList.remove(i);
						--i;
						--size;	// TODO?!
					}
				}
				wrapper.playerList.add(p);
			}
			//System.out.println("END: "+this.toString());
			
			
			
			String command = "/rcm ";
			if (wrapper.direction == Direction.REVERSE)
				command += wrapper.wrongWay;
			else if (wrapper.direction == Direction.NORMAL)
				command += wrapper.normalWay;
			else if (wrapper.direction == Direction.CRUISE)
				command += wrapper.cruise;
				
			wrapper.sendCommand(command);
			String userName = null;
			synchronized (wrapper.userManager)
			{
				userName = wrapper.userManager.getUsername(p.UCID, wrapper.id);
			}
			if (userName != null)
			{
				command = "/rcm_ply " + userName;
				wrapper.sendCommand(command);
			}
			//else
			//{
			//	System.out.println("Null entry for " + p.UCID + " with username: " + userName + " in inactivity join race handler.");
			//}
		}
		
	}

}
