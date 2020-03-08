package configuration;

/**
 * @version 1.0
 * @author anrc
 * This class is only used for extended classes.
 * Use to store Config information.
 */
public abstract class Config
{
	public String ip;
	public String password;
	public short[] ports;
	public String[] hostnames;

	
	public String memberlist;
	public String swearwords;
}
