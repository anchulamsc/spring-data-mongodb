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

import org.bson.Document;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.mongodb.client.result.UpdateResult;

/**
 * @author Christoph Strobl
 * @since 2.0
 */
class UpdateOperationSupport implements UpdateOperationBuilder {

	private final MongoTemplate template;

	UpdateOperationSupport(MongoTemplate template) {

		Assert.notNull(template, "Template must not be null!");
		this.template = template;
	}

	@Override
	public <T> UpdateOperation<T> update(Class<T> domainType) {

		Assert.notNull(domainType, "DomainType must not be null!");
		return new UpdateBuilder<T>(template, domainType, null, null, null);
	}

	static class UpdateBuilder<T> implements UpdateOperation<T>, Where<T>, InCollection<T>, ApplyUpdateTo<T> {

		private final MongoTemplate template;
		private final Class<T> domainType;
		private final Update update;
		private final String collection;
		private final FindAndModifyOptions options;

		private UpdateBuilder(MongoTemplate template, Class<T> domainType, Update update, String collection,
				FindAndModifyOptions options) {

			this.template = template;
			this.domainType = domainType;
			this.update = update;
			this.collection = collection;
			this.options = options;
		}

		@Override
		public Where apply(Update update) {

			Assert.notNull(update, "Update must not be null!");
			return new UpdateBuilder<T>(template, domainType, update, collection, options);
		}

		@Override
		public ApplyUpdateTo<T> inCollection(String collection) {

			Assert.hasText(collection, "Collection must not be null nor empty!");
			return new UpdateBuilder<T>(template, domainType, update, collection, options);
		}

		@Override
		public UpdateResult upsertIfNoneMatching(Query filter) {

			Assert.notNull(filter, "Filter must not be null!");
			return doUpdate(filter, true, true);
		}

		@Override
		public UpdateResult first() {
			return doUpdate(new BasicQuery(new Document()), false, false);
		}

		@Override
		public UpdateResult firstMatching(Query filter) {

			Assert.notNull(filter, "Filter must not be null!");
			return doUpdate(filter, false, false);
		}

		@Override
		public UpdateResult all() {
			return doUpdate(new BasicQuery(new Document()), true, false);
		}

		@Override
		public UpdateResult allMatching(Query filter) {

			Assert.notNull(filter, "Filter must not be null!");
			return doUpdate(filter, true, false);
		}

		@Override
		public T findAndModifyMatching(Query filter) {

			Assert.notNull(filter, "Filter must not be null!");

			String collectionName = StringUtils.hasText(collection) ? collection
					: template.determineCollectionName(domainType);

			return template.findAndModify(filter, update, options, domainType, collectionName);
		}

		private UpdateResult doUpdate(Query query, boolean multi, boolean upsert) {

			String collectionName = StringUtils.hasText(collection) ? collection
					: template.determineCollectionName(domainType);

			return template.doUpdate(collectionName, query, update, domainType, upsert, multi);
		}

		@Override
		public DoFindAndModify withOptions(FindAndModifyOptions options) {

			Assert.notNull(options, "Options must not be null!");
			return new UpdateBuilder<T>(template, domainType, update, collection, options);
		}
	}
}
