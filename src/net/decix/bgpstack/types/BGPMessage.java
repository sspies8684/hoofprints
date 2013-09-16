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

/**
 * Contains all classes that cope with BGP types
 * @see BGPConstants 
 */
package net.decix.bgpstack.types;

import static net.decix.bgpstack.util.Utility.concatenateTwoByteArrays;
import net.decix.bgpstack.BGPConstants;
import net.decix.bgpstack.util.PacketSerializable;
import net.decix.bgpstack.util.UtilityException;

/**
 * Superclass for all BGP messages. see <a
 * href="http://tools.ietf.org/html/rfc4271">RFC4271</a>
 * 
 * @author sspies
 * 
 */
public abstract class BGPMessage implements PacketSerializable, BGPConstants
{
	private BGPMessageHeader header;

	protected BGPMessage()
	{

	}

	/**
	 * Instantiates a new BGPMessage with the provided header
	 * 
	 * @param header the header of this BGP message
	 */
	public BGPMessage(BGPMessageHeader header)
	{
		super();
		this.header = header;
	}

	/**
	 * Gets the header of this BGP message
	 * 
	 * @return the header of this BGP message
	 */
	public BGPMessageHeader getHeader()
	{
		return header;
	}

	protected void setHeader(BGPMessageHeader header)
	{
		this.header = header;
	}

	@Override
	public String toString()
	{
		return "header: " + header;
	}

	public byte[] toBytes() throws UtilityException
	{
		if(getByteLength() > BGP_MAX_MESSAGE_SIZE)
			throw new UtilityException("BGP_MAX_MESSAGE_SIZE exceeded: "+ getByteLength());
		
		return concatenateTwoByteArrays(header.toBytes(), toBytesTemplate());
	}

	/**
	 * Decodes the BGP message (excluding the header) to an byte-array
	 * 
	 * @return the BGP message content part
	 * @throws UtilityException if the data structure is semantically incorrect
	 */
	public abstract byte[] toBytesTemplate() throws UtilityException;

}
