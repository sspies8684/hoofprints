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

package net.decix.bgpstack.types;

import net.decix.bgpstack.BGPConstants;
import net.decix.bgpstack.util.PacketSerializable;
import net.decix.bgpstack.util.UtilityException;
import static net.decix.bgpstack.util.Utility.*;

/*
 0                   1                   2                   3
 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 |                                                               |
 +                                                               +
 |                                                               |
 +                                                               +
 |                           Marker                              |
 +                                                               +
 |                                                               |
 +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 |          Length               |      Type     |
 +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+

 */

/**
 * Represents a header of a BGP message
 */
public class BGPMessageHeader implements BGPConstants, PacketSerializable
{

	private byte[] marker = new byte[16];
	private int length;
	private int type;

	protected BGPMessageHeader(byte[] marker, int length, int type)
	{
		super();
		this.marker = marker;
		this.length = length;
		this.type = type;
	}

	protected BGPMessageHeader()
	{
		
	}
	
	
	/**
	 * Parses the raw header data of a BGP message
	 * 
	 * @param rawData the data of the header as a byte-array
	 * @return a new BGP message header  
	 * @throws UtilityException if the header is malformed
	 */
	public static BGPMessageHeader parse(byte[] rawData) throws UtilityException
	{
		BGPMessageHeader p = new BGPMessageHeader();
		
		// Marker
		System.arraycopy(rawData, 0, p.getMarker(), 0, 16);

		// Length
		byte[] length = new byte[2];
		System.arraycopy(rawData, 16, length, 0, 2);
		p.setLength(twoBytesToInteger(length));
		
		// Type
		p.setType(oneByteToInteger(rawData[18]));
		
		return p;
	}

	/**
	 * Gets the 16 marker-bytes
	 * 
	 * @return 16 marker-bytes if message is well-formed
	 */
	public byte[] getMarker()
	{
		return marker;
	}

	/**
	 * Gets the complete length of the BGP message as defined in the BGP header
	 * 
	 * @return the length of the complete BGP message
	 */
	public int getLength()
	{
		return length;
	}

	/**
	 * Gets the type-code of the BGP message
	 *  
	 * @return the type-code of the BGP message
	 */
	public int getType()
	{
		return type;
	}

	protected void setMarker(byte[] marker)
	{
		this.marker = marker;
	}

	protected void setLength(int length)
	{
		this.length = length;
	}

	protected void setType(int type)
	{
		this.type = type;
	}
	
	
	@Override
	public String toString()
	{
		return "length: " + length + " bytes, type: " + type + " ("+BGP_MESSAGE_TYPES.values()[type-1]+")"; 
	}

	public byte[] toBytes() throws UtilityException
	{
		byte[] result = new byte[19];
		result = concatenateTwoByteArrays(marker, integerToTwoBytes(length));
		result = concatenateTwoByteArrays(result, new byte[] { integerToOneByte(type) });
		return result;
	}
	
	/**
	 * Gets the length of a BGP message header
	 * 
	 * @return 19
	 */
	public int getByteLength()
	{
		return 19;
	}
}
