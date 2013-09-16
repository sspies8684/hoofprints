package net.decix.bgpstack;

import java.net.InetAddress;

import net.decix.bgpstack.types.IPv6Prefix;
import net.decix.bgpstack.types.pathattributes.BGPPathAttributeSequence;

public class BGPRoute6 extends BGPRoute
{
	private InetAddress nextHop;

	public BGPRoute6(IPv6Prefix prefix, BGPPathAttributeSequence sequence, InetAddress nextHop)
	{
		super(prefix, sequence);
		this.nextHop = nextHop;
	}

	/**
	 * @return the nextHop
	 */
	public InetAddress getNextHop()
	{
		return nextHop;
	}

	/**
	 * @param nextHop the nextHop to set
	 */
	protected void setNextHop(InetAddress nextHop)
	{
		this.nextHop = nextHop;
	}
	
	
}
