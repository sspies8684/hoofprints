package net.decix.bgpstack.types.pathattributes;

import java.util.LinkedList;
import java.util.List;

import net.decix.bgpstack.BGPConstants;
import net.decix.bgpstack.types.pathattributes.community.Community;
import net.decix.bgpstack.util.UtilityException;
import static net.decix.bgpstack.util.Utility.*;


@PathAttributeAnnotation(name = "COMMUNITY", typeCode = 8)
public class BGPPathAttributeCommunity implements BGPConstants, BGPPathAttributeContent
{
	private List<Community> communities = new LinkedList<Community>();

	
	
	protected BGPPathAttributeCommunity()
	{
	}



	public int getTypeCode()
	{
		return BGP_PATH_ATTRIBUTE_COMMUNITY;
	}


	public void parse(byte[] data) throws UtilityException
	{
		parse(data, null);
	}
	public void parse(byte[] data, Object param) throws UtilityException
	{
		if(data.length % 4 != 0)
			throw new UtilityException("COMMUNITY path attribute is not well-formed");
		
		for(int i = 0; i < data.length; i += 4)
		{
			byte[] community = new byte[4];
			System.arraycopy(data, i, community, 0, 4);
			communities.add(new Community(fourBytesToLong(community)));
		}
		
	}



	public int getByteLength()
	{
		int sum = 0;
		for(Community c : communities)
			sum += c.getByteLength();
		return sum;
	}



	public byte[] toBytes() throws UtilityException
	{
		byte[] result = {};
		for(Community c : communities)
			result = concatenateTwoByteArrays(result, c.toBytes());
		return result;
	}



	/**
	 * @return the communities
	 */
	public List<Community> getCommunities()
	{
		return communities;
	}
	

	public void addCommunity(Community c)
	{
		communities.add(c);
	}

}
