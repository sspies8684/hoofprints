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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import net.decix.bgpstack.BGPConstants;
import net.decix.bgpstack.BGPEvent;
import net.decix.bgpstack.BGPEventHandler;
import net.decix.bgpstack.BGPException;
import net.decix.bgpstack.BGPPeerFSM;
import net.decix.bgpstack.BGPRoute;
import net.decix.bgpstack.BGPSession;
import net.decix.bgpstack.BGPSessionImpl;
import net.decix.bgpstack.states.BGPEstablishedState;
import net.decix.bgpstack.states.BGPState;
import net.decix.bgpstack.types.IPv4Prefix;
import net.decix.bgpstack.types.pathattributes.BGPPathAttribute;
import net.decix.bgpstack.types.pathattributes.BGPPathAttributeAsPath;
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
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class T3CrossConnect extends RSTestcase implements BGPConstants, BGPEventHandler
{

	// testcase variables
	private int numOfPrefixes;
	private int numOfPeers;

	// testcase constants

	private final boolean RS_CLIENT_FEATURE = false;

	// helpers
	private static Configuration conf;
	private static Collection<Object[]> testSetup = new LinkedList<Object[]>();

	private RouteserverManager rsManager;

	// testcase runtime
	private PollCPUTimeout pollCPUThread;
	private static Random random = new Random();
	private List<BGPSession> sessions = new LinkedList<BGPSession>();
	private int peer;

	// test data input
	@Parameters
	public static Collection<Object[]> data() throws RSTestcaseException, FileNotFoundException, IOException
	{

		conf = new Configuration();
		conf.setAsn(1);
		conf.setHoldTime(90);

		RouteserverManager quagga = initQuagga(conf);

		int peer = Integer.parseInt(networkProperties.getProperty("cross.id"));

		testSetup.add(new Object[] { quagga, 500, 1000, peer });

		return testSetup;
	}

	// test constructor
	public T3CrossConnect(RouteserverManager rsManager, int peers, int prefixes, int peer) throws FileNotFoundException, IOException
	{
		this.rsManager = rsManager;
		this.numOfPeers = peers;
		this.numOfPrefixes = prefixes;
		this.peer = peer;

	}

	// setup test
	@Before
	public void setup() throws Exception
	{
		if (rsManager.isRunning()) rsManager.stopRouteServer();
		rsManager.startRouteserver();

		conf = new Configuration();
		conf.setAsn(1);
		conf.setHoldTime(90);

		Neighbor cross = new Neighbor();
		cross.setAsn(1);
		cross.setAddress(InetAddress.getByName(networkProperties.getProperty("cross.ip." + (peer == 1 ? 2 : 1))));
		cross.setDescription("Crossconnect " + (peer == 1 ? 2 : 1));
		cross.setPort(rsManager.getListenPort());
		cross.setLocalAddress(rsManager.getListenAddress());
		conf.addNeighbor(cross);

		for (int i = 0; i < numOfPeers; i++)
		{
			Neighbor n = new Neighbor();
			n.setAsn(10000 + i);
			n.setAddress(generateIP(i));
			n.setDescription("Peer " + i);
			n.setRsClient(RS_CLIENT_FEATURE);
			n.setPassive(true);
			conf.addNeighbor(n);
		}

		rsManager.loadConfiguration(conf);
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

		for (Neighbor n : conf.getNeighbors())
			if (n.getAsn() > 2)
			{
				final BGPPeerFSM fsm = new BGPPeerFSM(n.getAddress(), n.getAsn(), conf.getAsn(), conf.getHoldTime(), (Inet4Address) n.getAddress());
				final BGPSession session = new BGPSessionImpl(rsManager.getListenAddress(), rsManager.getListenPort(), n.getAddress(), fsm);
				fsm.setSession(session);
				fsm.addObserver(this);
				sessions.add(session);
			}

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
		waitForState(sessions, "Established");

		logger.info("PHASE3: all peers are state ESTABLISHED, sending prefixes, waiting for convergence");

		Thread.sleep(100000);

		if (peer == 1) for (final BGPSession session : sessions)
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

		Thread.sleep(100000);
		if (peer == 1 && testFails)
		{
			logger.severe(lastError);
			org.junit.Assert.fail(lastError);
		}

		logger.info("PHASE5: passed all health checks");

		Thread.sleep(500000);

		logger.info("Test for " + rsManager + " completed");

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
				// System.out.println(sender.getMyAsn() + ": " +
				// event.getOldState().getName() + " -> " +
				// event.getNewState().getName());
			break;

			// case KeepAliveMsg:
			// System.out.println(sender.getMyAsn() + ": got keepalive");
			// break;

			case NotifMsg:
				fail("NOTIFICATION received - this is always bad");
				if (peer == 2) org.junit.Assert.fail("NOTIFICATION received - this is always bad");
			break;

			case HoldTimer_Expires:
				fail("Holdtimer of at least one peer expired");
				if (peer == 2) org.junit.Assert.fail("Holdtimer of at least one peer expired");
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

}
