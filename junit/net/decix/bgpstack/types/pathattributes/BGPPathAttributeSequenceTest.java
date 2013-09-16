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

import static net.decix.bgpstack.util.Utility.hexStringToByteArray;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import net.decix.bgpstack.BGPConstants;

import org.junit.Before;
import org.junit.Test;

public class BGPPathAttributeSequenceTest implements BGPConstants
{
	private byte[] wellFormed = hexStringToByteArray("40010100" +
			"40020a010201f401f40201febb" +
			"400304c0a8000f" +
			"40050400000064" +
			"400600c00706febac0a8000ac0080cfebf000103160004015" +
			"400fa800904c0a8000f800a04c0a800fa");
	
	
	private BGPPathAttributeSequence attributeSequenceParsed;
	private BGPPathAttributeSequence attributeSequenceFixture;

	@Before
	public void setUp() throws Exception
	{
		attributeSequenceParsed = new BGPPathAttributeSequence(wellFormed);
		attributeSequenceFixture = new BGPPathAttributeSequence();
		BGPPathAttributeSequence seq1 = new BGPPathAttributeSequence();
		
	}

	@Test
	public void testParse() throws Exception
	{
		attributeSequenceParsed.parse(wellFormed);
	}

	@Test
	public void testSize() throws Exception
	{
		assertEquals(9, attributeSequenceParsed.size());
	}

	@Test
	public void testHeaderConsistency()
	{
		assertEquals(1, attributeSequenceParsed.get(0).getLength());
		BGPPathAttribute a;
		
		a = attributeSequenceParsed.get(1);
		assertEquals(10, a.getLength());
		assertEquals(BGP_PATH_ATTRIBUTE_AS_PATH, a.getTypeCode());
		assertTrue(a.isTransitive());
		assertFalse(a.isExtendedLength());
		assertFalse(a.isIncomplete());
		assertFalse(a.isOptional());
		assertEquals(a.getLength(), ((BGPPathAttributeAsPath) attributeSequenceParsed.get(1).getContent()).getByteLength());
		
		a = attributeSequenceParsed.get(8);
		assertEquals(4, a.getLength());
		assertEquals(BGP_PATH_ATTRIBUTE_CLUSTER_LIST,a.getTypeCode());
		assertEquals(a.getLength(), ((BGPPathAttributeUnknown) attributeSequenceParsed.get(8).getContent()).getValue().length);
	}

}
