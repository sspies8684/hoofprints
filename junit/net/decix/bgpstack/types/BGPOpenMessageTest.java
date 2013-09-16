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
import static org.junit.Assert.*;

import java.net.InetAddress;
import java.net.UnknownHostException;

import net.decix.bgpstack.BGPConstants;
import net.decix.bgpstack.util.UtilityException;

import org.junit.Before;
import org.junit.Test;

public class BGPOpenMessageTest implements BGPConstants
{

	private byte[] wellFormed = hexStringToByteArray("04fe0900b4c0a8000f10020e4006005a000101004104121d0064");
	private BGPOpenMessage omFromWellFormed;
	
	@Before
	public void setUp() throws Exception
	{
		omFromWellFormed = omFromWellFormed.parse(wellFormed);
		
	}

	@Test
	public void testParseContent() throws UtilityException
	{
		omFromWellFormed.parse(wellFormed);
	}

	@Test
	public void testGetVersion()
	{
		assertEquals(4, omFromWellFormed.getVersion());
	}

	@Test
	public void testGetAsNumber()
	{
		assertEquals(65033, omFromWellFormed.getAsNumber());
	}

	@Test
	public void testGetHoldTime()
	{
		assertEquals(180, omFromWellFormed.getHoldTime());
	}

	@Test
	public void testGetIdentifier() throws UnknownHostException
	{
		assertEquals(InetAddress.getByName("192.168.0.15"), omFromWellFormed.getIdentifier());
	}

	@Test
	public void testGetOptParamLen()
	{
		assertEquals(16, omFromWellFormed.getOptParamLen());
	}
	
	@Test
	public void testOptionalParameters()
	{
		assertEquals(1, omFromWellFormed.getParameters().size());
		for(BGPOpenMessageParameter param: omFromWellFormed.getParameters())
		{
			assertEquals(BGP_MESSAGE_OPEN_ATTRIBUTE_TYPE_CAPABILITY, param.getType());
			assertEquals(14, param.getLength());
//			assertEquals(param.getLength(), param.getValue().length);
		}
	}

}
