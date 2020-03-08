package Main;
import java.rmi.server.UID;
import java.util.Date;

/**
 * This class is used as structure for users and their information
 * Users are created by the UserManagement. Don't do it yourself.
 * @author anrc
 *
 */
public class User
{
	public byte UCID;
	public short OUCID;
	public String userName;
	public String playerName;
	public byte insimID;
	public boolean member;
	public boolean admin;
	public boolean list;
	public boolean teamchat;
	public boolean report;
	public Date last_activity;
	public boolean accepted_rules;
	
	/**
	 * Creates a new user
	 * @param UCID of the user
	 * @param OUCID of the user
	 * @param userName name of the user used on lfs.net
	 * @param playerName temporary playername with colors and stuff
	 * @param insimID ID of the InSimWrapper this user is connected on
	 * @param member self explaining
	 * @param admin self explaining
	 */
	public User(byte UCID, short OUCID, String userName, String playerName, byte insimID, boolean member, boolean admin)
	{
		this.UCID = UCID;
		this.OUCID = OUCID;
		this.userName = userName;
		this.playerName = playerName;
		this.insimID = insimID;
		this.member = member;
		this.admin = admin;
		this.list = false;
		this.teamchat = false;
		this.report = false;
		this.accepted_rules = false;
	}
}	
