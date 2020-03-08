package Main;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.openbakery.jinsim.SimpleClient;
import org.openbakery.jinsim.request.ButtonFunctionRequest;
import org.openbakery.jinsim.request.ButtonRequest;
import org.openbakery.jinsim.request.ButtonSubtype;
import org.openbakery.jinsim.request.MessageRequest;
import org.openbakery.jinsim.request.TinyRequest;
import org.openbakery.jinsim.response.InSimListener;
import org.openbakery.jinsim.response.InSimResponse;

import ChatAddons.SwearWordPrevention;
import Routines.DirectionControl;
import Routines.DirectionControl.Direction;
import extensions.MessageToConnectionRequestEx;
import packetInterfaces.packetHandler;

public class InSimWrapper implements InSimListener
{
	/*
	 * 
	 */
	public byte id;
	public static int counter = 0;
	public SimpleClient client;
	public String hostname;
	public final String wrongWay = "^1=== ^7Please drive ^1REVERSE^7! ^1==��";
	public final String normalWay = "^6��== ^7Please drive ^6NORMAL^7! ^6===";
	public final String cruise = "^6��== ^7Cruise allowed! ^1==��";
	
	/*
	 * The ID's used here are for all pre-defined Buttons created.
	 */
	public final byte TEAMCHATID = 1;
	public final byte PMID = 2;
	public final byte SHOWID = 3;
	public final byte SPECID = 4;
	public final byte KICKID = 5;
	public final byte BANID = 6;
	public final byte INFOID = 7;
	public final byte L1 = 8;
	public final byte L2 = 9;
	public final byte L3 = 10;
	public final byte L4 = 11;
	public final byte L5 = 12;
	public final byte L6 = 13;
	public final byte L7 = 14;
	public final byte L8 = 15;
	public final byte L9 = 16;
	public final byte L10 = 17;
	public final byte L11 = 18;
	public final byte L12 = 19;
	public final byte REPORTID = (byte)20;
	public final byte CMD_OFFSET = 21;
	
	public long last_update = 0;
	
	public final UserManagement userManager = UserManagement.CreateUsermanagement();
	public final CrossChat crossChat = CrossChat.CreateCrossChat();
	public DirectionControl.Direction direction;
	public SwearWordPrevention prevention;
	
	public static packetHandler[] handlers;
	
	public ArrayList<Player> playerList;
	
	private ExecutorService service = Executors.newFixedThreadPool( 16 ); //TODO static?
	
	public InSimWrapper(SimpleClient client)
	{
		this.id = (byte)counter++;
		this.client = client;
		this.hostname = null;
		this.direction = Direction.CRUISE;
		this.playerList = new ArrayList<Player>();
		this.prevention = new SwearWordPrevention();
		this.prevention.loadList(Main.config.swearwords);
	}
	
	/**
	 * Sends a message to a user on the host
	 * @param UCID of the user to send the message to
	 * @param message to be send to the user
	 */
	public synchronized void sendMessage(byte UCID, String message)
	{
		
		MessageToConnectionRequestEx msg = new MessageToConnectionRequestEx();
		msg.setConnectionId(UCID);
		msg.setMessage(message);
		
		try
		{
			this.client.send(msg);
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Executes a command on the host
	 * @param command to be executed
	 */
	public synchronized void sendCommand(String command)
	{
		MessageRequest msg = new MessageRequest();
		msg.setMessage(command);
		try
		{
			this.client.send(msg);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Sends a TinyRequest packet to the server
	 * @param request 
	 */
	public synchronized void sendRequest(TinyRequest request)
	{
		try
		{
			this.client.send(request);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Shuts down the client. Host will remain running.
	 */
	public void quit()
	{
		try
		{
			client.close();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Determines whether a name contains the tofu team tag or not
	 * @param playerName of the user to be checked
	 * @return true if the name contains the tofu team tag, false if not
	 */
	public static boolean containsTofu(String playerName)
	{
		String[] split = playerName.toLowerCase().split("\\^[0-9]");
		playerName = "";
		for (int i = 0; i < split.length; i++)
			playerName += split[i];
		if (playerName.matches("t.f.\\[�\\].*"))
		//if (playerName.contains( "Tofu[�]".toLowerCase()) || playerName.contains( ("T"+ (char)959 + "fu[�]").toLowerCase()) || playerName.contains( ("T"+ (char)1086 + "fu[�]").toLowerCase()) )
			return true;
		else 
		{
			return false;
		}	
	}
	
	public static boolean containsCurb(String playerName)
	{
		String[] split = playerName.toLowerCase().split("\\^[0-9]");
		playerName = "";
		for (int i = 0; i < split.length; i++)
			playerName += split[i];
		if (playerName.matches("c.r.\\^l\\*\\^r.*"))
			return true;
		else 
		{
			return false;
		}	
	}
	
	/**
	 * Will be executed when the client receives a packet
	 */
	public void packetReceived(InSimResponse response)
	{
		service.execute(new runner(response, this));
		/*
		long now = new Date().getTime();
		
		if(now - this.last_update >= 500)
		{				
			this.last_update = now;
			TinyRequest req = new TinyRequest(Tiny.MULTI_CAR_INFO);
			this.sendRequest(req);
		}
		*/
	}
	
	/**
	 * Removes the buttons of a user
	 * @param UCID of the user
	 */
	public void removeButtons(byte UCID)
	{
		ButtonFunctionRequest req = new ButtonFunctionRequest();
		req.setSubtype(ButtonSubtype.CLEAR);
		req.setConnectionId(UCID);
		try
		{
			client.send(req);
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Creates the connection list for a user
	 * @param UCID of the user
	 */
	public void createConnectionList(byte UCID)
	{
		short OUCID = -1;
		ArrayList<String> users;
		ArrayList<Short> OUCIDs;
		ArrayList<Short> unbannable;
		boolean isMember = false;
		
		synchronized (userManager)
		{
			OUCID = userManager.getOuterUserID(UCID, id);
			userManager.setListStatus((byte)OUCID, true);
			
			users = userManager.getAllUserNames(true);
			isMember = userManager.isMember(UCID, id);
			OUCIDs = userManager.getAllUserOUCIDs();
			unbannable = userManager.getAllMemberOUCIDs();
		}
		
		int size = users.size();
		
		int btnheight = 160 / size;
		if (btnheight > 5)
			btnheight = 5;
		
		for(int i = 0; i < size; i++)
		{
			ButtonRequest namebox = new ButtonRequest();
			namebox.setConnectionId(UCID);
			namebox.setTypeIn((byte)77);
			namebox.setWidth((byte)60);//60 //96
			namebox.setHeight((byte)btnheight);
			namebox.setLeft((byte)70);//70 //34
			namebox.setTop((byte)(10 + btnheight * i));
			namebox.setClickId((byte)(CMD_OFFSET + i * 4));
			namebox.setRequestInfo(PMID);
			namebox.setButtonStyle(ButtonRequest.BUTTON_STYLE_BLUE + ButtonRequest.BUTTON_STYLE_CLICK + ButtonRequest.BUTTON_STYLE_DARK);
			namebox.setText(users.get(i) + " (WRITE PM)" + "\0");
			
			
			ButtonRequest specbox = null, kickbox = null, banbox = null;
			boolean allowkick = false;
			
			if (!unbannable.contains(OUCIDs.get(i)))
			{
				allowkick = true;
				
				specbox = new ButtonRequest();
				specbox.setConnectionId(UCID);
				specbox.setWidth((byte)10);
				specbox.setHeight((byte)btnheight);
				specbox.setLeft((byte)132);
				specbox.setTop((byte)(10 + btnheight * i));
				specbox.setClickId((byte)(CMD_OFFSET + i *4 + 1));
				specbox.setRequestInfo(SPECID);
				specbox.setButtonStyle(ButtonRequest.BUTTON_STYLE_BLUE + ButtonRequest.BUTTON_STYLE_CLICK + ButtonRequest.BUTTON_STYLE_DARK);
				specbox.setText("SPECTATE\0");
				
				kickbox = new ButtonRequest();
				kickbox.setConnectionId(UCID);
				kickbox.setWidth((byte)10);
				kickbox.setHeight((byte)btnheight);
				kickbox.setLeft((byte)144);
				kickbox.setTop((byte)(10 + btnheight * i));
				kickbox.setClickId((byte)(CMD_OFFSET + i *4 + 2));
				kickbox.setRequestInfo(KICKID);
				kickbox.setButtonStyle(ButtonRequest.BUTTON_STYLE_BLUE + ButtonRequest.BUTTON_STYLE_CLICK + ButtonRequest.BUTTON_STYLE_DARK);
				kickbox.setText("KICK\0");
				
				banbox = new ButtonRequest();
				banbox.setConnectionId(UCID);
				banbox.setWidth((byte)10);
				banbox.setHeight((byte)btnheight);
				banbox.setLeft((byte)156);
				banbox.setTop((byte)(10 + btnheight * i));
				banbox.setClickId((byte)(CMD_OFFSET + i *4 + 3));
				banbox.setRequestInfo(BANID);
				banbox.setButtonStyle(ButtonRequest.BUTTON_STYLE_BLUE + ButtonRequest.BUTTON_STYLE_CLICK + ButtonRequest.BUTTON_STYLE_DARK);
				banbox.setText("BAN\0");
			}
			

			
			try
			{
				client.send(namebox);
				if (isMember && allowkick)
				{
					client.send(specbox);
					client.send(kickbox);
					client.send(banbox);
				}
			} catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Creates the teamchat button for a user
	 * @param UCID of the user
	 */
	public void createTeamchatButton(byte UCID)
	{
		ButtonRequest r = new ButtonRequest();
		r.setConnectionId(UCID);
		r.setTypeIn((byte)77);
		r.setWidth((byte)10);
		r.setHeight((byte)5);
		r.setLeft((byte)1);
		r.setTop((byte)180);
		r.setClickId((byte)239);
		r.setRequestInfo(this.TEAMCHATID);
		r.setButtonStyle(ButtonRequest.BUTTON_STYLE_BLUE + ButtonRequest.BUTTON_STYLE_CLICK + ButtonRequest.BUTTON_STYLE_DARK);
		r.setText("\0Teamchat\0");
		
		try
		{
			this.client.send(r);
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		
		this.userManager.setTeamchatStatus(UCID, this.id, true);
	}
	
	/**
	 * Creates a report button for a user
	 * @param UCID of the user
	 */
	public void createReportButton(byte UCID)
	{
		ButtonRequest r = new ButtonRequest();
		r.setConnectionId(UCID);
		r.setTypeIn((byte)77);
		r.setWidth((byte)10);
		r.setHeight((byte)5);
		r.setLeft((byte)1);
		r.setTop((byte)170);
		r.setClickId((byte)238);
		r.setRequestInfo(this.REPORTID);
		r.setButtonStyle(ButtonRequest.BUTTON_STYLE_BLUE + ButtonRequest.BUTTON_STYLE_CLICK + ButtonRequest.BUTTON_STYLE_DARK);
		r.setText("\0^1REPORT\0");
		
		try
		{
			this.client.send(r);
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		
		this.userManager.setReportStatus(UCID, this.id, true);
	}
}

class runner implements Runnable
{

	private InSimResponse packet;
	private InSimWrapper wrapper;
	
	public runner(InSimResponse packet, InSimWrapper wrapper)
	{
		this.wrapper = wrapper;
		this.packet = packet;
	}
	
	public void run()
	{
		for(int i = 0; i < InSimWrapper.handlers.length; i++)
		{
			InSimWrapper.handlers[i].handlePacket(packet, wrapper);
		}
	}
	
}


	/*
	 * Below there is the old code I used before I implemented the packetHandlers
	 * You sould not use this code, since it's very bad to look at, as well as less
	 * stable since it wasn't updated! 
	 */

	/*@Override
	public synchronized void packetReceived(InSimResponse packet)*/
//	public void oldoverride(InSimResponse packet)
//	{	
//		//	New Connection
//		if (packet instanceof NewConnectionResponse)
//		{
//			NewConnectionResponse _packet = (NewConnectionResponse)packet;
//			
//			byte UCID = (byte)_packet.getConnectionId();
//			String username = _packet.getUsername();
//			String playername = _packet.getPlayerName();
//			boolean admin = _packet.isAdmin();
//			boolean member = false;
//			
//			//UserManagement userManager = UserManagement.CreateUsermanagement();
//			synchronized (userManager)
//			{				
//				userManager.addUser(UCID, username, playername, this.id, admin);
//				member = userManager.isMember(UCID, this.id);
//			}
//			
//			this.sendMessage(UCID, " ");
//			this.sendMessage(UCID, " ");
//			this.sendMessage(UCID, "^3Welcome to the ^7Tofu[^1�^7] servers, " + playername);
//			if (member)
//				this.sendMessage(UCID, "^1You are logged in as ^6member^1.");
//			else
//				this.sendMessage(UCID, "^1You are logged in as ^7user^1.");
//			this.sendMessage(UCID, "^3Use ^7/i help ^3to get help with the server's commands.");
//			this.sendMessage(UCID, "^3Have a nice stay!");
//			this.sendMessage(UCID, " ");
//			this.sendMessage(UCID, " ");
//			
//			if (containsTofu(playername) && !member)
//			{
//				sendCommand(playername + " (" + username + ") ^1was auto-kicked for faking!");
//				sendMessage(UCID, "^1You are not a Tofu member! Remove \"^7Tofu[^1�^7]^1\" from your name!");
//				sendCommand("/kick " + username);
//			}
//			
//			/*
//			 * 	Send Welcome Dialog!
//			 */
//			
//			ButtonRequest welcome = new ButtonRequest();
//			welcome = new ButtonRequest();
//			welcome.setConnectionId(UCID);
//			welcome.setWidth((byte)180);
//			welcome.setHeight((byte)180);
//			welcome.setLeft((byte)10);
//			welcome.setTop((byte)(10));
//			//welcome.setClickId((byte)(CMD_OFFSET + i *4 + 3));
//			welcome.setRequestInfo(BANID);
//			welcome.setButtonStyle(welcome.BUTTON_STYLE_BLUE + welcome.BUTTON_STYLE_CLICK + welcome.BUTTON_STYLE_DARK);
//			
//			String welcome_msg = 
//				"Hello, "
//				+ playername + " ("+username+") "
//				+"and welcome to The Tofu Servers (The tofu servers line should be in red color, that's my opinion. You choose though :P)"
//				+"Here on tofu we have set some rules that the players should follow, so please read them all:"
//				+"#1 - Do NOT crash into other cars on purpose."
//				+"#2 - Offensive Language is a no-no! There are minors using the services we provide."
//				+"#3 - You have to drive " 
//				+ this.direction.toString()
//				+ " here."
//				+"#4 - You are to ALWAYS respect other players, tofu members and admins."
//				+"#5 - Please enjoy your stay here, and do your best to lead by example in creating a civil environment of mutual understanding, and fun!"
//				+"#6 - Rules are subject to change without notice."
//				+"Violation of the rules will result in a warning, a kick or in the worst case, a ban depending on the severity of the violation."
//				+"Thank you,"
//				+"The Tofu Team.\0" ;
//			
//			welcome.setText(welcome_msg);
//			
//			//OnlineLogWrapper log = OnlineLogWrapper.getWrapper();
//			//log.connect(username, playername);
//		}
//	//	Connection Leaves
//		else if (packet instanceof ConnectionLeaveResponse)
//		{
//			ConnectionLeaveResponse _packet = (ConnectionLeaveResponse)packet;
//			
//			//UserManagement u = UserManagement.CreateUsermanagement();
//			
//			byte UCID = (byte)_packet.getConnectionId();
//			String username;
//			synchronized (userManager)
//			{
//				username = userManager.getUsername(UCID, this.id);
//			}
//						
//			synchronized (playerList)
//			{
//				int size = playerList.size();
//				Player temp = null;
//				
//				for (int i = 0; i < size; i++)
//				{
//					temp = playerList.get(i);
//					if (temp.UCID == UCID)
//					{
//						playerList.remove(i);
//						break;
//					}
//				}
//			}
//			synchronized (userManager)
//			{
//				userManager.remUser(UCID, this.id);
//			}
//			
//			//OnlineLogWrapper w = OnlineLogWrapper.getWrapper();
//			//w.disconnect(username);
//		}
//	//	Insim Connection Close
//		else if (packet instanceof ConnectionCloseResponse)
//		{
//			//NOT NEEDED
//			ConnectionCloseResponse _packet = (ConnectionCloseResponse)packet;
//			System.out.println("CONNECTION CLOSED!");
//		}
//	//	Conection Rename
//		else if (packet instanceof ConnectionPlayerRenameResponse)
//		{
//			ConnectionPlayerRenameResponse _packet = (ConnectionPlayerRenameResponse)packet;
//			
//			byte UCID = (byte)_packet.getConnectionId();
//			String playerName = _packet.getNewName();
//			
//			//UserManagement u = UserManagement.CreateUsermanagement();
//			String userName;
//			boolean isMember = false;
//			synchronized (userManager)
//			{
//				userManager.changePlayerName(UCID, this.id, playerName);
//				userName = userManager.getUsername(UCID, this.id);
//				isMember = userManager.isMember(UCID, this.id);
//			}
//			
//			
//			if (containsTofu(playerName) && !isMember)
//			{
//				sendCommand(playerName + " (" + userName + ") ^1was auto-kicked for faking!");
//				sendMessage(UCID, "^1You are not a Tofu member! Remove '^7Tofu[^1�^7] ^1from your name!");
//				sendCommand("/kick " + userName);
//			}
//			
//			//OnlineLogWrapper.getWrapper().rename(userName, playerName);
//		}
//	//	Player Join Race
//		else if (packet instanceof NewPlayerResponse)
//		{
//			NewPlayerResponse _packet = (NewPlayerResponse)packet;
//			Player p = new Player();
//			p.PLID = (byte)_packet.getPlayerId();
//			p.UCID = (byte)_packet.getConnectionId();
//			p.x_pos = p.y_pos = 0;
//			
//			synchronized (playerList)
//			{
//				for(int i = 0; i < this.playerList.size(); i++)
//				{
//					if(playerList.get(i).PLID == p.PLID || playerList.get(i).UCID == p.UCID)
//					{
//						playerList.remove(i);
//						--i;
//					}
//				}
//				this.playerList.add(p);
//			}
//			
//			
//			
//			
//			String command = "/rcm ";
//			if (this.direction == Direction.REVERSE)
//				command += this.wrongWay;
//			else if (this.direction == Direction.NORMAL)
//				command += this.normalWay;
//			else if (this.direction == Direction.CRUISE)
//				return;//command += this.cruise;
//				
//			this.sendCommand(command);
//			String userName;
//			synchronized (userManager)
//			{
//				userName = userManager.getUsername(p.UCID, this.id);
//			}
//			command = "/rcm_ply " + userName;
//			this.sendCommand(command);
//			
//			//OnlineLogWrapper w = OnlineLogWrapper.getWrapper();
//			//w.joinRace(userName);
//		}
//	//	Player Pits
//		else if (packet instanceof PlayerPitsResponse)
//		{
//			PlayerPitsResponse _packet = (PlayerPitsResponse)packet;
//			
//			byte PLID = (byte)_packet.getPlayerId();
//			
//			byte UCID = -1;
//			synchronized (playerList)
//			{
//				for (int i = 0 ; i < playerList.size(); i++)
//				{
//					if (PLID == playerList.get(i).PLID)
//					{
//						UCID = playerList.get(i).UCID;
//						break;
//					}
//				}
//			}
//			
//			String userName;
//			synchronized (userManager)
//			{
//				userName = userManager.getUsername(UCID, this.id);
//			}
//			/*if (userName != null)
//			{
//				OnlineLogWrapper w = OnlineLogWrapper.getWrapper();
//				w.leaveRace(userName);
//			}*/
//		}
//	//	Car Take Over
//		else if (packet instanceof TakeOverCarResponse)
//		{
//			TakeOverCarResponse _packet = (TakeOverCarResponse)packet;
//			
//			byte O_UCID = (byte)_packet.getOldConnectionId();
//			byte N_UCID = (byte)_packet.getNewConnectionId();
//			
//			synchronized (playerList)
//			{
//				int size = playerList.size();
//				Player temp = null;
//				for (int i = 0; i < size; i++)
//				{
//					temp = playerList.get(i);
//					if (temp.UCID == O_UCID)
//					{
//						playerList.remove(i);
//						temp.UCID = N_UCID;
//						playerList.add(temp);
//						break;
//					}
//				}
//			}
//			//UserManagement m = UserManagement.CreateUsermanagement();
//			String newUserName = null;
//			String oldUserName = null;
//			synchronized(userManager)
//			{
//				newUserName = userManager.getUsername(N_UCID, this.id);
//				oldUserName = userManager.getUsername(O_UCID, this.id);
//			}
//			
//			//OnlineLogWrapper w = OnlineLogWrapper.getWrapper();
//			//w.joinRace(newUserName);
//			//w.leaveRace(oldUserName);
//		}
//	//	Player Pitlane
//		else if(packet instanceof PitLaneResponse)
//		{
//			PitLaneResponse _packet = (PitLaneResponse)packet;
//			if (_packet.isPitsExit())
//			{
//				byte PLID = (byte)_packet.getPlayerId();
//				//UserManagement uman = UserManagement.CreateUsermanagement();
//				String userName;
//				byte UCID = (byte)255;
//				
//				
//				synchronized (playerList)
//				{
//					for (int i = 0; i < playerList.size(); i++)
//					{
//						if (playerList.get(i).PLID == PLID)
//						{
//							UCID = playerList.get(i).UCID;
//							break;
//						}
//					}
//				}
//				
//				synchronized (userManager)
//				{
//					userName = userManager.getUsername(UCID,  this.id);
//				}
//				
//				this.sendCommand("/p_clear " + userName);
//				this.sendCommand("/rcc_ply " + userName);				
//			}
//			
//			/*
//			TinyRequest req = new TinyRequest(Tiny.MULTI_CAR_INFO);
//			try
//			{
//				client.send(req);
//			} catch (IOException e)
//			{
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			*/
//		}
//	//	Player Penalty
//		/*
//		else if (packet instanceof PenaltyResponse)
//		{
//			PenaltyResponse _packet = (PenaltyResponse)packet;
//		}
//		*/
//	//	Player Leave Race
//		else if (packet instanceof PlayerLeavingResponse)
//		{
//			PlayerLeavingResponse _packet = (PlayerLeavingResponse)packet;
//			Player temp = null;
//			byte UCID = (byte) 255;
//			//UserManagement uman = UserManagement.CreateUsermanagement();
//			//OnlineLogWrapper log = OnlineLogWrapper.getWrapper();
//			synchronized (playerList)
//			{
//				int size = playerList.size();
//				for (int i = 0; i < size; i++)
//				{
//					temp = playerList.get(i);
//					if (temp.PLID == _packet.getPlayerId())
//					{
//						UCID = temp.UCID;
//						playerList.remove(i);
//						break;
//						
//					}
//				}
//			}
//			
//			String userName;
//			
//			synchronized(userManager)
//			{
//				userName = userManager.getUsername(UCID, this.id);
//			}
//			
//			//log.leaveRace(userName);
//			
//		}
//	//	Miltiplayer Car info
//		/*
//		else if (packet instanceof MultiCarInfoResponse)
//		{
//			MultiCarInfoResponse _packet = (MultiCarInfoResponse)packet;
//			ArrayList<CompCar> carList = _packet.getCarInfoList();
//			for (int i = 0; i < carList.size(); i++)
//			{
//				CompCar temp = carList.get(i);
//				byte PLID = (byte)temp.getPlayerId();
//				Player tPlayer  = null;
//				
//				for (int j = 0; j < playerList.size(); j++)
//				{
//					if (PLID == playerList.get(j).PLID)
//						tPlayer = playerList.get(j);
//				}
//				
//				if (tPlayer != null)
//				{
//					double tempx = DirectionControl.normalize(temp.getPosition().getX());
//					double tempy = DirectionControl.normalize(temp.getPosition().getY());
//					if (tempx != tPlayer.x_pos || tempy != tPlayer.y_pos) 
//					{
//						if (!this.dirCTRL.conform(tPlayer.x_pos, tPlayer.y_pos, tempx, tempy))
//							System.out.println("Kick!");
//						tPlayer.x_pos = DirectionControl.normalize(temp.getPosition().getX());
//						tPlayer.y_pos = DirectionControl.normalize(temp.getPosition().getY());
//						
//						System.out.println(tPlayer.x_pos+" , "+tPlayer.y_pos);	
//						
//					}
//				}
//				else System.out.println("unknown");
//			}
//			
//			
//			boolean success = false;
//			while (!success)
//			{
//				try
//				{
//					Thread.sleep(50);
//				} catch (InterruptedException e)
//				{
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				
//				TinyRequest req = new TinyRequest(Tiny.MULTI_CAR_INFO);
//				try
//				{
//					client.send(req);
//					success = true;
//				} catch (IOException e)
//				{
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//		}*/
//	//	Message
//		else if (packet instanceof MessageResponse)
//		{
//			MessageResponse _packet = (MessageResponse)packet;
//			
//			//UserManagement u = UserManagement.CreateUsermanagement();
//			byte UCID = (byte)_packet.getConnectionId();
//			String userName;
//			synchronized (userManager)
//			{
//				userName = userManager.getUsername(UCID, this.id);
//			}
//			
//			
//			
//			synchronized (this.prevention)
//			{
//				if (userName != null)
//				{
//					int count = this.prevention.bWordsPenalty(userName, _packet.getMessage().toLowerCase());
//					switch(count)
//					{
//					case 0:
//						break;
//					case 1:
//						this.sendMessage(UCID, "^1Don't swear! This is your 1st time, so be more careful!");
//						break;
//					case 2:
//						this.sendMessage(UCID, "^1Don't swear! This is your 2nd time, you will be kicked next time!");
//						break;
//					case 3:
//						this.sendMessage(UCID, "^1Don't swear! This is your 3rd time, so byebye!");
//						this.sendCommand("/msg ^1" + userName + " gets kicked for swearing!");
//						this.sendCommand("/kick " + userName);
//						this.prevention.reset(userName);
//						break;
//					default:
//						this.sendMessage(UCID, "^1Byebye!");
//						this.sendCommand("/msg ^1" + userName + " gets kicked for swearing!");
//						this.sendCommand("/kick " + userName);
//						this.prevention.reset(userName);
//						break;
//					}
//				}
//			}
//		}
//		
//	// Button Text response
//		else if (packet instanceof ButtonTypeResponse)
//		{
//			ButtonTypeResponse _packet = (ButtonTypeResponse)packet;
//			byte UCID = (byte)_packet.getConnectionId();
//			String message = _packet.getTypeInText();
//			byte reqID = (byte)_packet.getRequestInfo();
//			
//			if (reqID == TEAMCHATID)
//			{
//				synchronized (crossChat)
//				{
//					crossChat.sendTeamMessage(UCID, this.id, message);
//				}
//			}
//			else if (reqID == PMID)
//			{
//				ArrayList<Short> userList = null;
//				short OUCID = 0;
//				
//				synchronized (userManager)
//				{
//					userList = userManager.getAllUserOUCIDs();
//					OUCID = userList.get( (_packet.getClickId() - CMD_OFFSET) / 4);
//				}
//				
//				if (OUCID != 0)
//				{
//					synchronized (crossChat)
//					{
//						crossChat.sendPrivateMessage(UCID, this.id, OUCID, _packet.getTypeInText());
//					}
//				}
//			}
//			
//			
//		}
//		
//	//	Button Click Response
//		else if (packet instanceof ButtonClickedResponse)
//		{
//			ButtonClickedResponse _packet = (ButtonClickedResponse)packet;
//			byte UCID = (byte)_packet.getConnectionId();
//			byte reqID = (byte)_packet.getRequestInfo();
//			
//			if (reqID >= SPECID && reqID <= BANID)
//			{
//				ArrayList<Short> userList = null;
//				String userName_k = null;
//				String userName_e = null;
//				String nickName_e = null;
//				boolean isKickable_k = false;
//				boolean isMember_e = false;
//				
//				synchronized (userManager)
//				{
//					//userName = userManager.getUsername(, insimID)
//					userList = userManager.getAllUserOUCIDs();
//					short OUCID = 0;
//					if (reqID == SPECID)
//						OUCID = userList.get( (_packet.getClickId() - 1 - CMD_OFFSET) / 4 );
//					else if (reqID == KICKID)
//						OUCID = userList.get( (_packet.getClickId() - 2 - CMD_OFFSET) / 4 );
//					else if (reqID == BANID)
//						OUCID = userList.get( (_packet.getClickId() - 3 - CMD_OFFSET) / 4 );
//					
//					userName_k = userManager.getUserName(OUCID);
//					isKickable_k = userManager.isKickable(OUCID);
//					isMember_e = userManager.isMember(UCID, this.id);
//					userName_e = userManager.getUsername(UCID, this.id);
//					nickName_e = userManager.getNickName(UCID, this.id);
//				}
//				if (userName_k != null && isKickable_k && isMember_e)
//				{				
//					synchronized (crossChat)
//					{
//						if (reqID == SPECID)
//						{
//							String message = nickName_e + " ^7(" + userName_e + ") attempts to spec " + userName_k;
//							crossChat.sendCommand("/spec " + userName_k);
//						}
//						else if (reqID == KICKID)
//						{
//							String message = nickName_e + " ^7(" + userName_e + ") attempts to kick " + userName_k;
//							crossChat.sendCommand("/kick "+ userName_k);
//						}
//						else if (reqID == BANID)
//						{
//							String message = nickName_e + " ^7(" + userName_e + ") attempts to ban " + userName_k;
//							crossChat.sendCommand("/ban " + userName_k + " 0");
//						}
//					}
//				}
//				else
//				{
//					String message = "^1You can't use commands on members, admins or users that don't exist.";
//					sendMessage(UCID, message);
//				}
//			}
//		}
//		
//	//	Hidden Message
//		else if (packet instanceof HiddenMessageResponse)
//		{
//			HiddenMessageResponse _packet = (HiddenMessageResponse)packet;
//			
//			String msg = _packet.getMessage();
//			byte UCID = (byte)_packet.getConnectionId();
//			//UserManagement u = UserManagement.CreateUsermanagement();
//			
//			if (msg.startsWith("help"))
//			{
//				boolean isMember = false;
//				boolean isAdmin = false;
//				synchronized (userManager)
//				{
//					isMember = userManager.isMember(UCID, this.id);
//					isAdmin = userManager.isAdmin(UCID, this.id);
//				}
//				
//				this.sendMessage(UCID, "^3The following commands can be used on this server:");
//				this.sendMessage(UCID, "^3/i help - ^7shows this help");
//				this.sendMessage(UCID, "^3/i list - ^7shows a list of all connected users and their ^1<chatID>");
//				this.sendMessage(UCID, "^3/i pm ^1<chat ID> ^2<message> ^3- ^7sends a private message to the user");
//				this.sendMessage(UCID, "^3/i report ^2<message> ^7- sends a message to all admins. Use it to report players.");
//				if (isMember)
//				{
//					this.sendMessage(UCID, "^3/i team ^2<message> ^7- sends a message to all connected team members");
//					this.sendMessage(UCID, "^3/i kick ^6<userName> ^7OR ^3/i kick ^1<chatID> ^7- kicks the player");
//					this.sendMessage(UCID,"^3/i ban ^6<userName> ^7 OR ^3/i ban ^1<chatID> ^7- bans the player for 12 hours");
//					this.sendMessage(UCID,"^3/i spec ^6<userName> ^7 OR ^3/i spec ^1<chatID> ^7- spectates the player");
//				}
//				if (isAdmin)
//				{
//					this.sendMessage(UCID, "^3/i direction ^5<direction> ^7- set direction start message (reverse, normal, cruise)");
//				}
//			}
//			else if (msg.startsWith("list"))
//			{
//				
//				ArrayList<String> users;
//				ArrayList<Short> OUCIDs;
//				ArrayList<Short> unbannable;
//				boolean isMember = false;
//				
//				synchronized (userManager)
//				{
//					users = userManager.getAllUserNames(true);
//					isMember = userManager.isMember(UCID, this.id);
//					OUCIDs = userManager.getAllUserOUCIDs();
//					unbannable = userManager.getAllMemberOUCIDs();
//				}
//				
//				int size = users.size();
//				
//				int btnheight = 160 / size;
//				if (btnheight > 5)
//					btnheight = 5;
//				
//				for(int i = 0; i < size; i++)
//				{
//					ButtonRequest namebox = new ButtonRequest();
//					namebox.setConnectionId(UCID);
//					namebox.setTypeIn((byte)77);
//					namebox.setWidth((byte)60);//60 //96
//					namebox.setHeight((byte)btnheight);
//					namebox.setLeft((byte)70);//70 //34
//					namebox.setTop((byte)(10 + btnheight * i));
//					namebox.setClickId((byte)(CMD_OFFSET + i * 4));
//					namebox.setRequestInfo(PMID);
//					namebox.setButtonStyle(namebox.BUTTON_STYLE_BLUE + namebox.BUTTON_STYLE_CLICK + namebox.BUTTON_STYLE_DARK);
//					namebox.setText(users.get(i) + " (WRITE PM)" + "\0");
//					
//					
//					ButtonRequest specbox = null, kickbox = null, banbox = null;
//					boolean allowkick = false;
//					
//					if (!unbannable.contains(OUCIDs.get(i)))
//					{
//						allowkick = true;
//						
//						specbox = new ButtonRequest();
//						specbox.setConnectionId(UCID);
//						specbox.setWidth((byte)10);
//						specbox.setHeight((byte)btnheight);
//						specbox.setLeft((byte)132);
//						specbox.setTop((byte)(10 + btnheight * i));
//						specbox.setClickId((byte)(CMD_OFFSET + i *4 + 1));
//						specbox.setRequestInfo(SPECID);
//						specbox.setButtonStyle(specbox.BUTTON_STYLE_BLUE + specbox.BUTTON_STYLE_CLICK + specbox.BUTTON_STYLE_DARK);
//						specbox.setText("SPECTATE\0");
//						
//						kickbox = new ButtonRequest();
//						kickbox.setConnectionId(UCID);
//						kickbox.setWidth((byte)10);
//						kickbox.setHeight((byte)btnheight);
//						kickbox.setLeft((byte)144);
//						kickbox.setTop((byte)(10 + btnheight * i));
//						kickbox.setClickId((byte)(CMD_OFFSET + i *4 + 2));
//						kickbox.setRequestInfo(KICKID);
//						kickbox.setButtonStyle(kickbox.BUTTON_STYLE_BLUE + kickbox.BUTTON_STYLE_CLICK + kickbox.BUTTON_STYLE_DARK);
//						kickbox.setText("KICK\0");
//						
//						banbox = new ButtonRequest();
//						banbox.setConnectionId(UCID);
//						banbox.setWidth((byte)10);
//						banbox.setHeight((byte)btnheight);
//						banbox.setLeft((byte)156);
//						banbox.setTop((byte)(10 + btnheight * i));
//						banbox.setClickId((byte)(CMD_OFFSET + i *4 + 3));
//						banbox.setRequestInfo(BANID);
//						banbox.setButtonStyle(banbox.BUTTON_STYLE_BLUE + banbox.BUTTON_STYLE_CLICK + banbox.BUTTON_STYLE_DARK);
//						banbox.setText("BAN\0");
//					}
//					
//
//					
//					try
//					{
//						this.client.send(namebox);
//						if (isMember && allowkick)
//						{
//							this.client.send(specbox);
//							this.client.send(kickbox);
//							this.client.send(banbox);
//						}
//					} catch (IOException e)
//					{
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//				}
//				
//				/*String seperator = " ^7|| ";
//				
//				this.sendMessage(UCID, "^1||||||||||||||||||||||||^7USERLIST^1||||||||||||||||||||||||");
//				this.sendMessage(UCID, " ");
//				for (int i = 0; i < size; i++)
//				{
//					String user = users.get(i);
//					int j = 0;
//					for (j = 0; j < size -i -1; j++)
//					{
//						int str_len = user.length() + seperator.length() + users.get(i+j+1).length();
//						if (str_len < 120)
//							user += seperator + users.get(i+j+1);
//						else
//							break;
//					}
//					i+=j;
//					this.sendMessage(UCID, user);
//				}
//				this.sendMessage(UCID, " ");
//				this.sendMessage(UCID, "^1||||||||||||||||||||||||^7--------^1||||||||||||||||||||||||");*/
//			}
//			else if(msg.startsWith("pm "))
//			{
//				String[] split = msg.split(" ", 3);
//				if (split.length < 3)
//					return;
//				
//				//CrossChat crc = CrossChat.CreateCrossChat();
//				try
//				{
//					byte OUCID = Byte.parseByte(split[1]);
//					String message = split[2];	
//					synchronized (crossChat)
//					{
//						crossChat.sendPrivateMessage(UCID, this.id, OUCID, message);
//					}
//				}
//				catch(NumberFormatException e)
//				{
//					sendMessage(UCID, "^1You need to enter a valid ID");
//				}
//				
//			}
//			else if (msg.startsWith("team"))
//			{
//				String[] split = msg.split(" ", 2);
//				if (split.length == 2)
//				{
//					String message = split[1];
//					
//					//CrossChat crc = CrossChat.CreateCrossChat();
//					synchronized (crossChat)
//					{
//						crossChat.sendTeamMessage(UCID, this.id, message);
//					}	
//				}
//				else
//				{
//					ButtonRequest r = new ButtonRequest();
//					r.setConnectionId(UCID);
//					r.setTypeIn((byte)77);
//					r.setWidth((byte)10);
//					r.setHeight((byte)5);
//					r.setLeft((byte)1);
//					r.setTop((byte)180);
//					r.setClickId((byte)239);
//					r.setRequestInfo(TEAMCHATID);
//					r.setButtonStyle(r.BUTTON_STYLE_BLUE + r.BUTTON_STYLE_CLICK + r.BUTTON_STYLE_DARK);
//					r.setText("\0Teamchat\0");
//					
//					try
//					{
//						client.send(r);
//					} catch (IOException e)
//					{
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//				}
//			}
//			else if (msg.startsWith("report "))
//			{
//				String[] split = msg.split(" ", 2);
//				String message = split[1];
//				
//				//CrossChat crc = CrossChat.CreateCrossChat();
//				synchronized (crossChat)
//				{
//					crossChat.reportToAdmin(UCID, this.id, message);
//				}
//				
//			}
//			else if (msg.startsWith("kick "))
//			{
//				boolean isMember = false;
//				String userName;
//				String nickName;
//				
//				synchronized (userManager)
//				{
//					isMember = userManager.isMember(UCID, this.id);
//					userName = userManager.getUsername(UCID, this.id);
//					nickName = userManager.getNickName(UCID, this.id);
//				}
//				
//				
//				if (isMember)
//				{
//					String name = msg.split(" ", 2)[1];
//					short OUCID = -1;
//					try
//					{
//						OUCID =  Short.parseShort(name);
//					}
//					catch (NumberFormatException e)
//					{
//						
//					}
//					
//					boolean cankick = false;
//					
//					if (OUCID > 0)
//					{
//						synchronized (userManager)
//						{
//							cankick = userManager.isKickable(OUCID);
//							name = userManager.getUserName(OUCID);
//						}
//					}
//					else
//					{
//						synchronized (userManager)
//						{
//							cankick = userManager.isKickable(name);
//						}
//					}
//					
//					if (cankick)
//					{
//						String message = nickName + " ^7(" + userName + ") attempts to kick " + name;
//						//CrossChat.CreateCrossChat().sendTeamMessage(message);
//						//CrossChat.CreateCrossChat().sendCommand("/kick " + name);
//						synchronized (crossChat)
//						{
//							crossChat.sendTeamMessage(message);
//							crossChat.sendCommand("/kick " + name);
//						}
//					}
//					else
//					{
//						String message = "^1You can't kick members, admins or users that don't exist.";
//						sendMessage(UCID, message);
//					}
//					
//				}
//			}
//			
//			else if (msg.startsWith("ban "))
//			{
//				boolean isMember = false;
//				String userName;
//				String nickName;
//				
//				synchronized (userManager)
//				{
//					isMember = userManager.isMember(UCID, this.id);
//					userName = userManager.getUsername(UCID, this.id);
//					nickName = userManager.getNickName(UCID, this.id);
//				}
//				
//				if (isMember)
//				{
//					String[] split = msg.split(" ", 2);
//					if (split.length == 2)
//					{
//						String name = split[1];
//						short OUCID = -1;
//						try
//						{
//							OUCID = Short.parseShort(name);
//						}
//						catch(NumberFormatException e)
//						{
//							
//						}
//						
//						boolean canBan = false;
//						
//						if (OUCID > 0)
//						{
//							synchronized (userManager)
//							{
//								canBan = userManager.isKickable(OUCID);
//								name = userManager.getUserName(OUCID);
//							}
//						}
//						else
//						{
//							synchronized (userManager)
//							{
//								canBan = userManager.isKickable(name);
//							}
//						}
//						
//						if (canBan)
//						{
//							String message = nickName + " ^7(" + userName + ") attempts to ban " + name;
//							//CrossChat.CreateCrossChat().sendTeamMessage(message);
//							//CrossChat.CreateCrossChat().sendCommand("/ban " + name + " 0");
//							synchronized (crossChat)
//							{
//								crossChat.sendTeamMessage(message);
//								crossChat.sendCommand("/ban " + name + " 0");
//							}
//						}
//						else
//						{
//							String message = "^1You can't ban members, admins or users that don't exist.";
//							sendMessage(UCID, message);
//						}
//					}
//				}
//			}
//			
//			else if (msg.startsWith("spec "))
//			{
//				boolean isMember = false;
//				String userName;
//				String nickName;
//				
//				synchronized (userManager)
//				{
//					isMember = userManager.isMember(UCID, this.id);
//					userName = userManager.getUsername(UCID, this.id);
//					nickName = userManager.getNickName(UCID, this.id);
//				}
//				
//				
//				if (isMember)
//				{
//					String[] split = msg.split(" ", 2);
//					if (split.length == 2)
//					{
//						String name = split[1];
//						short OUCID = -1;
//						try
//						{
//							OUCID = Short.parseShort(name);
//						}
//						catch (NumberFormatException e)
//						{
//							
//						}
//						
//						boolean canSpec = false;
//						
//						if (OUCID > 0)
//						{
//							synchronized (userManager)
//							{
//								canSpec = userManager.isKickable(OUCID);
//								name = userManager.getUserName(OUCID);
//							}
//						}
//						else
//						{
//							synchronized (userManager)
//							{
//								canSpec = userManager.isKickable(name);
//							}
//						}
//						
//						if (canSpec)
//						{
//							String message = nickName + " ^7(" + userName + ") attempts to spectate " + name;
//							//CrossChat.CreateCrossChat().sendTeamMessage(message);
//							//CrossChat.CreateCrossChat().sendCommand("/spec " + name);
//							synchronized (crossChat)
//							{
//								crossChat.sendTeamMessage(message);
//								crossChat.sendCommand("/spec " + name);
//							}
//						}
//						else
//						{
//							String message = "^1You can't spectate members, admins or users that don't exist.";
//							sendMessage(UCID, message);
//						}
//					}
//				}
//			}
//			
//			else if (msg.startsWith("direction "))
//			{
//				boolean isAdmin = false;
//				synchronized (userManager)
//				{
//					isAdmin = userManager.isAdmin(UCID, this.id);
//				}
//				
//				if (isAdmin)
//				{
//					String[] split = msg.split(" ", 2);
//					if (split[1].equals("normal"))
//					{
//						this.direction = Direction.NORMAL;
//						this.sendMessage(UCID, "^7Direction set to ^6normal^7.");
//					}
//					else if (split[1].equals("reverse"))
//					{
//						this.direction = Direction.REVERSE;
//						this.sendMessage(UCID, "^7Direction set to ^1reverse^7.");
//					}
//					else if (split[1].equals("cruise"))
//					{
//						this.direction = Direction.CRUISE;
//						this.sendMessage(UCID, "^7Direction set to ^2cruise^7.");
//					}
//					else
//					{
//						this.sendMessage(UCID, "^1Can't set direction. Valid directions are ^7normal^1, ^7reverse^1, ^7cruise^1!");
//					}
//				}
//			}
//			else
//				this.sendMessage(UCID, "^1Unknown command! Use ^3/i help ^1to get information!");
//		}
//	//	Experimental
//		else if (packet.getPacketType() == PacketType.ADMIN_COMMAND_REPORT)
//		{
//			AdminCommandReport _packet = (AdminCommandReport)packet;
//			
//			String message = _packet.getMessage();
//			
//			boolean send = _packet.isAdmin();
//			boolean result = _packet.getResult() == 1;
//			boolean notHost = _packet.getConnectionID() > 0;
//			boolean unBanKick = (message.startsWith("/ban ") || message.startsWith("/kick ") || message.startsWith("/unban "));
//			
//			if (send && result && notHost && unBanKick)
//			{
//				String command = _packet.getMessage();
//				//CrossChat.CreateCrossChat().sendCommand(command);
//				synchronized (crossChat)
//				{
//					crossChat.sendCommand(command);
//				}
//				System.out.println(command);
//				this.sendMessage((byte)_packet.getConnectionID(), "^1Command executed for user on all servers: " + message);
//			}
//		}
//	}