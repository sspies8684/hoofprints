package net.decix.bgpstack.types.capabilities;

import static net.decix.bgpstack.util.Utility.*;
import net.decix.bgpstack.util.UtilityException;

@CapabilityAnnotation(name = "BGP Multiprotocol Capability (RFC4760)", typeCode = 1)
public class BGPCapabilityMultiprotocol extends BGPCapability
{
	private int addressFamilyIdentifier;
	private int subsequentAddressFamilyIdentifier;
	private byte reserved = 0x00;
	
	public BGPCapabilityMultiprotocol(int afi, int safi)
	{
		this();
		addressFamilyIdentifier = afi;
		subsequentAddressFamilyIdentifier = safi;
	}
	
	
	public BGPCapabilityMultiprotocol()
	{
		super(BGP_CAPABILITY_MULTIPROTOCOL);
	}

	public int getByteLengthTemplate()
	{
		return 4;
	}

	public byte[] toBytesTemplate() throws UtilityException
	{
		return concatenateTwoByteArrays(integerToTwoBytes(addressFamilyIdentifier), new byte[] { reserved, integerToOneByte(subsequentAddressFamilyIdentifier) });
	}

	@Override
	protected void parseTemplate(byte[] data) throws UtilityException
	{
		this.addressFamilyIdentifier = twoBytesToInteger(new byte[] { data[0], data[1] });
		this.reserved = data[2];
		this.subsequentAddressFamilyIdentifier = oneByteToInteger(data[3]);
	}


	/**
	 * @return the addressFamilyIdentifier
	 */
	public int getAddressFamilyIdentifier()
	{
		return addressFamilyIdentifier;
	}


	/**
	 * @return the subsequentAddressFamilyIdentifier
	 */
	public int getSubsequentAddressFamilyIdentifier()
	{
		return subsequentAddressFamilyIdentifier;
	}


	/**
	 * @return the reserved
	 */
	public byte getReserved()
	{
		return reserved;
	}


	/**
	 * @param addressFamilyIdentifier the addressFamilyIdentifier to set
	 */
	protected void setAddressFamilyIdentifier(int addressFamilyIdentifier)
	{
		this.addressFamilyIdentifier = addressFamilyIdentifier;
	}


	/**
	 * @param subsequentAddressFamilyIdentifier the subsequentAddressFamilyIdentifier to set
	 */
	protected void setSubsequentAddressFamilyIdentifier(int subsequentAddressFamilyIdentifier)
	{
		this.subsequentAddressFamilyIdentifier = subsequentAddressFamilyIdentifier;
	}


	/**
	 * @param reserved the reserved to set
	 */
	protected void setReserved(byte reserved)
	{
		this.reserved = reserved;
	}
	
	
	@Override
	public String toString()
	{
		return "BGP capability multiprotocol: afi " + addressFamilyIdentifier + ", safi " + subsequentAddressFamilyIdentifier; 
	}

}
