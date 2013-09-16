package net.decix.bgpstack.types.pathattributes;


import static net.decix.bgpstack.util.Utility.hexStringToByteArray;
import static org.junit.Assert.assertEquals;
import net.decix.bgpstack.BGPConstants;
import net.decix.bgpstack.types.pathattributes.community.Community;
import net.decix.bgpstack.util.UtilityException;

import org.junit.Before;
import org.junit.Test;

public class BGPPathAttributeCommunityTest implements BGPConstants
{
	BGPPathAttributeCommunity communityFixture;
	BGPPathAttributeCommunity communityParsed;
	
	byte[] wellFormed = hexStringToByteArray("fe0901f4fe090258");

	@Before
	public void setUp() throws Exception
	{
		communityParsed = new BGPPathAttributeCommunity();
		communityParsed.parse(wellFormed);
		communityFixture = new BGPPathAttributeCommunity();
		communityFixture.addCommunity(new Community(0xfe0901f4L));
		communityFixture.addCommunity(new Community(0xfe090258L));
	}

	@Test
	public void testParse() throws UtilityException
	{
		communityParsed.parse(wellFormed);
	}
	

	@Test
	public void testToBytes() throws UtilityException
	{
		assertEquals(communityParsed.toBytes().length, communityFixture.toBytes().length);
		
		for(int i = 0; i < communityFixture.toBytes().length; i++)
		{
			assertEquals(communityParsed.toBytes()[i], communityFixture.toBytes()[i]);
			assertEquals(communityParsed.toBytes()[i], wellFormed[i]);
		}
	}
	
	@Test
	public void testGetCommunity()
	{
		assertEquals(communityFixture.getCommunities().get(0).getCommunityValue(), communityParsed.getCommunities().get(0).getCommunityValue());
		assertEquals(communityFixture.getCommunities().get(1).getCommunityValue(), communityParsed.getCommunities().get(1).getCommunityValue());
	}
	
	@Test
	public void testGetByteLength()
	{
		assertEquals(communityParsed.getByteLength(), wellFormed.length);
		assertEquals(communityParsed.getByteLength(), communityFixture.getByteLength());
	}
	
	@Test
	public void testToString()
	{
		assertEquals(communityFixture.getCommunities().get(0).toString(), communityParsed.getCommunities().get(0).toString());
		assertEquals("NO_EXPORT", new Community(NO_EXPORT).toString());
	}
}
