/*
 * Copyright 2002-2017 the original author or authors.
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

package org.springframework.core.env;

/**
 * Interface indicating a component that contains and exposes an {@link Environment} reference.
 *
 * <p>All Spring application contexts are EnvironmentCapable, and the interface is used primarily
 * for performing {@code instanceof} checks in framework methods that accept BeanFactory
 * instances that may or may not actually be ApplicationContext instances in order to interact
 * with the environment if indeed it is available.
 *
 * <p>As mentioned, {@link org.springframework.context.ApplicationContext ApplicationContext}
 * extends EnvironmentCapable, and thus exposes a {@link #getEnvironment()} method; however,
 * {@link org.springframework.context.ConfigurableApplicationContext ConfigurableApplicationContext}
 * redefines {@link org.springframework.context.ConfigurableApplicationContext#getEnvironment
 * getEnvironment()} and narrows the signature to return a {@link ConfigurableEnvironment}.
 * The effect is that an Environment object is 'read-only' until it is being accessed from
 * a ConfigurableApplicationContext, at which point it too may be configured.
 *
 * @author Chris Beams
 * @since 3.1
 * @see Environment
 * @see ConfigurableEnvironment
 * @see org.springframework.context.ConfigurableApplicationContext#getEnvironment()
 */

/**
 * 实现了此接口的类有应该有一个Environment类型的域，并且可以通过getEnvironment方法取得。 Spring中所有的
 * 应用上下文类都实现了此接口。这个接口的主要作用是用于类型检查的。例如框架中有些与用户定义的BeanFactory交
 * 互的方法，这些方法有些就需要使用用户定义的BeanFactory的环境变量。这个时候就要看其是否是EnvironmentCapable
 * 接口的子类了。 Spring中所有的应用上下文都实现了EnvironmentCapable接口。但是ConfigurableApplicationContext
 * 接口重新定义了getEnvironment方法，并将其返回值限定为ConfigurableEnvironment，这样的后果就是，使用
 * ConfigurableApplicationContext接口会覆盖Environment接口。
 */
public interface EnvironmentCapable {

	/**
	 * Return the {@link Environment} associated with this component.
	 */
	// 返回与此组件关联的Environment（可以为null或默认环境）
	Environment getEnvironment();

}
