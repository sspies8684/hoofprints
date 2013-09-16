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

package net.decix.tasks;

import java.io.File;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;

import net.decix.bgpstack.util.UtilityException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class SetIP6 extends Task
{
	private InetAddress ip;
	private String networkInterface;
	private String netbits;
	private int num = 0;

	private InetAddress generateInetAddressByOffset(InetAddress baseAddress, int offset) throws UtilityException, UnknownHostException
	{
		BigInteger addressAsBI = new BigInteger(baseAddress.getAddress());
		addressAsBI = addressAsBI.add(new BigInteger(Integer.toString(offset)));
		return InetAddress.getByAddress(addressAsBI.toByteArray());
	}

	@Override
	public void execute() throws BuildException
	{
		if (!new File("/sbin/ip").canExecute())
			throw new BuildException("ip command cannot be executed");
		if (!new File("/usr/bin/sudo").canExecute())
			throw new BuildException("sudo command cannot be executed");

		Runtime rt = Runtime.getRuntime();
		try
		{

			Process proc = null;
			if (num == 0)
			{
				System.out.println("adding " + ip.getHostAddress() + "/" + netbits + " to " + networkInterface);
				proc = rt.exec("/usr/bin/sudo /sbin/ip -6 a a " + ip.getHostAddress() + "/" + netbits + " dev " + networkInterface);
				if (proc.waitFor() != 0)
					throw new BuildException(proc.exitValue() + "");
			}
			else
			{
				System.out.println("adding " + num + " addresses beginning at " + ip.getHostAddress() + "/" + netbits + " to interface " + networkInterface);
				for (int i = 0; i < num; i++)
				{
					proc = rt.exec("/usr/bin/sudo /sbin/ip -6 a a " + generateInetAddressByOffset(ip, i).getHostAddress() + "/" + netbits + " dev " + networkInterface);
					if (proc.waitFor() != 0)
						throw new BuildException(proc.exitValue() + "");
				}
			}

		}
		catch (Exception e)
		{
			throw new BuildException("could not set ip: " + ip.getHostAddress() + " " + e.getMessage());
		}
	}

	public void setIp(String ip) throws UnknownHostException
	{
		this.ip = InetAddress.getByName(ip);
	}

	public void setNetworkInterface(String networkInterface)
	{
		this.networkInterface = networkInterface;
	}

	public void setNetbits(String netbits)
	{
		this.netbits = netbits;
	}

	public void setNum(int numOfAddresses)
	{
		this.num = numOfAddresses;
	}

}
