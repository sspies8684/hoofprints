package net.decix.rs.tc;

import static org.junit.Assert.assertEquals;

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
import net.decix.bgpstack.BGPEventHandler;
import net.decix.bgpstack.BGPPeerFSM;
import net.decix.bgpstack.BGPRoute;
import net.decix.bgpstack.BGPSession;
import net.decix.bgpstack.BGPSessionImpl;
import net.decix.bgpstack.routes.BGPAsPathRouteFactory;
import net.decix.bgpstack.routes.BGPStandardRouteFactory;
import net.decix.bgpstack.types.BGPUpdateMessage;
import net.decix.bgpstack.types.IPv4Prefix;
import net.decix.bgpstack.types.pathattributes.BGPPathAttributeAsPath;
import net.decix.rs.RSTestcaseException;
import net.decix.rs.conf.ASPathFilter;
import net.decix.rs.conf.Configuration;
import net.decix.rs.conf.Neighbor;
import net.decix.rs.conf.PrefixFilter;
import net.decix.rs.managers.RouteserverManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(value = Parameterized.class)
public class T5Filters extends RSTestcase implements BGPConstants
{
	@Parameters
	public static Collection<Object[]> data() throws FileNotFoundException, IOException, RSTestcaseException
	{
		Collection<Object[]> testSetup = new LinkedList<Object[]>();

		Configuration conf = new Configuration();
		conf.setAsn(1);
		conf.setHoldTime(90);

		List<PrefixFilter> martians = new LinkedList<PrefixFilter>();
		martians.add(new PrefixFilter(new IPv4Prefix(InetAddress.getByName("0.0.0.0"), 0), -1, -1, false));
		martians.add(new PrefixFilter(new IPv4Prefix(InetAddress.getByName("0.0.0.0"), 8), 32, -1, false));
		martians.add(new PrefixFilter(new IPv4Prefix(InetAddress.getByName("10.0.0.0"), 8), 32, -1, false));
		martians.add(new PrefixFilter(new IPv4Prefix(InetAddress.getByName("172.16.0.0"), 12), 32, -1, false));
		martians.add(new PrefixFilter(new IPv4Prefix(InetAddress.getByName("192.168.0.0"), 16), 32, -1, false));
		martians.add(new PrefixFilter(new IPv4Prefix(InetAddress.getByName("14.0.0.0"), 8), 32, -1, false));
		martians.add(new PrefixFilter(new IPv4Prefix(InetAddress.getByName("24.0.0.0"), 8), 32, -1, false));
		martians.add(new PrefixFilter(new IPv4Prefix(InetAddress.getByName("39.0.0.0"), 8), 32, -1, false));
		martians.add(new PrefixFilter(new IPv4Prefix(InetAddress.getByName("127.0.0.0"), 8), 32, -1, false));
		martians.add(new PrefixFilter(new IPv4Prefix(InetAddress.getByName("128.0.0.0"), 8), 32, -1, false));
		martians.add(new PrefixFilter(new IPv4Prefix(InetAddress.getByName("169.254.0.0"), 16), 32, -1, false));
		martians.add(new PrefixFilter(new IPv4Prefix(InetAddress.getByName("191.255.0.0"), 16), 32, -1, false));
		martians.add(new PrefixFilter(new IPv4Prefix(InetAddress.getByName("192.0.0.0"), 24), 32, -1, false));
		martians.add(new PrefixFilter(new IPv4Prefix(InetAddress.getByName("192.0.2.0"), 24), 32, -1, false));
		martians.add(new PrefixFilter(new IPv4Prefix(InetAddress.getByName("198.18.0.0"), 15), 32, -1, false));
		martians.add(new PrefixFilter(new IPv4Prefix(InetAddress.getByName("223.255.255.0"), 24), 32, -1, false));
		martians.add(new PrefixFilter(new IPv4Prefix(InetAddress.getByName("224.0.0.0"), 4), 32, -1, false));
		martians.add(new PrefixFilter(new IPv4Prefix(InetAddress.getByName("240.0.0.0"), 4), 32, -1, false));
		martians.add(new PrefixFilter(new IPv4Prefix(InetAddress.getByName("0.0.0.0"), 0), 32, -1, true));
		conf.addGlobalFilters(martians);

		// RouteserverManager quagga = initQuagga(conf);
		// testSetup.add(new Object[] { quagga, conf });

		RouteserverManager openbgpd = initRemote(conf, new File("conf/openbgpd_script.properties"));
		testSetup.add(new Object[] { openbgpd, conf });

		return testSetup;
	}

	PollGraphs grapher;

	private RouteserverManager rsManager;
	private Configuration configuration;
	private BGPPeerFSM prefixSenderFsm1;
	private BGPPeerFSM prefixSenderFsm2;
	private BGPPeerFSM prefixReceiverFsm;

	public T5Filters(RouteserverManager rsManager, Configuration configuration) throws Exception
	{
		this.rsManager = rsManager;
		this.configuration = (Configuration) configuration.clone();
		// init two peers, one for sending prefixes, the other one to receive
		// prefixes

		configure();
	}

	private void configure() throws Exception
	{
		// prefix sender2
		Neighbor prefixSender = new Neighbor();
		prefixSender.setAsn(10000);
		prefixSender.setAddress(generateIP(0));
		prefixSender.setDescription("Prefix Sender 1");
		prefixSender.setPassive(true);
		prefixSender.setAttributeUnchanged(true);
		prefixSender.addASPathFilter(new ASPathFilter(2, true));
		prefixSender.addOutFilter(new PrefixFilter(new IPv4Prefix(InetAddress.getByName("0.0.0.0"), 0), 32, -1, false));
		configuration.addNeighbor(prefixSender);

		prefixSenderFsm1 = new BGPPeerFSM(prefixSender.getAddress(), prefixSender.getAsn(), configuration.getAsn(), configuration.getHoldTime(), (Inet4Address) prefixSender.getAddress());
		BGPSession prefixSenderSession = new BGPSessionImpl(rsManager.getListenAddress(), rsManager.getListenPort(), prefixSender.getAddress(), prefixSenderFsm1);
		prefixSenderFsm1.setSession(prefixSenderSession);
		prefixSenderFsm1.addObserver(new PrefixSenderEventHandler());

		Neighbor prefixSender2 = new Neighbor();
		prefixSender2.setAsn(10001);
		prefixSender2.setAddress(generateIP(1));
		prefixSender2.setDescription("Prefix Sender 2");
		prefixSender2.setPassive(true);
		prefixSender2.setAttributeUnchanged(true);
		prefixSender2.addASPathFilter(new ASPathFilter(2, true));
		prefixSender2.addOutFilter(new PrefixFilter(new IPv4Prefix(InetAddress.getByName("0.0.0.0"), 0), 32, -1, false));
		configuration.addNeighbor(prefixSender2);

		prefixSenderFsm2 = new BGPPeerFSM(prefixSender2.getAddress(), prefixSender2.getAsn(), configuration.getAsn(), configuration.getHoldTime(), (Inet4Address) prefixSender2.getAddress());
		BGPSession prefixSenderSession2 = new BGPSessionImpl(rsManager.getListenAddress(), rsManager.getListenPort(), prefixSender2.getAddress(), prefixSenderFsm2);
		prefixSenderFsm2.setSession(prefixSenderSession2);
		prefixSenderFsm2.addObserver(new PrefixSenderEventHandler());

		// prefix receiver
		Neighbor prefixReceiver = new Neighbor();
		prefixReceiver.setAsn(20000);
		prefixReceiver.setAddress(generateIP(1000));
		prefixReceiver.setDescription("Prefix Receiver");
		prefixReceiver.setPassive(true);
		prefixReceiver.setAttributeUnchanged(true);
		configuration.addNeighbor(prefixReceiver);

		prefixReceiverFsm = new BGPPeerFSM(prefixReceiver.getAddress(), prefixReceiver.getAsn(), configuration.getAsn(), configuration.getHoldTime(), (Inet4Address) prefixReceiver.getAddress());
		BGPSession prefixReceiverSession = new BGPSessionImpl(rsManager.getListenAddress(), rsManager.getListenPort(), prefixReceiver.getAddress(), prefixReceiverFsm);
		prefixReceiverFsm.setSession(prefixReceiverSession);
		prefixReceiverFsm.addObserver(new PrefixReceiverEventHandler());
	}

	@Before
	public void setup() throws Exception
	{
		if (rsManager.isRunning())
			rsManager.stopRouteServer();
		rsManager.startRouteserver();

		rsManager.loadConfiguration(configuration);

		logger.info("PHASE1: " + rsManager + " armed and ready");

		prefixSenderFsm1.receiveEvent(new BGPEvent(EVENT_TYPE.AutomaticStart));
		prefixSenderFsm2.receiveEvent(new BGPEvent(EVENT_TYPE.AutomaticStart));
		prefixReceiverFsm.receiveEvent(new BGPEvent(EVENT_TYPE.AutomaticStart));

		grapher = new PollGraphs(rsManager, "T5");
		grapher.start();

		waitForState(prefixSenderFsm1.getSession(), "Established");
		logger.info("PHASE2: prefix sender 1 entered state ESTABLISHED");

		waitForState(prefixSenderFsm2.getSession(), "Established");
		logger.info("PHASE2: prefix sender 2 entered state ESTABLISHED");

		waitForState(prefixReceiverFsm.getSession(), "Established");
		logger.info("PHASE3: prefix receiver entered state ESTABLISHED");
	}

	@Test
	public void testMartians() throws Exception
	{
		logger.info("testMartians: starting test");

		// sending prefixes
		IPv4Prefix nonFilteredPrefix = new IPv4Prefix(InetAddress.getByName("1.2.3.0"), 24);
		IPv4Prefix filteredPrefix = new IPv4Prefix(InetAddress.getByName("192.168.19.0"), 29);
		BGPPathAttributeAsPath asPath = new BGPPathAttributeAsPath();
		asPath.addSequence(prefixSenderFsm1.getMyAsn(), 4, 5, 6, 7, 2);
		BGPStandardRouteFactory routeFactory = new BGPStandardRouteFactory(BGP_PATH_ORIGIN_IGP, prefixSenderFsm1.getSession().getMyAddress(), asPath);
		BGPRoute nonFilteredRoute = routeFactory.createRoute(nonFilteredPrefix);
		BGPRoute filteredRoute = routeFactory.createRoute(filteredPrefix);
		prefixSenderFsm1.publishRoutes(nonFilteredRoute, filteredRoute);

		Thread.sleep(5000);

		assertEquals(1, prefixReceiverFsm.getLearnedRoutes().size());
		assertEquals(nonFilteredPrefix, prefixReceiverFsm.getLearnedRoutes().get(0).getPrefix());

		testForFailure();

		logger.info("testMartians: martian filters working as expected");
	}

	@Test
	public void testAsPathFilter() throws Exception
	{
		logger.info("testAsPathFilter: starting test");

		IPv4Prefix asPathTestPrefix = new IPv4Prefix(InetAddress.getByName("9.2.0.0"), 16);
		BGPAsPathRouteFactory asRouteFactory = new BGPAsPathRouteFactory(BGP_PATH_ORIGIN_IGP, prefixSenderFsm1.getSession().getMyAddress(), asPathTestPrefix);

		BGPPathAttributeAsPath filteredAsPath = new BGPPathAttributeAsPath();
		filteredAsPath.addSequence(prefixSenderFsm2.getMyAsn(), 10, 9, 8);
		BGPRoute filteredAsPathRoute = asRouteFactory.createRoute(filteredAsPath);

		BGPPathAttributeAsPath nonFilteredAsPath = new BGPPathAttributeAsPath();
		nonFilteredAsPath.addSequence(prefixSenderFsm1.getMyAsn(), 5, 4, 3, 2, 5, 2);
		BGPRoute nonFilteredAsPathRoute = asRouteFactory.createRoute(nonFilteredAsPath);

		prefixSenderFsm1.publishRoutes(nonFilteredAsPathRoute);
		prefixSenderFsm2.publishRoutes(filteredAsPathRoute);

		Thread.sleep(5000);

		assertEquals(1, prefixReceiverFsm.getLearnedRoutes().size());
		BGPPathAttributeAsPath checkAsPath = (BGPPathAttributeAsPath) prefixReceiverFsm.getLearnedRoutes().get(0).getSequence().getByTypeCode(BGP_PATH_ATTRIBUTE_AS_PATH).getContent();
		assertEquals(nonFilteredAsPath, checkAsPath);

		testForFailure();

		logger.info("testAsPathFilter: AS_PATH filters working as expected");

	}

	@After
	public void tearDown() throws Exception
	{
		grapher.stopPolling();
		prefixSenderFsm1.receiveEvent(new BGPEvent(EVENT_TYPE.AutomaticStop));
		prefixReceiverFsm.receiveEvent(new BGPEvent(EVENT_TYPE.AutomaticStop));
		rsManager.stopRouteServer();
	}

	protected class PrefixSenderEventHandler implements BGPEventHandler
	{
		public void receiveEvent(BGPEvent event, BGPPeerFSM sender)
		{
			switch (event.getEventType())
			{
				case UpdateMsg:
					BGPUpdateMessage updateMessage = (BGPUpdateMessage) event.getMessage();
					if (updateMessage.getNlriLength() > 0 || updateMessage.getWithdrawnRoutesLength() > 0)
						fail("prefix sender received a prefix");
			}
		}
	}

	protected class PrefixReceiverEventHandler implements BGPEventHandler
	{

		public void receiveEvent(BGPEvent event, BGPPeerFSM sender)
		{

		}

	}
}
