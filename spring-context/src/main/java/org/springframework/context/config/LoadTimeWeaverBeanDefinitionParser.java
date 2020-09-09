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

package org.springframework.context.config;

import org.w3c.dom.Element;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.weaving.AspectJWeavingEnabler;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;

/**
 * Parser for the &lt;context:load-time-weaver/&gt; element.
 *
 * @author Juergen Hoeller
 * @since 2.5
 */
class LoadTimeWeaverBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

	/**
	 * The bean name of the internally managed AspectJ weaving enabler.
	 * @since 4.3.1
	 */
	public static final String ASPECTJ_WEAVING_ENABLER_BEAN_NAME =
			"org.springframework.context.config.internalAspectJWeavingEnabler";// AspectJWeavingEnabler的别名

	private static final String ASPECTJ_WEAVING_ENABLER_CLASS_NAME =
			"org.springframework.context.weaving.AspectJWeavingEnabler";

	private static final String DEFAULT_LOAD_TIME_WEAVER_CLASS_NAME =
			"org.springframework.context.weaving.DefaultContextLoadTimeWeaver";

	private static final String WEAVER_CLASS_ATTRIBUTE = "weaver-class";

	private static final String ASPECTJ_WEAVING_ATTRIBUTE = "aspectj-weaving";


	@Override
	protected String getBeanClassName(Element element) {
		if (element.hasAttribute(WEAVER_CLASS_ATTRIBUTE)) {
			return element.getAttribute(WEAVER_CLASS_ATTRIBUTE);
		}
		return DEFAULT_LOAD_TIME_WEAVER_CLASS_NAME;
	}

	@Override
	protected String resolveId(Element element, AbstractBeanDefinition definition, ParserContext parserContext) {
		return ConfigurableApplicationContext.LOAD_TIME_WEAVER_BEAN_NAME;
	}

	// 核心逻辑是从 parse 函数开始的，而经过父类的封装，LoadTimeWeaverBeanDefinitionParser类的核心实现被转移到了 doParse 函数中
	@Override
	protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
		builder.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);

		// 1、是否开启 AspectJ
		if (isAspectJWeavingEnabled(element.getAttribute(ASPECTJ_WEAVING_ATTRIBUTE), parserContext)) {
			if (!parserContext.getRegistry().containsBeanDefinition(ASPECTJ_WEAVING_ENABLER_BEAN_NAME)) {
				RootBeanDefinition def = new RootBeanDefinition(ASPECTJ_WEAVING_ENABLER_CLASS_NAME);
				// 2、将org.Springframework.context.weaving.AspectJWeavingEnabler 封装在 BeanDefinition
				parserContext.registerBeanComponent(
						new BeanComponentDefinition(def, ASPECTJ_WEAVING_ENABLER_BEAN_NAME));
			}

			if (isBeanConfigurerAspectEnabled(parserContext.getReaderContext().getBeanClassLoader())) {
				new SpringConfiguredBeanDefinitionParser().parse(element, parserContext);
			}
		}
	}

	// 是否开启 AspectJ <br/>
	/**
	 * 之前虽然反复提到了在配置文件中加入了＜context:load-time-weaver／＞便相当于加入了
	 * AspectJ 开关 但是，并不是配置了这个标签就意味着开启了 AspectJ 功能，这个标签中还有一
	 * aspectj-weaving ，这个属性有3个备选值， on、off、autodetect，默认为autodetect ，也
	 * 就是说，如果我们只是使用了＜context:load-time-weaver／＞ ，那么 Spring 会帮助我们检测是否可
	 * 以使用 AspectJ 功能，而检测的依据便是 META-INF/aop.xml 是否存在，看看在 Spring
	 * 的实现方式
	 */
	protected boolean isAspectJWeavingEnabled(String value, ParserContext parserContext) {
		if ("on".equals(value)) {
			return true;
		}
		else if ("off".equals(value)) {
			return false;
		}
		else {
			// Determine default...
			// 自动检测
			ClassLoader cl = parserContext.getReaderContext().getBeanClassLoader();
			return (cl != null && cl.getResource(AspectJWeavingEnabler.ASPECTJ_AOP_XML_RESOURCE) != null);
		}
	}

	protected boolean isBeanConfigurerAspectEnabled(@Nullable ClassLoader beanClassLoader) {
		return ClassUtils.isPresent(SpringConfiguredBeanDefinitionParser.BEAN_CONFIGURER_ASPECT_CLASS_NAME,
				beanClassLoader);
	}

}
