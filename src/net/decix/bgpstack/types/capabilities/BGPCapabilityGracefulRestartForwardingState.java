package net.decix.bgpstack.types.capabilities;

import static net.decix.bgpstack.util.Utility.*;
import net.decix.bgpstack.BGPConstants;
import net.decix.bgpstack.util.PacketSerializable;
import net.decix.bgpstack.util.UtilityException;

public class BGPCapabilityGracefulRestartForwardingState implements PacketSerializable, BGPConstants
{

	private int afi;
	private int safi;
	private boolean forwardingState;

	public BGPCapabilityGracefulRestartForwardingState(int afi, int safi, boolean forwardingState)
	{
		super();
		this.afi = afi;
		this.safi = safi;
		this.forwardingState = forwardingState;
	}

	protected BGPCapabilityGracefulRestartForwardingState()
	{
	}

	public int getByteLength()
	{
		return 4;
	}

	public byte[] toBytes() throws UtilityException
	{
		return concatenateTwoByteArrays(integerToTwoBytes(afi), 
				new byte[] { integerToOneByte(safi), integerToOneByte(forwardingState ? 0x80 : 0x00) });
	}

	public static BGPCapabilityGracefulRestartForwardingState parse(byte[] data) throws UtilityException
	{
		BGPCapabilityGracefulRestartForwardingState state = new BGPCapabilityGracefulRestartForwardingState();

		// get address family identifier
		byte[] afi = new byte[2];
		System.arraycopy(data, 0, afi, 0, 2);
		state.setAfi(twoBytesToInteger(afi));

		// get safi
		state.setSafi(oneByteToInteger(data[2]));

		// get forwarding state
		state.setForwardingState((oneByteToInteger(data[3]) & 0x80) == 0x80);
		if ((oneByteToInteger(data[3]) & 0x7F) != 0x00) throw new UtilityException("Graceful Restart Capability is not well-formed");

		return state;
	}

	/**
	 * @return the afi
	 */
	public int getAfi()
	{
		return afi;
	}

	/**
	 * @return the safi
	 */
	public int getSafi()
	{
		return safi;
	}

	/**
	 * @return the forwardingState
	 */
	public boolean isForwardingState()
	{
		return forwardingState;
	}

	/**
	 * @param afi the afi to set
	 */
	protected void setAfi(int afi)
	{
		this.afi = afi;
	}

	/**
	 * @param safi the safi to set
	 */
	protected void setSafi(int safi)
	{
		this.safi = safi;
	}

	/**
	 * @param forwardingState the forwardingState to set
	 */
	protected void setForwardingState(boolean forwardingState)
	{
		this.forwardingState = forwardingState;
	}

}
