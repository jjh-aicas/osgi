/*
 * Copyright (c) IBM Corporation (2009). All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.osgi.test.cases.webcontainer.optional.annotation.validate.junit;

import java.util.HashMap;
import java.util.Map;

import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.test.cases.webcontainer.util.ManifestHeadersTestBundleControl;

/**
 * @version $Rev$ $Date$
 * 
 *          test Bundle-ManifestVersion manifest header processed correctly with
 *          various scenarios
 */
public class BundleManifestVersionTest extends ManifestHeadersTestBundleControl {

    private Map<String, Object> createOptions(String version, String cp) {
        final Map<String, Object> options = new HashMap<String, Object>();
        options.put(Constants.BUNDLE_MANIFESTVERSION, version);
        options.put(WEB_CONTEXT_PATH, cp);
        return options;
    }

    /*
     * verify valid deployOptions overwrite original manifest
     * Bundle-ManifestVersion
     */
    public void testBundleManifestVersion002() throws Exception {
        final Map<String, Object> options = createOptions(MANIFESTVERSION1, "/tw2");
        this.b = super.installWar(options, "tw2.war", false);
        super.generalHeadersTest(options, "tw2.war", true, this.b);
    }

    /*
     * verify valid deployOptions overwrite original manifest
     * Bundle-ManifestVersion
     */
    public void testBundleManifestVersion003() throws Exception {
        final Map<String, Object> options = createOptions(MANIFESTVERSION1, "/tw3");
        this.b = super.installWar(options, "tw3.war", false);
        super.generalHeadersTest(options, "tw3.war", true, this.b);
    }

    /*
     * verify valid deployOptions overwrite original manifest
     * Bundle-ManifestVersion
     */
    public void testBundleManifestVersion007() throws Exception {
        final Map<String, Object> options = createOptions(MANIFESTVERSION1, "/tw2");
        this.b = super.installWar(options, "wmtw2.war", true);
        generalHeadersTest(options, "wmtw2.war", true, this.b);
    }

    /*
     * verify valid deployOptions overwrite original manifest
     * Bundle-ManifestVersion
     */
    public void testBundleManifestVersion008() throws Exception {
        final Map<String, Object> options = createOptions(MANIFESTVERSION1, "/tw3");
        this.b = super.installWar(options, "wmtw3.war", false);
        generalHeadersTest(options, "wmtw3.war", true, this.b);
    }
}