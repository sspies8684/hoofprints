package net.decix.bgpstack.routes;

import java.net.InetAddress;

import net.decix.bgpstack.BGPRoute;
import net.decix.bgpstack.types.IPv4Prefix;
import net.decix.bgpstack.types.pathattributes.BGPPathAttribute;
import net.decix.bgpstack.types.pathattributes.BGPPathAttributeAsPath;
import net.decix.bgpstack.types.pathattributes.BGPPathAttributeNextHop;
import net.decix.bgpstack.types.pathattributes.BGPPathAttributeOrigin;
import net.decix.bgpstack.types.pathattributes.BGPPathAttributeSequence;

public class BGPAsPathRouteFactory implements BGPRouteFactory
{

	private BGPPathAttributeSequence sequence;
	private BGPPathAttributeOrigin origin;
	private BGPPathAttributeNextHop nextHop;
	private IPv4Prefix prefix;

	public BGPAsPathRouteFactory(int origin, InetAddress nextHop, IPv4Prefix prefix)
	{
		this.nextHop = new BGPPathAttributeNextHop(nextHop);
		this.origin = new BGPPathAttributeOrigin(origin);
		this.nextHop = new BGPPathAttributeNextHop(nextHop);
		this.prefix = prefix;
	}

	public BGPRoute createRoute(BGPPathAttributeAsPath asPath)
	{
		sequence = new BGPPathAttributeSequence();
		sequence.add(new BGPPathAttribute(this.origin));
		sequence.add(new BGPPathAttribute(this.nextHop));
		sequence.add(new BGPPathAttribute(asPath));
		return new BGPRoute(prefix, sequence);
	}
}
