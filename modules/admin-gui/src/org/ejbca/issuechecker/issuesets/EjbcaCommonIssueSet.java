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

package org.ejbca.issuechecker.issuesets;

import java.util.Set;

import org.ejbca.issuechecker.Issue;
import org.ejbca.issuechecker.IssueSet;
import org.ejbca.issuechecker.issues.EccWithKeyEncipherment;
import org.ejbca.issuechecker.issues.NotInProductionMode;

import com.google.common.collect.ImmutableSet;

/**
 * Issue set containing issues which should be checked on a typical EJBCA installation.
 *
 * @version $Id$
 */
public class EjbcaCommonIssueSet extends IssueSet {
    private final Set<Class<? extends Issue>> issues = new ImmutableSet.Builder<Class<? extends Issue>>()
            .add(NotInProductionMode.class)
            .add(EccWithKeyEncipherment.class)
            .build();

    @Override
    public Set<Class<? extends Issue>> getIssues() {
        return issues;
    }

    @Override
    public String getDatabaseValue() {
        return "EjbcaCommonIssueSet";
    }

    @Override
    public String getTitleLanguageString() {
        return "EJBCA_COMMON_ISSUESET_TITLE";
    }

    @Override
    public String getDescriptionLanguageString() {
        return "EJBCA_COMMON_ISSUESET_DESCRIPTION";
    }

}
