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

import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import com.mongodb.client.result.UpdateResult;

/**
 * @author Christoph Strobl
 * @since 2.0
 */
public interface UpdateOperationBuilder {

	/**
	 * Start creating an update operation for the given {@literal domainType}. <br />
	 *
	 * @param domainType must not be {@literal null}.
	 * @param <T>
	 * @return
	 */
	public <T> UpdateOperation<T> update(Class<T> domainType);

	/**
	 * Terminating operations invoking the actual update execution.
	 *
	 * @param <T>
	 * @author Christoph Strobl
	 * @since 2.0
	 */
	interface ApplyUpdateTo<T> extends DoFindAndModify<T> {

		/**
		 * Creates a new document if no documents match the filter query or updates the matching ones.
		 *
		 * @param filter must not be {@literal null}.
		 * @return
		 */
		UpdateResult upsertIfNoneMatching(Query filter);

		/**
		 * Update the first document in the collection.
		 *
		 * @return
		 */
		UpdateResult first();

		/**
		 * Update the first document that matches the filter query.
		 *
		 * @param filter must not be {@literal null}.
		 * @return
		 */
		UpdateResult firstMatching(Query filter);

		/**
		 * Update all documents in the collection.
		 *
		 * @return
		 */
		UpdateResult all();

		/**
		 * Update all documents matching the filter query.
		 *
		 * @param filter must not be {@literal null}.
		 * @return
		 */
		UpdateResult allMatching(Query filter);
	}

	interface DoFindAndModify<T> {

		/**
		 * Find, modify and return the first matching document.
		 *
		 * @param filter must not be {@literal null}.
		 * @return
		 */
		T findAndModifyMatching(Query filter);
	}

	/**
	 * @param <T>
	 * @author Christoph Strobl
	 * @since 2.0
	 */
	interface UpdateOperation<T> {

		/**
		 * Set the {@link Update} to be applied.
		 *
		 * @param update must not be {@literal null}.
		 * @return
		 */
		Where<T> apply(Update update);
	}

	/**
	 * @param <T>
	 * @author Christoph Strobl
	 * @since 2.0
	 */
	interface InCollection<T> extends WithOptions<T> {

		/**
		 * Explicitly set the name of the collection to perform the query on. <br />
		 * Just skip this step to use the default collection derived from the domain type.
		 *
		 * @param collection must not be {@literal null} nor {@literal empty}.
		 * @return
		 */
		ApplyUpdateTo<T> inCollection(String collection);
	}

	/**
	 * @param <T>
	 * @author Christoph Strobl
	 * @since 2.0
	 */
	interface Where<T> extends InCollection<T>, ApplyUpdateTo<T> {

	}

	/**
	 * @param <T>
	 * @author Christoph Strobl
	 * @since 2.0
	 */
	interface WithOptions<T> {

		/**
		 * Explicitly define {@link FindAndModifyOptions} for the {@link Update}.
		 *
		 * @param options must not be {@literal null}.
		 * @return
		 */
		DoFindAndModify<T> withOptions(FindAndModifyOptions options);
	}

}
