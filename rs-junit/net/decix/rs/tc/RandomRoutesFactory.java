package net.decix.rs.tc;

import java.net.Inet6Address;
import java.util.Random;

import net.decix.bgpstack.BGPPeerFSM;
import net.decix.bgpstack.BGPRoute;
import net.decix.bgpstack.BGPRoute6;
import net.decix.bgpstack.routes.BGPRouteFactory;
import net.decix.bgpstack.types.IPv4Prefix;
import net.decix.bgpstack.types.IPv6Prefix;
import net.decix.bgpstack.types.NLRI;
import net.decix.bgpstack.types.pathattributes.BGPPathAttribute;
import net.decix.bgpstack.types.pathattributes.BGPPathAttributeAsPath;
import net.decix.bgpstack.types.pathattributes.BGPPathAttributeNextHop;
import net.decix.bgpstack.types.pathattributes.BGPPathAttributeOrigin;
import net.decix.bgpstack.types.pathattributes.BGPPathAttributeSequence;

public class RandomRoutesFactory implements BGPRouteFactory
{

	private BGPPathAttributeSequence sequence = new BGPPathAttributeSequence();
	private BGPPathAttributeOrigin origin;
	
	private float as4Possibility;

	private Random rand = new Random();

	private long prependAs;
	private boolean multiprotocol;
	
	private BGPPeerFSM fsm;

	public RandomRoutesFactory(int origin, long prependAs, BGPPeerFSM fsm, float as4Possibility)
	{
		this.prependAs = prependAs;
		this.multiprotocol = fsm.getSession().getMyAddress() instanceof Inet6Address;
		this.fsm = fsm;
		this.origin = new BGPPathAttributeOrigin(origin);
		this.as4Possibility = as4Possibility;
		sequence.add(new BGPPathAttribute(this.origin));
		
	}

	public BGPRoute createRoute(NLRI prefix, int asPathLength)
	{
		if (prefix instanceof IPv4Prefix)
		{
			BGPPathAttributeSequence deltaSequence = new BGPPathAttributeSequence();
			deltaSequence.addAll(this.sequence);
			BGPPathAttributeAsPath asPath = new BGPPathAttributeAsPath();
			asPath.setFourByteEncoding(fsm);
			asPath.addSequence(asPath(asPathLength, fsm));
			BGPPathAttributeNextHop nextHop = new BGPPathAttributeNextHop(fsm.getSession().getMyAddress());
			deltaSequence.add(new BGPPathAttribute(asPath));
			deltaSequence.add(new BGPPathAttribute(nextHop));
			return new BGPRoute(prefix, deltaSequence);
		}

		return null;
	}
	
	public BGPRoute6 createRoute6(NLRI prefix, int asPathLength)
	{
		if(multiprotocol && prefix instanceof IPv6Prefix)
		{
			BGPPathAttributeSequence deltaSequence = new BGPPathAttributeSequence();
			deltaSequence.addAll(this.sequence);
			BGPPathAttributeAsPath asPath = new BGPPathAttributeAsPath();
			asPath.setFourByteEncoding(fsm);
			asPath.addSequence(asPath(asPathLength, fsm));
			deltaSequence.add(new BGPPathAttribute(asPath));
			return new BGPRoute6((IPv6Prefix) prefix, deltaSequence, fsm.getSession().getMyAddress());
			
		}
		return null;
	}
	
	private long[] asPath(int asPathLength, BGPPeerFSM fsm)
	{
		long[] asPathL = new long[asPathLength];
		asPathL[0] = prependAs;
		for (int i = 1; i < asPathLength; i++)
		{
			asPathL[i] = rand.nextLong();
			if(asPathL[i] < 0)
				asPathL[i] *= -1L;
			
			boolean isAs4 = rand.nextInt(101) <= (as4Possibility * 100);

			if (!fsm.fourByteAsnCapabilitySentAndReceived() || !isAs4)
				asPathL[i] %= BGP_AS2_MAX;
		}
		return asPathL;
	}

}
