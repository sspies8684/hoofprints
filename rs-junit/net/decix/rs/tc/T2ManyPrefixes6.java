/**
 * Hoofprints - An Extensible Testbed for Route-Servers
 * Copyright (C) 2009 Sebastian Spies
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *  
 */

package net.decix.rs.tc;

import java.io.File;
import java.io.IOException;
import java.net.Inet4Address;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import net.decix.bgpstack.BGPConstants;
import net.decix.bgpstack.BGPEvent;
import net.decix.bgpstack.BGPEventHandler;
import net.decix.bgpstack.BGPException;
import net.decix.bgpstack.BGPPeerFSM;
import net.decix.bgpstack.BGPRoute;
import net.decix.bgpstack.BGPRoute6;
import net.decix.bgpstack.BGPSession;
import net.decix.bgpstack.BGPSessionImpl;
import net.decix.bgpstack.states.BGPEstablishedState;
import net.decix.bgpstack.states.BGPState;
import net.decix.bgpstack.types.IPv4Prefix;
import net.decix.bgpstack.types.IPv6Prefix;
import net.decix.bgpstack.types.pathattributes.BGPPathAttribute;
import net.decix.bgpstack.types.pathattributes.BGPPathAttributeAsPath;
import net.decix.bgpstack.types.pathattributes.BGPPathAttributeNextHop;
import net.decix.bgpstack.types.pathattributes.BGPPathAttributeOrigin;
import net.decix.bgpstack.types.pathattributes.BGPPathAttributeSequence;
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
public class T2ManyPrefixes6 extends RSTestcase implements BGPConstants, BGPEventHandler
{

	// testcase variables
	private int numOfPrefixes;
	private int numOfPeers;

	// testcase constants
	private final int CONVERGENCE_TIMEOUT = 10000;
	private final boolean RS_CLIENT_FEATURE = false;

	// helpers
	private Logger logger = Logger.getLogger("net.decix.rs.tc.T2ManyPrefixes");
	private static Configuration conf;
	private static Collection<Object[]> testSetup = new LinkedList<Object[]>();
	private RouteserverManager rsManager;
	private PollGraphs pollGraphs;

	// testcase runtime
	private PollThread pollCPUThread;
	private static Random random = new Random();
	private static List<IPv6Prefix> prefixes = new LinkedList<IPv6Prefix>();
	private TimeoutThread convergenceTimeoutThread;
	private List<BGPSession> sessions = new LinkedList<BGPSession>();

	// test data input
	@Parameters
	public static Collection<Object[]> data() throws RSTestcaseException
	{

		conf = new Configuration();
		conf.setAsn(1);
		conf.setHoldTime(90);

		// RouteserverManager quagga = initRemoteQuagga(conf, new
		// File("conf/monitorwall_script.properties"));
		RouteserverManager quagga = initQuagga(conf);
		RouteserverManager openbgpd = initRemote(conf, new File("conf/openbgpd_script.properties"));
		
		prefixes = generatePrefixes6(50000, random);

		// test quagga with 500 peers, 400 identical prefixes

		testSetup.add(new Object[] { "quagga,500,800", quagga, 500, 800 });
		testSetup.add(new Object[] { "openbgpd,500,800", openbgpd, 500, 800 });
		

		return testSetup;
	}

	// test constructor
	public T2ManyPrefixes6(String parameterTitle, RouteserverManager rsManager, int peers, int prefixes)
	{
		this.rsManager = rsManager;
		this.numOfPeers = peers;
		this.numOfPrefixes = prefixes;
		this.pollGraphs = new PollGraphs(rsManager, "T2_6(" + peers + "," + prefixes + "," + RS_CLIENT_FEATURE + ") with " + rsManager);

	}

	// setup test
	@Before
	public void setup() throws Exception
	{
		if (rsManager.isRunning())
			rsManager.stopRouteServer();

		rsManager.startRouteserver();
		pollGraphs.start();
		conf = new Configuration();
		conf.setAsn(1);
		conf.setHoldTime(90);

		for (int i = 0; i < numOfPeers; i++)
		{
			Neighbor n = new Neighbor();
			n.setAsn(10000 + i);
			n.setAddress(generateIP6(i));

			n.setDescription("Peer " + i);
			n.setRsClient(RS_CLIENT_FEATURE);
			conf.addNeighbor(n);
		}

		rsManager.loadConfiguration(conf);
		Thread.sleep(10000);
		logger.info("PHASE1: " + rsManager + " armed and ready");

	}

	// tear down test
	@SuppressWarnings("deprecation")
	@After
	public void tearDown() throws RSTestcaseException
	{
		logger.info("PHASE6: tearing down test setup");

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

	/**
	 * A set of prefixes is generated, which every peer announces
	 * 
	 * @throws BGPException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	// this is the real test
	@Test(timeout = 1000000)
	public void testIdenticalPrefixes() throws BGPException, IOException, InterruptedException
	{

		logger.info("PHASE2: configure & init client threads - waiting for all to be state ESTABLISHED");
		pollCPUThread = new PollThread();

		for (Neighbor n : conf.getNeighbors())
		{
			final BGPPeerFSM fsm = new BGPPeerFSM(n.getAddress(), n.getAsn(), conf.getAsn(), conf.getHoldTime(), n.getAddress());
			final BGPSession session = new BGPSessionImpl(rsManager.getListen6Address(), rsManager.getListenPort(), n.getAddress(), fsm);
			fsm.setSession(session);
			fsm.addObserver(this);
			sessions.add(session);
		}

		// send AutomaticStart event to all sessions, at random time-delay (10
		// secs
		// max.)
		for (final BGPSession session : sessions)
		{
			new Thread(new Runnable()
			{
				public void run()
				{
					try
					{
						BGPPeerFSM fsm = session.getFsm();
						Thread.sleep(random.nextInt(10000));
						fsm.receiveEvent(new BGPEvent(EVENT_TYPE.AutomaticStart));
					}
					catch (InterruptedException e)
					{
					}
				}
			}).start();
		}

		// waiting for all sessions to enter state established
		for (BGPSession session : sessions)
			synchronized (session)
			{
				if (!session.getFsm().getCurrentState().getName().equals("Established"))
					session.wait();
			}

		logger.info("PHASE3: all peers are state ESTABLISHED, sending prefixes, waiting for convergence");

		// instantiate TimeoutThread
		convergenceTimeoutThread = new TimeoutThread(CONVERGENCE_TIMEOUT);
		pollCPUThread.start();
		convergenceTimeoutThread.start();

		long before = System.currentTimeMillis();

		for (final BGPSession session : sessions)
		{
			new Thread(new Runnable()
			{
				public void run()
				{
					BGPPeerFSM fsm = session.getFsm();

					BGPPathAttributeSequence seq = new BGPPathAttributeSequence();

					BGPPathAttributeAsPath asPath = new BGPPathAttributeAsPath();
					asPath.setFourByteEncoding(fsm);
					asPath.addSequence(fsm.getMyAsn());

					BGPPathAttributeOrigin origin = new BGPPathAttributeOrigin(BGP_PATH_ORIGIN_IGP);

					seq.add(new BGPPathAttribute(origin));
					seq.add(new BGPPathAttribute(asPath));

					BGPRoute6[] routes = new BGPRoute6[numOfPrefixes];

					for (int i = 0; i < numOfPrefixes; i++)
						routes[i] = new BGPRoute6(prefixes.get(i), seq, session.getMyAddress());

					fsm.publishRoutes(routes);
				}
			}).start();
		}

		Thread.sleep(50000);
		convergenceTimeoutThread.join();
		logger.info("PHASE4: RS seems to be converged");

		pollGraphs.stopPolling();
		if (testFails)
		{
			logger.severe(lastError);
			org.junit.Assert.fail(lastError);
		}

		logger.info("PHASE5: passed all health checks");

		long convergenceTime = System.currentTimeMillis() - before;

		logger.info("Testrun results:" + " RS manager: " + rsManager + "\n" + " number of peers: " + numOfPeers + "\n" + " number of identical prefixes: " + numOfPrefixes + "\n" + " convergence time: " + convergenceTime + "ms (+~"
				+ CONVERGENCE_TIMEOUT + "ms)");

		// logger.info(NUM_OF_PEERS+";"+NUM_OF_IDENTICAL_PREFIXES+";"+convergenceTime);

		logger.info("Test for " + rsManager + " completed");

	}

	// @Test
	public void testDisjunctPrefixes() throws BGPException, IOException, InterruptedException
	{
		logger.info("PHASE2: configure & init client threads - waiting for all to be state ESTABLISHED");
		pollCPUThread = new PollThread();

		for (Neighbor n : conf.getNeighbors())
		{
			final BGPPeerFSM fsm = new BGPPeerFSM(n.getAddress(), n.getAsn(), conf.getAsn(), conf.getHoldTime(), (Inet4Address) n.getAddress());
			final BGPSession session = new BGPSessionImpl(rsManager.getListenAddress(), rsManager.getListenPort(), n.getAddress(), fsm);
			fsm.setSession(session);
			fsm.addObserver(this);
			sessions.add(session);
		}

		// send AutomaticStart event to all sessions, at random time-delay (10
		// secs max.)
		for (final BGPSession session : sessions)
		{
			new Thread(new Runnable()
			{
				public void run()
				{
					try
					{
						BGPPeerFSM fsm = session.getFsm();
						Thread.sleep(random.nextInt(10000));
						fsm.receiveEvent(new BGPEvent(EVENT_TYPE.AutomaticStart));
					}
					catch (InterruptedException e)
					{
					}
				}
			}).start();
		}

		// waiting for all sessions to enter state established
		for (BGPSession session : sessions)
			synchronized (session)
			{
				if (!session.getFsm().getCurrentState().getName().equals("Established"))
					session.wait();
			}

		logger.info("PHASE3: all peers are state ESTABLISHED, sending prefixes, waiting for convergence");

		// instantiate TimeoutThread
		convergenceTimeoutThread = new TimeoutThread(CONVERGENCE_TIMEOUT);
		pollCPUThread.start();
		convergenceTimeoutThread.start();

		long before = System.currentTimeMillis();

		// sending prefixes
		// TODO Factory pattern preferred for BGPRoutes instantiation
		for (final BGPSession session : sessions)
		{
			new Thread(new Runnable()
			{
				final List<IPv4Prefix> myPrefixes = generatePrefixes(numOfPrefixes, random);

				public void run()
				{
					BGPPeerFSM fsm = session.getFsm();

					BGPPathAttributeSequence seq = new BGPPathAttributeSequence();

					BGPPathAttributeAsPath asPath = new BGPPathAttributeAsPath();
					asPath.addSequence(fsm.getMyAsn());

					BGPPathAttributeOrigin origin = new BGPPathAttributeOrigin(BGP_PATH_ORIGIN_IGP);
					BGPPathAttributeNextHop nextHop = new BGPPathAttributeNextHop(session.getMyAddress());

					seq.add(new BGPPathAttribute(origin));
					seq.add(new BGPPathAttribute(asPath));
					seq.add(new BGPPathAttribute(nextHop));

					BGPRoute[] routes = new BGPRoute[numOfPrefixes];
					for (int i = 0; i < numOfPrefixes; i++)
						routes[i] = new BGPRoute(myPrefixes.get(i), seq);

					fsm.publishRoutes(routes);
				}
			}).start();
		}

		convergenceTimeoutThread.join();
		logger.info("PHASE4: RS seems to be converged");

		if (testFails)
		{
			logger.severe(lastError);
			org.junit.Assert.fail(lastError);
		}

		logger.info("PHASE5: passed all health checks");

		long convergenceTime = System.currentTimeMillis() - before;

		logger.info("Testrun results:" + " RS manager: " + rsManager + "\n" + " number of peers: " + numOfPeers + "\n" + " number of identical prefixes: " + numOfPrefixes + "\n" + " convergence time: " + convergenceTime + "ms (+~"
				+ CONVERGENCE_TIMEOUT + "ms)");

		logger.info(numOfPeers + ";" + numOfPrefixes + ";" + (convergenceTime - CONVERGENCE_TIMEOUT));

		logger.info("Test for " + rsManager + " completed");

	}

	// T2ManyPrefixes observes all client fsms
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
				logger.fine(sender.getMyAsn() + ": " + event.getOldState().getName() + " -> " + event.getNewState().getName());
			break;

			case NotifMsg:
				fail("NOTIFICATION received - this is always bad");
			break;

			case HoldTimer_Expires:
				fail("Holdtimer of at least one peer expired");
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

	// this thread polls cpu time
	public class PollThread extends Thread
	{
		@Override
		public void run()
		{
			for (;;)
			{
				setName("PollThread");
				setPriority(MAX_PRIORITY);
				try
				{
					if (rsManager.isRunning())
					{
						int cpu = rsManager.getCPU();
						if (cpu > 20)
							convergenceTimeoutThread.reset();
						else
							;
					}
					else
						fail(rsManager + " RS crashed");
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
	}

}
