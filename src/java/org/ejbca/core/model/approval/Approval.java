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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.math.BigInteger;
import java.security.cert.X509Certificate;
import java.sql.Date;

import org.ejbca.util.CertTools;



/**
 * Class representing one approval of a request data. 
 * Includes information like:
 * Approval admin certificate
 * isApproved (rejected otherwise)
 * ApprovalDate
 * Comment
 *  
 * 
 * Approvals is sorted by dates.
 * 
 * @author Philip Vendil
 * @version $Id: Approval.java,v 1.1 2006-07-29 11:26:35 herrvendil Exp $
 */

public class Approval implements Comparable, Externalizable { 
	
	private static final int LATEST_VERSION = 1;

    private String adminCertIssuerDN = null;
    private String adminCertSerialNumber = null;
    private boolean approved = false;
    private Date approvalDate = null;
    private String comment = null;
    private String approvalSignature = null; 
    private String username = null;
    
    
	/**
	 * @param approved
	 * @param approvalDate
	 * @param comment
	 */
	public Approval(boolean approved, Date approvalDate, String comment) {
		super();
		this.approved = approved;
		this.approvalDate = approvalDate;
		this.comment = comment;
	}

	/**
	 * @return Returns the adminCertIssuerDN.
	 */
	public String getAdminCertIssuerDN() {
		return adminCertIssuerDN;
	}
	
	
	/**
	 * @return Returns the adminCertSerialNumber.
	 */
	public BigInteger getAdminCertSerialNumber() {
		return new BigInteger(adminCertSerialNumber,16);
	}
	
	
	/**
	 * @return Returns the approvalDate.
	 */
	public Date getApprovalDate() {
		return approvalDate;
	}
	
	
	/**
	 * @return Returns the approved.
	 */
	public boolean isApproved() {
		return approved;
	}
	
	
	/**
	 * @return Returns the comment.
	 */
	public String getComment() {
		return comment;
	}		
	
	/**
	 * @return Returns the username of the approving administrator
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * The cert and username of the approving administrator. Should only be set
	 * by the ApprovalSessionBean
	 * 
	 * 
	 */
	public void setApprovalCertificateAndUsername(X509Certificate approvalAdminCert, String username) {
		this.adminCertSerialNumber = approvalAdminCert.getSerialNumber().toString(16);
		this.adminCertIssuerDN = CertTools.getIssuerDN(approvalAdminCert);
		this.username = username;
	}

    /**
     * Sort by approval date
     */
	public int compareTo(Object arg0) {				
		return approvalDate.compareTo(((Approval) arg0).approvalDate);
	}

	public void writeExternal(ObjectOutput out) throws IOException {
		out.write(LATEST_VERSION);
		out.writeObject(this.adminCertIssuerDN);
		out.writeObject(this.adminCertSerialNumber);
		out.writeBoolean(this.approved);
		out.writeObject(this.approvalDate);
		out.writeObject(this.comment);	
		out.writeObject(this.approvalSignature);
		out.writeObject(this.username);
	}

	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        
		int version = in.readInt();
		if(version == 1){
			this.adminCertIssuerDN = (String) in.readObject();
			this.adminCertSerialNumber = (String) in.readObject();
			this.approved = in.readBoolean();
			this.approvalDate = (Date) in.readObject();
			this.comment = (String) in.readObject();
			this.approvalSignature = (String) in.readObject();
			this.username = (String) in.readObject();
		}
		
	}




}
