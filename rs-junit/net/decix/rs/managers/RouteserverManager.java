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

import java.net.InetAddress;

import net.decix.bgpstack.BGPConstants;
import net.decix.bgpstack.util.AntProperties;
import net.decix.rs.RSTestcaseException;
import net.decix.rs.conf.Configuration;

/**
 * Represents a RS manager
 * 
 * @author sspies
 *
 */
public abstract class RouteserverManager implements BGPConstants
{
	protected final String STANDARDPASSWORD = "showmeyourhoofs";	
	protected int listenPort = 1790;
	
	protected InetAddress listenAddress;
	protected InetAddress listen6Address;
	protected Configuration startupConfiguration;

	// this is what implementors have to do
	// Extension point: 1
	/**
	 * Read the properties from configuration file
	 */
	public abstract void readProperties() throws RSTestcaseException;
	
	/**
	 * Read the properties from configuration file
	 */
	public abstract AntProperties getProperties() throws RSTestcaseException;
	
	/**
	 * Start the route server
	 * @throws RSTestcaseException if the route server could not be started
	 */
	public abstract void startRouteserver() throws RSTestcaseException;
	
	/**
	 * Stop the route server
	 * @throws RSTestcaseException if the route server could not be stopped
	 */
	public abstract void stopRouteServer() throws RSTestcaseException;
	
	/**
	 * Load the route server configuration into the route server
	 * @param config the route server configuration
	 * @throws RSTestcaseException if the configuration could not be loaded
	 */
	public abstract void loadConfiguration(Configuration config) throws RSTestcaseException;
	
	/**
	 * Gets the running state of the route server
	 * @return true if the route server is running. false if not.
	 * @throws RSTestcaseException if the state could not be checked
	 */
	public abstract boolean isRunning() throws RSTestcaseException;
	
	/**
	 * Gets the CPU consumption of the route server
	 * @return the CPU consumption of the route server
	 * @throws RSTestcaseException if the CPU consumption information is not available
	 */
	public abstract int getCPU() throws RSTestcaseException;
	
	/**
	 * Gets the memory consumption of the route server in megabytes
	 * @return the memory consumption of the route server in megabytes
	 * @throws RSTestcaseException if the memory consumption information is not available
	 */
	public abstract int getMemory() throws RSTestcaseException;
	
	
	/**
	 * Instantiates a new {@link RouteserverManager} object.
	 * @param startupConfiguration a startup configuration for the route server
	 * @throws RSTestcaseException if the route server could not read the properties 
	 */
	public RouteserverManager(Configuration startupConfiguration) throws RSTestcaseException
	{
		this.startupConfiguration = startupConfiguration;

		readProperties();
	}
	
	/**
	 * Gets the TCP listen port of the route server
	 * @return the listen port
	 */
	public int getListenPort()
	{
		return listenPort;
	}

	/**
	 * Gets the IP listen address of the route server
	 * @return
	 */
	public InetAddress getListenAddress()
	{
		return listenAddress;
	}

	public InetAddress getListen6Address()
	{
		return listen6Address;
	}
	
	
	
}
