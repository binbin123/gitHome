/******************************************************************
*
*	CyberHTTP for Java
*
*	Copyright (C) Satoshi Konno 2002-2003
*
*	File: HTTPServerThread.java
*
*	Revision;
*
*	10/10/03
*		- first revision.
*	
******************************************************************/

package org.cybergarage.http;

import java.net.Socket;

import org.cybergarage.util.Debug;

import android.util.Log;

public class HTTPServerThread extends Thread
{
	private HTTPServer httpServer;
	private Socket sock;
	
	////////////////////////////////////////////////
	//	Constructor
	////////////////////////////////////////////////
	
	public HTTPServerThread(HTTPServer httpServer, Socket sock)
	{
        super("Cyber.HTTPServerThread");
		this.httpServer = httpServer;
		this.sock = sock;
	}

	////////////////////////////////////////////////
	//	run	
	////////////////////////////////////////////////

	public void run()
	{
		HTTPSocket httpSock = new HTTPSocket(sock);
		if (httpSock.open() == false)
			return;
		
		HTTPRequest httpReq = new HTTPRequest();
		httpReq.setSocket(httpSock);
		
		
		while (httpReq.read() == true) {
			Debug.d("HTTPServer","httpReq content string = " + httpReq.getContent());
			Debug.d("HTTPServer","httpReq head string = " + httpReq.getHeader());
			httpServer.performRequestListener(httpReq);
			Debug.d("HTTPServer","httpReq.isKeepAlive = " + httpReq.isKeepAlive());	
			if (httpReq.isKeepAlive() == false)
				break;
		}
		Debug.d("HTTPServer","HTTPServerThread close = " +  httpSock + "sock RemoteSocketAddress = " +  sock.getRemoteSocketAddress());
		httpSock.close();
	}
}
