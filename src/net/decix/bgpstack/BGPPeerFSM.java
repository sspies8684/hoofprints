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

import java.io.IOException;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import net.decix.bgpstack.states.BGPState;
import net.decix.bgpstack.states.BGPStateSingletonFactory;
import net.decix.bgpstack.types.BGPKeepaliveMessage;
import net.decix.bgpstack.types.BGPMessage;
import net.decix.bgpstack.types.BGPNotificationMessage;
import net.decix.bgpstack.types.BGPOpenMessage;
import net.decix.bgpstack.types.BGPOpenMessageParameter;
import net.decix.bgpstack.types.BGPPacket;
import net.decix.bgpstack.types.BGPUpdateMessage;
import net.decix.bgpstack.types.IPv4Prefix;
import net.decix.bgpstack.types.IPv6Prefix;
import net.decix.bgpstack.types.NLRI;
import net.decix.bgpstack.types.capabilities.BGPCapabilityFourByteASN;
import net.decix.bgpstack.types.capabilities.BGPCapabilityMultiprotocol;
import net.decix.bgpstack.types.capabilities.BGPCapabilityRouteRefresh;
import net.decix.bgpstack.types.pathattributes.BGPPathAttribute;
import net.decix.bgpstack.types.pathattributes.BGPPathAttributeMPReachNLRI;
import net.decix.bgpstack.types.pathattributes.BGPPathAttributeMPUnreachNLRI;
import net.decix.bgpstack.types.pathattributes.BGPPathAttributeSequence;

/**
 * 
 * @author sspies
 * 
 *         this is a standard BGP speaker * open sessions * send keepalives *
 *         inject routes * withdraw routes *
 * 
 */
public class BGPPeerFSM implements BGPConstants
{
	Logger logger = Logger.getLogger(BGPPeerFSM.class.getCanonicalName());

	private List<BGPRoute> learnedRoutes = new LinkedList<BGPRoute>();

	private int connectRetryCounter = 0;

	private BGPSession session;

	private List<BGPEventHandler> observers = new LinkedList<BGPEventHandler>();
	private List<TimerTask> scheduledTasks = new LinkedList<TimerTask>();

	private List<BGPUpdateMessage> updateQueue = new LinkedList<BGPUpdateMessage>();
	private long lastSentUpdate = 0;
	private long minUpdateDelay = 30000;

	private List<BGPOpenMessageParameter> receivedParameters = new LinkedList<BGPOpenMessageParameter>();
	private List<BGPOpenMessageParameter> sentParameters = new LinkedList<BGPOpenMessageParameter>();

	private boolean multiprotocolV4Enabled = true;
	private boolean multiprotocolV6Enabled = true;
	private boolean routeRefreshEnabled = true;
	private boolean as4Enabled = true;

	private Timer timer = new Timer();

	private KeepaliveTimerExpiredTask keepaliveExpiredTask;
	private HoldTimerExpiredTask holdtimeExpiredTask;

	private BGPState activeState;
	private BGPState establishedState;
	private BGPState idleState;
	private BGPState openConfirmState;
	private BGPState openSentState;

	private BGPState currentState;

	private long myAsn;
	private long remoteAsn;
	private int holdTime;
	private int keepaliveTime;
	private Inet4Address identifier;

	private boolean timerCancelled = false;

	/**
	 * Instantiates a new {@link BGPPeerFSM} object
	 * 
	 * @param myAddress the IPv4 address of this peer
	 * @param myAsn the ASN - autonomous system number of this peer
	 * @param remoteAsn the remote ASN - autonomous system number of the remote
	 *            peer
	 * @param holdTime the hold timeout of this peer
	 */
	public BGPPeerFSM(InetAddress myAddress, long myAsn, long remoteAsn, int holdTime, InetAddress identifier)
	{
		this.myAsn = myAsn;
		this.remoteAsn = remoteAsn;
		this.holdTime = holdTime;

		if (identifier instanceof Inet6Address)
			try
			{
				this.identifier = (Inet4Address) InetAddress.getByName("192.168.99.99");
			}
			catch (UnknownHostException e)
			{
			}
		else
			this.identifier = (Inet4Address) identifier;

		createStandardStates();

		currentState = idleState;
	}

	private void createStandardStates()
	{
		BGPStateSingletonFactory stateFactory = new BGPStateSingletonFactory(this);

		activeState = stateFactory.createActiveState();
		establishedState = stateFactory.createEstablishedState();
		idleState = stateFactory.createIdleState();
		openConfirmState = stateFactory.createOpenConfirmState();
		openSentState = stateFactory.createOpenSentState();
	}

	/**
	 * Receives an event, which will be processed. As this method yields
	 * synchronized, it returns fast. If you extend the finite state machine, by
	 * changing the states, please keep that in mind.
	 * 
	 * @param event the event to process
	 */
	public synchronized void receiveEvent(BGPEvent event)
	{
		logger.fine("Received event: " + event);
		currentState.handleEvent(event);
		notifyObservers(event);
	}

	/**
	 * Sends a BGP Open message and BGP Keepalive message
	 */
	public synchronized void open()
	{
		sendPacket(generateOpenMessage());
		sendPacket(generateKeepaliveMessage());
	}

	/**
	 * Connects this peer to its remote endpoint. Forwarded to
	 * {@link BGPSession}.
	 */
	public void connect()
	{
		if (session != null)
			session.connect();
	}

	/**
	 * Gets the connect state
	 * 
	 * @return true if the socket is connected. false if the socket is not
	 *         connected. (see {@link BGPSession})
	 */
	public boolean isConnected()
	{
		if (session == null || session.getSocket() == null)
		{
			return false;
		}

		return session.getSocket().isConnected();
	}
	
	public boolean isEstablished()
	{
		return getEstablishedState() == getCurrentState();
	}

	/**
	 * Learns the semantics from a BGP Update message
	 * 
	 * @param message the BGP message, which provides feasible and unfeasible
	 *            routes, including path attributes
	 */
	public void learnUpdate(BGPMessage message)
	{
		// BGPUpdateMessage updateMessage = (BGPUpdateMessage) message;

		// if (updateMessage.getWithdrawnRoutesLength() > 0)
		// for (IPv4Prefix prefix : updateMessage.getWithdrawnRoutes())
		// learnedRoutes.remove(prefix);
		//
		// if (updateMessage.getNlriLength() > 0)
		// for (IPv4Prefix prefix : updateMessage.getNlri())
		// learnedRoutes.add(new BGPRoute(prefix,
		// updateMessage.getPathAttributeSequence()));
	}

	/**
	 * Generates a Keepalive message
	 * 
	 * @return a {@link BGPPacket} containing one Keepalive message
	 */
	public BGPPacket generateKeepaliveMessage()
	{
		BGPPacket packet = new BGPPacket();
		BGPKeepaliveMessage message = new BGPKeepaliveMessage();
		packet.addMessage(message);
		return packet;
	}

	/**
	 * Generates an Open message
	 * 
	 * @return a {@link BGPPacket} containing one Open message
	 */
	public BGPPacket generateOpenMessage()
	{
		BGPPacket packet = new BGPPacket();

		if (multiprotocolV4Enabled)
			sentParameters.add(new BGPOpenMessageParameter(new BGPCapabilityMultiprotocol(AFI_INET,
																							SAFI_UNICAST)));
		if (multiprotocolV6Enabled)
			sentParameters.add(new BGPOpenMessageParameter(new BGPCapabilityMultiprotocol(AFI_INET6,
																							SAFI_UNICAST)));
		if (routeRefreshEnabled)
			sentParameters.add(new BGPOpenMessageParameter(new BGPCapabilityRouteRefresh()));
		if (as4Enabled)
			sentParameters.add(new BGPOpenMessageParameter(new BGPCapabilityFourByteASN(this.myAsn)));

		BGPOpenMessage openMessage = new BGPOpenMessage(myAsn < BGP_AS2_MAX ? (int) myAsn : BGP_AS_TRANS,
														holdTime, identifier, sentParameters);

		packet.addMessage(openMessage);

		return packet;
	}

	public BGPPacket generateShutdownMessage()
	{
		BGPPacket packet = new BGPPacket();

		BGPNotificationMessage notificationMessage = new BGPNotificationMessage();

		packet.addMessage(notificationMessage);
		return packet;
	}

	/**
	 * Sends Update messages to the remote peer for publishing {@link BGPRoute}
	 * s. This method tries to use as few packets as possible by aggregating
	 * routes with the same path attribute and splitting it multiple packets if
	 * exceeding BGP_MAX_MESSAGE_SIZE
	 * 
	 * @param routes the routes to announce
	 * 
	 */
	public void publishRoutes(BGPRoute... routes)
	{
		// sort into messages
		Map<BGPPathAttributeSequence, List<IPv4Prefix>> v4Map = new HashMap<BGPPathAttributeSequence, List<IPv4Prefix>>();
		for (BGPRoute route : routes)
			if (route.getPrefix() instanceof IPv4Prefix)
				if (v4Map.containsKey(route.getSequence()))
					v4Map.get(route.getSequence()).add((IPv4Prefix) route.getPrefix());
				else
				{
					List<IPv4Prefix> nlriList = new LinkedList<IPv4Prefix>();
					nlriList.add((IPv4Prefix) route.getPrefix());
					v4Map.put(route.getSequence(), nlriList);
				}

		// construct messages with IPv4 NLRI
		for (BGPPathAttributeSequence attributeSequence : v4Map.keySet())
		{
			List<IPv4Prefix> prefixes = v4Map.get(attributeSequence);
			List<IPv4Prefix> prefixesLeft = new LinkedList<IPv4Prefix>();
			prefixesLeft.addAll(prefixes);

			while (prefixesLeft.size() > 0)
			{
				BGPUpdateMessage updateMessage;
				List<IPv4Prefix> putPrefixes = new LinkedList<IPv4Prefix>();
				putPrefixes.addAll(prefixesLeft);
				for (;;)
				{
					updateMessage = new BGPUpdateMessage(null, putPrefixes, attributeSequence);

					if (updateMessage.getHeader().getLength() <= BGP_MAX_MESSAGE_SIZE)
						break;

					putPrefixes.remove(0);
				}
				prefixesLeft.removeAll(putPrefixes);
				scheduleUpdateMessage(updateMessage);
			}

		}

	}

	public void publishRoutes(BGPRoute6... routes)
	{
		// sort into messages
		Map<BGPPathAttributeSequence, List<BGPRoute6>> v6Map = new HashMap<BGPPathAttributeSequence, List<BGPRoute6>>();
		for (BGPRoute6 route : routes)
			if (route.getPrefix() instanceof IPv6Prefix)
				if (v6Map.containsKey(route.getSequence()))
					v6Map.get(route.getSequence()).add(route);
				else
				{
					List<BGPRoute6> nlriList = new LinkedList<BGPRoute6>();
					nlriList.add(route);
					v6Map.put(route.getSequence(), nlriList);
				}

		// construct messages with IPv6 NLRI
		for (BGPPathAttributeSequence attributeSequence : v6Map.keySet())
		{
			List<BGPRoute6> sameAttributeRoutes = v6Map.get(attributeSequence);
			List<BGPRoute6> routesLeft = new LinkedList<BGPRoute6>();
			routesLeft.addAll(sameAttributeRoutes);

			while (routesLeft.size() > 0)
			{
				BGPUpdateMessage updateMessage;
				List<BGPRoute6> putRoutes = new LinkedList<BGPRoute6>();
				putRoutes.addAll(routesLeft);

				for (;;)
				{
					BGPPathAttributeMPReachNLRI nlri = new BGPPathAttributeMPReachNLRI();
					BGPPathAttributeSequence sendSequence = (BGPPathAttributeSequence) attributeSequence.clone();
					for (BGPRoute6 route6 : putRoutes)
					{
						InetAddress nextHop = route6.getNextHop();
						if (!nlri.getNextHops().contains(nextHop))
							nlri.addNextHop(nextHop);
						nlri.addReachableNLRI(route6.getPrefix());
					}

					sendSequence.add(new BGPPathAttribute(nlri));

					updateMessage = new BGPUpdateMessage(null, null, sendSequence);

					if (updateMessage.getHeader().getLength() <= BGP_MAX_MESSAGE_SIZE)
						break;

					putRoutes.remove(0);

				}
				routesLeft.removeAll(putRoutes);
				scheduleUpdateMessage(updateMessage);
			}
		}

	}

	/**
	 * Sends Update messages to the remote peer to withdraw previously announced
	 * routes.
	 * 
	 * @param routes the routes to withdraw.
	 */
	public void withdrawRoutes(BGPRoute... routes)
	{
		BGPPacket packet = new BGPPacket();
		List<IPv4Prefix> prefixes = new LinkedList<IPv4Prefix>();
		for (BGPRoute route : routes)
			if (route.getPrefix() instanceof IPv4Prefix)
				prefixes.add((IPv4Prefix) route.getPrefix());
		BGPPathAttributeSequence sequence = new BGPPathAttributeSequence();
		BGPUpdateMessage updateMessage = new BGPUpdateMessage(prefixes, null, sequence);
		packet.addMessage(updateMessage);

		sendPacket(packet);
	}

	public void withdrawPrefixes(NLRI... nlri)
	{
		BGPPacket packet = new BGPPacket();
		List<IPv4Prefix> v4Prefixes = new LinkedList<IPv4Prefix>();
		List<NLRI> v6Prefixes = new LinkedList<NLRI>();

		for (NLRI n : nlri)
			if (n instanceof IPv6Prefix)
				v6Prefixes.add((IPv6Prefix) n);
			else if (n instanceof IPv4Prefix)
				v4Prefixes.add((IPv4Prefix) n);

		BGPPathAttributeSequence sequence = new BGPPathAttributeSequence();
		if (v6Prefixes.size() > 0)
		{
			BGPPathAttributeMPUnreachNLRI mpUnreach = new BGPPathAttributeMPUnreachNLRI(v6Prefixes);
			sequence.add(new BGPPathAttribute(mpUnreach));
		}
		BGPUpdateMessage updateMessage = new BGPUpdateMessage(v4Prefixes, null, sequence);
		packet.addMessage(updateMessage);
		sendPacket(packet);
	}

	/**
	 * Sends a {@link BGPPacket} to the remote peer.
	 * 
	 * @param packet the packet to send to the remote peer
	 */
	public void sendPacket(BGPPacket packet)
	{
		try
		{
			getOutputStream().write(packet.toBytes());
		}
		catch (Exception e)
		{
			logger.throwing(getClass().getCanonicalName(), "sendPacket", e);
		}
	}

	private OutputStream getOutputStream() throws IOException
	{
		return session.getSocket().getOutputStream();
	}

	/**
	 * Sets the {@link BGPSession} object of this peer
	 * 
	 * @param session
	 */
	public void setSession(BGPSession session)
	{
		this.session = session;
	}

	/**
	 * Gets the current state
	 * 
	 * @return the current state
	 */
	public BGPState getCurrentState()
	{
		return currentState;
	}

	/**
	 * Adds an {@link BGPEventHandler} to the list of observers
	 * 
	 * @param observer the observer to add
	 */
	public void addObserver(BGPEventHandler observer)
	{
		observers.add(observer);
	}

	/**
	 * Removes a {@link BGPEventHandler} from the list of observers
	 * 
	 * @param observer the observer to remove
	 */
	public void removeObserver(BGPEventHandler observer)
	{
		observers.remove(observer);
	}

	private void notifyObservers(BGPEvent event)
	{
		for (BGPEventHandler o : observers)
			o.receiveEvent(event, this);
	}

	/**
	 * Gets the local ASN - autonomous system number
	 * 
	 * @return the local ASN
	 */
	public long getMyAsn()
	{
		return myAsn;
	}

	/**
	 * Gets the learned routes during BGP communication
	 * 
	 * @return a list of learned routes
	 */
	public List<BGPRoute> getLearnedRoutes()
	{
		return learnedRoutes;
	}

	/**
	 * Gets the {@link BGPSession}
	 * 
	 * @return the session
	 */
	public BGPSession getSession()
	{
		return session;
	}

	/**
	 * Gets the list of observers, that are notified of {@link BGPEvent}s.
	 * 
	 * @return the list of observers
	 */
	public List<BGPEventHandler> getObservers()
	{
		return observers;
	}

	/**
	 * Gets the remote ASN - autonomous system number
	 * 
	 * @return the remote ASN
	 */
	public long getRemoteAsn()
	{
		return remoteAsn;
	}

	/**
	 * Gets the configured or negotiated hold time
	 * 
	 * @return the configured hold time if it's not yet negotiated. the
	 *         negotiated hold time if it's negotiated with
	 *         {@link BGPOpenMessage}s.
	 */
	public int getHoldTime()
	{
		return holdTime;
	}

	/**
	 * Gets the resulting keepalive time
	 * 
	 * @return the resulting keepalive time which is 1/3 of the negotiated hold
	 *         time or manually set.
	 */
	public int getKeepaliveTime()
	{
		return keepaliveTime;
	}

	/**
	 * Sets the local ASN - autonomous system number
	 * 
	 * @param myAsn the new local ASN
	 */
	public void setMyAsn(int myAsn)
	{
		this.myAsn = myAsn;
	}

	/**
	 * Sets the remote ASN - autonomous system number
	 * 
	 * @param remoteAsn the new remote ASN
	 */
	public void setRemoteAsn(int remoteAsn)
	{
		this.remoteAsn = remoteAsn;
	}

	/**
	 * Sets the hold time (configured)
	 * 
	 * @param holdTime the new holdtime
	 */
	public void setHoldTime(int holdTime)
	{
		this.holdTime = holdTime;
	}

	/**
	 * Sets the keepalive time (configured)
	 * 
	 * @param keepaliveTime the new keepalive time
	 */
	public void setKeepaliveTime(int keepaliveTime)
	{
		this.keepaliveTime = keepaliveTime;
	}

	/**
	 * Gets the current value of the connectRetryCounter
	 * 
	 * @return value of the connectRetryCounter
	 */
	public int getConnectRetryCounter()
	{
		return connectRetryCounter;
	}

	public BGPState getActiveState()
	{
		return activeState;
	}

	public void setActiveState(BGPState activeState)
	{
		this.activeState = activeState;
	}

	public BGPState getEstablishedState()
	{
		return establishedState;
	}

	public void setEstablishedState(BGPState establishedState)
	{
		this.establishedState = establishedState;
	}

	public BGPState getOpenConfirmState()
	{
		return openConfirmState;
	}

	public void setOpenConfirmState(BGPState openConfirmState)
	{
		this.openConfirmState = openConfirmState;
	}

	public BGPState getOpenSentState()
	{
		return openSentState;
	}

	public void setOpenSentState(BGPState openSentState)
	{
		this.openSentState = openSentState;
	}

	public BGPState getIdleState()
	{
		return idleState;
	}

	public void setIdleState(BGPState idleState)
	{
		this.idleState = idleState;
	}

	public boolean isScheduled(TimerTask task)
	{
		return scheduledTasks.contains(task);
	}

	/**
	 * Schedules a {@link TimerTask} to be executed
	 * 
	 * @param task the task to schedule
	 * @param delay the delay, before the task runs
	 */
	public synchronized void schedule(TimerTask task, long delay)
	{
		long secDelay = delay * 1000;
		if (timerCancelled)
		{
			timer = new Timer();
			timerCancelled = false;
		}
		timer.schedule(task, secDelay);
	}

	/**
	 * Set the current {@link BGPState}. Notify observers.
	 * 
	 * @param currentState the new State
	 */
	public void setCurrentState(BGPState currentState)
	{
		BGPState beforeState = this.currentState;
		this.currentState = currentState;
		notifyObservers(BGPEvent.fromStateChange(beforeState, currentState));
	}

	/**
	 * Restarts the hold timer
	 */
	public void restartHoldTimer()
	{
		if (holdtimeExpiredTask != null)
			holdtimeExpiredTask.cancel();
		holdtimeExpiredTask = new HoldTimerExpiredTask();
		schedule(holdtimeExpiredTask, holdTime);

	}

	/**
	 * Restarts the keepalive timer
	 */
	public void restartKeepaliveTimer()
	{
		if (keepaliveExpiredTask != null)
			keepaliveExpiredTask.cancel();
		keepaliveExpiredTask = new KeepaliveTimerExpiredTask();
		schedule(keepaliveExpiredTask, keepaliveTime);
	}

	/**
	 * Represents a {@link TimerTask} which runs if the Keepalive timer expired
	 * 
	 * @author sspies
	 * 
	 */
	private class KeepaliveTimerExpiredTask extends TimerTask
	{
		@Override
		public void run()
		{
			BGPEvent event = new BGPEvent(EVENT_TYPE.KeepaliveTimer_Expires);
			currentState.handleEvent(event);
			notifyObservers(event);
		}
	}

	/**
	 * Represents a {@link TimerTask} which runs if the Hold timer expired
	 * 
	 * @author sspies
	 * 
	 */
	private class HoldTimerExpiredTask extends TimerTask
	{
		@Override
		public void run()
		{
			BGPEvent event = new BGPEvent(EVENT_TYPE.HoldTimer_Expires);
			currentState.handleEvent(event);
			notifyObservers(event);
		}
	}

	/**
	 * Shuts the session down and cancels timer
	 */
	public synchronized void shutdown()
	{
		session.shutdown();
		session = session.clone();

		timer.cancel();
		timerCancelled = true;
	}

	/**
	 * @return the receivedCapabilities
	 */
	public List<BGPOpenMessageParameter> getReceivedParameters()
	{
		return receivedParameters;
	}

	/**
	 * @return the sentCapabilities
	 */
	public List<BGPOpenMessageParameter> getSentParameters()
	{
		return sentParameters;
	}

	public void setReceivedParameters(List<BGPOpenMessageParameter> parameters)
	{
		this.receivedParameters = parameters;

	}

	// four byte asn sucks

	protected boolean isFourByteAsnCapabilityReceived()
	{
		for (BGPOpenMessageParameter omParamRecv : getReceivedParameters())
			if (omParamRecv.getValue() instanceof BGPCapabilityFourByteASN)
				return true;
		return false;
	}

	protected boolean isFourByteAsnCapabilitySent()
	{
		for (BGPOpenMessageParameter omParamSent : getSentParameters())
			if (omParamSent.getValue() instanceof BGPCapabilityFourByteASN)
				return true;
		return false;
	}

	public boolean fourByteAsnCapabilitySentAndReceived()
	{
		return isFourByteAsnCapabilityReceived() && isFourByteAsnCapabilitySent();
	}

	/**
	 * @return the multiprotocolV4Enabled
	 */
	public boolean isMultiprotocolV4Enabled()
	{
		return multiprotocolV4Enabled;
	}

	/**
	 * @param multiprotocolV4Enabled the multiprotocolV4Enabled to set
	 */
	public void setMultiprotocolV4Enabled(boolean multiprotocolV4Enabled)
	{
		this.multiprotocolV4Enabled = multiprotocolV4Enabled;
	}

	/**
	 * @return the multiprotocolV6Enabled
	 */
	public boolean isMultiprotocolV6Enabled()
	{
		return multiprotocolV6Enabled;
	}

	/**
	 * @param multiprotocolV6Enabled the multiprotocolV6Enabled to set
	 */
	public void setMultiprotocolV6Enabled(boolean multiprotocolV6Enabled)
	{
		this.multiprotocolV6Enabled = multiprotocolV6Enabled;
	}

	/**
	 * @return the aS4Enabled
	 */
	public boolean isAS4Enabled()
	{
		return as4Enabled;
	}

	/**
	 * @param aS4Enabled the aS4Enabled to set
	 */
	public void setAS4Enabled(boolean aS4Enabled)
	{
		as4Enabled = aS4Enabled;
	}

	/**
	 * @return the routeRefreshEnabled
	 */
	public boolean isRouteRefreshEnabled()
	{
		return routeRefreshEnabled;
	}

	/**
	 * @param routeRefreshEnabled the routeRefreshEnabled to set
	 */
	public void setRouteRefreshEnabled(boolean routeRefreshEnabled)
	{
		this.routeRefreshEnabled = routeRefreshEnabled;
	}

	private void scheduleUpdateMessage(BGPUpdateMessage updateMessage)
	{
		long currentTime = System.currentTimeMillis();
		long timeSinceLastUpdate = currentTime - lastSentUpdate;
		long nextSlot;

		synchronized (updateQueue)
		{
			if (timeSinceLastUpdate > minUpdateDelay)
				nextSlot = 0;
			else
				nextSlot = (minUpdateDelay - timeSinceLastUpdate) / 1000;

			updateQueue.add(updateMessage);
		}

		schedule(new TimerTask()
		{
			@Override
			public void run()
			{
				synchronized (updateQueue)
				{
					BGPPacket p = new BGPPacket();
					p.addMessages(updateQueue);
					updateQueue.clear();
					sendPacket(p);
					lastSentUpdate = System.currentTimeMillis();
				}
			}
		}, nextSlot);

	}

}
