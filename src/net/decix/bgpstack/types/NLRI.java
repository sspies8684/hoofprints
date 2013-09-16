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

import static net.decix.bgpstack.util.Utility.concatenateTwoByteArrays;
import static net.decix.bgpstack.util.Utility.integerToOneByte;

import java.net.InetAddress;

import net.decix.bgpstack.util.PacketSerializable;
import net.decix.bgpstack.util.UtilityException;

public abstract class NLRI implements PacketSerializable
{
	protected InetAddress address;
	protected int prefixLength;
	
	
	/**
	 * Instantiates a new {@link NLRIPv4Prefix} object
	 * 
	 * @param address the base-address of this prefix
	 * @param prefixLength the length of bits, that mask the network-part of
	 *            this prefix
	 */
	public NLRI(InetAddress address, int prefixLength)
	{
		this.address = address;
		this.prefixLength = prefixLength;
	}
	
	

	/**
	 * Instantiates a new {@link NLRIPv4Prefix} object
	 * 
	 * @param address the base-address as a byte-array
	 * @param prefixLengththe length of bits, that mask the network-part of this
	 *            prefix
	 */
	public NLRI(byte[] address, int prefixLength)
	{
		this.prefixLength = prefixLength;
		constructInetAddressFromBytes(address);
	}
	
	
	protected abstract void constructInetAddressFromBytes(byte[] address);
	public abstract int getAfi();
	public abstract int getSafi();
	
	
	
	
	/**
	 * Gets the base address of this prefix (e.g. if this prefix would be
	 * 192.168.19.0/24, the base address is 192.168.19.0). IPv6 is also possible.
	 * 
	 * @return the base address of this prefix
	 */
	public InetAddress getAddress()
	{
		return address;
	}

	/**
	 * Gets the prefix length in bits of this prefix (e.g. if this prefix would
	 * be 192.168.19.0/24, the prefix length is 24). IPv6 is also possible.
	 * 
	 * @return the prefix length in bits of this prefix
	 */
	public int getPrefixLength()
	{
		return prefixLength;
	}
	
	protected int prefixToByteLength()
	{
		return (int) Math.ceil(((double) prefixLength) / 8);
	}
	
	public int getByteLength()
	{
		return 1 + prefixToByteLength();
	}
	

	protected byte[] addressToPrefix()
	{
		int byteLength = prefixToByteLength();
		byte[] prefix = new byte[byteLength];

		System.arraycopy(address.getAddress(), 0, prefix, 0, byteLength);

		return prefix;
	}
	
	public byte[] toBytes() throws UtilityException
	{
		byte[] result = { integerToOneByte(prefixLength) };
		result = concatenateTwoByteArrays(result, addressToPrefix());
		return result;
	}
	
	@Override
	public int hashCode()
	{
		return toString().hashCode();
	}
	
	@Override
	public String toString()
	{
		return address.getHostAddress() + "/" + getPrefixLength();
	}

}
