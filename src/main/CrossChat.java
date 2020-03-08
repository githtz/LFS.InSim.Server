package main;
import java.util.ArrayList;

/**
 * The CrossChat is the connection between the servers.
 * @author anrc
 * @version 1.1
 */
public class CrossChat
{
	private ArrayList<InSimWrapper> wrappers;
	/**
	 * Stores the one and only instance of the CrossChat
	 */
	public static CrossChat CrossChat = null;
	
	/**
	 * Creates and/or returns the one and only CrossChat
	 * @return
	 */
	public static CrossChat CreateCrossChat()
	{
		if (CrossChat == null)
			CrossChat = new CrossChat();
		return CrossChat;
	}
	
	/**
	 * Private constructor
	 */
	private CrossChat()
	{
		this.wrappers = new ArrayList<InSimWrapper>();
		UserManagement.CreateUsermanagement();
	}
	
	/**
	 * Adds a new InSimWrapper to the CrossChat
	 * @param insim wrapper to add
	 */
	public void addInsim(InSimWrapper insim)
	{
		this.wrappers.add(insim);
	}
	
	/**
	 * Sends a private message to a player
	 * @param UCID of the player (sender)
	 * @param insimID of the server (sender)
	 * @param OUCID of the player (receiver)
	 * @param message the message, dude!
	 * @TODO enable synchronization for UserManagement
	 */
	public void sendPrivateMessage(byte UCID, byte insimID, short OUCID, String message)
	{
		UserManagement userManager = UserManagement.CreateUsermanagement();
		byte[] data = userManager.getInsimAndUCID(OUCID);
		
		String sender_userName = userManager.getNickName(UCID, insimID);
		short sender_OUCID = userManager.getOuterUserID(UCID, insimID);
		
		
		String pre_message = "^3[" + sender_userName + "^3(^7" + sender_OUCID + "^3)]^7: ^3";
		String receiver_message = pre_message + message;
		
		if (data != null)
		{
			this.wrappers.get(data[0]).sendMessage(data[1], receiver_message);
			this.wrappers.get(insimID).sendMessage(UCID, receiver_message);
		}
		else
			this.wrappers.get(insimID).sendMessage(UCID, "^1ID ^7" + OUCID + " ^1could not be found!");
	}
	
	/**
	 * Shuts down all wrappers and quits the program
	 */
	public void quit()
	{
		for (int i = 0; i < wrappers.size(); i++)
		{
			wrappers.get(i).quit();
		}
	}
	
	/**
	 * Sends a message to all team members
	 * @param UCID of the player (sender)
	 * @param insimID of the server (sender)
	 * @param message the message, dude!!1
	 * @TODO Enable the synchronization for the UserManagement
	 */
	public void sendTeamMessage(byte UCID, byte insimID, String message)
	{
		UserManagement userManager = UserManagement.CreateUsermanagement();
		ArrayList<Short> mems = userManager.getAllMemberOUCIDs();
		
		String sender_userName = userManager.getNickName(UCID, insimID);
		short sender_OUCID = userManager.getOuterUserID(UCID, insimID);
		
		if (mems.contains(sender_OUCID))
		{	
			String receiver_message = "^6[" + sender_userName + "^6(^7" + sender_OUCID + "^6)]^7: ^6" + message;
			
			for (int i = 0; i < mems.size(); i++)
			{
				byte[] data = userManager.getInsimAndUCID(mems.get(i));
				if(data != null)
				{
					this.wrappers.get(data[0]).sendMessage(data[1], receiver_message);
				}
				else
					this.wrappers.get(insimID).sendMessage(UCID, "^1ID could not be found!");
			}
		}
		else
			this.wrappers.get(insimID).sendMessage(UCID, "^1You're not allowed to use this command!");
	}
	
	/**
	 * Sends a server message to all team members
	 * This function is used to all members if another member changed it's status (connected, disconnected)
	 * @param message
	 * @TODO Enable synchronization for the UserManagement
	 */
	public void sendTeamMessage(String message)
	{
		UserManagement userManager = UserManagement.CreateUsermanagement();
		ArrayList<Short> mems = userManager.getAllMemberOUCIDs();
		
		String receiver_message = message;
		
		for (int i = 0; i < mems.size(); i++)
		{
			byte[] data = userManager.getInsimAndUCID(mems.get(i));
			if(data != null)
			{
				this.wrappers.get(data[0]).sendMessage(data[1], receiver_message);
			}
		}
		
	}
	
	/**
	 * This function reports a message to all admins
	 * @TODO Enable synchronization for the UserManagement 
	 * @param UCID
	 * @param insimID
	 * @param message
	 */
	public void reportToAdmin(byte UCID, byte insimID, String message)
	{
		UserManagement userManager = UserManagement.CreateUsermanagement();
		ArrayList<Short> mems = userManager.getAllMemberOUCIDs();
		
		String sender_userName = userManager.getNickName(UCID, insimID);
		short sender_OUCID = userManager.getOuterUserID(UCID, insimID);
		
		String receiver_message = "^1[REPORT]" + sender_userName + "^1(^7" + sender_OUCID + "^1)^7: " + message;
		
		if (mems.size() > 0)
		{
			for (int i = 0; i < mems.size(); i++)
			{
				byte[] data = userManager.getInsimAndUCID(mems.get(i));
				if(data != null)
				{
					this.wrappers.get(data[0]).sendMessage(data[1], receiver_message);
				}
			}
			this.wrappers.get(insimID).sendMessage(UCID, "^2Report was sent.");
		}
		else
			this.wrappers.get(insimID).sendMessage(UCID, "^1Sorry, no admin online.");
	}
	
	/**
	 * executes a command
	 * command will be executed on ALL servers
	 * @param command to execute
	 */
	public void sendCommand(String command)
	{
		int size = this.wrappers.size();
		
		for (int i = 0; i < size; i++)
		{
			wrappers.get(i).sendCommand(command);
		}
	}
	
	/**
	 * changes the hostname of the server
	 * will be displayd, wenn members connect / disconnect
	 * @param insimID of the server
	 * @param hostname of the server
	 */
	public void setHostname(byte insimID, String hostname)
	{
		if (insimID >= 0 && insimID < wrappers.size())
			if (this.wrappers.get(insimID).hostname == null)
				this.wrappers.get(insimID).hostname = hostname;
	}
	
	/**
	 * Gets the hostname of a server
	 * @param insimID of the server
	 * @return hostname as String
	 */
	public String getHostname(byte insimID)
	{
		String retVal = null;
		if (insimID >= 0 && insimID < wrappers.size())
			retVal = this.wrappers.get(insimID).hostname;
		
		return retVal;
	}
	
	/**
	 * Refreshes the connection list for everyone who has it open
	 * @TODO Enable synchronization for the UserManagement
	 */
	public void refreshConnectionList()
	{
		UserManagement userManagement = UserManagement.CreateUsermanagement();
		ArrayList<Byte[]> ids;
		InSimWrapper wrapper = null;
		
		synchronized (userManagement)
		{
			ids = userManagement.getAllListUsers();
			for (int i = 0; i < ids.size(); i++)
			{
				for(int j = 0; j < wrappers.size(); j++)
				{
					wrapper = wrappers.get(j);
					if (wrapper.id == ids.get(i)[1])
					{
						byte UCID = ids.get(i)[0];
						wrapper.removeButtons(UCID);
						wrapper.createConnectionList(UCID);
						if (ids.get(i)[2] == 1)
						{
							wrapper.createTeamchatButton(UCID);
						}
						if (ids.get(i)[3] == 1)
						{
							wrapper.createReportButton(UCID);
						}
						
						
						ids.remove(i);
						i--;
						break;
					}
				}
			}
		}
	}
}
