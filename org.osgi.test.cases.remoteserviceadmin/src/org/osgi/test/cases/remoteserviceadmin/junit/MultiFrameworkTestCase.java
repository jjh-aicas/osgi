/*
 * Copyright (c) OSGi Alliance (2009). All Rights Reserved.
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
package org.osgi.test.cases.remoteserviceadmin.junit;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import org.osgi.service.packageadmin.ExportedPackage;
import org.osgi.service.packageadmin.PackageAdmin;
import org.osgi.test.support.compatibility.DefaultTestBundleControl;

/**
 * @author <a href="mailto:tdiekman@tibco.com">Tim Diekmann</a>
 *
 */
public abstract class MultiFrameworkTestCase extends DefaultTestBundleControl /*OSGiTestCase*/ {
	private static final String STORAGEROOT = "org.osgi.test.cases.remoteserviceadmin.storageroot";
	private static final String DEFAULT_STORAGEROOT = "generated/testframeworkstorage";
	private static final String FRAMEWORK_FACTORY = "/META-INF/services/org.osgi.framework.launch.FrameworkFactory";
	private Framework framework;
	private String frameworkFactoryClassName;
	private FrameworkFactory frameworkFactory;
	private String rootStorageArea;

	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		frameworkFactoryClassName = getFrameworkFactoryClassName();
		assertNotNull("Could not find framework factory class", frameworkFactoryClassName);
		frameworkFactory = getFrameworkFactory();

		rootStorageArea = getStorageAreaRoot();
		assertNotNull("No storage area root found", rootStorageArea);
		File rootFile = new File(rootStorageArea);
		delete(rootFile);
		assertFalse("Root storage area is not a directory: " + rootFile.getPath(), rootFile.exists() && !rootFile.isDirectory());
		if (!rootFile.isDirectory())
			assertTrue("Could not create root directory: " + rootFile.getPath(), rootFile.mkdirs());
		
		Map<String, String> configuration = getConfiguration();
		configuration.put(Constants.FRAMEWORK_STORAGE, rootFile.getAbsolutePath());
		
		framework = createFramework(configuration);
		initFramework();
		startFramework();
		
		installFramework();
	}


	/**
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		stopFramework();
		super.tearDown();
	}

	/**
	 * @return started Framework instance
	 */
	public Framework getFramework() {
		Framework f = framework;
		if (f == null || f.getState() != Bundle.ACTIVE) {
			fail("Framework is not started yet");
		}
		return f;
	}
	
	/**
	 * This method is implemented by subclasses, which contain the test cases
	 * @return Map with framework properties.
	 */
	public abstract Map<String, String> getConfiguration();

	/**
	 * Install a bundle into the framework.
	 * @param bundle Bundle location to install
	 * @return Bundle that was created by the framework and installed
	 * @throws BundleException
	 * @throws IOException
	 */
	public Bundle installBundle(String bundle) throws BundleException, IOException {
		BundleContext fwkContext = getFramework().getBundleContext();
		assertNotNull("Framework context is null", fwkContext);
		URL input = getBundleInput(bundle);
		assertNotNull("Cannot find resource: " + bundle, input);
		return fwkContext.installBundle(bundle, input.openStream());
	}

	private String getFrameworkFactoryClassName() throws IOException {
		BundleContext context = getBundleContextWithoutFail();
        URL factoryService = context == null ? this.getClass().getResource(FRAMEWORK_FACTORY) : context.getBundle(0).getEntry(FRAMEWORK_FACTORY);
		assertNotNull("Could not locate: " + FRAMEWORK_FACTORY, factoryService);
		return getClassName(factoryService);

	}

	private String getClassName(URL factoryService) throws IOException {
		InputStream in = factoryService.openStream();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(in));
			for (String line = br.readLine(); line != null; line=br.readLine()) {
				int pound = line.indexOf('#');
				if (pound >= 0)
					line = line.substring(0, pound);
				line.trim();
				if (!"".equals(line))
					return line;
			}
		} finally {
			try {
				if (br != null)
					br.close();
			}
			catch (IOException e) {
				// did our best; just ignore
			}
		}
		return null;
	}

	private String getStorageAreaRoot() {
		BundleContext context = getBundleContextWithoutFail();
		if (context == null) {
			String storageroot = System.getProperty(STORAGEROOT, DEFAULT_STORAGEROOT);
			assertNotNull("Must set property: " + STORAGEROOT, storageroot);
			return storageroot;
		}
		return context.getDataFile("storageroot").getAbsolutePath();
	}

	private Class loadFrameworkClass(String className)
			throws ClassNotFoundException {
		BundleContext context = getBundleContextWithoutFail();
        return context == null ? Class.forName(className) : getContext().getBundle(0).loadClass(className);
	}

	private BundleContext getBundleContextWithoutFail() {
		try {
			if ("true".equals(System.getProperty("noframework")))
				return null;
			return getContext();
		} catch (Throwable t) {
			return null; // don't fail
		}
	}

	private FrameworkFactory getFrameworkFactory() {
		try {
			Class clazz = loadFrameworkClass(frameworkFactoryClassName);
			return (FrameworkFactory) clazz.newInstance();
		} catch (Exception e) {
			fail("Failed to get the framework constructor", e);
		}
		return null;
	}

	private boolean delete(File file) {
		if (file.exists()) {
			if (file.isDirectory()) {
				String list[] = file.list();
				if (list != null) {
					int len = list.length;
					for (int i = 0; i < len; i++)
						if (!delete(new File(file, list[i])))
							return false;
				}
			}

			return file.delete();
		}
		return (true);
	}

	private Framework createFramework(Map configuration) {
		Framework framework = null;
		try {
			framework = frameworkFactory.newFramework(configuration);
		}
		catch (Exception e) {
			fail("Failed to construct the framework", e);
		}
		assertEquals("Wrong state for newly constructed framework", Bundle.INSTALLED, framework.getState());
		return framework;
	}

	private URL getBundleInput(String bundle) {
		BundleContext context = getBundleContextWithoutFail();
		return context == null ? this.getClass().getResource(bundle) : context.getBundle().getEntry(bundle);
	}


	private void initFramework() {
		try {
			framework.init();
			assertNotNull("BundleContext is null after init", framework.getBundleContext());
		}
		catch (BundleException e) {
			fail("Unexpected BundleException initializing", e);
		}
		assertEquals("Wrong framework state after init", Bundle.STARTING, framework.getState());
	}

	private void startFramework() {
		try {
			framework.start();
			assertNotNull("BundleContext is null after start", framework.getBundleContext());
		}
		catch (BundleException e) {
			fail("Unexpected BundleException initializing", e);
		}
		assertEquals("Wrong framework state after init", Bundle.ACTIVE, framework.getState());

	}

	private void stopFramework() {
		int previousState = framework.getState();
		try {
            framework.stop();
			FrameworkEvent event = framework.waitForStop(10000);
			assertNotNull("FrameworkEvent is null", event);
			assertEquals("Wrong event type", FrameworkEvent.STOPPED, event.getType());
			assertNull("BundleContext is not null after stop", framework.getBundleContext());
		}
		catch (BundleException e) {
			fail("Unexpected BundleException stopping", e);
		}
		catch (InterruptedException e) {
			fail("Unexpected InterruptedException waiting for stop", e);
		}
		// if the framework was not STARTING STOPPING or ACTIVE then we assume the waitForStop returned immediately with a FrameworkEvent.STOPPED 
		// and does not change the state of the framework
		int expectedState = (previousState & (Bundle.STARTING | Bundle.ACTIVE | Bundle.STOPPING)) != 0 ? Bundle.RESOLVED : previousState;
		assertEquals("Wrong framework state after init", expectedState, framework.getState());
	}

	private void installFramework() throws Exception {
		System.out.println("Installing child framework");
		
		Framework f = getFramework();
		
		List bundles = new LinkedList();
		
		StringTokenizer st = new StringTokenizer(System.getProperty(
				"org.osgi.test.cases.remoteserviceadmin.bundles", ""), "|");
		while (st.hasMoreTokens()) {
			String bundle = st.nextToken();
			
			Bundle b = f.getBundleContext().installBundle("file:" + bundle);
			assertNotNull(b);
			assertEquals("Bundle " + b.getSymbolicName() + " is not INSTALLED", Bundle.INSTALLED, b.getState());
			
			System.out.println("installed bundle " + b.getSymbolicName() + " " + b.getVersion());
			bundles.add(b);
		}
		
		for (Iterator it = bundles.iterator(); it.hasNext();) {
			Bundle b = (Bundle) it.next();
			
			if (b.getHeaders().get(Constants.FRAGMENT_HOST) == null) {
				b.start();
				assertEquals("Bundle " + b.getSymbolicName() + " is not ACTIVE", Bundle.ACTIVE, b.getState());
				
				System.out.println("started bundle " + b.getSymbolicName());
			}
		}
	}
	/**
	 * Verifies the child framework that it exports the test packages for the interface
	 * used by the test service.
	 * @throws Exception
	 */
	protected void verifyFramework() throws Exception {
		Framework f = getFramework();
//		assertFalse("child framework must have a different UUID",
//				getContext().getProperty("org.osgi.framework.uuid").equals(f.getBundleContext().getProperty("org.osgi.framework.uuid")));
		
		ServiceReference sr = f.getBundleContext().getServiceReference(PackageAdmin.class.getName());
		assertNotNull("Framework is not supplying PackageAdmin service", sr);
		
		PackageAdmin pkgAdmin = (PackageAdmin) f.getBundleContext().getService(sr);
		ExportedPackage[] exportedPkgs = pkgAdmin.getExportedPackages(f.getBundleContext().getBundle());
		assertNotNull(exportedPkgs);
		f.getBundleContext().ungetService(sr);
		
		String pkgXtras = f.getBundleContext().getProperty(Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA);
		List<String> pkgList = splitString(pkgXtras, ",");
		
		for (int i=0;i<exportedPkgs.length;i++) {
			String name = exportedPkgs[i].getName();
			pkgList.remove(name);
		}
		assertTrue("Framework does not export some packages " + pkgList, pkgList.isEmpty());
	}

	/**
	 * Install a bundle using the given BundleContext
	 * 
	 * @param context BundleContext of target framework
	 * @param bundle URL to the bundle.
	 * @return Bundle object
	 */
	protected Bundle installBundle(BundleContext context, String bundle) throws Exception {
		if (!bundle.startsWith(getWebServer())) {
			bundle = getWebServer() + bundle;
		}
		URL location = new URL(bundle);
		InputStream inputStream = location.openStream();
		
		Bundle b = context.installBundle(bundle, inputStream);
		return b;
	}

	private List<String> splitString(String string, String delim) {
		List<String> result = new ArrayList<String>();
		for (StringTokenizer st = new StringTokenizer(string, delim); st
				.hasMoreTokens();) {
			String token = st.nextToken();
			int pos = token.indexOf(";");
			if (pos != -1) {
				token = token.substring(0, pos);
			}
			result.add(token);
		}
		return result;
	}
}