package net.decix.rs.tc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import net.decix.bgpstack.BGPConstants;
import net.decix.bgpstack.BGPEvent;
import net.decix.bgpstack.BGPPeerFSM;
import net.decix.bgpstack.BGPSession;
import net.decix.bgpstack.BGPSessionImpl;
import net.decix.bgpstack.types.BGPOpenMessageParameter;
import net.decix.bgpstack.types.BGPPacket;
import net.decix.bgpstack.types.BGPRouteRefreshMessage;
import net.decix.bgpstack.types.BGPUpdateMessage;
import net.decix.bgpstack.types.IPv4Prefix;
import net.decix.bgpstack.types.IPv6Prefix;
import net.decix.bgpstack.types.pathattributes.BGPPathAttribute;
import net.decix.bgpstack.types.pathattributes.BGPPathAttributeAsPath;
import net.decix.bgpstack.types.pathattributes.BGPPathAttributeMPReachNLRI;
import net.decix.bgpstack.types.pathattributes.BGPPathAttributeNextHop;
import net.decix.bgpstack.types.pathattributes.BGPPathAttributeOrigin;
import net.decix.bgpstack.types.pathattributes.BGPPathAttributeSequence;
import net.decix.rs.RSTestcaseException;
import net.decix.rs.conf.Configuration;
import net.decix.rs.conf.Neighbor;
import net.decix.rs.managers.RouteserverManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized.Parameters;

@RunWith(value = LabelledParameterized.class)
public class T6Features extends RSTestcase implements BGPConstants
{
	@Parameters
	public static Collection<Object[]> data() throws FileNotFoundException, IOException, RSTestcaseException
	{
		Collection<Object[]> testSetup = new LinkedList<Object[]>();

		Configuration conf = new Configuration();
		conf.setAsn(1);
		conf.setHoldTime(90);

		RouteserverManager quagga = initQuagga(conf);
		RouteserverManager openbgpd = initRemote(conf, new File("conf/openbgpd_script.properties"));
		
		testSetup.add(new Object[] { "quagga", quagga, conf });
		testSetup.add(new Object[] { "openbgpd", openbgpd, conf });

		return testSetup;
	}

	PollGraphs grapher;

	private RouteserverManager rsManager;
	private Configuration configuration;
	private BGPPeerFSM v6FSM;


	public T6Features(String parameterTitle, RouteserverManager rsManager, Configuration configuration) throws Exception
	{
		this.rsManager = rsManager;
		this.configuration = (Configuration) configuration.clone();
		configure();
	}

	private void configure() throws Exception
	{
		Neighbor v6Neighbor = new Neighbor();
		v6Neighbor.setAsn(1000000L);
		v6Neighbor.setAddress(generateIP6(1));
		v6Neighbor.setDescription("Test IPv6 Neighbor");
		v6Neighbor.setPassive(true);
		configuration.addNeighbor(v6Neighbor);

		v6FSM = new BGPPeerFSM(v6Neighbor.getAddress(), v6Neighbor.getAsn(), configuration.getAsn(), configuration.getHoldTime(), (Inet4Address) InetAddress.getByName("1.2.3.4"));
		BGPSession v6Session = new BGPSessionImpl(rsManager.getListen6Address(), rsManager.getListenPort(), v6Neighbor.getAddress(), v6FSM);
		v6FSM.setSession(v6Session);
	}

	@Before
	public void setup() throws Exception
	{
		if (rsManager.isRunning())
			rsManager.stopRouteServer();
		rsManager.startRouteserver();

		rsManager.loadConfiguration(configuration);

		logger.info("PHASE1: " + rsManager + " armed and ready");

		v6FSM.receiveEvent(new BGPEvent(EVENT_TYPE.AutomaticStart));

		grapher = new PollGraphs(rsManager, "T6");
		grapher.start();

		waitForState(v6FSM.getSession(), "Established");
		logger.info("PHASE2: v6FSM entered state ESTABLISHED");
	}

	@Test
	public void testConnection() throws Exception
	{
		logger.info("testConnection: starting test");

		BGPPathAttributeSequence sequence = new BGPPathAttributeSequence();
		BGPPathAttributeAsPath asPath = new BGPPathAttributeAsPath();

		for(BGPOpenMessageParameter param : v6FSM.getReceivedParameters())
			if(param.getValue().getTypeCode() == BGP_CAPABILITY_4_BYTE_ASN)
			{
				asPath.setFourByteEncoding(true);
				break;
			}
		
		asPath.addSequence(v6FSM.getMyAsn(),4,5,3,2);
		sequence.add(new BGPPathAttribute(asPath));
		sequence.add(new BGPPathAttribute(new BGPPathAttributeOrigin(BGP_PATH_ORIGIN_IGP)));
		sequence.add(new BGPPathAttribute(new BGPPathAttributeNextHop(InetAddress.getByName("10.15.2.10"))));
		BGPPathAttributeMPReachNLRI mpReach = new BGPPathAttributeMPReachNLRI();
		
		mpReach.addNextHop(v6FSM.getSession().getMyAddress());
		mpReach.addReachableNLRI(new IPv6Prefix(InetAddress.getByName("3ffe:c00:400::"), 48));
//		mpReach.addNextHop(InetAddress.getByName("10.15.2.10"));
//		mpReach.addReachableNLRI();
		sequence.add(new BGPPathAttribute(mpReach));
		BGPPacket packet = new BGPPacket();
		List<IPv4Prefix> prefixes = new LinkedList<IPv4Prefix>();
		prefixes.add(new IPv4Prefix(InetAddress.getByName("192.168.0.0"), 16));
		packet.addMessage(new BGPUpdateMessage(null, prefixes, sequence));
		v6FSM.sendPacket(packet);
		
		Thread.sleep(5000);
		
		packet = new BGPPacket();
		packet.addMessage(new BGPRouteRefreshMessage(AFI_INET, SAFI_UNICAST));
		packet.addMessage(new BGPRouteRefreshMessage(AFI_INET6, SAFI_UNICAST));
		v6FSM.sendPacket(packet);
		
		Thread.sleep(10000);
		testForFailure();

		logger.info("testConnection: test succeeded");
	}

	@After
	public void tearDown() throws Exception
	{
		grapher.stopPolling();
		v6FSM.receiveEvent(new BGPEvent(EVENT_TYPE.AutomaticStop));
		rsManager.stopRouteServer();
	}

}
