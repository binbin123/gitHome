/******************************************************************
 *
 *	CyberUPnP for Java
 *
 *	Copyright (C) Satoshi Konno 2002
 *
 *	File: SSDPSearchResponse.java
 *
 *	Revision;
 *
 *	01/14/03
 *		- first revision.
 *	
 ******************************************************************/

package org.cybergarage.upnp.ssdp;

import org.cybergarage.http.*;

import org.cybergarage.upnp.*;

import com.letv.upnpControl.tools.LetvUtils;

public class SSDPSearchResponse extends SSDPResponse {
	// //////////////////////////////////////////////
	// Constructor
	// //////////////////////////////////////////////

//	public SSDPSearchResponse() {
//		setStatusCode(HTTPStatus.OK);
//		setCacheControl(Device.DEFAULT_LEASE_TIME);
//		setHeader(HTTP.SERVER, UPnP.getServerName());
//		if (LetvUtils.isDmrOnly()) {
//			if (LetvUtils.isHideNameFunc()) {
//				setHeader(HTTP.EXT, "dpkk_letv");
//			} else {
//				setHeader(HTTP.EXT, "dpkk");
//			}
//
//		}
//	}
	public SSDPSearchResponse(String ext) {
		setStatusCode(HTTPStatus.OK);
		setCacheControl(Device.DEFAULT_LEASE_TIME);
		setHeader(HTTP.SERVER, UPnP.getServerName());
		setHeader(HTTP.EXT, ext);
			
	}
}
