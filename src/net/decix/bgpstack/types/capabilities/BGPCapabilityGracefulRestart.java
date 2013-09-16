package net.decix.bgpstack.types.capabilities;

import static net.decix.bgpstack.util.Utility.*;

import java.util.LinkedList;
import java.util.List;

import net.decix.bgpstack.util.UtilityException;

// TODO implement
@CapabilityAnnotation(name = "Graceful Restart RFC4724", typeCode = 64)
public class BGPCapabilityGracefulRestart extends BGPCapability
{
	private boolean restartState;
	private int restartTime;

	private List<BGPCapabilityGracefulRestartForwardingState> states = new LinkedList<BGPCapabilityGracefulRestartForwardingState>();

	public BGPCapabilityGracefulRestart(boolean restartState, int restartTime)
	{
		this();
		this.restartState = restartState;
		this.restartTime = restartTime;
	}
	
	public BGPCapabilityGracefulRestart()
	{
		super(BGP_CAPABILITY_GRACEFUL_RESTART);
	}

	protected BGPCapabilityGracefulRestart(int typeCode)
	{
		super(typeCode);
	}

	@Override
	protected void parseTemplate(byte[] data) throws UtilityException
	{
		if (data.length < 6) throw new UtilityException("Graceful Restart Capability is not well-formed");

		this.restartState = (oneByteToInteger(data[0]) & 0x80) == 0x80;
		if ((oneByteToInteger(data[0]) & 0x40) != 0x00) throw new UtilityException("Graceful Restart Capability is not well-formed");

		// get restart time - 12 bits
		restartTime = ((data[0] & 0x0F) << 8) + oneByteToInteger(data[1]);

		for (int offset = 2; offset < data.length; offset += 4)
		{
			byte[] state = new byte[4];
			System.arraycopy(data, offset, state, 0, 4);
			states.add(BGPCapabilityGracefulRestartForwardingState.parse(state));
		}
	}

	public int getByteLengthTemplate()
	{
		int sum = 2;
		for (BGPCapabilityGracefulRestartForwardingState s : states)
			sum += s.getByteLength();
		return sum;
	}

	public byte[] toBytesTemplate() throws UtilityException
	{
		byte[] result = { integerToOneByte((restartState ? 0x80 : 0x00) | (restartTime & 0xF00)), integerToOneByte(restartTime & 0x0FF) };
		
		for(BGPCapabilityGracefulRestartForwardingState s : states)
			result = concatenateTwoByteArrays(result, s.toBytes());
		
		return result;
	}

	/**
	 * @return the restartState
	 */
	public boolean isRestartState()
	{
		return restartState;
	}

	/**
	 * @return the restartTime
	 */
	public int getRestartTime()
	{
		return restartTime;
	}

	/**
	 * @return the states
	 */
	protected List<BGPCapabilityGracefulRestartForwardingState> getStates()
	{
		return states;
	}

	/**
	 * @param states the states to set
	 */
	public void addState(BGPCapabilityGracefulRestartForwardingState state)
	{
		states.add(state);
	}

}
