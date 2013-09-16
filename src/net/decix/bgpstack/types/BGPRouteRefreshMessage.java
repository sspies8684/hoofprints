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

import static net.decix.bgpstack.util.Utility.*;
import net.decix.bgpstack.util.UtilityException;

/* 
  Type: 5 - ROUTE-REFRESH

  Message Format: One <AFI, SAFI> encoded as

          0       7      15      23      31
          +-------+-------+-------+-------+
          |      AFI      | Res.  | SAFI  |
          +-------+-------+-------+-------+

  The meaning, use and encoding of this <AFI, SAFI> field is the
  same as defined in [BGP-MP, sect. 7]. More specifically,

       AFI  - Address Family Identifier (16 bit).

       Res. - Reserved (8 bit) field. Should be set to 0 by the
              sender and ignored by the receiver.

       SAFI - Subsequent Address Family Identifier (8 bit).
 
 */
public class BGPRouteRefreshMessage extends BGPMessage
{
	private int afi;
	private int safi;
	
	public BGPRouteRefreshMessage(int afi, int safi)
	{
		this.afi = afi;
		this.safi = safi;
		
		setHeader(new BGPMessageHeader(MARKER, 19 + getByteLength(), BGP_MESSAGE_TYPE_ROUTE_REFRESH));
	}
	
	protected BGPRouteRefreshMessage()
	{
	}

	@Override
	public byte[] toBytesTemplate() throws UtilityException
	{
		byte[] result = {};
		result = concatenateTwoByteArrays(result, integerToTwoBytes(afi));
		result = concatenateTwoByteArrays(result, new byte[] { integerToOneByte(0), integerToOneByte(safi) });
		return result;
	}

	public int getByteLength()
	{
		return 4;
	}

	public static BGPMessage parse(byte[] contentData) throws UtilityException
	{
		BGPRouteRefreshMessage m = new BGPRouteRefreshMessage();
		
		byte[] afi = new byte[2];
		System.arraycopy(contentData, 0, afi, 0, 2);
		m.setAfi(twoBytesToInteger(afi));		
		m.setSafi(oneByteToInteger(contentData[3]));
		
		return m;
	}

	/**
	 * @return the afi
	 */
	public int getAfi()
	{
		return afi;
	}

	/**
	 * @param afi the afi to set
	 */
	protected void setAfi(int afi)
	{
		this.afi = afi;
	}

	/**
	 * @return the safi
	 */
	public int getSafi()
	{
		return safi;
	}

	/**
	 * @param safi the safi to set
	 */
	protected void setSafi(int safi)
	{
		this.safi = safi;
	}

}
