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

import org.springframework.data.mongodb.core.query.Query;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.mongodb.client.result.DeleteResult;

/**
 * @author Christoph Strobl
 * @since 2.0
 */
class RemoveOperationSupport implements RemoveOperationBuilder {

	private final MongoTemplate tempate;

	RemoveOperationSupport(MongoTemplate template) {
		this.tempate = template;
	}

	@Override
	public <T> RemoveOperation<T> remove(Class<T> domainType) {

		Assert.notNull(domainType, "DomainType must not be null!");
		return new RemoveBuilder<T>(tempate, domainType, null);
	}

	static class RemoveBuilder<T> implements RemoveOperation<T>, InCollection<T> {

		private final MongoTemplate template;
		private final Class<T> domainType;
		private final String collection;

		public RemoveBuilder(MongoTemplate template, Class<T> domainType, String collection) {

			this.template = template;
			this.domainType = domainType;
			this.collection = collection;
		}

		@Override
		public RemoveBy inCollection(String collection) {

			Assert.hasText(collection, "Collection must not be null nor empty!");
			return new RemoveBuilder<T>(template, domainType, collection);
		}

		@Override
		public DeleteResult allMatching(Query filter) {

			Assert.notNull(filter, "Filter must not be null!");
			String collectionName = StringUtils.hasText(collection) ? collection
					: template.determineCollectionName(domainType);

			return template.doRemove(collectionName, filter, domainType);
		}

		@Override
		public List andReturnAllMatching(Query filter) {

			Assert.notNull(filter, "Filter must not be null!");

			String collectionName = StringUtils.hasText(collection) ? collection
					: template.determineCollectionName(domainType);

			return template.doFindAndDelete(collectionName, filter, domainType);
		}
	}
}
