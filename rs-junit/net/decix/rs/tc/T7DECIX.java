package net.decix.rs.tc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Inet6Address;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import net.decix.bgpstack.BGPConstants;
import net.decix.bgpstack.BGPEvent;
import net.decix.bgpstack.BGPEventHandler;
import net.decix.bgpstack.BGPPeerFSM;
import net.decix.bgpstack.BGPRoute;
import net.decix.bgpstack.BGPRoute6;
import net.decix.bgpstack.BGPSessionImpl;
import net.decix.bgpstack.types.IPv4Prefix;
import net.decix.bgpstack.types.IPv6Prefix;
import net.decix.bgpstack.types.NLRI;
import net.decix.bgpstack.util.AntProperties;
import net.decix.bgpstack.util.UtilityException;
import net.decix.rs.RSTestcaseException;
import net.decix.rs.conf.Configuration;
import net.decix.rs.conf.Neighbor;
import net.decix.rs.managers.RouteserverManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized.Parameters;

@RunWith(LabelledParameterized.class)
public class T7DECIX extends RSTestcase implements BGPEventHandler, BGPConstants
{
	private static final Logger logger = Logger.getLogger("net.decix.rs.tc.T7DECIX");

	protected static final String T7_CONF_DIRECTORY = "conf/test/7";

	protected RouteserverManager rsManager;

	// instance variables
	private Set<BGPPeerFSM> v4Peers = new HashSet<BGPPeerFSM>();
	private Set<BGPPeerFSM> v6Peers = new HashSet<BGPPeerFSM>();
	private Set<BGPPeerFSM> peers = new HashSet<BGPPeerFSM>();
	private List<IPv4Prefix> v4Prefixes = new LinkedList<IPv4Prefix>();
	private List<IPv6Prefix> v6Prefixes = new LinkedList<IPv6Prefix>();
	private Map<BGPPeerFSM, List<? extends NLRI>> prefixesPerPeer = new HashMap<BGPPeerFSM, List<? extends NLRI>>();

	// Properties
	protected Integer testRunTime;
	protected Integer numberOfPeers;
	protected Integer numberOfV4PrefixesPerPeer;
	protected Integer numberOfV6PrefixesPerPeer;
	protected Integer updateRate;
	protected Integer withdrawalRate;
	protected Integer tcpFailRate;
	protected Integer allowFilterHitMartiansOutRate;
	protected Integer denyFilterHitMartiansOutRate;
	protected Integer allowFilterHitPrefixesInRate;
	protected Integer denyFilterHitPrefixesInRate;
	protected Integer allowFilterHitSourceAsInRate;
	protected Integer denyFilterHitSourceAsInRate;
	protected Integer communityNoExportRate;
	protected Integer communityAllowAllRate;
	protected Integer communityDenyAllRate;
	protected Integer communityDenyPeerRate;
	protected Integer communityAllowPeerRate;
	protected Float fractionAttributeUnchanged;
	protected Float fractionRsClientPeers;
	// protected Float fractionNonRsClientPeers = 1 - fractionRsClientPeers;
	protected Integer allowedPrefixesPerPeer;
	protected Integer allowedSourceAsPerPeer;
	protected Float fractionMD5Peers;
	protected Float fractionRouteRefreshPeers;
	protected Float fractionAS4Peers;
	protected Float fractionV6Endpoints;
	// protected Float fractionV4Endpoints = 1 - fractionV6Endpoints;
	protected Float fractionV4MP;
	// protected Float fractionV4NonMP = 1 - fractionV4MP;
	protected Float fractionV4MPOverNLRI;
	// protected Float fractionV4MPOverMP = 1 - fractionV4MPOverNLRI;
	protected Integer averageAsPathLength;
	protected Integer asPathLengthEpsilon;
	protected Float asPathBiggerAs2Possibility;

	@Parameters
	public static Collection<Object[]> data() throws RSTestcaseException, FileNotFoundException, IOException
	{
		List<Object[]> setup = new LinkedList<Object[]>();
		File[] foundFiles = propertyFiles(T7_CONF_DIRECTORY);

		logger.fine("Test-Configuration files found: " + foundFiles.length);

		for (File f : foundFiles)
		{
			AntProperties properties = new AntProperties();
			properties.load(f);
			logger.fine("Properties loaded from: " + f);

			// load managers from config
			RouteserverManager[] managers = managersFromProperties(properties, "T7.daemons", smallStartupConfig());
			restartManagers(managers);

			for (RouteserverManager manager : managers)
				setup.add(new Object[] { properties.getFile().getName() + "," + manager.toString(), properties, manager });
		}

		return setup;
	}

	public T7DECIX(String testName, AntProperties testProperties, RouteserverManager rsManager) throws SecurityException, IllegalArgumentException, NoSuchFieldException, ClassNotFoundException, IllegalAccessException
	{
		this.rsManager = rsManager;

		Random rand = new Random();

		propertiesToFields(testProperties, this);

		v4Prefixes = generatePrefixes(numberOfV4PrefixesPerPeer * numberOfPeers, rand);
		v6Prefixes = generatePrefixes6(numberOfV6PrefixesPerPeer * numberOfPeers, rand);
	}

	@Before
	public void setup() throws UtilityException, IOException, RSTestcaseException
	{
		Configuration conf = smallStartupConfig();

		int numberOfV6Peers = (int) (numberOfPeers * fractionV6Endpoints);
		int numberOfV4Peers = numberOfPeers - numberOfV6Peers;

		for (int i = 0; i < numberOfV4Peers; i++)
		{
			Neighbor neighbor = new Neighbor();
			neighbor.setAddress(generateIP(i));
			neighbor.setAsn(10000 + i);
			neighbor.setDescription("IPv4 Peer " + i);
			neighbor.setPassive(true);

			BGPPeerFSM fsm = new BGPPeerFSM(neighbor.getAddress(), neighbor.getAsn(), conf.getAsn(), conf.getHoldTime(), neighbor.getAddress());
			fsm.setSession(new BGPSessionImpl(rsManager.getListenAddress(), rsManager.getListenPort(), neighbor.getAddress(), fsm));
			fsm.addObserver(this);

			v4Peers.add(fsm);
			conf.addNeighbor(neighbor);
		}

		for (int i = 0; i < numberOfV6Peers; i++)
		{
			Neighbor neighbor = new Neighbor();
			neighbor.setAddress(generateIP6(i));
			neighbor.setAsn(10000 + i);
			neighbor.setDescription("IPv6 Peer " + i);

			BGPPeerFSM fsm = new BGPPeerFSM(neighbor.getAddress(), neighbor.getAsn(), conf.getAsn(), conf.getHoldTime(), generateIP(i + numberOfV4Peers));
			fsm.setSession(new BGPSessionImpl(rsManager.getListen6Address(), rsManager.getListenPort(), neighbor.getAddress(), fsm));
			fsm.addObserver(this);

			v6Peers.add(fsm);
			conf.addNeighbor(neighbor);
		}

		peers.addAll(v4Peers);
		peers.addAll(v6Peers);

		configureTransparentPeers(conf);
		configureRSClient(conf);

		disableRRCap();
		disableAS4Cap();
		disableMultiprotocol();

		assignPrefixesToPeers();

		rsManager.loadConfiguration(conf);

	}

	@After
	public void tearDown() throws RSTestcaseException
	{
		cancelTimers();
		rsManager.stopRouteServer();

	}

	/*
	 * 
	 * 
	 * SETUP methods
	 */

	private void assignPrefixesToPeers()
	{
		int i = 0;
		for (BGPPeerFSM v4FSM : v4Peers)
		{
			prefixesPerPeer.put(v4FSM, v4Prefixes.subList(i * numberOfV4PrefixesPerPeer, (i + 1) * numberOfV4PrefixesPerPeer - 1));
			i++;
		}

		i = 0;
		for (BGPPeerFSM v6FSM : v6Peers)
		{
			prefixesPerPeer.put(v6FSM, v6Prefixes.subList(i * numberOfV6PrefixesPerPeer, (i + 1) * numberOfV6PrefixesPerPeer - 1));
			i++;
		}
	}

	private void configureTransparentPeers(Configuration conf)
	{
		// attribute unchanged
		int transparentPeers = (int) (fractionAttributeUnchanged * numberOfPeers);
		for (int i = 0; i < transparentPeers; i++)
		{
			Random rand = new Random();

			Neighbor transparentNeighbor;
			do
			{
				int transparentPeerIndex = rand.nextInt(peers.size());
				transparentNeighbor = conf.getNeighbors().get(transparentPeerIndex);
			}
			while (transparentNeighbor.isAttributeUnchanged());

			transparentNeighbor.setAttributeUnchanged(true);
		}
	}

	private void configureRSClient(Configuration conf)
	{
		int rsClientPeers = (int) (fractionRsClientPeers * numberOfPeers);
		for (int i = 0; i < rsClientPeers; i++)
		{
			Random rand = new Random();

			Neighbor rsNeighbor;
			do
			{
				int rsPeerIndex = rand.nextInt(peers.size());
				rsNeighbor = conf.getNeighbors().get(rsPeerIndex);
			}
			while (rsNeighbor.isRsClient());

			rsNeighbor.setRsClient(true);
		}
	}

	// disable (1 - fractionRouteRefreshPeers) * numberOfPeers rr cap
	private void disableRRCap()
	{
		int peersNoRR = (int) ((1 - fractionRouteRefreshPeers) * numberOfPeers);
		int i = 0;
		for (BGPPeerFSM peer : peers)
		{
			peer.setRouteRefreshEnabled(false);
			if (i++ >= peersNoRR)
				break;
		}
	}

	// disable (1 - fractionAS4Peers) * numberOfPeers AS4 cap
	private void disableAS4Cap()
	{

		int peersNoAS4 = (int) ((1 - fractionAS4Peers) * numberOfPeers);
		int i = 0;
		for (BGPPeerFSM peer : peers)
		{
			peer.setAS4Enabled(false);
			if (i++ >= peersNoAS4)
				return;
		}
	}

	// disable (1 - fractionV4MP) * numberOfV4Peers MP cap
	private void disableMultiprotocol()
	{
		int peersNoMP = (int) ((1 - fractionV4MP) * v4Peers.size());
		int i = 0;
		for (BGPPeerFSM peer : v4Peers)
		{
			peer.setMultiprotocolV4Enabled(true);
			peer.setMultiprotocolV6Enabled(false);
			if (i++ >= peersNoMP)
				break;
		}
	}

	/*
	 * 
	 * 
	 * TESTS
	 */

	@Test
	public void testRealWorld() throws InterruptedException
	{
		initTimers();

		startPeers();

		// Thread.sleep(1000000);
		synchronized (this)
		{
			wait();
		}

	}

	private void startPeers()
	{
		Iterator<BGPPeerFSM> peerIterator = peers.iterator();

		// every rand % 20 connect rand % 50 peers
		Random rand = new Random();

		while (peerIterator.hasNext())
		{
			peerIterator.next().receiveEvent(new BGPEvent(EVENT_TYPE.AutomaticStart));
			try
			{
				Thread.sleep(rand.nextInt(5000));
			}
			catch (InterruptedException e)
			{
				logger.throwing(T7DECIX.class.getCanonicalName(), "startPeers", e);
			}
		}
	}

	/*
	 * 
	 * TIMERS
	 */

	private Timer withdrawalTimer = new Timer();
	private Timer peersConnectedTimer = new Timer();

	private void initTimers()
	{
		// TODO consider delay
		withdrawalTimer.scheduleAtFixedRate(new WithdrawalTimerTask(), 5000, withdrawalRate * 1000);
		peersConnectedTimer.scheduleAtFixedRate(new CountConnectedPeers(), 5000, 10000);

	}

	private void cancelTimers()
	{
		withdrawalTimer.cancel();
		peersConnectedTimer.cancel();
	}

	protected class WithdrawalTimerTask extends TimerTask
	{
		protected Random rand = new Random();

		public WithdrawalTimerTask()
		{

		}

		@Override
		public void run()
		{

			// pick a peer
			BGPPeerFSM withdrawingPeer = null;

			do
			{
				int randPeer = rand.nextInt(peers.size());
				Iterator<BGPPeerFSM> peersIterator = peers.iterator();
				for (int i = 0; i <= randPeer; i++)
					if (peersIterator.hasNext())
						withdrawingPeer = peersIterator.next();
			}
			while (!withdrawingPeer.getCurrentState().getName().equals("Established"));

			RandomRoutesFactory routeFactory = new RandomRoutesFactory(BGP_PATH_ORIGIN_IGP, withdrawingPeer.getMyAsn(), withdrawingPeer, asPathBiggerAs2Possibility);

			// pick prefix
			List<? extends NLRI> prefixesOfPeer = prefixesPerPeer.get(withdrawingPeer);
			NLRI nlri = prefixesOfPeer.get(rand.nextInt(prefixesOfPeer.size()));
			
			logger.fine("withdraw " + nlri + " by " + withdrawingPeer.getSession().getMyAddress());

			withdrawingPeer.withdrawPrefixes(nlri);

//			withdrawingPeer.publishRoutes((nlri instanceof IPv6Prefix ? routeFactory.createRoute6(nlri, averageAsPathLength) : routeFactory.createRoute(nlri, averageAsPathLength)));

		}

	}

	protected class CountConnectedPeers extends TimerTask
	{
		@Override
		public void run()
		{
			int connectedPeers = 0;
			for (BGPPeerFSM p : peers)
				if (p.getCurrentState().getName().equals("Established"))
					connectedPeers++;
			logger.info("Connected peers: " + connectedPeers);

		}
	}

	public void receiveEvent(BGPEvent event, final BGPPeerFSM sender)
	{
		switch (event.getEventType())
		{
			case StateChange:
				if (event.getNewState().getName().equals("Established"))
				{
					BGPPeerFSM fsm = event.getOldState().getFsm();
					RandomRoutesFactory routeFactory = new RandomRoutesFactory(BGP_PATH_ORIGIN_IGP, fsm.getMyAsn(), fsm, asPathBiggerAs2Possibility);
					if (fsm.getSession().getMyAddress() instanceof Inet6Address)
					{
						BGPRoute6[] v6Routes = new BGPRoute6[prefixesPerPeer.get(fsm).size()];
						int i = 0;
						for (NLRI nlri : prefixesPerPeer.get(fsm))
							v6Routes[i++] = routeFactory.createRoute6(nlri, averageAsPathLength);
						logger.finer("sending " + v6Routes.length + " routes");
						fsm.publishRoutes(v6Routes);
					}
					else
					{
						BGPRoute[] v4Routes = new BGPRoute[prefixesPerPeer.get(fsm).size()];
						int i = 0;
						for (NLRI nlri : prefixesPerPeer.get(fsm))
							v4Routes[i++] = routeFactory.createRoute(nlri, averageAsPathLength);
						logger.finer("sending " + v4Routes.length + " routes");
						fsm.publishRoutes(v4Routes);
					}
				}
				else if (event.getNewState().getName().equals("Idle"))
				{
					new Thread(new Runnable()
					{
						public void run()
						{
							// wait 180 seconds and then reconnect
							try
							{
								logger.fine("Sleeping at " + sender.getSession() + " for reconnect");
								Thread.sleep(720000);
							}
							catch (InterruptedException e)
							{
							}
							sender.receiveEvent(new BGPEvent(EVENT_TYPE.AutomaticStart));
							logger.info(sender.getMyAsn() + " started");
						}
					}).start();
				}
				logger.finer("State Change of " + sender.getMyAsn() + ": " + event.getOldState().getName() + " -> " + event.getNewState().getName());
			break;
			case NotifMsg:
				logger.info("NOTIFICATION for: " + sender.getSession().getMyAddress());
			break;
			case HoldTimer_Expires:
				logger.fine(sender.getMyAsn() + " Holdtimer expired");
			break;
			case TcpConnectionFails:
				logger.fine(sender.getMyAsn() + " TCP failed");
			break;

		}
	}

}
