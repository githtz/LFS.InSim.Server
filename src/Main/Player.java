package Main;

import java.util.Date;

import Routines.DirectionControl;
import Routines.DirectionControl.Direction;

/**
 * Represents a Player
 * @author anrc
 *
 */
public class Player
{
	public byte PLID;
	public byte UCID;
	private double x_pos;
	private double y_pos;
	private short last_node;
	private long last_acive;
	private long last_dir;
	private DirectionControl.Direction direction[] = new DirectionControl.Direction[3];
	private int off;
	
	public Player()
	{
		direction[0] = Direction.CRUISE;
		direction[1] = Direction.CRUISE;
		direction[2] = Direction.CRUISE;
		this.last_dir = Long.MAX_VALUE;
	}
	
	public int lastActive(int xpos, int ypos)
	{
		xpos /= 65536;
		ypos /= 65536;
		long now = new Date().getTime();
		
		if (this.x_pos != xpos || this.y_pos != ypos)
		{
			last_acive = now;
			this.x_pos = xpos;
			this.y_pos = ypos;
			return 0;
		}
		else
		{
			return (int)((now - last_acive) / 1000.0);	
		}
	}
	
	public Direction getDrivingDirection(short node)
	{
		if(this.last_node > node)
		{
			direction[off] = Direction.REVERSE;
			off = (off+1)%direction.length;
			this.last_node = node;
		}
		else if (this.last_node < node)
		{
			direction[off] = Direction.NORMAL;
			off = (off+1)%direction.length;
			this.last_node = node;
		}
		/*
		else
		{
			direction[off] = Direction.CRUISE;
		}
		this.last_node = node;
		off = (off+1)%direction.length;
		*/
		
		if (direction[0] == direction[1] && direction[1] == direction[2])
			return direction[0];
		else
			return Direction.CRUISE;
	}
	
	public void refreshDirectionTime()
	{
		this.last_dir = new Date().getTime();
	}
	
	public int getDirectionTime()
	{
		long now = new Date().getTime();
		return (int)((now - this.last_dir)/1000.0);
	}
}
