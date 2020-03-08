package packetHandlers;

import java.io.IOException;

import org.openbakery.jinsim.request.ButtonRequest;
import org.openbakery.jinsim.response.InSimResponse;
import org.openbakery.jinsim.response.NewConnectionResponse;

import main.InSimWrapper;
import packetInterfaces.packetHandler;

/**
 * @version 1.0
 * @author anrc
 * This class class handles new users on the server.
 * Every time a user connects his credentials will be
 * saved into the UserManagement.
 */
public class NewConnectionHandler implements packetHandler
{
	public void handlePacket(InSimResponse packet, InSimWrapper wrapper)
	{
		if (packet instanceof NewConnectionResponse)
		{
			NewConnectionResponse _packet = (NewConnectionResponse)packet;
			
			byte UCID = (byte)_packet.getConnectionId();
			String username = _packet.getUsername();
			if (username.equals(""))
				return;
			String playername = _packet.getPlayerName();
			boolean admin = _packet.isAdmin();
			boolean member = false;
			
			synchronized (wrapper.userManager)
			{				
				wrapper.userManager.addUser(UCID, username, playername, wrapper.id, admin);
				member = wrapper.userManager.isMember(UCID, wrapper.id);
			}
			
			wrapper.sendMessage(UCID, " ");
			wrapper.sendMessage(UCID, " ");
			//wrapper.sendMessage(UCID, "^3Welcome to the ^7Tofu[^1�^7] servers, " + playername);
			wrapper.sendMessage(UCID, "^3Wecome to the ^7Curb<^1*^7> servers, " + playername);
			if (member)
				wrapper.sendMessage(UCID, "^1You are logged in as ^6member^1.");
			else
			{
				wrapper.sendMessage(UCID, "^1You are logged in as ^7user^1.");
				wrapper.createReportButton(UCID);
			}
			wrapper.sendMessage(UCID, "^3Use ^7/i help ^3to get help with the server's commands.");
			wrapper.sendMessage(UCID, "^3Have a nice stay!");
			wrapper.sendMessage(UCID, " ");
			wrapper.sendMessage(UCID, " ");
			
			if (InSimWrapper.containsCurb(playername) && !member)
			{
				wrapper.sendCommand(playername + " (" + username + ") ^1was auto-kicked for faking!");
				//wrapper.sendMessage(UCID, "^1You are not a Tofu member! Remove \"^7Tofu[^1�^7]^1\" from your name!");
				wrapper.sendMessage(UCID, "^1You are not a Curb member! Remove \"^7Curb<^1*^7>\" from your name!");
				wrapper.sendCommand("/kick " + username);
			}
			
			/*
			 * 	Send Welcome Dialog!
			 */			
			ButtonRequest welcome = new ButtonRequest();
			welcome = new ButtonRequest();
			welcome.setConnectionId(UCID);
			welcome.setWidth((byte)140);
			welcome.setHeight((byte)170);
			welcome.setLeft((byte)30);
			welcome.setTop((byte)(10));
			welcome.setRequestInfo(wrapper.INFOID);
			welcome.setButtonStyle(ButtonRequest.BUTTON_STYLE_BLUE + ButtonRequest.BUTTON_STYLE_DARK);
			welcome.setText("");
			welcome.setClickId((byte)0);
			
			/*
			String line1 = 
				"Hello, " + playername + "^7 ("+username+") " + "^8and welcome to the Tofu Servers!";
			String line2 =
				"Here on tofu we have set some rules that the players should follow, so please read them all:";
			String line3 =
				"^1#1^7 - Do ^1NOT crash ^7into other cars on purpose.";
			String line4 =	
				"^1#2^7 - Offensive Language is a ^1no-no^7! There are minors using the services we provide.";
			String line5 =	
				"^1#3^7 - You have to drive ^1" + wrapper.direction.toString() + " ^7here.";
			String line6 =	
				"^1#4^7 - You are to ^1ALWAYS ^7respect other players, tofu members and admins.";
			/*String line7 =	
				"^1#5^7 - Please enjoy your stay here, and do your best to lead by example in creating a civil environment of fun!";
			String line8 =	
				"^1#6^7 - Rules are subject to change without notice.";
			String line9 =*
			String line7 =
				"Violation of the rules will result in a warning, a kick or in the worst case, a ban depending on the severity of the violation.";
			String line8 =
				"^6>>> ^1OUR SERVERS ^7finally ^1WILL SHUT DOWN ON 4th August, 2014. ^6<<<";
			String line9 = 
				"^6>>> ^7Visit our page for ^1the last ^7news: ^1http://tofu-project.web44.net ^6<<<";
			String line10 =
				"Thank you for everything," + /*" ^7Tofu[^1�^7]-Team.";* " ^7Tofu[^1�^7]soujir^6�";
			*/
			
			String line1 = 
					"Hello, " + playername + "^7 ("+username+") " + "^8and welcome to the Curb Servers!";
				String line2 =
					"We have set some rules here that the players should follow, so please read them all:";
				String line3 =
					"^1#1^7 - Do ^1NOT crash ^7into other cars on purpose.";
				String line4 =	
					"^1#2^7 - Offensive Language is a ^1no-no^7! There are minors using the services we provide.";
				String line5 =	
					"^1#3^7 - You have to drive ^1" + wrapper.direction.toString() + " ^7here.";
				String line6 =	
					"^1#4^7 - You are to ^1ALWAYS ^7respect other players, curb members and admins.";
				String line7 =	
					"^1#5^7 - Please enjoy your stay here, and do your best to lead by example in creating a civil environment of fun!";
				String line8 =	
					"^1#6^7 - Rules are subject to change without notice.";
				String line9 = 
						"Violation of the rules will result in a warning, a kick or in the worst case, a ban depending on the severity of the violation.";
				String line10 = "Have a nice stay!";
			
			
			ButtonRequest b_line1 = new ButtonRequest();
			b_line1 = new ButtonRequest();
			b_line1.setConnectionId(UCID);
			b_line1.setWidth((byte)134);
			b_line1.setHeight((byte)10);
			b_line1.setLeft((byte)33);
			b_line1.setTop((byte)(12));
			b_line1.setRequestInfo(wrapper.L1);
			b_line1.setButtonStyle(ButtonRequest.BUTTON_STYLE_WHITE);
			b_line1.setText(line1);
			b_line1.setClickId((byte)1);
			
			ButtonRequest b_line2 = new ButtonRequest();
			b_line2 = new ButtonRequest();
			b_line2.setConnectionId(UCID);
			b_line2.setWidth((byte)134);
			b_line2.setHeight((byte)10);
			b_line2.setLeft((byte)33);
			b_line2.setTop((byte)(32));
			b_line2.setRequestInfo(wrapper.L2);
			b_line2.setButtonStyle(ButtonRequest.BUTTON_STYLE_WHITE);
			b_line2.setText(line2);
			b_line2.setClickId((byte)2);
			
			ButtonRequest b_line3 = new ButtonRequest();
			b_line3 = new ButtonRequest();
			b_line3.setConnectionId(UCID);
			b_line3.setWidth((byte)134);
			b_line3.setHeight((byte)10);
			b_line3.setLeft((byte)33);
			b_line3.setTop((byte)(42));
			b_line3.setRequestInfo(wrapper.L3);
			b_line3.setButtonStyle(ButtonRequest.BUTTON_STYLE_WHITE);
			b_line3.setText(line3);
			b_line3.setClickId((byte)3);
			
			ButtonRequest b_line4 = new ButtonRequest();
			b_line4 = new ButtonRequest();
			b_line4.setConnectionId(UCID);
			b_line4.setWidth((byte)134);
			b_line4.setHeight((byte)10);
			b_line4.setLeft((byte)33);
			b_line4.setTop((byte)(52));
			b_line4.setRequestInfo(wrapper.L4);
			b_line4.setButtonStyle(ButtonRequest.BUTTON_STYLE_WHITE);
			b_line4.setText(line4);
			b_line4.setClickId((byte)4);
			
			ButtonRequest b_line5 = new ButtonRequest();
			b_line5 = new ButtonRequest();
			b_line5.setConnectionId(UCID);
			b_line5.setWidth((byte)134);
			b_line5.setHeight((byte)10);
			b_line5.setLeft((byte)33);
			b_line5.setTop((byte)(62));
			b_line5.setRequestInfo(wrapper.L5);
			b_line5.setButtonStyle(ButtonRequest.BUTTON_STYLE_WHITE);
			b_line5.setText(line5);
			b_line5.setClickId((byte)5);
			
			ButtonRequest b_line6 = new ButtonRequest();
			b_line6 = new ButtonRequest();
			b_line6.setConnectionId(UCID);
			b_line6.setWidth((byte)134);
			b_line6.setHeight((byte)10);
			b_line6.setLeft((byte)33);
			b_line6.setTop((byte)(72));
			b_line6.setRequestInfo(wrapper.L6);
			b_line6.setButtonStyle(ButtonRequest.BUTTON_STYLE_WHITE);
			b_line6.setText(line6);
			b_line6.setClickId((byte)6);
			
			ButtonRequest b_line7 = new ButtonRequest();
			b_line7 = new ButtonRequest();
			b_line7.setConnectionId(UCID);
			b_line7.setWidth((byte)134);
			b_line7.setHeight((byte)10);
			b_line7.setLeft((byte)33);
			b_line7.setTop((byte)(82));
			b_line7.setRequestInfo(wrapper.L7);
			b_line7.setButtonStyle(ButtonRequest.BUTTON_STYLE_WHITE);
			b_line7.setText(line7);
			b_line7.setClickId((byte)7);
			
			ButtonRequest b_line8 = new ButtonRequest();
			b_line8 = new ButtonRequest();
			b_line8.setConnectionId(UCID);
			b_line8.setWidth((byte)134);
			b_line8.setHeight((byte)10);
			b_line8.setLeft((byte)33);
			b_line8.setTop((byte)(92+10));
			b_line8.setRequestInfo(wrapper.L8);
			b_line8.setButtonStyle(ButtonRequest.BUTTON_STYLE_WHITE);
			b_line8.setText(line8);
			b_line8.setClickId((byte)8);
			
			ButtonRequest b_line9 = new ButtonRequest();
			b_line9 = new ButtonRequest();
			b_line9.setConnectionId(UCID);
			b_line9.setWidth((byte)134);
			b_line9.setHeight((byte)10);
			b_line9.setLeft((byte)33);
			b_line9.setTop((byte)(102+10));
			b_line9.setRequestInfo(wrapper.L9);
			b_line9.setButtonStyle(ButtonRequest.BUTTON_STYLE_WHITE);
			b_line9.setText(line9);
			b_line9.setClickId((byte)9);
			
			ButtonRequest b_line10 = new ButtonRequest();
			b_line10 = new ButtonRequest();
			b_line10.setConnectionId(UCID);
			b_line10.setWidth((byte)134);
			b_line10.setHeight((byte)10);
			b_line10.setLeft((byte)33);
			b_line10.setTop((byte)(122+10));
			b_line10.setRequestInfo(wrapper.L10);
			b_line10.setButtonStyle(ButtonRequest.BUTTON_STYLE_WHITE);
			b_line10.setText(line10);
			b_line10.setClickId((byte)10);	
			
			ButtonRequest b_line11 = new ButtonRequest();
			b_line11 = new ButtonRequest();
			b_line11.setConnectionId(UCID);
			b_line11.setWidth((byte)20);
			b_line11.setHeight((byte)10);
			b_line11.setLeft((byte)60);
			b_line11.setTop((byte)(152));
			b_line11.setRequestInfo(wrapper.L11);
			b_line11.setButtonStyle(ButtonRequest.BUTTON_STYLE_RED + ButtonRequest.BUTTON_STYLE_CLICK + ButtonRequest.BUTTON_STYLE_LIGHT);
			b_line11.setText("Decline");
			b_line11.setClickId((byte)11);
			
			ButtonRequest b_line12 = new ButtonRequest();
			b_line12 = new ButtonRequest();
			b_line12.setConnectionId(UCID);
			b_line12.setWidth((byte)20);
			b_line12.setHeight((byte)10);
			b_line12.setLeft((byte)120);
			b_line12.setTop((byte)(152));
			b_line12.setRequestInfo(wrapper.L12);
			b_line12.setButtonStyle(ButtonRequest.BUTTON_STYLE_BLUE + ButtonRequest.BUTTON_STYLE_CLICK + ButtonRequest.BUTTON_STYLE_LIGHT);
			b_line12.setText("Accept");
			b_line12.setClickId((byte)12);
			
			try
			{
				wrapper.client.send(welcome);
				wrapper.client.send(b_line1);
				wrapper.client.send(b_line2);
				wrapper.client.send(b_line3);
				wrapper.client.send(b_line4);
				wrapper.client.send(b_line5);
				wrapper.client.send(b_line6);
				wrapper.client.send(b_line7);
				wrapper.client.send(b_line8);
				wrapper.client.send(b_line9);
				wrapper.client.send(b_line10);
				wrapper.client.send(b_line11);
				wrapper.client.send(b_line12);
			} 
			catch (IOException e)
			{
				e.printStackTrace();
			}
			
			/*
			 * 	Refresh the connection list for everyone who has it open
			 */
			synchronized (wrapper.crossChat)
			{
				wrapper.crossChat.refreshConnectionList();
			}
		}		
	}
}
