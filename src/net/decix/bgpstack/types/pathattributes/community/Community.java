package net.decix.bgpstack.types.pathattributes.community;

import net.decix.bgpstack.BGPConstants;
import static net.decix.bgpstack.util.Utility.*;
import net.decix.bgpstack.util.PacketSerializable;
import net.decix.bgpstack.util.UtilityException;

public class Community implements PacketSerializable, BGPConstants
{
	private long communityValue;
	
	public Community(long communityValue)
	{
		this.communityValue = communityValue;
	}

	public int getByteLength()
	{
		return 4;
	}

	public byte[] toBytes() throws UtilityException
	{
		return longToFourBytes(communityValue);
	}
	
	
	@Override
	public String toString()
	{
		if(communityValue == NO_EXPORT)
			return "NO_EXPORT";
		else if(communityValue == NO_ADVERTISE)
			return "NO_ADVERTISE";
		else if(communityValue == NO_EXPORT_SUBCONFED)
			return "NO_EXPORT_SUBCONFED";
		
		return Long.toString(communityValue & 0xFFFF0000) + ":" + Long.toString(communityValue & 0xFFFF);
	}

	/**
	 * @return the communityValue
	 */
	public long getCommunityValue()
	{
		return communityValue;
	}

}
