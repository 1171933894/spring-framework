/*
 * Copyright 2002-2012 the original author or authors.
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

package org.springframework.aop;

/**
 * Core Spring pointcut abstraction.
 *
 * <p>A pointcut is composed of a {@link ClassFilter} and a {@link MethodMatcher}.
 * Both these basic terms and a Pointcut itself can be combined to build up combinations
 * (e.g. through {@link org.springframework.aop.support.ComposablePointcut}).
 *
 * @author Rod Johnson
 * @see ClassFilter
 * @see MethodMatcher
 * @see org.springframework.aop.support.Pointcuts
 * @see org.springframework.aop.support.ClassFilters
 * @see org.springframework.aop.support.MethodMatchers
 */

/**
 * 主要负责对系统的相应的Joinpoint进行捕捉，对系统中所有的对象进行Joinpoint所定义的规则进行匹配。提供
 * 了一个TruePointcut实例，当Pointcut为TruePointcut类型时，则会忽略所有的匹配条件，永远返回true
 *
 * ClassFilter与MethodMatcher分别用于在不同的级别上限定Joinpoint的匹配范围，满足不同粒度的匹配
 */
public interface Pointcut {

	/**
	 * Return the ClassFilter for this pointcut.
	 * @return the ClassFilter (never {@code null})
	 */
	//  ClassFilter限定在类级别上
	ClassFilter getClassFilter();

	/**
	 * Return the MethodMatcher for this pointcut.
	 * @return the MethodMatcher (never {@code null})
	 */
	// MethodMatcher限定在方法级别上
	MethodMatcher getMethodMatcher();


	/**
	 * Canonical Pointcut instance that always matches. 意思是：用于匹配上的一个实例
	 */
	Pointcut TRUE = TruePointcut.INSTANCE;

}
