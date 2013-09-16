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

package net.decix.bgpstack.types.pathattributes;

import net.decix.bgpstack.util.PacketSerializable;
import net.decix.bgpstack.util.UtilityException;

/**
 * Interface that defines the methods of a path attribute content part
 * 
 * @author sspies
 * 
 */
public interface BGPPathAttributeContent extends PacketSerializable
{
	/**
	 * Parses an path attribute content part
	 * 
	 * @param data the path attribute content as encoded in UPDATE message
	 * @throws UtilityException if the bytes are malformed
	 */
	public void parse(byte[] data, Object param) throws UtilityException;
	public void parse(byte[] data) throws UtilityException;

	/**
	 * Gets the type code of the path attribute content
	 * 
	 * @return the type code of the path attribute
	 */
	public int getTypeCode();

}
