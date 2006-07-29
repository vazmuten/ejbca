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
package org.ejbca.core.model.approval;

/**
 * General Exception when something serious goes wrong when
 * managing approvals
 * 
 * @author Philip Vendil
 * @version $Id: ApprovalException.java,v 1.1 2006-07-29 11:26:35 herrvendil Exp $
 */
public class ApprovalException extends Exception {


	public ApprovalException(String message, Throwable cause) {
		super(message, cause);
	}

	public ApprovalException(String message) {
		super(message);
	}

}
