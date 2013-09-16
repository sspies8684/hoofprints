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

import static net.decix.bgpstack.util.Utility.*;

import java.net.InetAddress;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import net.decix.bgpstack.BGPConstants;
import net.decix.bgpstack.BGPException;
import net.decix.bgpstack.util.PacketSerializable;
import net.decix.bgpstack.util.UtilityException;

/**
 * Represents a BGP Packet. A BGP packet consists of multiple stacked BGP
 * messages. (see <a href="http://tools.ietf.org/html/rfc4271">RFC4271</a>)
 * 
 * @author sspies
 * 
 */
public class BGPPacket implements BGPConstants, PacketSerializable
{
	private List<BGPMessage> messages = new LinkedList<BGPMessage>();
	private InetAddress sender;
	private long timestamp;

	/**
	 * Instantiates a new BGP packet
	 */
	public BGPPacket()
	{
		timestamp = System.currentTimeMillis();
	}

	/**
	 * Instantiates a new {@link BGPPacket} object
	 * 
	 * @param sender the sender address of this packet
	 * @param rawData the raw data from the packet as a byte-array
	 * @throws BGPException if the raw-data is not well-formed
	 */
	public BGPPacket(InetAddress sender, byte[] rawData, Object param) throws BGPException
	{
		this.sender = sender;
		this.timestamp = System.currentTimeMillis();
		try
		{
			do
			{
				BGPMessageHeader header = BGPMessageHeader.parse(rawData);

				int messageLength = header.getLength() - 19;
				byte[] contentData = new byte[messageLength];
				System.arraycopy(rawData, 19, contentData, 0, messageLength);

				BGPMessage currentMessage = null;

				switch (header.getType())
				{
					case BGP_MESSAGE_TYPE_OPEN:
						currentMessage = BGPOpenMessage.parse(contentData);
					break;
					case BGP_MESSAGE_TYPE_KEEPALIVE:
						currentMessage = BGPKeepaliveMessage.parse(contentData);
					break;
					case BGP_MESSAGE_TYPE_UPDATE:
						currentMessage = BGPUpdateMessage.parse(contentData, param);
					break;
					case BGP_MESSAGE_TYPE_NOTIFICATION:
						currentMessage = BGPNotificationMessage.parse(contentData);
					break;
					case BGP_MESSAGE_TYPE_ROUTE_REFRESH:
						currentMessage = BGPRouteRefreshMessage.parse(contentData);
					break;
					default:
						throw new BGPException("Unrecognized BGP message type");
				}

				currentMessage.setHeader(header);
				messages.add(currentMessage);
				rawData = truncateLeft(rawData, header.getLength());

			}
			while (rawData.length > 0);
		}
		catch (UtilityException e)
		{
			throw new BGPException(e.getMessage());
		}

	}

	/**
	 * Gets the contained BGP messages
	 * 
	 * @return a list of BGP messages carried by this packet
	 */
	public List<BGPMessage> getMessages()
	{
		return messages;
	}

	/**
	 * Adds a new BGP message to the end of the BGP messages carried by this
	 * packet
	 * 
	 * @param message the message to add
	 */
	public void addMessage(BGPMessage message)
	{
		messages.add(message);
	}
	
	public void addMessages(List<? extends BGPMessage> messages) 
	{
		this.messages.addAll(messages);
	}

	
	
	@Override
	public String toString()
	{
		String retVal = "BGP packet ";
		if (sender != null)
			retVal += "from: " + sender.getCanonicalHostName();
		else
			retVal += "sent";

		retVal += " at " + new Date(timestamp) + " ";
		retVal += " (" + messages.size() + " message(s))\n";
		for (BGPMessage m : messages)
			retVal += m;
		return retVal;
	}

	public byte[] toBytes() throws UtilityException
	{
		byte[] result = {};
		for (BGPMessage m : messages)
			result = concatenateTwoByteArrays(result, m.toBytes());
		return result;
	}

	public int getByteLength()
	{
		int total = 0;

		for (BGPMessage m : messages)
			total += m.getByteLength();

		return total;
	}

}
