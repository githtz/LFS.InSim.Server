package statistic;

import java.util.ArrayList;
import java.util.Date;

public class PlayerAccount
{
	public String accountname;
	public ArrayList<String> nickNames;
	public long lastLogin;
	public long onlineTime;
	public long lastOnTrack;
	public long playTime;
	
	public PlayerAccount()
	{
		this.nickNames = new ArrayList<String>();
		this.playTime = 0;
		this.onlineTime = 0;
		this.lastLogin = new Date().getTime();
		this.lastOnTrack = new Date().getTime();
	}
	
	public synchronized void searchOrAdd(String nickName)
	{
		StringBuilder str = new StringBuilder();
		String[] split = nickName.split("\0");
		nickName = "";
		for (int i = 0; i < split.length; i++)
		{
			str.append(split[i]);
		}
		
		nickName = str.toString();
		
		for(int i = 0; i < this.nickNames.size(); i++)
		{
			if (nickNames.get(i).equals(nickName))
				return;
		}
		this.nickNames.add(nickName);
	}
}
