package packetHandlers;

import java.util.ArrayList;
import java.util.Date;

import net.sf.jinsim.Tiny;
import net.sf.jinsim.request.TinyRequest;
import net.sf.jinsim.response.InSimResponse;
import net.sf.jinsim.response.MultiCarInfoResponse;
import net.sf.jinsim.types.CarContact;
import net.sf.jinsim.types.CompCar;
import Main.InSimWrapper;
import Main.Player;
import Routines.DirectionControl;
import Routines.DirectionControl.Direction;
import packetInterfaces.packetHandler;

public class PlayerMovementPacketHandler implements packetHandler
{

	@Override
	public void handlePacket(InSimResponse packet, InSimWrapper wrapper)
	{
		if (packet instanceof MultiCarInfoResponse)
		{
			MultiCarInfoResponse _packet = (MultiCarInfoResponse)packet;
			ArrayList<CompCar> carList = _packet.getCarInfoList();
			
			synchronized(wrapper.playerList)
			{
				int size = carList.size();
				int psize = wrapper.playerList.size();
				CompCar temp = null;
				Player ptemp = null;
				
				for(int i = 0; i < size; i++)
				{
					temp = carList.get(i);
					for(int j = 0; j < psize; j++)
					{
						ptemp = wrapper.playerList.get(j);
						if(ptemp.PLID == temp.getPlayerId())
						{							
							Direction d = ptemp.getDrivingDirection(temp.getNode());
							if (d != wrapper.direction && wrapper.direction != Direction.CRUISE && d != Direction.CRUISE)
							{
								int max = 10;
								int time = ptemp.getDirectionTime();
								if (time < max)
								{
									String col = wrapper.direction == Direction.REVERSE ? "^1" : "^6";
									wrapper.sendMessage(ptemp.UCID, "^1ALERT: ^7You need to drive "+col+ wrapper.direction + " ^7here! ^7You will be spectated in ^1" + (max - time) + "^7 seconds");
								}
								else
								{
									String username = null;
									String nickname = null;
									synchronized (wrapper.userManager)
									{
										username = wrapper.userManager.getUsername(ptemp.UCID, wrapper.id);
										nickname = wrapper.userManager.getNickName(ptemp.UCID, wrapper.id);
									}
									if (username != null && nickname != null)
									{
										wrapper.sendCommand("/msg " + nickname + " ^7was spectated: ^3direction^7.");
										wrapper.sendCommand("/spec " + username);
										continue;
									}
								}
							}
							else
							{
								ptemp.refreshDirectionTime();
							}
							
							
							/*
							int x = temp.getPosition().getX(), y = temp.getPosition().getY();
							int time = ptemp.lastActive(x, y );
							int warn = 60;
							int countdown = 80;
							int max = 90;
							
							if(time > warn && time <= countdown)
							{
								wrapper.sendMessage(ptemp.UCID, "^3ALERT: ^7You need to drive!");
							}
							else if (time > countdown && time < max)
							{
								wrapper.sendMessage(ptemp.UCID, "^1ALERT: ^7You will be spectated in ^1" + (max - time) + "^7 seconds");
							}
							else if (time >= max)
							{
								String username = null;
								String nickname = null;
								synchronized (wrapper.userManager)
								{
									username = wrapper.userManager.getUsername(ptemp.UCID, wrapper.id);
									nickname = wrapper.userManager.getNickName(ptemp.UCID, wrapper.id);
								}
								if(username != null && nickname != null)
								{
									wrapper.sendCommand("/spec " + username);
									String alert_all = "/msg " + nickname + " ^7was spectated: ^3inactivity^7.";
									wrapper.sendCommand(alert_all);
								}
								//else
								//{
								//	System.out.println("Null entry for " + ptemp.UCID + " with username: " + username + " and playername: " + nickname + " in inactivity spec manager.");
								//}
							}
							*/
							
							break;
						}
					}
				}
			}
		}
	}

}
