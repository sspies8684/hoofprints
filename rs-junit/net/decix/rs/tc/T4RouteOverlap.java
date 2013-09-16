package net.decix.rs.tc;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import net.decix.bgpstack.BGPConstants;
import net.decix.bgpstack.BGPEvent;
import net.decix.bgpstack.BGPEventHandler;
import net.decix.bgpstack.BGPPeerFSM;
import net.decix.bgpstack.BGPRoute;
import net.decix.bgpstack.BGPSession;
import net.decix.bgpstack.routes.BGPStandardRouteFactory;
import net.decix.bgpstack.states.BGPEstablishedState;
import net.decix.bgpstack.states.BGPState;
import net.decix.bgpstack.types.IPv4Prefix;
import net.decix.bgpstack.types.pathattributes.BGPPathAttributeAsPath;
import net.decix.bgpstack.util.TimeoutThread;
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
public class T4RouteOverlap extends RSTestcase implements BGPConstants, BGPEventHandler
{
	private static Configuration conf;
	private static PollCPUTimeout pollCPUThread;
	private static List<IPv4Prefix> prefixes = null;

	@Parameters
	public static Collection<Object[]> data() throws FileNotFoundException, IOException, RSTestcaseException
	{
		conf = new Configuration();
		conf.setAsn(1);
		conf.setHoldTime(90);

		RouteserverManager quagga = initQuagga(conf);
		// RouteserverManager bird = initBIRD(conf);
		// RouteserverManager openbgpd = initRemoteOpenBGPD(conf, new
		// File("conf/openbgpd_script.properties"));
		int peers = 500;
		int prefixesBase = 150;
		int prefixesNum = peers * prefixesBase;

		prefixes = generatePrefixes(prefixesBase * peers, new Random());

		Collection<Object[]> testSetup = new LinkedList<Object[]>();
		
		for (int i = 0; i <= 10; i++)
			for (int j = 0; j <= i; j++)
				testSetup.add(new Object[] { "quagga,p:" + peers + ",base:" + prefixesBase + ",pfx1:" + prefixesNum + ",pfx2:" + prefixesNum*i/10 + ",pfx3:" + prefixesNum*j/10, quagga, peers, prefixesBase, new int[] { prefixesNum, prefixesNum * i / 10, prefixesNum * j / 10 } });

		return testSetup;

	}

	private int peers;
	private int prefixesBase;
	private RouteserverManager rsManager;
	private int cpuTimeout = 10000;
	private TimeoutThread timeoutThread = new TimeoutThread(cpuTimeout);
	private List<BGPSession> sessions = new LinkedList<BGPSession>();
	private int[] overlapParameters;
	private PollGraphs grapher;

	public T4RouteOverlap(String parameterTitle, RouteserverManager rsManager, int peers, int prefixesBase, int[] overlapParameters)
	{
		this.rsManager = rsManager;
		this.peers = peers;
		this.prefixesBase = prefixesBase;
		this.overlapParameters = overlapParameters;
	}

	@Before
	public void setup() throws Exception
	{

		if (rsManager.isRunning())
			rsManager.stopRouteServer();
		rsManager.startRouteserver();

		grapher = new PollGraphs(rsManager, "T5");
		grapher.start();

		pollCPUThread = new PollCPUTimeout(rsManager, timeoutThread);

		conf = new Configuration();
		conf.setAsn(1);
		conf.setHoldTime(90);

		for (int i = 0; i < peers; i++)
		{
			Neighbor n = new Neighbor();
			n.setAsn(10000 + i);
			n.setAddress(generateIP(i));
			n.setDescription("Peer " + i);
			n.setPassive(true);
			conf.addNeighbor(n);
		}

		rsManager.loadConfiguration(conf);
		logger.info("PHASE1: " + rsManager + " armed and ready");
	}

	@Test
	public void testOverlap() throws Exception
	{
		sessions = setupSessions(conf, rsManager, this);
		startSessions(sessions);
		waitForState(sessions, "Established");
		logger.info("PHASE3: all peers are state ESTABLISHED");
		long before = System.currentTimeMillis();
		SessionOverlapMultiplexer mux = mapPrefixesToSessions(overlapParameters);
		logger.info(mux.toString());
		pollCPUThread.start();
		timeoutThread.start();
		before = System.currentTimeMillis();
		sendRoutes(mux);
		timeoutThread.join();
		long after = System.currentTimeMillis();
		testForFailure();
		logger.info("passed all health checks");
		logger.info("convergence time: " + (after - before) + "ms (+~10s)");
		logger.info("PHASE4: finished");
	}

	protected SessionOverlapMultiplexer mapPrefixesToSessions(int[] parameters)
	{
		SessionOverlapMultiplexer mux = new SessionOverlapMultiplexer();
		mux.addSessions(sessions);
		for (int i = 0; i < parameters.length; i++)
		{
			int numberOfPrefixes = parameters[i];
			int sessionOffset = 0;
			for (int j = 0; j < numberOfPrefixes; j++)
			{
				IPv4Prefix prefix = prefixes.get(j);
				BGPSession session;

				do
				{
					session = sessions.get(sessionOffset++);
					sessionOffset %= sessions.size();
				}
				while (mux.containsPrefix(session, prefix));

				mux.addPrefixToSession(session, prefix);
			}
		}
		return mux;
	}

	protected void sendRoutes(final SessionOverlapMultiplexer mux)
	{
		for (int i = 0; i < sessions.size(); i++)
		{
			final BGPSession session = sessions.get(i);
			BGPPathAttributeAsPath asPath = new BGPPathAttributeAsPath();
			final BGPPeerFSM fsm = session.getFsm();
			asPath.addSequence(fsm.getMyAsn());

			final BGPStandardRouteFactory routeFactory = new BGPStandardRouteFactory(BGP_PATH_ORIGIN_IGP, session.getMyAddress(), asPath);
			new Thread(new Runnable()
			{
				public void run()
				{
					BGPRoute[] routes = new BGPRoute[mux.getPrefixes(session).size()];
					Object[] prefixes = mux.getPrefixes(session).toArray();
					for (int i = 0; i < prefixes.length; i++)
						routes[i] = routeFactory.createRoute((IPv4Prefix) prefixes[i]);
					fsm.publishRoutes(routes);
				}
			}).start();
		}
	}

	@After
	public void tearDown() throws Exception
	{
		logger.info("PHASE6: tearing down test setup");
		grapher.stopPolling();
		try
		{
			pollCPUThread.stop();
			Thread.sleep(1000);

			// send all FSMs AutomaticStop
			for (BGPSession s : sessions)
				s.getFsm().receiveEvent(new BGPEvent(EVENT_TYPE.AutomaticStop));

			rsManager.stopRouteServer();

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	public void receiveEvent(BGPEvent event, BGPPeerFSM sender)
	{
		switch (event.getEventType())
		{
			case StateChange:
				BGPState newState = event.getNewState();
				if (event.getNewState().getName().equals("Established"))
				{
					BGPSession session = ((BGPEstablishedState) newState).getFsm().getSession();
					synchronized (session)
					{
						session.notify();
					}
				}
			break;

			case NotifMsg:
				fail("NOTIFICATION received - this is always bad");
			break;

			case HoldTimer_Expires:
				fail("Holdtimer of at least one peer expired");
			break;

			case UpdateMsg:
				timeoutThread.reset();
			break;

			case TcpConnectionFails:
				try
				{
					if (rsManager.isRunning())
						fail("lost TCP connection - while RS is up");
					else
						fail("lost TCP connection - maybe RS crashed");
				}
				catch (RSTestcaseException e)
				{
					fail(e.getMessage());
				}

			break;
		}

	}

	protected class SessionOverlapMultiplexer
	{
		private Map<BGPSession, Set<IPv4Prefix>> mux = new HashMap<BGPSession, Set<IPv4Prefix>>();

		public void addSessions(List<BGPSession> sessions)
		{
			for (BGPSession s : sessions)
				this.mux.put(s, new HashSet<IPv4Prefix>());
		}

		public boolean containsPrefix(BGPSession session, IPv4Prefix prefix)
		{
			return mux.get(session).contains(prefix);
		}

		public void addPrefixToSession(BGPSession session, IPv4Prefix prefix)
		{
			if (!containsPrefix(session, prefix))
				this.mux.get(session).add(prefix);
		}

		public Set<IPv4Prefix> getPrefixes(BGPSession session)
		{
			return this.mux.get(session);
		}

		@Override
		public String toString()
		{
			Map<IPv4Prefix, Integer> prefixesNum = new HashMap<IPv4Prefix, Integer>();

			for (BGPSession s : mux.keySet())
			{
				Set<IPv4Prefix> prefixes = mux.get(s);
				for (IPv4Prefix p : prefixes)
					if (!prefixesNum.containsKey(p))
						prefixesNum.put(p, 1);
					else
						prefixesNum.put(p, prefixesNum.get(p) + 1);
			}
			int[] counter = new int[10];
			for (IPv4Prefix p : prefixesNum.keySet())
				counter[prefixesNum.get(p)]++;

			String retVal = "";
			for (int i = 1; i <= 5; i++)
				retVal += counter[i] + " " + i + "-Overlaps\n";

			return retVal;
		}
	}

}
