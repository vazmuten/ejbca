/*************************************************************************
 *                                                                       *
 *  EJBCA: The OpenSource Certificate Authority                          *
 *                                                                       *
 *  This software is free software; you can redistribute it and/or       *
 *  modify it under the terms of the GNU Lesser General Public           *
 *  License as published by the Free Software Foundation; either         *
 *  version 2.1 of the License, or any later version.                    *
 *                                                                       *
 *  See terms of license at gnu.org.                                     *
 *                                                                       *
 *************************************************************************/

package org.ejbca.core.protocol.ocsp;


public interface IAuditLogger extends IOCSPLogger {

	public static final String OCSPREQUEST = "OCSPREQUEST";	//The byte[] ocsp-request that came with the http-request
	public static final String OCSPRESPONSE = "OCSPRESPONSE"; //The byte[] ocsp-response that was included in the http-response

}
