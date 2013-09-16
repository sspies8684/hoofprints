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
import static net.decix.bgpstack.util.Utility.integerToTwoBytes;
import static net.decix.bgpstack.util.Utility.oneByteToInteger;
import static net.decix.bgpstack.util.Utility.twoBytesToInteger;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import net.decix.bgpstack.BGPConstants;
import net.decix.bgpstack.types.pathattributes.BGPPathAttributeSequence;
import net.decix.bgpstack.util.UtilityException;

/*
 +-----------------------------------------------------+
 |   Withdrawn Routes Length (2 octets)                |
 +-----------------------------------------------------+
 |   Withdrawn Routes (variable)                       |
 +-----------------------------------------------------+
 |   Total Path Attribute Length (2 octets)            |
 +-----------------------------------------------------+
 |   Path Attributes (variable)                        |
 +-----------------------------------------------------+
 |   Network Layer Reachability Information (variable) |
 +-----------------------------------------------------+

 */

/**
 * Represents a BGP Update message. See <a
 * href="http://tools.ietf.org/html/rfc4271">RFC4271</a>
 */
public class BGPUpdateMessage extends BGPMessage implements BGPConstants
{
	private Logger logger = Logger.getLogger("net.decix.bgpstack");
	private int withdrawRoutesLength;
	private List<IPv4Prefix> withdrawRoutes = new LinkedList<IPv4Prefix>();
	private int totalPathAttributeLength;
	private BGPPathAttributeSequence pathAttributeSequence;
	private int nlriLength;
	private List<IPv4Prefix> nlri = new LinkedList<IPv4Prefix>();

	protected BGPUpdateMessage()
	{
		super();
	}

	/**
	 * Instantiates a new {@link BGPUpdateMessage} object
	 * 
	 * @param withdrawRoutes a list of prefixes to withdraw with this Update
	 *            message (see {@link IPv4Prefix})
	 * @param announceRoutes a list of prefixes to announce with this Update
	 *            message (see {@link IPv4Prefix}). Often referred to as NLRI
	 *            (network layer reachability information)
	 * @param pathAttributeSequence a list of path attributes for the announced
	 *            routes
	 */
	public BGPUpdateMessage(List<IPv4Prefix> withdrawRoutes, List<IPv4Prefix> announceRoutes, BGPPathAttributeSequence pathAttributeSequence)
	{
		super();
		this.withdrawRoutes = withdrawRoutes;
		this.totalPathAttributeLength = pathAttributeSequence.getByteLength();
		this.pathAttributeSequence = pathAttributeSequence;
		this.nlri = announceRoutes;

		if (withdrawRoutes == null) this.withdrawRoutes = new LinkedList<IPv4Prefix>();
		for(IPv4Prefix prefix : this.withdrawRoutes)
			this.withdrawRoutesLength += prefix.getByteLength();
		
		if (announceRoutes == null) this.nlri = new LinkedList<IPv4Prefix>();

		setHeader(new BGPMessageHeader(MARKER, 19 + getByteLength(), BGP_MESSAGE_TYPE_UPDATE));
	}

	/**
	 * Parses a BGP Update message from raw data
	 * 
	 * @param rawData the raw data bytes of the BGP Update message
	 * @return a new instance of a {@link BGPUpdateMessage} object
	 * 
	 * @throws UtilityException if the bytes of the Update message are not
	 *             well-formed
	 */
	public static BGPUpdateMessage parse(byte[] rawData) throws UtilityException
	{
		return BGPUpdateMessage.parse(rawData, null);
	}
	
	public static BGPUpdateMessage parse(byte[] rawData, Object param) throws UtilityException
	{
		BGPUpdateMessage m = new BGPUpdateMessage();

		// parse withdrawnRoutesLength
		byte[] withdrawnRoutesLength = new byte[2];
		System.arraycopy(rawData, 0, withdrawnRoutesLength, 0, 2);
		m.setWithdrawnRoutesLength(twoBytesToInteger(withdrawnRoutesLength));

		// parse withdrawn Routes
		if (m.getWithdrawnRoutesLength() > 0)
		{
			byte[] withdrawnRoutes = new byte[m.getWithdrawnRoutesLength()];
			System.arraycopy(rawData, 2, withdrawnRoutes, 0, m.getWithdrawnRoutesLength());

			int offset = 0;
			while (offset < m.getWithdrawnRoutesLength())
			{
				int prefixLengthInBits = oneByteToInteger(withdrawnRoutes[offset]);
				int prefixLength = (int) Math.ceil(((double) prefixLengthInBits) / 8);
				byte[] prefix = new byte[prefixLength];
				System.arraycopy(withdrawnRoutes, offset + 1, prefix, 0, prefixLength);
				m.addWithdrawnRoute(new IPv4Prefix(prefix, prefixLengthInBits));
				offset += 1 + prefixLength;
			}
		}

		// parse totalPathAttributeLength
		byte[] totalPathAttributeLength = new byte[2];
		System.arraycopy(rawData, 2 + m.getWithdrawnRoutesLength(), totalPathAttributeLength, 0, 2);
		m.setTotalPathAttributeLength(twoBytesToInteger(totalPathAttributeLength));

		// parse all pathAttributes
		byte[] pathAttributes = new byte[m.getTotalPathAttributeLength()];
		System.arraycopy(rawData, 4 + m.getWithdrawnRoutesLength(), pathAttributes, 0, m.getTotalPathAttributeLength());

		m.setPathAttributeSequence(new BGPPathAttributeSequence(pathAttributes, param));

		// NLRI length =
		// UPDATE message Length
		// - 23
		// - Total Path Attributes Length
		// - Withdrawn Routes Length

		m.setNlriLength(rawData.length
		// 23 - headerLength(=19), we do not get the header here
				- 4 - m.getTotalPathAttributeLength() - m.getWithdrawnRoutesLength());

		// parse NLRI
		int offset = rawData.length - m.getNlriLength();
		while (offset < rawData.length)
		{
			int prefixLengthInBits = oneByteToInteger(rawData[offset]);
			int prefixLength = (int) Math.ceil(((double) prefixLengthInBits) / 8);
			byte[] prefix = new byte[prefixLength];
			System.arraycopy(rawData, offset + 1, prefix, 0, prefixLength);
			m.addNlri(new IPv4Prefix(prefix, prefixLengthInBits));
			offset += 1 + prefixLength;
		}

		return m;

	}

	/**
	 * Gets the length of unfeasible routes in bytes
	 * 
	 * @return the value of the BGP Update message withdrawn routes length field
	 */
	public int getWithdrawnRoutesLength()
	{
		return withdrawRoutesLength;
	}

	protected void setWithdrawnRoutesLength(int withdrawnRoutesLength)
	{
		this.withdrawRoutesLength = withdrawnRoutesLength;
	}

	/**
	 * Gets the unfeasible prefixes
	 * 
	 * @return a list of unfeasible prefixes
	 */
	public List<IPv4Prefix> getWithdrawnRoutes()
	{
		return withdrawRoutes;
	}

	/**
	 * Adds an unfeasible route at the end of the list of unfeasible routes
	 * 
	 * @param withdrawnRoute the unfeasible route to add
	 */
	public void addWithdrawnRoute(IPv4Prefix withdrawnRoute)
	{
		this.withdrawRoutes.add(withdrawnRoute);
	}

	/**
	 * Gets the length of the attributes in bytes
	 * 
	 * @return the value of the BGP Update message total path attribute length
	 */
	public int getTotalPathAttributeLength()
	{
		return totalPathAttributeLength;
	}

	protected void setTotalPathAttributeLength(int totalPathAttributeLength)
	{
		this.totalPathAttributeLength = totalPathAttributeLength;
	}

	/**
	 * Gets the path attributes
	 * 
	 * @return the path attributes
	 */
	public BGPPathAttributeSequence getPathAttributeSequence()
	{
		return pathAttributeSequence;
	}

	/**
	 * Gets the implicit length of the nlri, which is calculated as follows:
	 * NLRI length = UPDATE message Length - 23 - Total Path Attributes Length -
	 * Withdrawn Routes Length
	 * 
	 * @return the length of the nlri in bytes
	 */
	public int getNlriLength()
	{
		return nlriLength;
	}

	protected void setWithdrawnRoutes(List<IPv4Prefix> withdrawnRoutes)
	{
		this.withdrawRoutes = withdrawnRoutes;
	}

	protected void setPathAttributeSequence(BGPPathAttributeSequence pathAttributeSequence)
	{
		this.pathAttributeSequence = pathAttributeSequence;
	}

	protected void setNlriLength(int nlriLength)
	{
		this.nlriLength = nlriLength;
	}

	/**
	 * Gets the NLRI - network layer reachability information
	 * 
	 * @return a list of prefixes, carried by this BGP Update message NLRI (see
	 *         {@link IPv4Prefix})
	 */
	public List<IPv4Prefix> getNlri()
	{
		return nlri;
	}

	/**
	 * Gets the i-th prefix of this BGP Update message NLRI
	 * 
	 * @param index the index of the prefix, that is contained in the NLRI (see
	 *            {@link BGPUpdateMessage#getNlri()})
	 * @return
	 */
	public IPv4Prefix getNlriPrefix(int index)
	{
		return nlri.get(index);
	}

	/**
	 * Adds a prefix to the NLRI of this BGP Update message (see
	 * {@link BGPUpdateMessage#getNlri()})
	 * 
	 * @param prefix the prefix to add to the NLRI
	 */
	public void addNlri(IPv4Prefix prefix)
	{
		this.nlri.add(prefix);
	}

	public String toString()
	{
		String retVal = super.toString() + "\n";
		retVal += "withdrawn routes: \n";
		for (IPv4Prefix prefix : withdrawRoutes)
			retVal += "* " + prefix + "\n";
		retVal += pathAttributeSequence;
		retVal += "Route: " + nlri + "\n";

		return retVal;
	}

	@Override
	public byte[] toBytesTemplate() throws UtilityException
	{
		byte[] result = new byte[0];
		result = concatenateTwoByteArrays(result, integerToTwoBytes(withdrawRoutesLength));
		for (IPv4Prefix withdrawnRoute : withdrawRoutes)
			result = concatenateTwoByteArrays(result, withdrawnRoute.toBytes());
		result = concatenateTwoByteArrays(result, integerToTwoBytes(totalPathAttributeLength));
		result = concatenateTwoByteArrays(result, pathAttributeSequence.toBytes());

		for (IPv4Prefix prefix : nlri)
			result = concatenateTwoByteArrays(result, prefix.toBytes());

		return result;
	}

	public int getByteLength()
	{
		int total = 4;

		for (IPv4Prefix withdrawnRoute : withdrawRoutes)
			total += withdrawnRoute.getByteLength();

		total += pathAttributeSequence.getByteLength();

		for (IPv4Prefix prefix : nlri)
			total += prefix.getByteLength();

		return total;
	}

}
