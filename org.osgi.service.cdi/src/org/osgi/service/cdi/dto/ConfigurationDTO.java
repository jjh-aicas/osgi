/*
 * Copyright (c) OSGi Alliance (2017). All Rights Reserved.
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

package org.osgi.service.cdi.dto;

import java.util.Map;
import org.osgi.service.cdi.dto.model.ConfigurationModelDTO;
import org.osgi.service.cdi.dto.model.DependencyModelDTO.MaximumCardinality;

/**
 * A snapshot of the runtime state of a {@link ComponentLifecycleDTO component
 * factory} configuration dependency
 *
 * @NotThreadSafe
 * @author $Id$
 */
public class ConfigurationDTO extends DependencyDTO {
	/**
	 * The static model of this configuration dependency as resolved at
	 * initialization time.
	 */
	public ConfigurationModelDTO	model;

	/**
	 * The set of configuration properties that match this configuration
	 * dependencies.
	 * <p>
	 * The value must not be null. An empty array indicates no matching
	 * configurations.
	 * <p>
	 * This dependency is satisfied when.
	 * <p>
	 * <pre>
	 * {@link DependencyDTO#minimumCardinality minimumCardinality} <= matches.size <= {@link MaximumCardinality#toInt() model.maximumCardinality.toInt()}
	 * </pre>
	 * <p>
	 * Each map contains the standard Configuration Admin keys
	 * <code>service.pid</code> and a <code>service.factoryPid<code> when
	 * {@link MaximumCardinality#MANY model.maximumCardinality=MANY}
	 */
	public Map<String, Object>[]	matches;
}