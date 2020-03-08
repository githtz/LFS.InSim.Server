package statistic;

import java.util.Date;
import java.util.ArrayList;

public class OnlineLog
{
	public ArrayList<PlayerAccount> players;
	
	public OnlineLog()
	{
		this.players = new ArrayList<PlayerAccount>();
	}
	
	private PlayerAccount find(String accountName)
	{
		int size = players.size();
		PlayerAccount temp = null;
		
		for(int i = 0; i < size; i++)
		{
			temp = players.get(i);
			if (temp.accountname.equals(accountName))
			{
				return temp;
			}
		}
		return null;
	}
	
	public void connect(String accountName, String nickName)
	{
		Date now = new Date();
		synchronized (players)
		{
			int size = players.size();
			PlayerAccount player = find(accountName);
			
			if (player == null)
			{
				player = new PlayerAccount();
				player.accountname = accountName;
				this.players.add(player);
			}
			player.searchOrAdd(nickName);
			player.lastLogin = now.getTime();
		}
	}
	
	public void disconnect(String accountName)
	{
		Date now = new Date();
		synchronized (players)
		{
			int size = players.size();
			PlayerAccount player = find(accountName);
			if (player == null)
			{
				player = new PlayerAccount();
				player.accountname = accountName;
				this.players.add(player);
			}
			player.onlineTime = now.getTime() - player.lastLogin + player.onlineTime;
		}

	}
	
	public void rename(String accountName, String nickName)
	{
		synchronized (players)
		{
			PlayerAccount player = find(accountName);
			if (player == null)
			{
				player = new PlayerAccount();
				player.accountname = accountName;
				this.players.add(player);
			}
			player.searchOrAdd(nickName);
		}
	}
	
	public void joinRace(String accountName)
	{
		Date now = new Date();
		synchronized (players)
		{
			PlayerAccount player = find(accountName);
			if (player == null)
			{
				player = new PlayerAccount();
				player.accountname = accountName;
				this.players.add(player);
			}
			player.lastOnTrack = now.getTime();
		}
	}
	
	public void leaveRace(String accountName)
	{
		Date now = new Date();
		synchronized (players)
		{
			PlayerAccount player = find(accountName);
			if (player == null)
			{
				player = new PlayerAccount();
				player.accountname = accountName;
				this.players.add(player);
			}
			if (player.lastOnTrack != 0)
			{
				player.playTime = now.getTime() - player.lastOnTrack + player.playTime;
				player.lastOnTrack = 0;
			}
		}
	}
}
