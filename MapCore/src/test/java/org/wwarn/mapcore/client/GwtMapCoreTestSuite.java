package org.wwarn.mapcore.client;

/*
 * #%L
 * MapCore
 * %%
 * Copyright (C) 2013 - 2014 University of Oxford
 * %%
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. Neither the name of the University of Oxford nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

import com.google.gwt.junit.tools.GWTTestSuite;
import junit.framework.Test;
import junit.framework.TestCase;
import org.wwarn.mapcore.client.components.customwidgets.GwtTestGenericMapWidget;
import org.wwarn.mapcore.client.components.customwidgets.GwtTestGenericMarker;
import org.wwarn.mapcore.client.components.customwidgets.facet.GwtTestFacetBuilder;
import org.wwarn.mapcore.client.utils.GwtTestXMLUtils;

/**
 * GWtTestSuite is run as mvn integration tests, Gwt* prefix test are ignored by mvn, see pom.xml for more info.
 * User: nigel
 * Date: 26/07/13
 * Time: 16:23
 */
public class GwtMapCoreTestSuite extends TestCase {
    public static Test suite(){
        GWTTestSuite suite = new GWTTestSuite( "All Gwt Tests go in here" );
        suite.addTestSuite( GwtTestFacetBuilder.class );
        suite.addTestSuite( GwtTestGenericMapWidget.class );
        suite.addTestSuite( GwtTestGenericMarker.class );
        suite.addTestSuite( GwtTestXMLUtils.class );
        return suite;
    }
}
