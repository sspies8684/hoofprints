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

package net.decix.rs.managers;

import static net.decix.bgpstack.util.Utility.readIntFromFile;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.logging.Logger;

import net.decix.bgpstack.util.AntProperties;
import net.decix.bgpstack.util.Utility;
import net.decix.rs.RSTestcaseException;
import net.decix.rs.conf.ASPathFilter;
import net.decix.rs.conf.Configuration;
import net.decix.rs.conf.Neighbor;
import net.decix.rs.conf.PrefixFilter;

/**
 * {@link RouteserverManager} implementation for Quagga
 * 
 * @author sspies
 * 
 */
public class QuaggaManager extends RouteserverManager
{
	private final int QUAGGA_MGM_PORT = 2605;
	private final String QUAGGA_HOSTNAME = "quagga-testhost";
	protected static File propertyFile = new File("conf/quagga.properties");
	private AntProperties properties;
	private File executable;
	private Logger logger = Logger.getLogger("net.decix.rs.managers.QuaggaManager");

	public QuaggaManager(Configuration startupConfiguration) throws RSTestcaseException
	{
		super(startupConfiguration);
		try
		{
			writeStartupConfiguration();
		}
		catch (IOException e)
		{
			throw new RSTestcaseException("could not write startup-file: " + e.getMessage());
		}
	}

	@Override
	public void readProperties() throws RSTestcaseException
	{
		properties = new AntProperties();
		try
		{
			properties.load(new File("conf/test.properties"));
			properties.load(new File("conf/quagga.properties"));

			listenAddress = InetAddress.getByName(properties.getProperty("quagga.ip"));
			listen6Address = InetAddress.getByName(properties.getProperty("quagga.ip6"));
			executable = new File(properties.getProperty("quagga.installation.home") + "/bin/bgpd");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new RSTestcaseException("Quagga misconfiguration: " + e.getMessage());
		}
	}

	/**
	 * write config to ${quagga.conf.standard}
	 * 
	 * @throws IOException
	 */
	private void writeStartupConfiguration() throws IOException
	{
		File startupConfigFile = new File(properties.getProperty("quagga.conf.standard"));
		FileWriter fw = new FileWriter(startupConfigFile);
		fw.write("hostname " + QUAGGA_HOSTNAME + "\n");
		fw.write("password " + STANDARDPASSWORD + "\n");
		fw.write("router bgp " + startupConfiguration.getAsn() + "\n");
		fw.write(" bgp router-id " + listenAddress.getHostAddress() + "\n");
		fw.close();
	}

	@Override
	public void startRouteserver() throws RSTestcaseException
	{
		if (isRunning())
			throw new RSTestcaseException("quagga is already running");

		Runtime rt = Runtime.getRuntime();

		try
		{
			// TODO get group of user
			Process proc = rt.exec(executable.getAbsolutePath() + " -p " + listenPort /*
																					 * +
																					 * " -l "
																					 * +
																					 * listenAddress
																					 * .
																					 * getHostAddress
																					 * (
																					 * )
																					 */+ " -A :: -u " + System.getProperty("user.name") + " -g sspies" + " -d");

			if (proc.waitFor() != 0)
				throw new RSTestcaseException("could not start quagga: exit-code " + proc.exitValue());
		}
		catch (IOException e)
		{
			throw new RSTestcaseException("cannot execute quagga: " + e.getMessage());
		}
		catch (InterruptedException e)
		{
			throw new RSTestcaseException("cannot execute quagga: " + e.getMessage());
		}

		while (!isRunning())
			try
			{
				Thread.sleep(1000);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}

	}

	@Override
	public void stopRouteServer() throws RSTestcaseException
	{
		if (!isRunning())
			throw new RSTestcaseException("quagga is not running");

		Runtime rt = Runtime.getRuntime();

		try
		{
			Process proc = rt.exec("kill -9 " + getPid());

			if (proc.waitFor() != 0)
				throw new RSTestcaseException("could not send kill-signal: exit-code" + proc.exitValue());

		}
		catch (IOException e)
		{
			throw new RSTestcaseException("cannot kill quagga: " + e.getMessage());
		}
		catch (InterruptedException e)
		{
			throw new RSTestcaseException("cannot kill quagga: " + e.getMessage());
		}

		while (isRunning())
			try
			{
				Thread.sleep(1000);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
	}

	protected String executeCommand(String cmd) throws RSTestcaseException
	{
		Socket s = new Socket();
		StringBuffer buffer = new StringBuffer();
		byte[] readBuffer = new byte[80];
		OutputStream os;
		InputStream is;

		try
		{
			s.connect(new InetSocketAddress(listenAddress, QUAGGA_MGM_PORT));
			os = s.getOutputStream();
			is = s.getInputStream();
			os.write((STANDARDPASSWORD + "\n" + cmd + "\n").getBytes());

			for (;;)
			{
				int bytesRead;
				if ((bytesRead = is.read(readBuffer)) == -1)
					break;

				buffer.append(new String(readBuffer, 0, bytesRead));
			}

			s.close();
		}
		catch (IOException e)
		{
			throw new RSTestcaseException(e.getMessage());
		}

		return buffer.toString();
	}

	@Override
	public void loadConfiguration(Configuration config) throws RSTestcaseException
	{
		StringBuffer command = new StringBuffer();

		command.append("enable\n");

		command.append("configure terminal\n");

		command.append("no router bgp " + config.getAsn() + "\n");

		command.append("router bgp " + config.getAsn() + "\n");
		command.append("bgp router-id " + listenAddress.getHostAddress() + "\n");

		// command.append("address-family ipv6\n");
		// command.append("network 1123::/16\n");
		// command.append("exit-address-family\n");
		// command.append("network 1.2.0.0/16\n");

		for (Neighbor n : config.getNeighbors())
		{

			command.append("neighbor " + n.getAddress().getHostAddress() + " remote-as " + n.getAsn() + "\n");

			if (n.getAddress() instanceof Inet6Address)
			{
				command.append("no neighbor " + n.getAddress().getHostAddress() + " activate\n");
				command.append("address-family ipv6\n");
				command.append("neighbor " + n.getAddress().getHostAddress() + " activate\n");
				if (n.isAttributeUnchanged())
					command.append("neighbor " + n.getAddress().getHostAddress() + " attribute-unchanged\n");
				if (n.isRsClient())
					command.append("neighbor " + n.getAddress().getHostAddress() + " route-server-client\n");
				command.append("exit-address-family\n");
			}

			command.append("neighbor " + n.getAddress().getHostAddress() + " description " + n.getDescription() + "\n");

			if (n.isPassive())
				command.append("neighbor " + n.getAddress().getHostAddress() + " passive\n");
			if (n.getPort() != BGP_PORT)
				command.append("neighbor " + n.getAddress().getHostAddress() + " port " + n.getPort() + "\n");
			if (n.getLocalAddress() != null)
				command.append("neighbor " + n.getAddress().getHostAddress() + " update-source " + n.getLocalAddress().getHostAddress() + "\n");
			if (n.getAsPathFilters().size() > 0)
				command.append("neighbor " + n.getAddress().getHostAddress() + " filter-list as-" + n.getAsn() + " in\n");
			if (config.getGlobalFilters().size() > 0)
				command.append("neighbor " + n.getAddress().getHostAddress() + " prefix-list global-filter out\n");
			if (n.getInFilters().size() > 0)
				command.append("neighbor " + n.getAddress().getHostAddress() + " prefix-list pref-" + n.getAsn() + "-in in\n");
			if (n.getOutFilters().size() > 0)
				command.append("neighbor " + n.getAddress().getHostAddress() + " prefix-list pref-" + n.getAsn() + "-out out\n");
			if (n.getAddress() instanceof Inet4Address)
			{
				if (n.isAttributeUnchanged())
					command.append("neighbor " + n.getAddress().getHostAddress() + " attribute-unchanged\n");
				if (n.isRsClient())
					command.append("neighbor " + n.getAddress().getHostAddress() + " route-server-client\n");
			}
		}

		int i = 5;
		// global filters
		for (PrefixFilter f : config.getGlobalFilters())
		{
			command.append("ip prefix-list global-filter seq " + i + " " + (f.isAllowed() ? "permit" : "deny") + " " + f.getPrefix() + " " + (f.getUpperBound() > -1 ? "le " + f.getUpperBound() : "") + " "
					+ (f.getLowerBound() > -1 ? "ge " + f.getLowerBound() : "") + "\n");
			i += 5;
		}

		for (Neighbor n : config.getNeighbors())
		{
			for (ASPathFilter f : n.getAsPathFilters())
				command.append("ip as-path access-list as-" + n.getAsn() + " " + (f.isAllowed() ? "permit" : "deny") + " _" + f.getSourceAs() + "$\n");
			i = 5;
			for (PrefixFilter inf : n.getInFilters())
			{
				command.append("ip prefix-list pref-" + n.getAsn() + "-in seq " + i + " " + (inf.isAllowed() ? "permit" : "deny") + " " + inf.getPrefix() + " " + (inf.getUpperBound() > -1 ? "le " + inf.getUpperBound() : "") + " "
						+ (inf.getLowerBound() > -1 ? "ge " + inf.getLowerBound() : "") + "\n");
				i += 5;
			}
			i = 5;
			for (PrefixFilter onf : n.getOutFilters())
			{
				command.append("ip prefix-list pref-" + n.getAsn() + "-out seq " + i + " " + (onf.isAllowed() ? "permit" : "deny") + " " + onf.getPrefix() + " " + (onf.getUpperBound() > -1 ? "le " + onf.getUpperBound() : "") + " "
						+ (onf.getLowerBound() > -1 ? "ge " + onf.getLowerBound() : "") + "\n");
				i += 5;
			}

		}

//		command.append("debug bgp\n");
		command.append("log file /tmp/quagga-testrun.log\n");

		command.append("exit\nexit\n");


		executeCommand(command.toString());
		try
		{
			Thread.sleep(10000);
		}
		catch (InterruptedException e)
		{
		}
	}

	@Override
	public boolean isRunning() throws RSTestcaseException
	{
		Socket s = new Socket();
		try
		{
			s.connect(new InetSocketAddress(listenAddress, QUAGGA_MGM_PORT));
		}
		catch (IOException e)
		{
			return false;
		}

		try
		{
			s.close();
		}
		catch (IOException e)
		{
			throw new RSTestcaseException("Could not check if quagga runs: " + e.getMessage());
		}

		return true;
	}

	protected static void setPropertyFile(File propertyFile)
	{
		QuaggaManager.propertyFile = propertyFile;
	}

	protected void setProperties(AntProperties properties)
	{
		this.properties = properties;
	}

	protected void setExecutable(File executable)
	{
		this.executable = executable;
	}

	public static File getPropertyFile()
	{
		return propertyFile;
	}

	public AntProperties getProperties()
	{
		return properties;
	}

	public File getExecutable()
	{
		return executable;
	}

	public int getPid() throws RSTestcaseException
	{
		try
		{
			return readIntFromFile(new File(properties.getProperty("quagga.pidfile")));
		}
		catch (Exception e)
		{
			throw new RSTestcaseException("quagga misconfiguration: " + e.getMessage());
		}
	}

	@Override
	public int getCPU() throws RSTestcaseException
	{
		try
		{
			return Utility.readIntFromScript(new File(properties.getProperty("quagga.scripts.cpu")));
		}
		catch (Exception e)
		{
			throw new RSTestcaseException("quagga misconfiguration: " + e.getMessage());
		}

	}

	public int getMemory() throws RSTestcaseException
	{
		try
		{
			return Utility.readIntFromScript(new File(properties.getProperty("quagga.scripts.memory"))) / 1024;
		}
		catch (Exception e)
		{
			throw new RSTestcaseException("quagga misconfiguration: " + e.getMessage());
		}
	}

	@Override
	public String toString()
	{
		return "QuaggaManager";
	}

	/*
	 * here you see an example of how to use the QuaggaManager
	 * 
	 * 
	 * 
	 * 
	 * public static void main(String[] args) throws RSTestcaseException,
	 * InterruptedException, UnknownHostException {
	 * 
	 * Configuration conf = new Configuration(); conf.setAsn(1); QuaggaManager
	 * manager = new QuaggaManager(conf);
	 * 
	 * if(! manager.isRunning()) { manager.startRouteserver();
	 * Thread.sleep(1000); }
	 * 
	 * System.out.println("quagga is running: " + manager.isRunning()+ " pid: "
	 * + manager.getPid());
	 * 
	 * 
	 * Neighbor n = new Neighbor(); n.setAsn(2); n.setDescription("test host");
	 * n.setAddress(InetAddress.getByName("10.15.10.10")); conf.addNeighbor(n);
	 * manager.loadConfiguration(conf);
	 * 
	 * 
	 * System.out.println(manager.executeCommand("show version")); // //
	 * if(manager.isRunning()) // { // manager.stopRouteServer(); //
	 * Thread.sleep(1000); // } // System.out.println("quagga is running: " +
	 * manager.isRunning()); }
	 */

}
