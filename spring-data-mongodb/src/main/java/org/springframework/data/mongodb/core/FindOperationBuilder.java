/*
 * Copyright 2017 the original author or authors.
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

/*
 * Copyright 2017 the original author or authors.
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
package org.springframework.data.mongodb.core;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.core.query.Query;

/**
 * @author Christoph Strobl
 * @since 2.0
 */
public interface FindOperationBuilder {

	/**
	 * Start creating a find operation for the given {@literal domainType}. <br />
	 *
	 * @param domainType must not be {@literal null}.
	 * @param <T>
	 * @return
	 */
	public <T> FindOperation<T> query(Class<T> domainType);

	/**
	 * Terminating operations invoking the actual query execution.
	 *
	 * @param <T>
	 * @author Christoph Strobl
	 * @since 2.0
	 */
	interface FindBy<T> {

		/**
		 * Find all documents applying the given {@link Query}.
		 *
		 * @param query can be {@literal null}.
		 * @return an empty {@link List} if not match found. Never {@literal null}.
		 */
		List<T> findAllBy(Query query);

		/**
		 * Find exactly one document applying the given {@link Query}.
		 *
		 * @param query can be {@literal null}.
		 * @return {@link Optional#empty()} when no match found.
		 * @throws org.springframework.dao.IncorrectResultSizeDataAccessException when more the one item found.
		 */
		Optional<T> findBy(Query query);

		/**
		 * Find the first document applying the given {@link Query}. <br />
		 * Unlike {@link #findBy(Query)} this method does not make hard assumptions on the expected result size and simply
		 * returns the first result even when more matches have been found.
		 *
		 * @param query can be {@literal null}.
		 * @return {@link Optional#empty()} when no match found.
		 */
		Optional<T> findFirstBy(Query query);

		/**
		 * Find all documents.
		 *
		 * @return an empty {@link List} if not match found. Never {@literal null}.
		 */
		default List<T> findAll() {
			return findAllBy(null);
		}
	}

	/**
	 * Collection override (Optional).
	 *
	 * @param <T>
	 * @author Christoph Strobl
	 * @since 2.0
	 */
	interface InCollection<T> extends FindBy<T> {

		/**
		 * Explicitly set the name of the collection to perform the query on. <br />
		 * Just skip this step to use the default collection derived from the domain type.
		 *
		 * @param collection must not be {@literal null} nor {@literal empty}.
		 * @return
		 */
		ProjectingTo<T> inCollection(String collection);
	}

	/**
	 * Result type override (Optional).
	 *
	 * @param <T>
	 * @author Christoph Strobl
	 * @since 2.0
	 */
	interface ProjectingTo<T> extends FindBy<T> {

		/**
		 * Define the target type fields should be mapped to. <br />
		 * Just skip this step if you are anyway only interested in the original domain type.
		 *
		 * @param resultType must not be {@literal null}.
		 * @param <T>
		 * @return
		 */
		<T1> FindBy<T1> returnResultsAs(Class<T1> resultType);
	}

	/**
	 * @param <T>
	 * @author Christoph Strobl
	 * @since 2.0
	 */
	interface FindOperation<T> extends InCollection<T>, ProjectingTo<T> {}

}
