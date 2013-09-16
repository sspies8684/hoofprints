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

import static net.decix.bgpstack.util.Utility.*;

import java.util.LinkedList;

import net.decix.bgpstack.BGPConstants;
import net.decix.bgpstack.util.PacketSerializable;
import net.decix.bgpstack.util.UtilityException;

/**
 * Represents a sequence of BGP path attributes<br />
 * 
 * see <a href="http://tools.ietf.org/html/rfc4271">RFC4271</a>
 * 
 * @author sspies
 * 
 */
public class BGPPathAttributeSequence extends LinkedList<BGPPathAttribute> implements BGPConstants, PacketSerializable
{
	private static final long serialVersionUID = -540121113182685741L;

	/**
	 * Instantiates a new BGPPathAttributeSequence that is initially empty
	 */
	public BGPPathAttributeSequence()
	{
	}

	/**
	 * Instantiates a new BGPPathAttributeSequence from raw byte data
	 * 
	 * @param pathAttributes the raw path attribute sequence as an array of
	 *            bytes
	 * @throws UtilityException if the path attribute sequence in the BGP UPDATE
	 *             message is malformed
	 */
	public BGPPathAttributeSequence(byte[] pathAttributes) throws UtilityException
	{
		this(pathAttributes, null);
	}

	public BGPPathAttributeSequence(byte[] pathAttributes, Object param) throws UtilityException
	{
		parse(pathAttributes, param);
	}

	protected void parse(byte[] data, Object param) throws UtilityException
	{
		try
		{
			while (data.length > 0)
			{

				// instantiate PA and add to sequence
				BGPPathAttribute a = new BGPPathAttribute(data, param);
				add(a);

				// truncate attribute from bytearray
				data = truncateLeft(data, a.getByteLength());
			}
		}
		catch (ArrayIndexOutOfBoundsException e)
		{
			e.printStackTrace();
			System.err.println(this);
		}
	}

	protected void parse(byte[] data) throws UtilityException
	{
		parse(data, null);
	}

	@Override
	public String toString()
	{
		String retVal = "*Path Attributes:\n";
		for (BGPPathAttribute attribute : this)
			retVal += attribute + "\n";
		return retVal;
	}

	public byte[] toBytes() throws UtilityException
	{
		byte[] result = {};
		for (BGPPathAttribute attribute : this)
			result = concatenateTwoByteArrays(result, attribute.toBytes());

		return result;
	}

	public int getByteLength()
	{
		int length = 0;

		for (BGPPathAttribute attribute : this)
			length += attribute.getByteLength();
		return length;
	}

	public BGPPathAttribute getByTypeCode(int typeCode)
	{
		for (BGPPathAttribute attribute : this)
			if (attribute.getTypeCode() == typeCode)
				return attribute;
		return null;
	}

}
