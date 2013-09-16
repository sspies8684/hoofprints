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
import static net.decix.bgpstack.util.Utility.integerToTwoBytes;
import static net.decix.bgpstack.util.Utility.oneByteToInteger;
import static net.decix.bgpstack.util.Utility.twoBytesToInteger;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;

import net.decix.bgpstack.BGPConstants;
import net.decix.bgpstack.util.UtilityException;

/*
 0                   1                   2                   3
 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 +-+-+-+-+-+-+-+-+
 |    Version    |
 +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 |     My Autonomous System      |
 +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 |           Hold Time           |
 +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 |                         BGP Identifier                        |
 +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 | Opt Parm Len  |
 +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 |                                                               |
 |             Optional Parameters (variable)                    |
 |                                                               |
 +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+


 */

/**
 * Represents a BGP Open Message. see <a
 * href="http://tools.ietf.org/html/rfc4271">RFC4271</a>
 * 
 * @author sspies
 * 
 */
public class BGPOpenMessage extends BGPMessage implements BGPConstants
{
	private int version;
	private int asNumber;
	private int holdTime;
	private InetAddress identifier;
	private int optParamLen;
	List<BGPOpenMessageParameter> parameters;

	/**
	 * Instantiates a new {@link BGPOpenMessage} object
	 * 
	 * @param asNumber ASN - autonomous system number, TODO 4-byte ASN
	 * @param holdTime hold timer in seconds
	 * @param identifier your IPv4 BGP identifier
	 * @param parameters a list of message parameters, see
	 *            {@link BGPOpenMessageParameter}
	 */
	public BGPOpenMessage(int asNumber, int holdTime, Inet4Address identifier, List<BGPOpenMessageParameter> parameters)
	{
		super();
		this.version = 4;
		this.asNumber = asNumber;
		this.holdTime = holdTime;
		this.identifier = identifier;
		this.parameters = parameters;
		this.optParamLen = 0;

		for (BGPOpenMessageParameter param : parameters)
			this.optParamLen += param.getByteLength();

		setHeader(new BGPMessageHeader(MARKER, 19 + getByteLength(), BGP_MESSAGE_TYPE_OPEN));

	}

	/*
	 * for testing purpose
	 */
	protected BGPOpenMessage()
	{
		parameters = new LinkedList<BGPOpenMessageParameter>();
	}

	/**
	 * Parses a BGP Open message from raw byte data as defined in RFC4271
	 * 
	 * @param rawData the raw data as an array of bytes
	 * @return a new {@link BGPOpenMessage} object that represents the
	 *         information from the BGP message
	 * @throws UtilityException if the raw data is not well-formed
	 */
	public static BGPOpenMessage parse(byte[] rawData) throws UtilityException
	{
		BGPOpenMessage m = new BGPOpenMessage();

		// Version
		m.setVersion(oneByteToInteger(rawData[0]));

		// ASN
		byte[] asn = new byte[2];
		System.arraycopy(rawData, 1, asn, 0, 2);
		m.setAsNumber(twoBytesToInteger(asn));

		// holdTime
		byte[] holdTime = new byte[2];
		System.arraycopy(rawData, 3, holdTime, 0, 2);
		m.setHoldTime(twoBytesToInteger(holdTime));

		// identifier
		byte[] identifier = new byte[4];
		System.arraycopy(rawData, 5, identifier, 0, 4);
		try
		{
			m.setIdentifier(InetAddress.getByAddress(identifier));
		}
		catch (UnknownHostException e)
		{
			//
		}

		// optParamLen
		m.setOptParamLen(oneByteToInteger(rawData[9]));

		// parse optional parameters
		int typePosition = 10;
		while (typePosition - 10 < m.getOptParamLen())
		{
			int type = oneByteToInteger(rawData[typePosition]);
			int paramValueLength = oneByteToInteger(rawData[typePosition + 1]);
			byte[] paramValue = new byte[paramValueLength];
			System.arraycopy(rawData, typePosition + 2, paramValue, 0, paramValueLength);
			m.getParameters().add(new BGPOpenMessageParameter(type, paramValueLength, paramValue));
			typePosition += 2 + paramValueLength;
		}
		return m;
	}

	/**
	 * Gets the version of this Open message
	 * 
	 * @return the value of the version field
	 */
	public int getVersion()
	{
		return version;
	}

	/**
	 * Gets the ASN - autonomous system number of this Open message
	 * 
	 * @return the value of the autonomous system number field
	 */
	public int getAsNumber()
	{
		return asNumber;
	}

	/**
	 * Gets the hold timer
	 * 
	 * @return the value of the hold timer field
	 */
	public int getHoldTime()
	{
		return holdTime;
	}

	/**
	 * Gets the BGP IPv4 identifier
	 * 
	 * @return the value of the BGP IPv4 identifier field
	 */
	public InetAddress getIdentifier()
	{
		return identifier;
	}

	/**
	 * Gets the optional parameters length
	 * 
	 * @return the value of the BGP Open OptParamLen field
	 */
	public int getOptParamLen()
	{
		return optParamLen;
	}

	/**
	 * Gets the BGP Open message parameters (see {@link BGPOpenMessageParameter}
	 * )
	 * 
	 * @return a list of BGP Open message parameters
	 */
	public List<BGPOpenMessageParameter> getParameters()
	{
		return parameters;
	}
	
	public void addParameter(BGPOpenMessageParameter parameter)
	{
		this.parameters.add(parameter);
	}

	protected void setVersion(int version)
	{
		this.version = version;
	}

	protected void setAsNumber(int asNumber)
	{
		this.asNumber = asNumber;
	}

	protected void setHoldTime(int holdTime)
	{
		this.holdTime = holdTime;
	}

	protected void setIdentifier(InetAddress identifier)
	{
		this.identifier = identifier;
	}

	protected void setOptParamLen(int optParamLen)
	{
		this.optParamLen = optParamLen;
	}

	@Override
	public String toString()
	{
		String retVal = super.toString() + "\n";
		retVal += "Version: " + version + "\n";
		retVal += "AS Number: " + asNumber + "\n";
		retVal += "Hold time: " + holdTime + "\n";
		retVal += "Identifier: " + identifier.getHostAddress() + "\n";
		retVal += "parameters (" + parameters.size() + "):\n";
		for (BGPOpenMessageParameter param : parameters)
			retVal += param + "\n";
		return retVal;
	}

	@Override
	public byte[] toBytesTemplate() throws UtilityException
	{
		byte[] result = {};
		result = concatenateTwoByteArrays(result, new byte[] { integerToOneByte(version) });
		result = concatenateTwoByteArrays(result, integerToTwoBytes(asNumber));
		result = concatenateTwoByteArrays(result, integerToTwoBytes(holdTime));
		result = concatenateTwoByteArrays(result, identifier.getAddress());
		result = concatenateTwoByteArrays(result, new byte[] { integerToOneByte(optParamLen) });
		for (BGPOpenMessageParameter parameter : parameters)
			result = concatenateTwoByteArrays(result, parameter.toBytes());
		return result;
	}

	public int getByteLength()
	{
		return 10 + optParamLen;
	}

}
