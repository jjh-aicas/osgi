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

package org.osgi.service.cdi.runtime.dto;

import org.osgi.dto.DTO;

/**
 * A snapshot of the runtime state of an extension dependency of a
 * {@link ContainerDTO container}
 *
 * @NotThreadSafe
 * @author $Id$
 */
public class ExtensionDTO extends DTO {
	/**
	 * The template of this extension dependency
	 * <p>
	 * Must not be {@code null}
	 */
	public String	name;

	/**
	 * The service to which the extension dependency is resolved.
	 * <p>
	 * This extension dependency is satisfied when {@code match > 0}.
	 */
	public long		match;
}