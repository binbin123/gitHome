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
import org.cybergarage.upnp.*;
import org.cybergarage.util.Debug;
import org.cybergarage.util.ListenerList;
import org.cybergarage.upnp.device.SearchListener;
import org.cybergarage.net.HostInterface;

import android.util.Log;

public class SSDPBCResponeSocket extends HTTPUSocket implements Runnable
{
	////////////////////////////////////////////////
	//	Constructor
	////////////////////////////////////////////////
     public SSDPBCResponeSocket(int port){
		super(port);
		String[] bindAddresses;

		int nHostAddrs = HostInterface.getNHostAddresses();
		bindAddresses = new String[nHostAddrs]; 
		for (int n=0; n<nHostAddrs; n++) {
			bindAddresses[n] = HostInterface.getHostAddress(n);
		}

		for (int i = 0; i < bindAddresses.length; i++) {
			if(bindAddresses[i]!=null){
				//Debug.d("SSDPBCResponeSocket", "i = " + i + "bindAddresses =" + bindAddresses[i]); 
				if(HostInterface.isIPv4Address(bindAddresses[i])){
					super.setLocalAddress(bindAddresses[i]);
				}		
			}
		}
	}
	private ListenerList deviceSearchListenerList = new ListenerList();
	 	
	public void addSearchListener(SearchListener listener)
	{
		deviceSearchListenerList.add(listener);
	}		

	public void removeSearchListener(SearchListener listener)
	{
		deviceSearchListenerList.remove(listener);
	}		

	public void performSearchListener(SSDPPacket ssdpPacket)
	{
	    
		//Log.d("ssdpbc","ssdp bc socket data listener");
		int listenerSize = deviceSearchListenerList.size();
		for (int n=0; n<listenerSize; n++) {
			SearchListener listener = (SearchListener)deviceSearchListenerList.get(n);
			listener.deviceSearchReceived(ssdpPacket);
		}
	}		


	////////////////////////////////////////////////
	//	run	
	////////////////////////////////////////////////

	private Thread deviceBroadcastResponseThread = null;
		
	public void run()
	{
		Thread thisThread = Thread.currentThread();
		
		while (deviceBroadcastResponseThread == thisThread) {
			Thread.yield();
			SSDPPacket packet = receive();
			if (packet == null)
				break;
			performSearchListener(packet);
		}
	}
	
	public void start()	{

		StringBuffer name = new StringBuffer("Cyber.SSDPBroadcastResponseSocket/");
		DatagramSocket s = getDatagramSocket();
		// localAddr is null on Android m3-rc37a (01/30/08)
		InetAddress localAddr = s.getLocalAddress();
		if (localAddr != null) {
			name.append(s.getLocalAddress()).append(':');
			name.append(s.getLocalPort());
		}
		deviceBroadcastResponseThread = new Thread(this,name.toString());
		deviceBroadcastResponseThread.start();		
	}
	
	public void stop()
	{
		deviceBroadcastResponseThread = null;
		DatagramSocket s = getDatagramSocket();
		if(s != null)
			s.close();
	}

	////////////////////////////////////////////////
	//	post
	////////////////////////////////////////////////

	public boolean post(String addr, int port, SSDPSearchResponse res)
	{
		return post(addr, port, res.getHeader());
	}

	////////////////////////////////////////////////
	//	post
	////////////////////////////////////////////////

	public boolean post(String addr, int port, SSDPSearchRequest req)
	{
		return post(addr, port, req.toString());
	}
}


