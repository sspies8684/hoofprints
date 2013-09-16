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
import net.decix.bgpstack.BGPSession;
import net.decix.bgpstack.BGPSessionImpl;
import net.decix.bgpstack.states.BGPEstablishedState;
import net.decix.bgpstack.states.BGPState;
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
public class T1ManyPeersConnecting extends RSTestcase implements BGPConstants, BGPEventHandler
{
	private static Configuration conf;
	private static Collection<Object[]> rsManagers = new LinkedList<Object[]>();
	Logger logger = Logger.getLogger("net.decix.rs.tc.T1ManyPeersConnecting");
	private Random random = new Random();
	List<BGPSession> sessions = new LinkedList<BGPSession>();
	
	
	@Parameters
	public static Collection<Object[]> data() throws RSTestcaseException
	{	
		conf = new Configuration();
		conf.setAsn(1);
		conf.setHoldTime(90);
//		rsManagers.add(new Object[] { initBIRD(conf) });
		rsManagers.add(new Object[] { initQuagga(conf) });
		return rsManagers;
	}
	
	private RouteserverManager rsManager;
	public T1ManyPeersConnecting(RouteserverManager rsManager)
	{
		this.rsManager = rsManager;
	}

	@Before
	public void setup() throws Exception
	{
		conf = new Configuration();
		conf.setAsn(1);
		conf.setHoldTime(90);

		for (int i = 0; i < 1000; i++)
		{
			Neighbor n = new Neighbor();
			n.setAsn(30000 + i);
			n.setAddress(generateIP(i));
			n.setDescription("Peer " + i);
			conf.addNeighbor(n);
		}

		rsManager.loadConfiguration(conf);
		logger.info("stopped sleeping - starting tests");
	}

	@After
	public void tearDown() throws RSTestcaseException
	{
		rsManager.stopRouteServer();
	}


	// actively connect, no listening
	@Test(timeout = 150000)
	public void testThousandConnects() throws BGPException, IOException, InterruptedException
	{

		for (Neighbor n : conf.getNeighbors())
		{
			final BGPPeerFSM fsm = new BGPPeerFSM(n.getAddress(), n.getAsn(), conf.getAsn(), conf.getHoldTime(), (Inet4Address) n.getAddress());
			final BGPSession session = new BGPSessionImpl(rsManager.getListenAddress(), rsManager.getListenPort(), n.getAddress(), fsm);
			fsm.setSession(session);
			fsm.addObserver(this);
			sessions.add(session);
		}

		
		for (final BGPSession session: sessions)
		{
			new Thread(new Runnable()
			{
				public void run()
				{
					try
					{
						BGPPeerFSM fsm = session.getFsm();
						Thread.sleep(random.nextInt(10000));
						fsm.receiveEvent(new BGPEvent(EVENT_TYPE.ManualStart));
					}
					catch (InterruptedException e)
					{}
				}
			}).start();
		}
		
		
		for (BGPSession session: sessions)
			synchronized (session)
			{
				if(! session.getFsm().getCurrentState().getName().equals("Established"))
					session.wait();
			}
		logger.info("Test for " + rsManager + " completed");

	}



	public void receiveEvent(BGPEvent event, BGPPeerFSM sender)
	{			
		switch(event.getEventType())
		{
			case StateChange:
				BGPState newState = event.getNewState();
				if(event.getNewState().getName().equals("Established"))
				{
					BGPSession session = ((BGPEstablishedState)newState).getFsm().getSession();
					synchronized (session)
					{
						session.notify();
					}
					
				}
			break;
		}
		
	}


}
