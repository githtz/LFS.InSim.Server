package main;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * This class is used to manage the users that connect to our hosts
 * <br/>It creates users, checks their permissions and stores relevant data
 * <br/>Some parts of this will be outsourced into a database sooner or later
 * @version 1.4
 * @author anrc
 *
 */
public class UserManagement
{
	private ArrayList<User> users;
	private ID_Provider IDGetter;
	private ArrayList<String> members;
	
	private static UserManagement userManagement = null;
	
	public static UserManagement CreateUsermanagement()
	{
		if (userManagement == null)
			userManagement = new UserManagement();
		return userManagement;
	}
	
	private UserManagement()
	{
		this.users = new ArrayList<User>();
		this.IDGetter = new ID_Provider();
		this.members = new ArrayList<String>();
	}
	
	/**
	 * This function is used to load the newest memberlist into the UserManagement
	 * @param path of the file to open
	 * @return a boolean, true if successful, false if not
	 */
	public boolean loadMemberList(String path)
	{
		File f = new File(path);
		Scanner s = null;
		
		try
		{
			s = new Scanner(f);
			this.members.clear();
			
			while(s.hasNextLine())
			{
				this.members.add(s.nextLine().toLowerCase());
			}
			
			s.close();
		}
		catch(FileNotFoundException e)
		{
			return false;
		}
		
		int size = this.users.size();
		for(int i = 0; i < size; i++)
		{
			User temp = this.users.get(i);
			if (this.members.contains(temp.userName.toLowerCase()))
				temp.member = true;
			else
				temp.member = false;
		}
		
		return true;
	}
	
	/**
	 * Writes the memberlist into a file
	 * @param path
	 */
	public void saveMemberList(String path)
	{
		File f = new File(path);
		try
		{
			PrintWriter pw = new PrintWriter(f);
			for(int i = 0; i < this.members.size(); i++)
			{
				pw.println(this.members.get(i));
			}
			pw.flush();
			pw.close();
		} catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Adds a new User to the UserManagement
	 * <br/>Will be called when a user connects to a host
	 * @param UCID of the user
	 * @param userName lfs.net username
	 * @param playerName adjustable nickname
	 * @param insimID ID of the server
	 * @param admin administator status
	 */
	public void addUser(byte UCID, String userName, String playerName, byte insimID, boolean admin)
	{
		boolean member = false;
		short OUCID = this.IDGetter.getNextFreeID();
		
		if (this.members.contains(userName.toLowerCase()))
		{
			member = true;
			String hostname = CrossChat.CreateCrossChat().getHostname(insimID);
			CrossChat.CreateCrossChat().sendTeamMessage("^8Member: " + playerName + " ^6(^7" + OUCID + "^6) ^8connected on " + hostname);
		}
			
		User user = new User(UCID, OUCID, userName, playerName, insimID, member, admin);
		
		/*System.out.println(user.playerName + 
							" (" + user.userName +
							"["+ user.UCID + "," + user.OUCID + "]" + ")" +
							" added. Memberstatus: " + user.member + 
							", Adminstatus: " + user.admin);
		*/
		this.users.add(user);
	}
	
	/**
	 * Removes a user from the UserManagement
	 * <br/>Will be called when a user disconnects from a host
	 * @param UCID Connection ID of the user
	 * @param insimID ID of the server
	 */
	public void remUser(byte UCID, byte insimID)
	{
		User temp = null;
		int size = this.users.size();
		
		for(int i = 0; i < size; i++)
		{
			temp = this.users.get(i);
			if (temp.UCID == UCID && temp.insimID == insimID)
			{
				String playerName = temp.playerName;
				boolean member = temp.member;
				
				short OUCID = temp.OUCID;
				this.users.remove(i);
				IDGetter.revokeID(OUCID);
				
				if (member)
				{
					String hostname = CrossChat.CreateCrossChat().getHostname(insimID);
					CrossChat.CreateCrossChat().sendTeamMessage("^8Member: " + playerName + " ^6(^7" + OUCID + "^6) ^8disconnected from " + hostname);
				}
				
				break;
			}
		}
	}
	
	/**
	 * Gets the username
	 * @param UCID Connection ID of the user
	 * @param insimID ID of the server
	 * @return username as String
	 */
	public String getUsername(byte UCID, byte insimID)
	{
		User temp = null;
		int size = this.users.size();
		
		for (int i = 0; i < size; i++)
		{
			temp = this.users.get(i);
			if (temp.UCID == UCID && temp.insimID == insimID)
				return temp.userName;
		}
		return null;
	}
	
	/**
	 * Gets the nickname 
	 * @param UCID Connection ID of the player
	 * @param insimID ID of the server
	 * @return nickname as String
	 */
	public String getNickName(byte UCID, byte insimID)
	{
		User temp = null;
		int size = this.users.size();
		
		for (int i = 0; i < size; i++)
		{
			temp = users.get(i);
			if (temp.UCID == UCID && temp.insimID == insimID)
				return temp.playerName;
		}
		return null;
	}
	
	/**
	 * Gets the outer user id (OUCID) of a player
	 * @param UCID Connection ID of the player
	 * @param insimID ID of the server
	 * @return OUCID as short
	 */
	public short getOuterUserID(byte UCID, byte insimID)
	{
		User temp = null;
		int size = this.users.size();
		
		for (int i = 0; i < size; i++)
		{
			temp = users.get(i);
			if (temp.UCID == UCID && temp.insimID == insimID)
				return temp.OUCID;
		}
		return -1;
	}
	
	/**
	 * Updates if a player changed its name
	 * @param UCID
	 * @param insimID
	 * @param playerName
	 */
	public void changePlayerName(byte UCID, byte insimID, String playerName)
	{
		User temp = null;
		int size = this.users.size();
		
		for (int i = 0; i < size; i++)
		{
			temp = this.users.get(i);
			if (temp.UCID == UCID && temp.insimID == insimID)
			{
				temp.playerName = playerName;				
				break;
			}
		}
	}
	
	/**
	 * This function gets UCID and insimID of a player
	 * <br/>It's used for the message system and for the admin system as well.
	 * @param OUCID of the Client 
	 * @return InsimID and UCID of the Client as byte array
	 */
	public byte[] getInsimAndUCID(short OUCID)
	{
		User temp = null;
		int size = this.users.size();
		byte[] retVal = null;
		
		for (int i = 0; i < size; i++)
		{
			temp = this.users.get(i);
			if (temp.OUCID == OUCID)
			{
				retVal = new byte[2];
				retVal[0] = temp.insimID;
				retVal[1] = temp.UCID;
				break;
			}
		}
		return retVal;
	}
	
	/**
	 * Gets all usernames and additional playernames
	 * <br/>This function is mostly used to create a userlist of ALL servers connected to the system.
	 * 
	 * @param withPlayernames
	 * @return ArrayList with all the usernames.
	 * 	Output: Playername
	 *  If the param is true, the playernames will be returned as well.
	 *  Output: Playername (Username) : OUCID
	 */
	public ArrayList<String> getAllUserNames(boolean withPlayernames)
	{
		ArrayList<String> names = new ArrayList<String>();
		
		User temp = null;
		int size = this.users.size();
		if (withPlayernames)
		{
			for (int i = 0; i < size; i++)
			{
				temp = this.users.get(i);
				names.add(temp.playerName + " ^7("+ temp.userName + ")" + ": ^6" + temp.OUCID + "");
			}
		}
		else
		{
			for (int i = 0; i < size; i++)
			{
				temp = this.users.get(i);
				names.add(temp.userName);
			}
		}
		return names;
	}
	
	/**
	 * This function is used to get all the OUCIDs of all connected servers.
	 * @return An ArrayList with the IDs.
	 */
	public ArrayList<Short> getAllUserOUCIDs()
	{
		ArrayList<Short> OUCIDs = new ArrayList<Short>();
		
		User temp = null;
		int size = this.users.size();
		for (int i = 0; i < size; i++)
		{
			temp = this.users.get(i);
			OUCIDs.add(temp.OUCID);
		}
		return OUCIDs;
	}
	
	/**
	 * This function is used to get all members for the teamchat!
	 * @return An ArrayList with all Members OUCIDs of all servers.
	 */
	public ArrayList<Short> getAllMemberOUCIDs()
	{
		ArrayList<Short> mems = new ArrayList<Short>();
		int size = this.users.size();
		User temp = null;
		
		for (int i = 0; i < size; i++)
		{
			temp = users.get(i);
			if (temp.member)
				mems.add(temp.OUCID);
		}
		
		return mems;
	}
	
	/**
	 * This function is used to get all admins for the report function.
	 * @return An ArrayList with all Admins OUCIDs of all servers.
	 */
	public ArrayList<Short> getAllAdminsOUCIDs()
	{
		ArrayList<Short> adms = new ArrayList<Short>();
		int size = this.users.size();
		User temp = null;
		
		for (int i = 0; i < size; i++)
		{
			temp = users.get(i);
			if (temp.admin)
				adms.add(temp.OUCID);
		}
		
		return adms;
	}
	
	/**
	 * Checks if a player with UCID and a server with insimID actually is a member or not.
	 * @param UCID connection id of the player
	 * @param insimID ID of the server
	 * @return true of false
	 */
	public boolean isMember(byte UCID, byte insimID)
	{
		int size = this.users.size();
		User temp = null;
		for (int i = 0; i < size; i++)
		{
			temp = users.get(i);
			if (temp.UCID == UCID && temp.insimID == insimID && temp.member)
				return true;
		}
		return false;
	}
	
	/**
	 * Checks if a player with UCID and a server with insimID actually is admin or not.
	 * @param UCID connection id of the player
	 * @param insimID ID of the server
	 * @return true or false
	 */
	public boolean isAdmin(byte UCID, byte insimID)
	{
		int size = this.users.size();
		User temp = null;
		for (int i = 0; i < size; i++)
		{
			temp = users.get(i);
			if (temp.UCID == UCID && temp.insimID == insimID)
				return temp.admin;
		}
		return false;
	}
	
	/**
	 * Not all players are allowed to be kicked. 
	 * <br/>This function determines, if a player can be kicked or not
	 * @param OUCID outer connection id of the player
	 * @return True if the player is allowed to be kicked, false if not.
	 */
	public boolean isKickable(short OUCID)
	{
		int size = this.users.size();
		User temp = null;
		for (int i = 0; i < size; i++)
		{
			temp = this.users.get(i);
			if (!temp.member && !temp.admin && temp.OUCID == OUCID)
				return true;
		}
		return false;
	}
	
	/**
	 * Not all players are allowed to be kicked. 
	 * <br/>This function determines, if a player can be kicked or not
	 * @param userName lfs.net username of the player
	 * @return True if the player is allowed to be kicked, false if not.
	 */
	public boolean isKickable(String userName)
	{
		int size = this.users.size();
		User temp = null;
		for (int i = 0; i < size; i++)
		{
			temp = this.users.get(i);
			if (!temp.member && !temp.admin && temp.userName.equals(userName))
				return true;
		}
		return false;
	}
	
	/**
	 * This function gets the lfs.net username of a player 
	 * @param OUCID outer connection ID of the player
	 * @return userName as a String
	 */
	public String getUserName(short OUCID)
	{
		int size = this.users.size();
		User temp = null;
		for (int i = 0; i < size; i++)
		{
			temp = this.users.get(i);
			if (temp.OUCID == OUCID)
				return temp.userName;
		}
		return null;
	}
	
	/**
	 * This function is WIP - REMOVE THIS ONCE DONE
	 * 
	 * <br/>This function is used to flag a player to refresh the connectionlist
	 * @param OUCID outer connection id of the player
	 * @param open refresh list if list is open, else not
	 */
	public void setListStatus(short OUCID, boolean open)
	{
		User temp = null;
		for (int i = 0; i < this.users.size(); i++)
		{
			temp = this.users.get(i);
			if (temp.OUCID == OUCID)
			{
				temp.list = open;
				break;
			}
		}
	}
	
	/**
	 * This function is WIP - REMOVE THIS ONCE DONE
	 * 
	 * This function is used to flag a player to refresh the connectionlist
	 * @param UCID connection id of the player
	 * @param insimID ID of the server
	 * @param open refresh list if list is open, else not
	 */
	public void setListStatus(byte UCID, byte insimID, boolean open)
	{
		User temp = null;
		for (int i = 0; i < this.users.size(); i++)
		{
			temp = this.users.get(i);
			if (temp.UCID == UCID && temp.insimID == insimID)
			{
				temp.list = open;
				break;
			}
		}
	}
	
	/**
	 * Gets if a player has the list opened or closed
	 * @param OUCID outer connection id of the player
	 * @return true or false
	 */
	public boolean getListStatus(short OUCID)
	{
		User temp = null;
		for (int i = 0; i < this.users.size(); i++)
		{
			temp = this.users.get(i);
			if (temp.OUCID == OUCID)
			{
				return temp.list;
			}
		}
		return false;
	}
	
	/**
	 * Gets if a player has the list opened or closed
	 * @param UCID connection id of the player
	 * @param insimID id of the server
	 * @return true of false
	 */
	public boolean getListStatus(byte UCID, byte insimID)
	{
		User temp = null;
		for (int i = 0; i < this.users.size(); i++)
		{
			temp = this.users.get(i);
			if (temp.UCID == UCID && temp.insimID == insimID)
			{
				return temp.list;
			}
		}
		return false;
	}
	
	/**
	 * Sets if a user has shown the teamchat button
	 * <br/>Only used for members until now
	 * @param UCID of the user
	 * @param insimID of the InSimWrapper
	 * @param status true (=visible) or false (=hidden)
	 */
	public void setTeamchatStatus(byte UCID, byte insimID, boolean status)
	{
		User temp = null;
		for (int i = 0; i < this.users.size(); i++)
		{
			temp = this.users.get(i);
			if (temp.UCID == UCID && temp.insimID == insimID)
			{
				temp.teamchat = status;
			}
		}
	}
	
	/**
	 * Sets if a user has shown the report button
	 * @param UCID of the user
	 * @param insimID of the InSimWrapper
	 * @param status true (=visible) or false (=hidden)
	 */
	public void setReportStatus(byte UCID, byte insimID, boolean status)
	{
		User temp = null;
		for (int i = 0; i < this.users.size(); i++)
		{
			temp = this.users.get(i);
			if (temp.UCID == UCID && temp.insimID == insimID)
			{
				temp.report = status;
			}
		}
	}
	
	/**
	 * This function gets all the users that have the userlist opened
	 * @return byte array with the information: UCID, insimID, 
	 * teamchat-button visible, report-button visible
	 */
	public ArrayList<Byte[]> getAllListUsers()
	{
		ArrayList<Byte[]> ids = new ArrayList<Byte[]>();
		User u = null;
		for (int i = 0; i < this.users.size(); i++)
		{
			u = users.get(i);
			byte teamchat = 0, report = 0;
			if (u.teamchat)
				teamchat = 1;
			if (u.report)
				report = 1;
			
			if (u.list)
			{
				ids.add(new Byte[]
						{
							(byte)u.UCID,
							(byte)u.insimID,
							teamchat,
							report
						});
			}
		}
		return ids;
	}
	
	/**
	 * This function is used to flag a user to have accepted the rules
	 * @param UCID	UCID of the user
	 * @param insimID	InsimID of the Server the user plays on
	 */
	public void setAccepted(byte UCID, byte insimID)
	{
		User temp = null;
		for(int i = 0; i < this.users.size(); i++)
		{
			temp = this.users.get(i);
			if (temp.insimID == insimID && temp.UCID == UCID)
			{
				temp.accepted_rules = true;
			}
		}
	}
	
	/**
	 * You can find out if a user accepted the rules with this function
	 * @param UCID		UCID of the user
	 * @param insimID	InsimID of the server the user plays on
	 * @return	True if the rules were accepted, false if not.
	 */
	public boolean getAccepted(byte UCID, byte insimID)
	{
		User temp = null;
		for(int i = 0; i < this.users.size(); i++)
		{
			temp = this.users.get(i);
			if (temp.insimID == insimID && temp.UCID == UCID)
			{
				return temp.accepted_rules;
			}
		}
	//	If no user was found return false as well!
		return false;
	}
	
	/**
	 * Gets the OUCID of a user
	 * @param userName of the user
	 * @return OUCID
	 */
	public short getOUCIDofUser(String userName)
	{
		int size = this.users.size();
		User temp = null;
		for (int i = 0; i < size; i++)
		{
			temp = this.users.get(i);
			if (temp.userName.equals(userName))
				return temp.OUCID;
		}
		return -1;
	}
	
	
	public void setMemberStatus(byte UCID, byte insimID, boolean isMember)
	{
		int size = this.users.size();
		User temp = null;
		for(int i = 0; i < size; i++)
		{
			temp = this.users.get(i);
			if(temp.UCID == UCID && temp.insimID == insimID)
			{
				if(isMember)
				{
					temp.member = true;
					if(!this.members.contains(temp.userName))
						this.members.add(temp.userName);
				}
				else
				{
					temp.member = false;
					if(this.members.contains(temp.userName))
						this.members.remove(temp.userName);
				}
			}
		}
	}
	
	
	public void setMemberStatus(short OUCID, boolean isMember)
	{
		int size = this.users.size();
		User temp = null;
		for(int i = 0; i < size; i++)
		{
			temp = this.users.get(i);
			if(temp.OUCID == OUCID)
			{
				if(isMember)
				{
					temp.member = true;
					if(!this.members.contains(temp.userName))
						this.members.add(temp.userName);
				}
				else
				{
					temp.member = false;
					if(this.members.contains(temp.userName))
						this.members.remove(temp.userName);
				}
			}
		}
	}
}

/**
 * This class provides the users with outer user ids, used by the UserManagement
 * <br/>IDs are set by the UserManagement. There is only one ID per user, wich does not change as long as he is connected
 * @author anrc
 * @version 1.0
 *
 */
class ID_Provider
{
	private boolean[] ids;
	
	/**
	 * Creates a new ID_PROVIDER
	 * <br/>Each ID_PROVIDER has it's very own IDs to provide
	 * <br/>HINT: use only one ID_PROVIDER
	 */
	public ID_Provider()
	{
		this.ids = new boolean[Short.MAX_VALUE -1];
	}
	
	/**
	 * Gets the next free id
	 * @return ID as short
	 */
	public short getNextFreeID()
	{
		for (int i = 0; i < ids.length; i++)
		{
			if(!ids[i])
			{
				ids[i] = true;
				return (short)++i;
			}
		}
		throw new OutOfMemoryError("Can't allocate more user slots!");
	}
	
	/**
	 * Takes back an id
	 * @param id to give back
	 */
	public void revokeID(short id)
	{
		ids[--id] = false;
	}
}