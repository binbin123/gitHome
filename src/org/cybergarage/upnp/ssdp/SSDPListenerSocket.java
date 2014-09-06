/******************************************************************
 *
 *	CyberUPnP for Java
 *
 *	Copyright (C) Satoshi Konno 2002
 *
 *	File: SSDPSearchResponseSocket.java
 *
 *	Revision;
 *
 *	11/20/02
 *		- first revision.
 *	05/28/03
 *		- Added post() to send a SSDPSearchRequest.
 *	01/31/08
 *		- Changed start() not to abort when the interface infomation is null on Android m3-rc37a.
 *	
 ******************************************************************/

package org.cybergarage.upnp.ssdp;

import java.net.DatagramSocket;
import java.net.InetAddress;
import org.cybergarage.util.ListenerList;
import org.cybergarage.upnp.device.ReceiveDataListener;
import org.cybergarage.net.HostInterface;

import android.os.Looper;
import android.util.Log;

public class SSDPListenerSocket extends HTTPUSocket implements Runnable {
	// //////////////////////////////////////////////
	// Constructor
	// //////////////////////////////////////////////
	public SSDPListenerSocket(int port) {
		super(port);
		String[] bindAddresses;

		int nHostAddrs = HostInterface.getNHostAddresses();
		bindAddresses = new String[nHostAddrs];
		for (int n = 0; n < nHostAddrs; n++) {
			bindAddresses[n] = HostInterface.getHostAddress(n);
		}

		for (int i = 0; i < bindAddresses.length; i++) {
			if (bindAddresses[i] != null) {
				// Debug.d("SSDPBCResponeSocket", "i = " + i + "bindAddresses ="
				// + bindAddresses[i]);
				if (HostInterface.isIPv4Address(bindAddresses[i])) {
					super.setLocalAddress(bindAddresses[i]);
				}
			}
		}
	}

	private ListenerList deviceReceiveDataListenerList = new ListenerList();

	public void addReceiveDataListener(ReceiveDataListener listener) {
		deviceReceiveDataListenerList.add(listener);
	}

	public void removeReceiveDataListener(ReceiveDataListener listener) {
		deviceReceiveDataListenerList.remove(listener);
	}

	public void performDataReceiveListener(SSDPPacket ssdpPacket) {
	    
		int listenerSize = deviceReceiveDataListenerList.size();
		for (int n = 0; n < listenerSize; n++) {
			ReceiveDataListener listener = (ReceiveDataListener) deviceReceiveDataListenerList.get(n);
		
			listener.DataReceived(ssdpPacket);
		}
	}

	// //////////////////////////////////////////////
	// run
	// //////////////////////////////////////////////

	private Thread deviceListenerThread = null;

	public void run() {
		Thread thisThread = Thread.currentThread();

		while (deviceListenerThread == thisThread) {
			Thread.yield();
			SSDPPacket packet = receive();
			if (packet == null)
				break;
			
			performDataReceiveListener(packet);
		}
	}

	public void start() {

		StringBuffer name = new StringBuffer("Cyber.deviceListenerThread/");
		DatagramSocket s = getDatagramSocket();
		// localAddr is null on Android m3-rc37a (01/30/08)
		InetAddress localAddr = s.getLocalAddress();
		if (localAddr != null) {
			name.append(s.getLocalAddress()).append(':');
			name.append(s.getLocalPort());
		}
		deviceListenerThread = new Thread(this, name.toString());
		deviceListenerThread.start();
	}

	public void stop() {
		deviceListenerThread = null;
		DatagramSocket s = getDatagramSocket();
		if (s != null)
			s.close();
	}

	// //////////////////////////////////////////////
	// post
	// //////////////////////////////////////////////

	public boolean post(String addr, int port, SSDPSearchResponse res) {
		return post(addr, port, res.getHeader());
	}

	// //////////////////////////////////////////////
	// post
	// //////////////////////////////////////////////

	public boolean post(String addr, int port, SSDPSearchRequest req) {
		return post(addr, port, req.toString());
	}
}
