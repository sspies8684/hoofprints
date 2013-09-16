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

package net.decix.bgpstack;

import net.decix.bgpstack.states.BGPState;
import net.decix.bgpstack.types.BGPMessage;

/**
 * Represents a BGP event
 * 
 * @author sspies
 * 
 */
public class BGPEvent implements BGPConstants
{
	private EVENT_TYPE eventType;
	private Object[] info;

	/**
	 * Instantiates a new {@link BGPEvent} object.
	 * 
	 * @param event
	 */
	public BGPEvent(EVENT_TYPE event)
	{
		this.eventType = event;
	}

	/**
	 * Construct a {@link BGPEvent} from a {@link BGPMessage}. The
	 * {@link BGPMessage} is available with {@link BGPEvent#getMessage()}
	 * 
	 * @param message the message which causes the {@link BGPEvent}
	 * @return the new {@link BGPEvent} resulting from a received
	 *         {@link BGPMessage}
	 */
	public static BGPEvent fromMessage(BGPMessage message)
	{
		BGPEvent newEvent = null;
		switch (message.getHeader().getType())
		{
			case BGP_MESSAGE_TYPE_OPEN:
				newEvent = new BGPEvent(EVENT_TYPE.BGPOpen);
			break;
			case BGP_MESSAGE_TYPE_KEEPALIVE:
				newEvent = new BGPEvent(EVENT_TYPE.KeepAliveMsg);
			break;
			case BGP_MESSAGE_TYPE_UPDATE:
				newEvent = new BGPEvent(EVENT_TYPE.UpdateMsg);
			break;
			case BGP_MESSAGE_TYPE_NOTIFICATION:
				newEvent = new BGPEvent(EVENT_TYPE.NotifMsg);

			break;
		}

		newEvent.setInfo(new Object[] { message });
		return newEvent;

	}

	/**
	 * Construct a {@link BGPEvent} from an FSM state change. The states are
	 * available with {@link BGPEvent#getOldState()} and
	 * {@link BGPEvent#getNewState()}
	 * 
	 * @param oldState the state before transition
	 * @param newState the state after transition
	 * @return the new {@link BGPEvent} resulting from an FSM state change
	 */
	public static BGPEvent fromStateChange(BGPState oldState, BGPState newState)
	{
		BGPEvent event = new BGPEvent(EVENT_TYPE.StateChange);
		event.setInfo(new Object[] { oldState, newState });
		return event;
	}

	/**
	 * Gets raw attributes info of this event
	 * 
	 * @return the raw attributes of this event
	 */
	public Object getInfo()
	{
		return info;
	}

	/**
	 * Gets a message after {@link BGPEvent#fromMessage(BGPMessage)} has been
	 * called
	 * 
	 * @return the resulting message. if this event is not caused by a received
	 *         {@link BGPMessage} null is returned
	 */
	public BGPMessage getMessage()
	{
		if (info[0] instanceof BGPMessage)
			return (BGPMessage) info[0];
		return null;
	}

	/**
	 * Gets the state before a transition, which has caused a
	 * {@link BGPEvent#fromStateChange(BGPState, BGPState)}
	 * 
	 * @return the state before the transition. if this event is not caused by a
	 *         state change null is returned.
	 */
	public BGPState getOldState()
	{
		if (info[0] instanceof BGPState)
			return (BGPState) info[0];
		return null;
	}

	/**
	 * Gets the state after a transition, which has caused a
	 * {@link BGPEvent#fromStateChange(BGPState, BGPState)}
	 * 
	 * @return the state after the transition. if this event is not caused by a
	 *         state change null is returned.
	 */
	public BGPState getNewState()
	{
		if (info[1] instanceof BGPState)
			return (BGPState) info[1];
		return null;
	}

	/**
	 * Gets the event type code of this event
	 * 
	 * @return the event type code
	 */
	public EVENT_TYPE getEventType()
	{
		return eventType;
	}

	private void setInfo(Object[] info)
	{
		this.info = info;
	}

	@Override
	public String toString()
	{
		return "Event " + eventType + (info != null && info.length > 0 ? " info[0]=" + info[0] : "");
	}
}
