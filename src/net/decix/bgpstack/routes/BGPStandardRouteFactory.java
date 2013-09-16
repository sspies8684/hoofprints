package net.decix.bgpstack.routes;

import java.net.InetAddress;

import net.decix.bgpstack.BGPRoute;
import net.decix.bgpstack.types.IPv4Prefix;
import net.decix.bgpstack.types.pathattributes.BGPPathAttribute;
import net.decix.bgpstack.types.pathattributes.BGPPathAttributeAsPath;
import net.decix.bgpstack.types.pathattributes.BGPPathAttributeNextHop;
import net.decix.bgpstack.types.pathattributes.BGPPathAttributeOrigin;
import net.decix.bgpstack.types.pathattributes.BGPPathAttributeSequence;

public class BGPStandardRouteFactory implements BGPRouteFactory
{

	private BGPPathAttributeSequence sequence = new BGPPathAttributeSequence();
	private BGPPathAttributeOrigin origin;
	private BGPPathAttributeNextHop nextHop;
	private BGPPathAttributeAsPath asPath;

	public BGPStandardRouteFactory(int origin, InetAddress nextHop, BGPPathAttributeAsPath asPath)
	{
		this.nextHop = new BGPPathAttributeNextHop(nextHop);
		this.asPath = asPath;
		this.origin = new BGPPathAttributeOrigin(origin);
		this.nextHop = new BGPPathAttributeNextHop(nextHop);
		sequence.add(new BGPPathAttribute(this.origin));
		sequence.add(new BGPPathAttribute(asPath));
		sequence.add(new BGPPathAttribute(this.nextHop));
	}

	public BGPRoute createRoute(Object parameter)
	{
		if (parameter instanceof IPv4Prefix)
			return new BGPRoute((IPv4Prefix) parameter, sequence);
		else
			return null;

	}
}
