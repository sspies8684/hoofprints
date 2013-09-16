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

/**
 * Constants from <a href="http://tools.ietf.org/html/rfc4271">RFC4271</a>
 * 
 * @author sspies
 * 
 */
public interface BGPConstants
{
	/**
	 * Socket timeout
	 */
	public final int SO_TIMEOUT = 1000;

	public final int AFI_INET = 1;
	public final int AFI_INET6 = 2;
	public final int SAFI_UNICAST = 1;
	/**
	 * BGP connect retries before session gives up
	 */
	public final int BGP_CONNECT_RETRIES = 3;

	/**
	 * Timeout for BGP connection
	 */
	public final int BGP_CONNECT_TIMEOUT = 5000;

	/**
	 * Standard BGP port
	 */
	public final int BGP_PORT = 179;
	public final int BGP_MAX_MESSAGE_SIZE = 4096;
	public final int BGP_AS_TRANS = 23456;
	public final int BGP_AS2_MAX = 65536;

	public final int BGP_MESSAGE_TYPE_OPEN = 1;
	public final int BGP_MESSAGE_TYPE_UPDATE = 2;
	public final int BGP_MESSAGE_TYPE_NOTIFICATION = 3;
	public final int BGP_MESSAGE_TYPE_KEEPALIVE = 4;
	public final int BGP_MESSAGE_TYPE_ROUTE_REFRESH = 5;

	public enum BGP_MESSAGE_TYPES
	{
		OPEN, UPDATE, NOTIFICATION, KEEPALIVE
	};

	public final int BGP_MESSAGE_OPEN_ATTRIBUTE_TYPE_CAPABILITY = 2;

	public final int BGP_CAPABILITY_MULTIPROTOCOL = 1;
	public final int BGP_CAPABILITY_ROUTE_REFRESH = 2;
	public final int BGP_CAPABILITY_GRACEFUL_RESTART = 64;
	public final int BGP_CAPABILITY_4_BYTE_ASN = 65;

	public final int BGP_PATH_ATTRIBUTE_ORIGIN = 1;
	public final int BGP_PATH_ATTRIBUTE_AS_PATH = 2;
	public final int BGP_PATH_ATTRIBUTE_NEXT_HOP = 3;
	public final int BGP_PATH_ATTRIBUTE_MED = 4;
	public final int BGP_PATH_ATTRIBUTE_COMMUNITY = 5;
	public final int BGP_PATH_ATTRIBUTE_CLUSTER_LIST = 10;
	public final int BGP_PATH_ATTRIBUTE_MP_UNREACH_NLRI = 15;
	public final int BGP_PATH_ATTRIBUTE_MP_REACH_NLRI = 14;

	public final int BGP_PATH_ORIGIN_IGP = 0;
	public final int BGP_PATH_ORIGIN_EGP = 1;
	public final int BGP_PATH_ORIGIN_INCOMPLETE = 2;

	public final int BGP_PATH_ATTRIBUTE_AS_PATH_SET = 1;
	public final int BGP_PATH_ATTRIBUTE_AS_PATH_SEQUENCE = 2;

	public final int NO_EXPORT = 0xFFFFFF01;
	public final int NO_ADVERTISE = 0xFFFFFF02;
	public final int NO_EXPORT_SUBCONFED = 0xFFFFFF03;

	public final byte[] MARKER = { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 };

	public final int AFI_IPV4_NH_SIZE = 4;
	public final int AFI_IPV6_NH_SIZE = 16;

	// rfc 4271
	/**
	 * Events from RFC4271
	 */
	public enum EVENT_TYPE
	{
		BGPOpen, KeepAliveMsg, UpdateMsg, NotifMsg, ManualStart, ManualStop, AutomaticStart,
		ManualStart_with_PassiveTcpEstablishment, AutomaticStart_with_PassiveTcpEstablishment,
		AutomaticStart_with_DampPeerOscillations,
		AutomaticStart_with_DampPeerOscillations_and_PassiveTcpEstablishment, AutomaticStop,
		ConnectRetryTimer_Expires, HoldTimer_Expires, KeepaliveTimer_Expires, DelayOpenTimer_Expires,
		IdleHoldTimer_Expires, BGPOpen_with_DelayOpenTimer_running, BGPHeaderErr, BGPOpenMsgErr,
		OpenCollisionDump, NotifMsgVerErr, UpdateMsgErr, TcpConnected, TcpConnectionFails, StateChange
	}
}
