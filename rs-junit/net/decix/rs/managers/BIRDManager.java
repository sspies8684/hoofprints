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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;

import net.decix.bgpstack.util.AntProperties;
import net.decix.bgpstack.util.UtilityException;
import static net.decix.bgpstack.util.Utility.*;
import net.decix.rs.RSTestcaseException;
import net.decix.rs.conf.Configuration;
import net.decix.rs.conf.Neighbor;

/**
 * {@link RouteserverManager} implementation for BIRD
 * 
 * @author sspies
 * 
 */
public class BIRDManager extends RouteserverManager
{
	private AntProperties properties;
	private File executable;

	public BIRDManager(Configuration startupConfiguration) throws RSTestcaseException
	{
		super(startupConfiguration);
		try
		{
			writeStartupConfiguration();
		}
		catch (IOException e)
		{
			throw new RSTestcaseException("Could not write startup-configuration for BIRD: " + e.getMessage());
		}
	}

	private void writeStartupConfiguration() throws IOException
	{
		File startupConfigFile = new File(properties.getProperty("bird.conf.standard"));
		FileWriter fw = new FileWriter(startupConfigFile);
		fw.write("router id " + properties.getProperty("bird.ip") + ";\n");
		fw.close();
	}

	protected String executeCommand(String cmd) throws RSTestcaseException
	{
		InputStream is;
		OutputStream os;
		byte[] readBuffer = new byte[1024];
		StringBuffer buffer = new StringBuffer();

		Runtime rt = Runtime.getRuntime();

		try
		{
			Process proc = rt.exec(properties.getProperty("bird.binary.remote"));
			
			is = proc.getInputStream();
			os = proc.getOutputStream();

			os.write((cmd + "\nquit\n").getBytes());
			os.flush();
			for (;;)
			{
				int bytesRead;
				if ((bytesRead = is.read(readBuffer)) == -1) break;
				buffer.append(new String(readBuffer, 0, bytesRead));
			}

		}
		catch (IOException e)
		{
			throw new RSTestcaseException("cannot execute birdc: " + e.getMessage());
		}
		return buffer.toString();
	}

	@Override
	public void readProperties() throws RSTestcaseException
	{
		properties = new AntProperties();
		try
		{
			properties.load(new File("conf/test.properties"));
			properties.load(new File("conf/bird.properties"));

			listenAddress = InetAddress.getByName(properties.getProperty("bird.ip"));
			executable = new File(properties.getProperty("bird.binary"));
		}
		catch (Exception e)
		{
			throw new RSTestcaseException("BIRD misconfiguration: " + e.getMessage());
		}

	}

	@Override
	public void loadConfiguration(Configuration config) throws RSTestcaseException
	{
		File startupConfigFile = new File(properties.getProperty("bird.conf.standard"));
		FileWriter fw;
		try
		{
			fw = new FileWriter(startupConfigFile);
			fw.write("router id " + properties.getProperty("bird.ip") + ";\n");
			fw.write("listen bgp address 10.15.2.2 port " + listenPort + ";\n");
			fw.write("protocol device { scan time 10; }\n");
//			fw.write("protocol pipe { import all; export all;} \n");
			//fw.write("debug protocols all;\n");
			for (Neighbor n : config.getNeighbors())
			{
				fw.write("protocol bgp {\n");
				fw.write("export filter { accept; };\n");
				fw.write("import all;\n");
				fw.write("local as " + config.getAsn() + ";\n");
				fw.write("neighbor " + n.getAddress().getHostAddress() + " as " + n.getAsn() + ";\n");
				fw.write("source address " + properties.getProperty("bird.ip") + ";\n");
				fw.write("}\n");
			}

			fw.close();
		}
		catch (IOException e)
		{
			throw new RSTestcaseException("could not load config: " + e.getMessage());
		}
		executeCommand("configure");

	}

	@Override
	public void startRouteserver() throws RSTestcaseException
	{
		if (isRunning()) throw new RSTestcaseException("BIRD is already running");

		Runtime rt = Runtime.getRuntime();

		try
		{
			Process proc = rt.exec("start-stop-daemon --start -m -p " + properties.getProperty("bird.pid.file") + " --exec "+ executable.getAbsolutePath() +" ");

			if (proc.waitFor() != 0) throw new RSTestcaseException("could not start BIRD: exit-code " + proc.exitValue());

		}
		catch (IOException e)
		{
			throw new RSTestcaseException("cannot execute BIRD: " + e.getMessage());
		}
		catch (InterruptedException e)
		{
			throw new RSTestcaseException("cannot execute BIRD: " + e.getMessage());
		}
	}

	@Override
	public void stopRouteServer() throws RSTestcaseException
	{
		executeCommand("down");

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

	@Override
	public boolean isRunning() throws RSTestcaseException
	{
		
		try
		{
			int pid = readIntFromFile(new File(properties.getProperty("bird.pid.file"))) + 1;
			return isProcessRunning(properties.getProperty("bird.binary.name"), pid);
		}
		catch (Exception e)
		{
			throw new RSTestcaseException("Don't know if bird is running: " + e.toString());
		}
	}

	@Override
	public int getCPU() throws RSTestcaseException
	{
		try
		{
			return readIntFromScript(new File(properties.getProperty("bird.scripts.cpu")));
		}
		catch(Exception e)
		{
			throw new RSTestcaseException("bird misconfiguration: " + e.getMessage());
		}
	}

	@Override
	public AntProperties getProperties() throws RSTestcaseException
	{
		return properties;
	}

	@Override
	public int getMemory() throws RSTestcaseException
	{
		throw new RSTestcaseException("not implemented");
	}

}
