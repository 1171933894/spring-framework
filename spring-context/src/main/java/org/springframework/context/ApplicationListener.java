/*
 * Copyright 2002-2016 the original author or authors.
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

package org.springframework.context;

import java.util.EventListener;

/**
 * Interface to be implemented by application event listeners.
 * Based on the standard {@code java.util.EventListener} interface
 * for the Observer（观察者）design pattern.
 *
 * <p>As of Spring 3.0, an ApplicationListener can generically declare（声明）the event type
 * that it is interested in. When registered with a Spring ApplicationContext, events
 * will be filtered accordingly, with the listener getting invoked for matching event
 * objects only.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @param <E> the specific ApplicationEvent subclass to listen to
 * @see org.springframework.context.event.ApplicationEventMulticaster
 */

/**
 * 用途说明<br/>
 * 在spring中可以通过ApplicationListener来实现相关的功能，加载完成后触发contextrefreshedevent事件（上下文件刷新事件）。
 * 因此可以使用在开发时有时候需要在整个应用开始运行时执行一些特定代码，比如初始化环境，准备测试数据、加载一些数据到内存等等。
 *
 * 但是这个时候，会存在一个问题，在web 项目中（spring mvc），系统会存在两个容器，一个是root application context ,另一个
 * 就是我们自己的 projectName-servlet context（作为root application context的子容器）。这种情况下，就会造成onApplicationEvent
 * 方法被执行两次。为了避免上面提到的问题，我们可以只在root application context初始化完成后调用逻辑代码，其他的容器的初始
 * 化完成，则不做任何处理。eg：
 * public class Init implements ApplicationListener<ContextRefreshedEvent>{
 *     @Override
 *     public void onApplicationEvent(ContextRefreshedEvent event) {
 *         if(event.getApplicationContext().getParent() == null){//root application context 没有parent
 *             // TODO 这里写下将要初始化的内容
 *         }
 *     }
 * }
 */
@FunctionalInterface
public interface ApplicationListener<E extends ApplicationEvent> extends EventListener {

	/**
	 * Handle an application event.
	 * @param event the event to respond to
	 */
	void onApplicationEvent(E event);

}
