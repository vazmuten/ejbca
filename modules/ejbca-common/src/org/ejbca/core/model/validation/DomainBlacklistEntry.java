/*************************************************************************
 *                                                                       *
 *  EJBCA Community: The OpenSource Certificate Authority                *
 *                                                                       *
 *  This software is free software; you can redistribute it and/or       *
 *  modify it under the terms of the GNU Lesser General Public           *
 *  License as published by the Free Software Foundation; either         *
 *  version 2.1 of the License, or any later version.                    *
 *                                                                       *
 *  See terms of license at gnu.org.                                     *
 *                                                                       *
 *************************************************************************/

package org.ejbca.core.model.validation;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import org.apache.commons.lang.StringUtils;

/**
 * Domain class representing a domain blacklist entry.
 * @version $Id$
 */

public class DomainBlacklistEntry extends BlacklistEntry implements Serializable, Cloneable {
    
    private static final long serialVersionUID = 542785977370630861L;
        
    public static final String TYPE="DOMAINBLACKLIST";

    /**
     * Creates a new instance.
     */
    public DomainBlacklistEntry() {
        super(DomainBlacklistEntry.TYPE);
    }
    
    /**
     * Creates a new instance.
     */
    public DomainBlacklistEntry(int id, String blacklist, String blacklistFileName) {
        super(id, DomainBlacklistEntry.TYPE, blacklist, blacklistFileName);
    }
    
    @Override
    public String getType() {
        return DomainBlacklistEntry.TYPE;
    }
    
    /**
     * Get the file name
     * @return the file name
     */
    public String getBlackListFileName() {
        return getValue();
    }
    
    /**
     * Set the file name
     * @param fileName the file name
     */
    public void setBlackListFileName(String fileName) {
        setValue(fileName);
    }
    
    /**
     * Get the blacklist domains
     * @return a string containing the blacklisted domains
     */
    public String getBlacklistedDomains() {
        return getData();
    }
    
    /**
     * Set the blacklisted domains
     * @param domainBlacklistByteArray containing the blacklisted domains to be parsed
     */
    public void setBlacklistedDomains(byte[] domainBlacklistByteArray) {
        setData(createValueFromFile(domainBlacklistByteArray));
    }
    
    /**
     * Parsing a byte array to set blacklisted domains 
     * @param domainBlacklistByteArray the byte array to parse
     * @return the String representation of the domain blacklist
     */
    public String createValueFromFile(byte[] domainBlacklistByteArray) {
        String valueString = null;
        try {
            StringBuilder valueStringBuilder = new StringBuilder();
            InputStream domainBlacklistInputStream = new ByteArrayInputStream(domainBlacklistByteArray);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(domainBlacklistInputStream));
            String blacklistFileline;
            while ((blacklistFileline = bufferedReader.readLine()) != null) {
                if (blacklistFileline.contains("#") || blacklistFileline.isEmpty()) {
                    continue;
                }
                blacklistFileline = StringUtils.deleteWhitespace(blacklistFileline);
                valueStringBuilder.append(blacklistFileline);
                valueStringBuilder.append(";");
            }
            bufferedReader.close();
            valueString = valueStringBuilder.toString();
            if (valueString.charAt(valueString.length() - 1) == ';') {
                valueString = valueString.substring(0, valueString.length() - 1);
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to parse domain black lists. ", e);
        }  
        return valueString;
    }
}
