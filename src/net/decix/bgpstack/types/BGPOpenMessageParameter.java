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

import net.decix.bgpstack.types.capabilities.BGPCapability;
import net.decix.bgpstack.util.PacketSerializable;
import net.decix.bgpstack.util.UtilityException;
import static net.decix.bgpstack.util.Utility.*;

/**
 * Represents a BGP Open message parameter
 * 
 * @author sspies
 * 
 */
public class BGPOpenMessageParameter implements PacketSerializable
{
	private int type;
	private int length;
	private BGPCapability value;

	/**
	 * Instantiates a new {@link BGPOpenMessageParameter} object
	 * 
	 * @param type the type-code of this parameter
	 * @param length the length of this parameter
	 * @param value the value of this parameter
	 * @throws UtilityException if a capability could not be decoded
	 */
	public BGPOpenMessageParameter(int type, int length, byte[] value) throws UtilityException
	{
		super();
		this.type = type;
		this.length = length;
		this.value = BGPCapability.parse(value);
	}

	public BGPOpenMessageParameter(BGPCapability capability)
	{
		this.type = BGP_MESSAGE_OPEN_ATTRIBUTE_TYPE_CAPABILITY;
		this.value = capability;
	}

	/**
	 * Gets the type-code
	 * 
	 * @return the type-code value
	 */
	public int getType()
	{
		return type;
	}

	/**
	 * Gets the length of this parameter
	 * 
	 * @return the length field value of this parameter
	 */
	public int getLength()
	{
		return length;
	}

	/**
	 * Gets the value of this parameter
	 * 
	 * @return the value of this parameter as raw byte-data
	 */
	public BGPCapability getValue()
	{
		return value;
	}

	protected void setType(int type)
	{
		this.type = type;
	}

	protected void setLength(int length)
	{
		this.length = length;
	}

	protected void setValue(BGPCapability value)
	{
		this.value = value;
	}

	@Override
	public String toString()
	{
		return " type: " + type + ", length: " + length;
	}

	public byte[] toBytes() throws UtilityException
	{
		byte[] result = { integerToOneByte(type), integerToOneByte(getByteLength() - 2) };
		result = concatenateTwoByteArrays(result, value.toBytes());
		return result;
	}

	public int getByteLength()
	{
		return 2 + value.getByteLength();
	}

}
