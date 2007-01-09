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

package org.ejbca.core.model.ca.certextensions;

import org.bouncycastle.asn1.DEREncodable;
import org.bouncycastle.asn1.DERPrintableString;
import org.ejbca.core.model.ca.caadmin.CA;
import org.ejbca.core.model.ca.certificateprofiles.CertificateProfile;
import org.ejbca.core.model.ra.UserDataVO;

/**
 * Dummy advanced certificate extension, used for demonstration
 * and testing of the certificate extension framework.
 * 
 * Should implement the getValue method.
 * 
 * 
 * @author Philip Vendil 2007 jan 5
 *
 * @version $Id: DummyAdvancedCertificateExtension.java,v 1.1 2007-01-09 16:47:19 herrvendil Exp $
 */

public class DummyAdvancedCertificateExtension extends CertificateExtension {

    private static String PROPERTY_VALUE = "value";

	/**
	 * The main method that should return a DEREncodable
	 * using the input data (optional) or defined properties (optional)
	 * 
	 * @see org.ejbca.core.model.ca.certextensions.CertificateExtension#getValue(org.ejbca.core.model.ra.UserDataVO, org.ejbca.core.model.ca.caadmin.CA, org.ejbca.core.model.ca.certificateprofiles.CertificateProfile)
	 */	
	public DEREncodable getValue(UserDataVO userData, CA ca,
			CertificateProfile certProfile) {
		
		String value = getProperties().getProperty(PROPERTY_VALUE);
		
		return new DERPrintableString(value);
	}

}
