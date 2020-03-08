package ChatAddons;



import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;

//First the class that will just store our information
class bPlayer
{
	// first the name of the player. OFC we need the accountname, so we call the
	// variable
	// accountname as well
	String accountName;
	// Long is very much like int, but you can save much bigger numbers in it.
	// We use
	// this long to save the time of the last swearing
	long last_swearword;
	
	// finally we still need to look after the number of swears
	int swearword_count;
}

// this is the class equivalent to you SwearPrevention class
public class SwearWordPrevention
{
	// Ok, just as you know we need the ArrayList
	private ArrayList<bPlayer> bPlayers;
	
	private ArrayList<String> bWords;
	
	// Now the constructor. This function gets called when you CREATE AN OBJECT
	// of this class
	public SwearWordPrevention()
	{
		this.bPlayers = new ArrayList<bPlayer>();
		this.bWords = new ArrayList<String>();
	}

	// and finally the function itself
	public int bWordsPenalty(String accountName, String message)
	{
		message = " " + message + " ";
		
		// We need a "container" to save the Player in. This is our REFERENCE
		// We set it to "null". "null" means it's empty
		bPlayer player = null;
		long temp = new Date().getTime();

		// Now we go through the List of players!
		for (int i = 0; i < this.bPlayers.size(); i++)
		{
			// See if the names match
			// IMPORTANT: You NEED to use "equals()" here!
			// Strings can NOT be compared with "=="
			if (accountName.equals(bPlayers.get(i).accountName))
			{
				// If the player is found we can set our REFERENCE to the player
				player = bPlayers.get(i);
				// then we break the loop, because we're not going to find any
				// other fitting player
				break;
			}
		}
		// If the loop was broken then our REFERENCE "player" is not null
		// anymore. If it is
		// still null we know, that there is no player with this account name
		// yet, so we need
		// to create it

		// Let's see if we need to create a new player
		if (player == null)
		{
			// Now we create the new Player
			player = new bPlayer();
			// We set his account name, the rest does not matter for now
			player.accountName = accountName;
			// Now add the player to the list, so we don't need to create him
			// again later
			bPlayers.add(player);
		}
		
		int oldcount = player.swearword_count;
		
		for (int i = 0; i < this.bWords.size(); i++)
		{
			if ( message.contains( " " + this.bWords.get(i) + " " ) )
			{
				if (temp - player.last_swearword < (1000 * 60 * 10) )
				{
					player.last_swearword = temp;
					player.swearword_count++;
				}
				else
				{
					player.last_swearword = temp;
					player.swearword_count = 1;
				}
				
				break;
			}
		}
		
		if (oldcount != player.swearword_count)
			return player.swearword_count;
		else 
			return 0;
	}
	
	public void reset(String accountName)
	{
		bPlayer player = null;
		for (int i = 0; i < this.bPlayers.size(); i++)
		{
			if (accountName.equals(bPlayers.get(i).accountName))
			{
				player = bPlayers.get(i);
				player.swearword_count = 0;
				break;
			}
		}
	}
	
	public void loadList(String path)
	{
		File f = new File(path);
		if (!f.exists())
			return;
		ArrayList<String> words = null;
		
		try
		{
			words = (ArrayList<String>) Files.readAllLines(f.toPath(), Charset.defaultCharset());
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (words != null)
			this.bWords = words;
	}
}

