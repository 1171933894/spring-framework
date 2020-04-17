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

package org.springframework.aop.support;

import java.lang.reflect.Method;

import org.springframework.aop.MethodMatcher;

/**
 * Convenient abstract superclass for dynamic method matchers,
 * which do care about arguments at runtime.
 *
 * @author Rod Johnson
 */
public abstract class DynamicMethodMatcher implements MethodMatcher {

	// 永远返回true
	@Override
	public final boolean isRuntime() {
		return true;
	}

	/**
	 * Can override to add preconditions（前提条件）for dynamic matching. This implementation
	 * always returns true.
	 */
	// 永远返回true，去匹配动态匹配的方法即可
	@Override
	public boolean matches(Method method, Class<?> targetClass) {
		return true;
	}

}
