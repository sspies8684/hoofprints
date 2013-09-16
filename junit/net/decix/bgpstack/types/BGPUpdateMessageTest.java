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

import static org.junit.Assert.assertEquals;

import java.net.InetAddress;
import java.net.UnknownHostException;

import net.decix.bgpstack.util.UtilityException;

import org.junit.Before;
import org.junit.Test;
import static net.decix.bgpstack.util.Utility.*;
public class BGPUpdateMessageTest
{
	private byte[] wellFormed = hexStringToByteArray("001010c0a810c0a817c0a9990017c0a99900" +
				/* path attributes */ "00484001010040020a010201f401f40201febb400304c0a8000f40050400000064400600c00706febac0a8000ac0080cfebf000103160004015400fa800904c0a8000f800a04c0a800fa" +
				/* NLRI */ "10ac10");
	
	private byte[] wellFormed2 = hexStringToByteArray("0000" + // no unfeasible routes
			"0019400101004002040201fe1f400304c0a8941880040400000000" +
			"18c0a863");
	
	private byte[] wellFormed3 = hexStringToByteArray("0012" + // 18 bytes unfeasible routes
			"1c7f2994d00e8fcc1d8fcd2cd81d934a8ed00000");
	
	private byte[] wellFormed4 = hexStringToByteArray("0000" +
			"001540010100500200060202000175304003040a0f02010f2f000ccf50");
	
	
	private BGPUpdateMessage umFromWellFormed, umFromWellFormed2, umFromWellFormed3, umFromWellFormed4;
	
	@Before
	public void setUp() throws Exception
	{
		umFromWellFormed = BGPUpdateMessage.parse(wellFormed);
		umFromWellFormed2 = BGPUpdateMessage.parse(wellFormed2);
		umFromWellFormed3 = BGPUpdateMessage.parse(wellFormed3);
		umFromWellFormed4 = BGPUpdateMessage.parse(wellFormed4);
	}

	@Test
	public void testParseContent() throws UtilityException
	{
		BGPUpdateMessage.parse(wellFormed);
		BGPUpdateMessage.parse(wellFormed2);
		BGPUpdateMessage.parse(wellFormed3);
		BGPUpdateMessage.parse(wellFormed4);
	}

	@Test
	public void testGetWithdrawnRoutesLength()
	{
		assertEquals(16, umFromWellFormed.getWithdrawnRoutesLength());
	}

	@Test
	public void testGetWithdrawnRoutes() throws UnknownHostException
	{
		assertEquals(6, umFromWellFormed.getWithdrawnRoutes().size());

		// 192.168.0.0/16
		InetAddress addr1 = InetAddress.getByName("192.168.0.0");
		assertEquals(addr1, umFromWellFormed.getWithdrawnRoutes().get(0).getAddress());
		assertEquals(16, umFromWellFormed.getWithdrawnRoutes().get(0).getPrefixLength());
		// again
		assertEquals(addr1, umFromWellFormed.getWithdrawnRoutes().get(1).getAddress());
		assertEquals(16, umFromWellFormed.getWithdrawnRoutes().get(1).getPrefixLength());
		
		// 192.169.153.0/23
		InetAddress addr2 = InetAddress.getByName("192.169.153.0");
		assertEquals(addr2, umFromWellFormed.getWithdrawnRoutes().get(2).getAddress());
		assertEquals(23, umFromWellFormed.getWithdrawnRoutes().get(2).getPrefixLength());
		
		// 0.0.0.0/0
		InetAddress addr3 = InetAddress.getByName("0.0.0.0");
		assertEquals(addr3, umFromWellFormed.getWithdrawnRoutes().get(3).getAddress());
		assertEquals(0, umFromWellFormed.getWithdrawnRoutes().get(3).getPrefixLength());
		
		// 192.169.153.0/23
		assertEquals(addr2, umFromWellFormed.getWithdrawnRoutes().get(4).getAddress());
		assertEquals(23, umFromWellFormed.getWithdrawnRoutes().get(4).getPrefixLength());
		
		// 0.0.0.0/0
		assertEquals(addr3, umFromWellFormed.getWithdrawnRoutes().get(5).getAddress());
		assertEquals(0, umFromWellFormed.getWithdrawnRoutes().get(5).getPrefixLength());
		
		// nlriLength
		assertEquals(3, umFromWellFormed.getNlriLength());
		
		// nlri
		// 172.16.0.0/16
		InetAddress addr4 = InetAddress.getByName("172.16.0.0");
		assertEquals(addr4, umFromWellFormed.getNlriPrefix(0).getAddress());
		assertEquals(16, umFromWellFormed.getNlriPrefix(0).getPrefixLength());
		
		
		// nlri from umFromWllFormed2
		InetAddress addr5 = InetAddress.getByName("192.168.99.0");
		assertEquals(addr5, umFromWellFormed2.getNlriPrefix(0).getAddress());
		assertEquals(24, umFromWellFormed2.getNlriPrefix(0).getPrefixLength());
		
		
	}
	
	@Test
	public void testGetTotalPathAttributeLength() throws UtilityException
	{
		assertEquals(72, umFromWellFormed.getTotalPathAttributeLength());
	}
	
	@Test
	public void testGetPathAttributes() throws UtilityException
	{
		assertEquals(9, umFromWellFormed.getPathAttributeSequence().size());
		assertEquals(6, umFromWellFormed.getPathAttributeSequence().get(4).getTypeCode());
	}

}
