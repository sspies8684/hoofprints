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

package net.decix.bgpstack.util;

/**
 * Classes implementing this interface are serializable and can be decoded to
 * the network during a BGP session
 * 
 * @author sspies
 * 
 */
public interface PacketSerializable
{
	/**
	 * Decodes the data structure to bytes representation
	 * 
	 * @return the data structure as bytes
	 * @throws UtilityException if something goes wrong with decoding (e.g.,
	 *             semantically incorrect data structure)
	 */
	public byte[] toBytes() throws UtilityException;

	/**
	 * Gets the length of bytes of this data structure. It must not depend on
	 * length-fields in the message.
	 * 
	 * @return the length in bytes of this data structure
	 */
	public int getByteLength();

}
