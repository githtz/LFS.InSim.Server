package Main;
import java.io.IOException;
import java.util.Scanner;

import org.openbakery.jinsim.Channel;
import org.openbakery.jinsim.SimpleClient;
import org.openbakery.jinsim.Tiny;
import org.openbakery.jinsim.UDPChannel;
import org.openbakery.jinsim.request.TinyRequest;

import Configuration.Config;
import Routines.DirectionControl.Direction;
import packetHandlers.ButtonClickHandler;
import packetHandlers.ButtonRemoveHandler;
import packetHandlers.ButtonTextResponse;
import packetHandlers.CloseConnectionHandler;
import packetHandlers.ConnectionRenameHandler;
import packetHandlers.MessageHandler;
import packetHandlers.NewConnectionHandler;
import packetHandlers.PlayerCarTakeoverHandler;
import packetHandlers.PlayerJoinRaceHandler;
import packetHandlers.PlayerLeaveRaceHandler;
import packetHandlers.PlayerMovementPacketHandler;
import packetHandlers.PlayerPitlaneHandler;
import packetHandlers.SpecialHandlers.CommandExecutorHandler;
import packetHandlers.hiddenMessageHandlers.AdminHandler;
import packetHandlers.hiddenMessageHandlers.GeneralHandler;
import packetHandlers.hiddenMessageHandlers.TeamHandler;
import packetInterfaces.packetHandler;

public class Main
{	
	/*
	 * 	The config contains the most important connection configurations,
	 *  for example ip, port and password of the server you want to use
	 *  this software on. 
	 */
	public static final Config config = null;//ServerConfig or LocalConfig

	
	public static void main(String[] args) throws IOException
	{		
		/*
		 * 	Handlers are the most important part of this software.
		 * 	A handler is able to do stuff when the server triggers
		 * 	certain events. For example when a player connects, 
		 * 	or when someone writes stuff in the chat.
		 */
		InSimWrapper.handlers = new packetHandler[]
				{
				//	Connection Handlers
					new NewConnectionHandler(),
					new ConnectionRenameHandler(),
					new CloseConnectionHandler(),
					
				//	Normal Messages Handler
					new MessageHandler(),
					
				//	Player Action Handlers
					new PlayerCarTakeoverHandler(),
					new PlayerJoinRaceHandler(),
					new PlayerLeaveRaceHandler(),
					//new PlayerPitHandler(),
					new PlayerPitlaneHandler(),
					
				//	Button Handlers
					new ButtonClickHandler(),
					new ButtonTextResponse(),
					new ButtonRemoveHandler(),
					
				//	Hidden Messages Handlers
					new AdminHandler(),
					new GeneralHandler(),
					new TeamHandler(),
					
				//	Special Handlers
					new CommandExecutorHandler(),
					
				//	Idle Handler
					new PlayerMovementPacketHandler(),
					//new RaceStartHandler()
					
				//	User Defined Handlers
				};
		
		//	The scanner is used for direct input commands
		Scanner sc = new Scanner(System.in);
		//	The CrossChat connects all the servers to each others
		CrossChat chat = CrossChat.CreateCrossChat();
		//	UserManagement manages the users and their rights
		UserManagement.CreateUsermanagement().loadMemberList(config.memberlist);
		//	The SimpleClient is used to connect and maintain connection
		SimpleClient[] clients = new SimpleClient[config.ports.length];
		
		for (int i = 0; i < config.ports.length; i++)
		{
			SimpleClient client = new SimpleClient();
			clients[i] = client;
			//	The UDP Channel creates a fast but insecure connection to the server
			//	Since we are using this client for out local server only it's the best ;)
			Channel channel = new UDPChannel(config.ip, config.ports[i]);
			InSimWrapper wrapper = new InSimWrapper(client);
			//	The host name will be the name to be displayed in-game, when a member connects
			wrapper.hostname = config.hostnames[i];
			if (i==0)
				wrapper.direction = Direction.REVERSE;
			else
				wrapper.direction = Direction.CRUISE;
			//	Now we add the wrapper as listener to the SimpleClient
			client.addListener(wrapper);
			//	Then we add the wrapper to the CrossChat
			chat.addInsim(wrapper);
			client.setRequestVersion(true);
			//	Now we establish the connection
			client.connect(channel, config.password, "CrossChat");
			//	Finally we send a request to the server to get all connections
			TinyRequest treq = new TinyRequest(Tiny.ALL_CONNECTIONS);
			client.send(treq);
			
			treq = new TinyRequest(Tiny.ALL_PLAYERS);
			client.send(treq);
			
			//	This will be needed in future
			//	Sending this request will allow us to check players
			TinyRequest req = new TinyRequest(Tiny.MULTI_CAR_INFO);
			client.send(req);
			
		}
		
		ConnectionRefresher r = new ConnectionRefresher(clients);
		Thread t = new Thread(r);
		t.start();
		
		boolean loop = true;
		while(loop)
		{
			String cmd = sc.nextLine();
			if (cmd.equals("exit"))
			{
				chat.quit();
				loop = false;
			}
			else if (cmd.equals("reload"))
			{
				UserManagement u;
				synchronized (u = UserManagement.CreateUsermanagement())
				{
					boolean success = u.loadMemberList(config.memberlist);
					if (success)
					{
						System.out.println("The memberlist was reloaded successfully :)");
					}
					else
					{
						System.out.println("Something went wrong :(");
					}
				}
			}
			else if (cmd.equals("reconnect"))
			{
				//System.out.println("Sorry, the reconnect function does not work yet.");
				for(int i = 0; i < clients.length; i++)
				{
					clients[i].close();
					Channel c = new UDPChannel(config.ip, config.ports[i]);
					clients[i].connect(c, config.password, "CrossChat");
				}
			}
		}
		System.out.println("quitting");
		for(int i = 0; i < clients.length; i++)
		{
			clients[i].close();
		}
		sc.close();
	}

}

/**
 * @version 0.1a
 * @author anrc
 * This will be needed to detect timeouts
 * When a host suddenly loses connection this 
 * should be able to find it out, and reconnect
 */
class ConnectionRefresher implements Runnable
{
	private SimpleClient[] clients;
	
	public ConnectionRefresher(SimpleClient[] clients)
	{
		this.clients = clients;
	}
	
	public void run()
	{
			while (true)
			{
				for (int i = 0; i < clients.length; i++)
				{
					try
					{
						clients[i].send(new TinyRequest(Tiny.MULTI_CAR_INFO));
					} catch (IOException e)
					{
						System.err.println("Could not send alive packet");
						e.printStackTrace();
					}
				}
				try
				{
					Thread.sleep(1000);
				} catch (InterruptedException e)
				{
					System.err.println("Coult not set timeout :(");
					e.printStackTrace();
				}
			}
	}
}