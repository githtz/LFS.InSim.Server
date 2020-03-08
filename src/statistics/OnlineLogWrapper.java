package statistics;

import java.io.File;
import java.io.IOException;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;


public class OnlineLogWrapper
{
	//private static final String savepath = "online_log.xml";
	private static final String savepath = "C:\\Users\\anrc\\Desktop\\online_log.xml";
	private OnlineLog log;
	private static OnlineLogWrapper wrapper = null;
	public static OnlineLogWrapper getWrapper()
	{
		if (OnlineLogWrapper.wrapper == null)
		{
			OnlineLogWrapper.wrapper = new OnlineLogWrapper();
		}
		return wrapper;
	}
	
	private OnlineLogWrapper()
	{
		this.log = load();
	}
	
	private OnlineLog load()
	{
		OnlineLog log = new OnlineLog();
		try
		{

			File f = new File(savepath);
			Serializer serializer = new Persister();
			log = serializer.read(OnlineLog.class, f);
		} 
		catch(Exception e)
		{
			/*
			try
			{
				istr = new FileInputStream(savepath);
				XMLDecoder dec = new XMLDecoder(istr);
				Object read = dec.readObject();
				dec.close();
				istr.close();
				log.players = (ArrayList<PlayerAccount>)read;
			}
			catch(IOException f)
			{
				
			}
			catch(RuntimeException f)
			{
				
			}
			*/
		}
		return log;
	}
	
	private void save(OnlineLog save)
	{
		try
		{
			synchronized (save)
			{
				File f = new File(savepath);
				if (!f.exists())
					f.createNewFile();
				Serializer serializer = new Persister();
				serializer.write(save, f);
			}
			//ostr = new FileOutputStream(f);
			//XMLEncoder enc = new XMLEncoder(ostr);
			//enc.writeObject(save.players);
			//enc.close();
			//ostr.close();
		}
		catch (IOException e)
		{
			
		}
		catch (RuntimeException e) 
		{
		}
		catch (Exception e)
		{
		
		}
	}
	
	public void connect(String accountName, String nickName)
	{
		this.log.connect(accountName, nickName);
		save(this.log);
	}
	
	public void disconnect(String accountName)
	{
		this.log.disconnect(accountName);
		save(this.log);
	}
	
	public void rename(String accountName, String nickName)
	{
		
		save(this.log);
	}
	
	public void joinRace(String accountName)
	{
		this.log.joinRace(accountName);
		save(this.log);
	}
	
	public void leaveRace(String accountName)
	{
		this.log.leaveRace(accountName);
		save(this.log);
	}
}
