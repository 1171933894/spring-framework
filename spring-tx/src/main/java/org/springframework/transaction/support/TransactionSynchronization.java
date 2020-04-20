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

package org.springframework.transaction.support;

import java.io.Flushable;

/**
 * Interface for transaction synchronization callbacks.
 * Supported by AbstractPlatformTransactionManager.
 *
 * <p>TransactionSynchronization implementations can implement the Ordered interface
 * to influence（影响）their execution order. A synchronization that does not implement the
 * Ordered interface is appended to the end of the synchronization chain.
 *
 * <p>System synchronizations performed by Spring itself use specific order values,
 * allowing for fine-grained interaction with their execution order (if necessary).
 *
 * @author Juergen Hoeller
 * @since 02.06.2003
 * @see TransactionSynchronizationManager
 * @see AbstractPlatformTransactionManager
 * @see org.springframework.jdbc.datasource.DataSourceUtils#CONNECTION_SYNCHRONIZATION_ORDER
 */
// 这个类是程序员对事务同步的扩展点：用于事务同步回调的接口（在事务的suspend、resume、rollback、commit方法中会触发这个接口提供的钩子）

/**
 * TransactionSynchronization接口定义了一系列的回调方法，对应一个事务执行的不同阶段：挂起、恢复、flush、提交（前、后）、完成（事务成功或失败）等。
 * 当事务运行到对应阶段时，事务管理器会从TransactionSynchronizationManager维护的synchronizations中拿出所有的回调器，逐个回调其中的对应方法
  */
public interface TransactionSynchronization extends Flushable {

	/** Completion status in case of proper commit. */
	int STATUS_COMMITTED = 0;

	/** Completion status in case of proper rollback. */
	int STATUS_ROLLED_BACK = 1;

	/** Completion status in case of heuristic（启发式）mixed completion or system errors. */
	int STATUS_UNKNOWN = 2;


	/**
	 * Suspend（暂停）this synchronization.
	 * Supposed（应该）to unbind resources from TransactionSynchronizationManager if managing any.
	 * @see TransactionSynchronizationManager#unbindResource
	 */
	// 事务挂起
	/**
	 * 此钩子在事务中的suspend方法触发。
	 * 这个钩子意味者如果自己在事务运行期间在当前线程绑定过资源，可能需要移除掉。
	 * 想象下事务里挂起的时候，会移除掉开启事务时绑定的connectionHolder.
	 */
	default void suspend() {
	}

	/**
	 * Resume this synchronization.
	 * Supposed to rebind resources to TransactionSynchronizationManager if managing any.
	 * @see TransactionSynchronizationManager#bindResource
	 */
	// 事务恢复（与suspend相反，在事务中的resume方法触发）
	default void resume() {
	}

	/**
	 * Flush the underlying session to the datastore, if applicable:
	 * for example, a Hibernate/JPA session.
	 * @see org.springframework.transaction.TransactionStatus#flush()
	 */
	// 将基础会话刷新到数据存储区(如果适用)，比如Hibernate/JPA的Session
	@Override
	default void flush() {
	}

	/**
	 * Invoked before transaction commit (before "beforeCompletion").
	 * Can e.g. flush transactional O/R Mapping sessions to the database.
	 * <p>This callback does <i>not</i> mean that the transaction will actually be committed.
	 * A rollback decision can still occur after this method has been called. This callback
	 * is rather meant to perform work that's only relevant if a commit still has a chance
	 * to happen, such as flushing SQL statements to the database.
	 * <p>Note that exceptions will get propagated to the commit caller and cause a
	 * rollback of the transaction.
	 * @param readOnly whether the transaction is defined as read-only transaction
	 * @throws RuntimeException in case of errors; will be <b>propagated to the caller</b>
	 * (note: do not throw TransactionException subclasses here!)
	 * @see #beforeCompletion
	 */
	// 在事务提交前触发，此处若发生异常，会导致回滚
	/**
	 * 此钩子在事务提交前执行
	 * 在调用commit方法时触发，之后再走commit逻辑。我们知道事务实际的结果还是需要根据
	 * commit方法来决定，因为即使调用了commit方法也仍然可能会执行回滚。
	 * 如果按照正常流程走提交的话，我们通过这个钩子方法由机会在事务真正提交前还能对数据库做一些操作。
	 */
	default void beforeCommit(boolean readOnly) {
	}

	/**
	 * Invoked before transaction commit/rollback.
	 * Can perform resource cleanup <i>before</i> transaction completion.
	 * <p>This method will be invoked after {@code beforeCommit}, even when
	 * {@code beforeCommit} threw an exception. This callback allows for
	 * closing resources before transaction completion, for any outcome.
	 * @throws RuntimeException in case of errors; will be <b>logged but not propagated</b>
	 * (note: do not throw TransactionException subclasses here!)
	 * @see #beforeCommit
	 * @see #afterCompletion
	 */
	// 在beforeCommit之后，commit/rollback之前执行。即使异常，也不会回滚
	/**
	 * 此钩子在事务完成前执行(发生在beforeCommit之后)
	 * 调用commit或者rollback都能触发，此钩子在真正提交或者真正回滚前执行。
	 */
	default void beforeCompletion() {
	}

	/**
	 * Invoked after transaction commit. Can perform further operations right
	 * <i>after</i> the main transaction has <i>successfully</i> committed.
	 * <p>Can e.g. commit further operations that are supposed to follow on a successful
	 * commit of the main transaction, like confirmation messages or emails.
	 * <p><b>NOTE:</b> The transaction will have been committed already, but the
	 * transactional resources might still be active and accessible. As a consequence,
	 * any data access code triggered at this point will still "participate" in the
	 * original transaction, allowing to perform some cleanup (with no commit following
	 * anymore!), unless it explicitly declares that it needs to run in a separate
	 * transaction. Hence: <b>Use {@code PROPAGATION_REQUIRES_NEW} for any
	 * transactional operation that is called from here.</b>
	 * @throws RuntimeException in case of errors; will be <b>propagated to the caller</b>
	 * (note: do not throw TransactionException subclasses here!)
	 */
	// 事务提交后执行（事务真正提交后执行，这个钩子执行意味着事务真正提交了。）
	default void afterCommit() {
	}

	/**
	 * Invoked after transaction commit/rollback.
	 * Can perform resource cleanup <i>after</i> transaction completion.
	 * <p><b>NOTE:</b> The transaction will have been committed or rolled back already,
	 * but the transactional resources might still be active and accessible. As a
	 * consequence, any data access code triggered at this point will still "participate"
	 * in the original transaction, allowing to perform some cleanup (with no commit
	 * following anymore!), unless it explicitly declares that it needs to run in a
	 * separate transaction. Hence: <b>Use {@code PROPAGATION_REQUIRES_NEW}
	 * for any transactional operation that is called from here.</b>
	 * @param status completion status according to the {@code STATUS_*} constants
	 * @throws RuntimeException in case of errors; will be <b>logged but not propagated</b>
	 * (note: do not throw TransactionException subclasses here!)
	 * @see #STATUS_COMMITTED
	 * @see #STATUS_ROLLED_BACK
	 * @see #STATUS_UNKNOWN
	 * @see #beforeCompletion
	 */
	// 事务提交/回滚执行（可能是真正提交了，也有可能是回滚）
	/**
	 * TransactionSynchronization中没有afterRollback()。如果需要在事务回滚后做某些处理，需要在afterCompletion(int)方法中判断入参的值，然后再做处理
	 */
	default void afterCompletion(int status) {
	}

}
