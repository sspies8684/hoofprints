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

import static net.decix.bgpstack.util.Utility.inetAddressLongToString;
import static net.decix.bgpstack.util.Utility.inetAddressStringToLong;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Logger;

import net.decix.bgpstack.BGPEvent;
import net.decix.bgpstack.BGPEventHandler;
import net.decix.bgpstack.BGPPeerFSM;
import net.decix.bgpstack.BGPSession;
import net.decix.bgpstack.BGPSessionImpl;
import net.decix.bgpstack.BGPConstants.EVENT_TYPE;
import net.decix.bgpstack.types.IPv4Prefix;
import net.decix.bgpstack.types.IPv6Prefix;
import net.decix.bgpstack.util.AntProperties;
import net.decix.bgpstack.util.TimeoutThread;
import net.decix.bgpstack.util.Utility;
import net.decix.bgpstack.util.UtilityException;
import net.decix.rs.RSTestcaseException;
import net.decix.rs.conf.Configuration;
import net.decix.rs.conf.Neighbor;
import net.decix.rs.conf.PrefixFilter;
import net.decix.rs.managers.BIRDManager;
import net.decix.rs.managers.QuaggaManager;
import net.decix.rs.managers.RemoteScriptManager;
import net.decix.rs.managers.RouteserverManager;

/**
 * Superclass for Testcase conveniance
 * 
 * @author sspies
 * 
 */
public class RSTestcase
{
	protected boolean testFails = false;
	protected String lastError;
	protected static AntProperties networkProperties = new AntProperties();
	protected Logger logger = Logger.getLogger("RSTestcase");

	static
	{
		try
		{
			networkProperties.load(new File("conf/network.properties"));
		}
		catch (Exception e)
		{
			System.err.println(e.getMessage());
		}

	}

	protected static RouteserverManager initQuagga(Configuration startupConfig) throws RSTestcaseException
	{
		RouteserverManager quaggaManager = new QuaggaManager(startupConfig);
		if (quaggaManager.isRunning())
			quaggaManager.stopRouteServer();
		quaggaManager.startRouteserver();
		return quaggaManager;
	}

	protected static RouteserverManager initRemote(Configuration startupConfig, File propertiesFile) throws RSTestcaseException
	{
		RouteserverManager quaggaManager = new RemoteScriptManager(startupConfig, propertiesFile);
		if (quaggaManager.isRunning())
			quaggaManager.stopRouteServer();
		quaggaManager.startRouteserver();
		return quaggaManager;
	}


	protected static RouteserverManager initBIRD(Configuration startupConfig) throws RSTestcaseException
	{
		RouteserverManager birdManager = new BIRDManager(startupConfig);
		if (birdManager.isRunning())
			birdManager.stopRouteServer();

		birdManager.startRouteserver();
		return birdManager;
	}

	protected static Configuration smallStartupConfig()
	{
		Configuration conf = new Configuration();
		conf.setAsn(1);
		conf.setHoldTime(90);
		return conf;
	}

	protected static List<PrefixFilter> martianFilters()
	{
		List<PrefixFilter> martians = new LinkedList<PrefixFilter>();
		try
		{
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
		}
		catch (UnknownHostException e)
		{// Intentionally empty
		}

		return martians;
	}

	protected void waitForState(List<BGPSession> sessions, String state) throws InterruptedException
	{
		for (BGPSession session : sessions)
			waitForState(session, state);
	}

	protected void waitForState(final BGPSession session, final String state) throws InterruptedException
	{
		synchronized (session)
		{
			session.getFsm().addObserver(new BGPEventHandler()
			{
				public void receiveEvent(BGPEvent event, BGPPeerFSM sender)
				{
					switch (event.getEventType())
					{
						case StateChange:
							if (event.getNewState().getName().equals(state))
								synchronized (session)
								{
									session.notify();
								}
					}
				}
			});

			if (!session.getFsm().getCurrentState().getName().equals(state))
				session.wait();
		}
	}

	// test will fail
	protected synchronized void fail(String message)
	{
		lastError = message;
		testFails = true;
	}

	protected void testForFailure()
	{
		if (testFails)
		{
			logger.severe(lastError);
			org.junit.Assert.fail(lastError);
		}
	}

	/**
	 * Generates a bunch of prefixes
	 * 
	 * @param numOfPrefixes
	 * @param rand
	 * @return
	 */
	protected static List<IPv4Prefix> generatePrefixes(int numOfPrefixes, Random rand)
	{
		Set<IPv4Prefix> resultSet = new HashSet<IPv4Prefix>();
		for (int i = 0; i < numOfPrefixes; i++)
		{
			IPv4Prefix prefix;
			do
				prefix = Utility.generateRandomPrefix(rand);
			while (resultSet.contains(prefix));

			resultSet.add(prefix);
		}
		List<IPv4Prefix> result = new LinkedList<IPv4Prefix>();
		result.addAll(resultSet);
		return result;
	}

	/**
	 * Generates a bunch of prefixes
	 * 
	 * @param numOfPrefixes
	 * @param rand
	 * @return
	 */
	protected static List<IPv6Prefix> generatePrefixes6(int numOfPrefixes, Random rand)
	{
		Set<IPv6Prefix> resultSet = new HashSet<IPv6Prefix>();
		for (int i = 0; i < numOfPrefixes; i++)
		{
			IPv6Prefix prefix;
			do
				prefix = Utility.generateRandomPrefix6(rand);
			while (resultSet.contains(prefix) || prefix.getAddress().isLinkLocalAddress());

			resultSet.add(prefix);
		}
		List<IPv6Prefix> result = new LinkedList<IPv6Prefix>();
		result.addAll(resultSet);
		return result;
	}

	protected static InetAddress generateIP(int i) throws UtilityException
	{
		/*
		 * test.subnet.ip.start=10.15.20.0 test.subnet.ip.num=2560
		 */
		long address = inetAddressStringToLong(networkProperties.getProperty("test.subnet.ip.start"));
		address += i;
		return inetAddressLongToString(address);
	}

	protected static InetAddress generateIP6(int i) throws UtilityException
	{
		try
		{
			BigInteger bi = new BigInteger(InetAddress.getByName(networkProperties.getProperty("test.subnet.ip6.start")).getAddress());
			bi = bi.add(new BigInteger(Integer.toString(i)));
			return InetAddress.getByAddress(bi.toByteArray());
		}
		catch (UnknownHostException e)
		{
			throw new UtilityException("Unknown Host: " + e.getMessage());
		}

	}

	protected List<BGPSession> setupSessions(Configuration conf, RouteserverManager rsManager, BGPEventHandler caller) throws IOException
	{
		List<BGPSession> sessions = new LinkedList<BGPSession>();
		for (Neighbor n : conf.getNeighbors())
		{
			final BGPPeerFSM fsm = new BGPPeerFSM(n.getAddress(), n.getAsn(), conf.getAsn(), conf.getHoldTime(), (Inet4Address) n.getAddress());
			final BGPSession session = new BGPSessionImpl(rsManager.getListenAddress(), rsManager.getListenPort(), n.getAddress(), fsm);
			fsm.setSession(session);
			fsm.addObserver(caller);
			sessions.add(session);
		}
		return sessions;
	}

	protected void startSessions(List<BGPSession> sessions)
	{
		final Random random = new Random();
		for (final BGPSession session : sessions)
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

	// I want closures!!!
	public static File[] propertyFiles(String directory)
	{
		File confDirectory = new File(directory);
		File[] files;

		if (confDirectory.isDirectory())
			files = confDirectory.listFiles(new FilenameFilter()
			{
				public boolean accept(File dir, String name)
				{
					return name.endsWith(".properties");
				}
			});
		else
			files = new File[0];

		return files;
	}

	public static void propertiesToFields(AntProperties properties, Object container) throws SecurityException, NoSuchFieldException, ClassNotFoundException, IllegalArgumentException, IllegalAccessException
	{
		for (Entry<Object, Object> e : properties.getProperties().entrySet())
		{
			String key = (String) e.getKey();
			String[] keyAsArray = key.split("\\.");
			if (keyAsArray.length == 3)
			{
				Field field = container.getClass().getDeclaredField(keyAsArray[1]);
				Class<?> klass = Class.forName("java.lang." + keyAsArray[2]);
				if (field.getType().equals(klass))
					field.set(container, properties.getProperty(klass, "parse" + (keyAsArray[2].equals("Integer") ? "Int" : keyAsArray[2]), key));
				else
					throw new NoSuchFieldException("requested type: " + klass + " but field is type: " + field.getType());
			}

		}

	}

	public static RouteserverManager[] managersFromProperties(AntProperties properties, String key, Configuration startupConfig) throws RSTestcaseException
	{
		String[] managersLine = properties.getProperty(key).split(",");
		int numOfManagers = managersLine.length;
		RouteserverManager[] managers = new RouteserverManager[numOfManagers];

		int i = 0;
		for (String l : managersLine)
		{
			String[] cd = l.split(":");
			String daemon = cd[0];

			try
			{
				Class<?> daemonClass = Class.forName("net.decix.rs.managers." + daemon.trim());

				RouteserverManager manager;
				switch (cd.length)
				{
					case 1:
						manager = (RouteserverManager) daemonClass.getConstructor(Configuration.class).newInstance(startupConfig);
					break;
					case 2:
						File managerProperties = new File(cd[1]);
						manager = (RouteserverManager) daemonClass.getConstructor(Configuration.class, File.class).newInstance(startupConfig, managerProperties);
					break;
					default:
						throw new RSTestcaseException("parse of " + key + " failed");

				}

				managers[i++] = manager;
			}
			catch (Exception e)
			{
				throw new RSTestcaseException("Manager Misconfiguration: " + e + ": " + e.getMessage());
			}

		}

		return managers;
	}

	public static void restartManagers(RouteserverManager[] managers) throws RSTestcaseException
	{
		for (RouteserverManager m : managers)
		{
			if (m.isRunning())
				m.stopRouteServer();
			m.startRouteserver();
		}
	}

	// this thread polls cpu time
	public class PollCPUTimeout extends Thread
	{
		private RouteserverManager rsManager;
		private TimeoutThread convergenceTimeoutThread;

		public PollCPUTimeout(RouteserverManager rsManager, TimeoutThread convergenceTimeoutThread)
		{
			this.rsManager = rsManager;
			this.convergenceTimeoutThread = convergenceTimeoutThread;
		}

		@Override
		public void run()
		{
			setName("PollThread");
			for (;;)
			{
				try
				{
					if (rsManager.isRunning())
						if (rsManager.getCPU() > 8)
							convergenceTimeoutThread.reset();
						else
							;
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

	public class PollGraphs extends Thread
	{
		private RouteserverManager rsManager;
		private RSTCGrapher cpuGrapher;
		private RSTCGrapher memGrapher;
		private boolean stop = false;
		private Object mutex = new Object();

		public PollGraphs(RouteserverManager rsManager, String title)
		{
			this.rsManager = rsManager;
			this.cpuGrapher = new RSTCGrapher(title, "CPU%");
			this.memGrapher = new RSTCGrapher(title, "MEM (in MB)");
		}

		@Override
		public void run()
		{
			setName("PollGraphs");
			try
			{

				while (!stop)
				{
					synchronized (mutex)
					{
						if (rsManager.isRunning())
						{
							cpuGrapher.addValue(System.currentTimeMillis(), rsManager.getCPU());
							memGrapher.addValue(System.currentTimeMillis(), rsManager.getMemory());
						}
						else
							stop = true;
					}
					Thread.sleep(1000);
				}

			}
			catch (Exception e)
			{
				e.printStackTrace();
				return;
			}
			cpuGrapher.createGraph();
			memGrapher.createGraph();
			synchronized (mutex)
			{
				mutex.notify();
			}

		}

		public void stopPolling() throws InterruptedException
		{
			synchronized (mutex)
			{
				if (!stop)
				{
					stop = true;
					mutex.wait();
				}
			}
		}
	}

}
