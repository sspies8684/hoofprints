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

package net.decix.bgpstack;

/**
 * Thrown if something concerning BGP communication is wrong
 * 
 * @author sspies
 * 
 */
public class BGPException extends Exception
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String message;

	/**
	 * Instantiate a new {@link BGPException} object
	 * 
	 * @param message the message describing the error
	 */
	public BGPException(String message)
	{
		this.message = message;
	}

	@Override
	/**
	 * Gets the message of this exception 
	 * @return the message of this exception
	 */
	public String getMessage()
	{
		return "BGPException: " + message;
	}
}
