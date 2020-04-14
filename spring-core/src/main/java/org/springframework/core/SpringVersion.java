/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.core;

import org.springframework.lang.Nullable;

/**
 * Class that exposes the Spring version. Fetches the
 * "Implementation-Version" manifest attribute from the jar file.
 *
 * <p>Note that some ClassLoaders do not expose the package metadata,
 * hence（因此）this class might not be able to determine the Spring version
 * in all environments. Consider using a reflection-based check instead:
 * For example, checking for the presence of a specific Spring 5.0
 * method that you intend to call.
 *
 * @author Juergen Hoeller
 * @since 1.1
 */
public final class SpringVersion {

	private SpringVersion() {
	}


	/**
	 * Return the full version string of the present Spring codebase,
	 * or {@code null} if it cannot be determined.
	 * @see Package#getImplementationVersion()
	 */
	@Nullable
	public static String getVersion() {
		Package pkg = SpringVersion.class.getPackage();
		/**
		 * 产品发布会打成一个JAR包.JAR除了包含.class文件外,还包括一个META-INF
		 * 文件夹.它下面又包含了一个MANIFEST.MF的文件.它包含了这个产品的产品信息
		 */
		return (pkg != null ? pkg.getImplementationVersion() : null);
	}

}
