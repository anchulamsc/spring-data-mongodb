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

import org.bson.Document;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Query;

import com.mongodb.client.result.DeleteResult;

/**
 * @author Christoph Strobl
 * @since 2.0
 */
public interface RemoveOperationBuilder {

	<T> RemoveOperation<T> remove(Class<T> domainType);

	/**
	 * @param <T>
	 * @author Christoph Strobl
	 * @since 2.0
	 */
	interface InCollection<T> extends RemoveBy {

		/**
		 * Explicitly set the name of the collection to perform the query on. <br />
		 * Just skip this step to use the default collection derived from the domain type.
		 *
		 * @param collection must not be {@literal null} nor {@literal empty}.
		 * @return
		 */
		RemoveBy inCollection(String collection);
	}

	/**
	 * @param <T>
	 * @author Christoph Strobl
	 * @since 2.0
	 */
	interface RemoveBy<T> {

		/**
		 * Remove all elements from the collection without dropping the collection.
		 *
		 * @return
		 */
		default DeleteResult all() {
			return allMatching(new BasicQuery(new Document()));
		}

		/**
		 * Remove all documents matching the given filter.
		 *
		 * @param filter must not be {@literal null}.
		 * @return
		 */
		DeleteResult allMatching(Query filter);

		/**
		 * Remove and return all documents matching the given filter. <strong>NOTE</strong> The entire list of documents
		 * will be fetched before sending the actual delete commands. Also
		 * {@link org.springframework.context.ApplicationEvent}s will be published for each and every delete operation.
		 *
		 * @param filter must not be {@literal null}.
		 * @return empty {@link List} if no match found. Never {@literal null}.
		 */
		List<T> andReturnAllMatching(Query filter);
	}

	/**
	 * @param <T>
	 * @author Christoph Strobl
	 * @since 2.0
	 */
	interface RemoveOperation<T> extends InCollection<T> {

	}

}
