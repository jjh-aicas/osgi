/*
 * Copyright 2009 Oracle Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */


package org.osgi.impl.service.jndi;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameNotFoundException;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.OperationNotSupportedException;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

class OSGiServiceListContext implements Context {

	private static final String	SERVICE_ID_PREFIX	= "(service.id=";
	
	private final BundleContext m_bundleContext;
	
	private final ServiceReference[] m_serviceReferences;
	
	/* map of service ids (Long) to ServiceReferences */
	private final Map m_mapOfServices = new HashMap(); 
	
	OSGiServiceListContext(BundleContext bundleContext, ServiceReference[] serviceReferences) {
		m_bundleContext = bundleContext;
		m_serviceReferences = serviceReferences;
		buildMapOfServices(m_mapOfServices, m_serviceReferences);
	}
	

	public Object addToEnvironment(String var0, Object var1)
			throws NamingException {
		operationNotSupported();
		return null;
	}

	public void bind(String var0, Object var1) throws NamingException {
		operationNotSupported();
	}

	public void bind(Name var0, Object var1) throws NamingException {
		operationNotSupported();
	}

	public void close() throws NamingException {
		// this operation is a no-op
	}

	public String composeName(String var0, String var1) throws NamingException {
		operationNotSupported();
		return null;
	}

	public Name composeName(Name var0, Name var1) throws NamingException {
		operationNotSupported();
		return null;
	}

	public Context createSubcontext(String var0) throws NamingException {
		operationNotSupported();
		return null;
	}

	public Context createSubcontext(Name var0) throws NamingException {
		operationNotSupported();
		return null;
	}

	public void destroySubcontext(String var0) throws NamingException {
		operationNotSupported();
	}

	public void destroySubcontext(Name var0) throws NamingException {
		operationNotSupported();
	}

	public Hashtable getEnvironment() throws NamingException {
		operationNotSupported();
		return null;
	}

	public String getNameInNamespace() throws NamingException {
		operationNotSupported();
		return null;
	}

	public NameParser getNameParser(String var0) throws NamingException {
		operationNotSupported();
		return null;
	}

	public NameParser getNameParser(Name var0) throws NamingException {
		operationNotSupported();
		return null;
	}

	public NamingEnumeration list(String name) throws NamingException {
		operationNotSupported();
		return null;
	}

	public NamingEnumeration list(Name var0) throws NamingException {
		operationNotSupported();
		return null;
	}

	public NamingEnumeration listBindings(String var0) throws NamingException {
		// TODO, implement listBindings support?
		operationNotSupported();
		return null;
	}

	public NamingEnumeration listBindings(Name var0) throws NamingException {
		operationNotSupported();
		return null;
	}

	public Object lookup(String name) throws NamingException {
		if(!name.startsWith(SERVICE_ID_PREFIX)) {
			throw new NamingException("Lookup was not in the correct (service.id=) form");
		}
		
		Long serviceId = getServiceIdFromLookupName(name);
		if(serviceId == null) {
			throw new NameNotFoundException("Service with the name = " + name + " does not exist in this context");
		} else {
			if(m_mapOfServices.containsKey(serviceId)) {
				ServiceReference serviceReference = (ServiceReference)m_mapOfServices.get(serviceId);
				return m_bundleContext.getService(serviceReference);
			} else {
				throw new NameNotFoundException("Service with the name = " + name + " does not exist in this context");
			}
		}
	}

	public Object lookup(Name var0) throws NamingException {
		operationNotSupported();
		return null;
	}

	public Object lookupLink(String var0) throws NamingException {
		operationNotSupported();
		return null;
	}

	public Object lookupLink(Name var0) throws NamingException {
		operationNotSupported();
		return null;
	}

	public void rebind(String var0, Object var1) throws NamingException {
		operationNotSupported();
	}

	public void rebind(Name var0, Object var1) throws NamingException {
		operationNotSupported();
	}

	public Object removeFromEnvironment(String var0) throws NamingException {
		operationNotSupported();
		return null;
	}

	public void rename(String var0, String var1) throws NamingException {
		operationNotSupported();
	}

	public void rename(Name var0, Name var1) throws NamingException {
		operationNotSupported();
	}

	public void unbind(String var0) throws NamingException {
		operationNotSupported();
	}

	public void unbind(Name var0) throws NamingException {
		operationNotSupported();
	}
	
	private void operationNotSupported() throws OperationNotSupportedException {
		throw new OperationNotSupportedException("This operation is not supported in an osgi:servicelist context");
	}
	
	private Long getServiceIdFromLookupName(String name) {
		if(!name.startsWith(SERVICE_ID_PREFIX)) {
			return null;
		} else {
			int indexOfClosingParenthesis = name.indexOf(')');
			if(indexOfClosingParenthesis >= 0) {
				String id = name.substring(SERVICE_ID_PREFIX.length(), indexOfClosingParenthesis);
				return new Long(id);
			} else {
				return null;
			}
			
		}
	}
	
	/**
	 * Convenience method for building a lookup map of service id's to services.  
	 * @param mapOfServices
	 * @param serviceReferences
	 */
	private static void buildMapOfServices(Map mapOfServices, ServiceReference[] serviceReferences) {
		for(int i = 0; i < serviceReferences.length; i++) {
			Long serviceId = (Long)serviceReferences[i].getProperty("service.id");
			mapOfServices.put(serviceId, serviceReferences[i]);
		}
	}
	
	
	private static class ServiceBasedListNamingEnumeration implements NamingEnumeration {

		private boolean m_isOpen = false;
		
		private int m_index = -1;
		
		private final BundleContext m_bundleContext;
		
		private final ServiceReference[] m_serviceReferences;
		
		ServiceBasedListNamingEnumeration(BundleContext bundleContext, ServiceReference[] serviceReferences) {
			m_bundleContext = bundleContext;
			m_serviceReferences = serviceReferences;
			if(m_serviceReferences.length > 0) {
				m_isOpen = true;
				m_index = 0;
			}
		}
		
		public void close() throws NamingException {
			m_isOpen = false;
		}

		public boolean hasMore() throws NamingException {
			return (m_index < m_serviceReferences.length);
		}

		public Object next() throws NamingException {
			throw new OperationNotSupportedException("This operation is not supported yet");
		}

		public boolean hasMoreElements() {
			return (m_index < m_serviceReferences.length);
		}

		public Object nextElement() {
			return m_serviceReferences[m_index++];
		}
		
	}

}