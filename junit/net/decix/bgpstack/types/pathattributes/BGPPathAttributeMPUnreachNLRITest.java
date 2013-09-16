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


import static org.junit.Assert.assertEquals;

import java.net.InetAddress;
import java.net.UnknownHostException;

import net.decix.bgpstack.types.IPv6Prefix;
import net.decix.bgpstack.types.NLRI;
import net.decix.bgpstack.util.Utility;
import net.decix.bgpstack.util.UtilityException;

import org.junit.Before;
import org.junit.Test;

public class BGPPathAttributeMPUnreachNLRITest
{
	byte[] wellFormed1 = Utility.hexStringToByteArray("000201202607fc18");
	byte[] wellFormed2 = Utility.hexStringToByteArray("000201202607fc18302002AFFEAFFE");
	
	BGPPathAttributeMPUnreachNLRI mpUnreachNlri1;
	BGPPathAttributeMPUnreachNLRI mpUnreachNlri2;
	BGPPathAttributeMPUnreachNLRI mpUnreachNlriFixture;
	
	@Before
	public void setUp() throws Exception
	{
		mpUnreachNlri1 = new BGPPathAttributeMPUnreachNLRI();
		mpUnreachNlri1.parse(wellFormed1);
		mpUnreachNlri2 = new BGPPathAttributeMPUnreachNLRI();
		mpUnreachNlri2.parse(wellFormed2);
		
		mpUnreachNlriFixture = new BGPPathAttributeMPUnreachNLRI();
		mpUnreachNlriFixture.addUnreachableNLRI(new IPv6Prefix(InetAddress.getByName("2607:fc18::"), 32));
	}
	
	@Test
	public void testParse() throws UtilityException
	{
		mpUnreachNlri1.parse(wellFormed1);
	}
	
	@Test
	public void testToBytes() throws UtilityException
	{
		assertEquals(mpUnreachNlri1.toBytes().length, mpUnreachNlriFixture.toBytes().length);
		
		for(int i = 0; i < mpUnreachNlriFixture.toBytes().length; i++)
		{
			assertEquals(mpUnreachNlri1.toBytes()[i], mpUnreachNlriFixture.toBytes()[i]);
			assertEquals(mpUnreachNlri1.toBytes()[i], wellFormed1[i]);
		}
		
		
		assertEquals(wellFormed2.length, mpUnreachNlri2.toBytes().length);
		
		for(int i = 0; i < wellFormed2.length; i++)
			assertEquals(wellFormed2[i], mpUnreachNlri2.toBytes()[i]);
		
		
	}
	
	@Test
	public void testGetUnreachableNlri() throws UnknownHostException
	{
		assertEquals(mpUnreachNlri1.getUnreachableNLRI().size(), mpUnreachNlriFixture.getUnreachableNLRI().size());
		
		for(NLRI n : mpUnreachNlri1.getUnreachableNLRI())
		{
			assertEquals(n.getAddress(), InetAddress.getByName("2607:fc18::"));
			assertEquals(n.getPrefixLength(), 32);
		}
		
		assertEquals(2, mpUnreachNlri2.getUnreachableNLRI().size());
		assertEquals(InetAddress.getByName("2607:fc18::"), mpUnreachNlri2.getUnreachableNLRI().get(0).getAddress());
		assertEquals(32, mpUnreachNlri2.getUnreachableNLRI().get(0).getPrefixLength());
		assertEquals(InetAddress.getByName("2002:affe:affe::"), mpUnreachNlri2.getUnreachableNLRI().get(1).getAddress());
		assertEquals(48, mpUnreachNlri2.getUnreachableNLRI().get(1).getPrefixLength());
	}

}
