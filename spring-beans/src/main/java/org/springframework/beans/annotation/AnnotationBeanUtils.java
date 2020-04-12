/*
 * Copyright 2002-2019 the original author or authors.
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

package org.springframework.beans.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringValueResolver;

/**
 * General utility methods for working with annotations in JavaBeans style.
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 */
// 拷贝注解值到指定的类中
public abstract class AnnotationBeanUtils {

	/**
	 * Copy the properties of the supplied {@link Annotation} to the supplied target bean.
	 * Any properties defined in {@code excludedProperties} will not be copied.
	 * @param ann the annotation to copy from
	 * @param bean the bean instance to copy to
	 * @param excludedProperties the names of excluded properties, if any
	 * @see org.springframework.beans.BeanWrapper
	 */
	public static void copyPropertiesToBean(Annotation ann, Object bean, String... excludedProperties) {
		copyPropertiesToBean(ann, bean, null, excludedProperties);
	}

	/**
	 * Copy the properties of the supplied {@link Annotation} to the supplied target bean.
	 * Any properties defined in {@code excludedProperties} will not be copied.
	 * <p>A specified value resolver may resolve placeholders in property values, for example.
	 * @param ann the annotation to copy from
	 * @param bean the bean instance to copy to
	 * @param valueResolver a resolve to post-process String property values (may be {@code null})
	 * @param excludedProperties the names of excluded properties, if any
	 * @see org.springframework.beans.BeanWrapper
	 */
	public static void copyPropertiesToBean(Annotation ann, Object bean, @Nullable StringValueResolver valueResolver,
			String... excludedProperties) {

		Set<String> excluded = (excludedProperties.length == 0 ? Collections.emptySet() :
				new HashSet<>(Arrays.asList(excludedProperties)));
		Method[] annotationProperties = ann.annotationType().getDeclaredMethods();// 获取注解上的方法
		BeanWrapper bw = PropertyAccessorFactory.forBeanPropertyAccess(bean);// 通过bean对象获取bean的定义BeanDefinition
		for (Method annotationProperty : annotationProperties) {// 遍历方法
			String propertyName = annotationProperty.getName();// 获取方法名
			if (!excluded.contains(propertyName) && bw.isWritableProperty(propertyName)) {
				Object value = ReflectionUtils.invokeMethod(annotationProperty, ann);// 获取注解方法上的值
				if (valueResolver != null && value instanceof String) {
					// 处理value的值，StringValueResolver的作用比如处理占位符${}
					value = valueResolver.resolveStringValue((String) value);
				}
				bw.setPropertyValue(propertyName, value);// 把该值设置到bean定义上
			}
		}
	}

}
