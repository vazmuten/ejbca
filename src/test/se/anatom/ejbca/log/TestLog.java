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

package se.anatom.ejbca.log;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import javax.naming.Context;
import javax.naming.NamingException;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.ejbca.core.ejb.log.ILogSessionHome;
import org.ejbca.core.ejb.log.ILogSessionRemote;
import org.ejbca.core.model.log.Admin;
import org.ejbca.core.model.log.CsvLogExporter;
import org.ejbca.core.model.log.ILogDevice;
import org.ejbca.core.model.log.ILogExporter;
import org.ejbca.core.model.log.LogConfiguration;
import org.ejbca.core.model.log.LogConstants;
import org.ejbca.core.model.log.LogEntry;
import org.ejbca.util.query.BasicMatch;
import org.ejbca.util.query.LogMatch;
import org.ejbca.util.query.Query;

/**
 * Tests the log modules entity and session beans.
 *
 * @version $Id: TestLog.java,v 1.7 2007-12-04 14:23:04 jeklund Exp $
 */
public class TestLog extends TestCase {
    private static Logger log = Logger.getLogger(TestLog.class);

    private ILogSessionRemote cacheLogSession;

    private static ILogSessionHome cacheHome;

    private Admin admin = new Admin(Admin.TYPE_INTERNALUSER);

    /**
     * Creates a new TestLog object.
     *
     * @param name name
     */
    public TestLog(String name) {
        super(name);
    }

    protected void setUp() throws Exception {

        log.debug(">setUp()");

        if (cacheLogSession == null) {
            if (cacheHome == null) {
                Context jndiContext = getInitialContext();
                Object obj1 = jndiContext.lookup("LogSession");
                cacheHome = (ILogSessionHome) javax.rmi.PortableRemoteObject.narrow(obj1, ILogSessionHome.class);

            }

            cacheLogSession = cacheHome.create();
        }


        log.debug("<setUp()");
    }

    protected void tearDown() throws Exception {
    }

    private Context getInitialContext() throws NamingException {
        log.debug(">getInitialContext");

        Context ctx = new javax.naming.InitialContext();
        log.debug("<getInitialContext");

        return ctx;
    }


    /**
     * tests adding a log configuration and checks if it can be read again.
     *
     * @throws Exception error
     */
    public void test01AddLogConfiguration() throws Exception {
        log.debug(">test01AddLogConfiguration()");

        LogConfiguration logconf = new LogConfiguration();
        logconf.setLogEvent(LogConstants.EVENT_INFO_DATABASE, false);
        logconf.setLogEvent(LogConstants.EVENT_ERROR_DATABASE, true);

        cacheLogSession.saveLogConfiguration(admin, "CN=TEST".hashCode(), logconf);

        LogConfiguration logconf2 = cacheLogSession.loadLogConfiguration("CN=TEST".hashCode());
        assertTrue("Couldn't retrieve correct log confirguration data from database.", !logconf2.getLogEvent(LogConstants.EVENT_INFO_DATABASE).booleanValue());
        assertTrue("Couldn't retrieve correct log confirguration data from database.", logconf2.getLogEvent(LogConstants.EVENT_ERROR_DATABASE).booleanValue());

        log.debug("<test01AddLogConfiguration()");
    }

    /**
     * tests adds some log events and checks that they have been stored
     * correctly.
     *
     * @throws Exception error
     */
    public void test02AddAndCheckLogEvents() throws Exception {
        log.debug(">test02AddAndCheckLogEvents()");

        cacheLogSession.log(admin, "CN=TEST".hashCode(), LogConstants.MODULE_LOG, new Date(), null, null, LogConstants.EVENT_ERROR_UNKNOWN, "Test");


        Collection logDeviceNames = cacheLogSession.getAvailableLogDevices();
        Iterator iterator = logDeviceNames.iterator();
        Collection result = null;
        while (iterator.hasNext()) {
        	String logDeviceName = (String) iterator.next();
        	if (logDeviceName.equalsIgnoreCase("Log4JLogDevice")) {
        		continue;
        	}
        	Query query = new Query(Query.TYPE_LOGQUERY);
        	query.add(LogMatch.MATCH_WITH_COMMENT,BasicMatch.MATCH_TYPE_EQUALS,"Test");
        	result = cacheLogSession.query(logDeviceName, query, "", "caid=" + Integer.toString("CN=TEST".hashCode()));
        	Iterator iter = result.iterator();
        	boolean found = false;
        	while (iter.hasNext()) {
        		LogEntry entry = (LogEntry) iter.next();
        		if ( (entry.getComment() != null) && (entry.getComment().equals("Test")) ) {
        			found = true;
        		}
        	}
        	assertTrue("Couldn't retrieve correct log data from database.", found);
        }

 	   ILogExporter exporter = new CsvLogExporter();
 	   exporter.setEntries(result);
	   byte[] export = exporter.export();
	   assertNotNull(export);
	   String str = new String(export);
	   //assertEquals("foo", str);
	   int ind = str.indexOf("Test\t");
	   assertTrue(ind > 0);

        log.debug("<test02AddAndCheckLogEvents()");
    }
}
