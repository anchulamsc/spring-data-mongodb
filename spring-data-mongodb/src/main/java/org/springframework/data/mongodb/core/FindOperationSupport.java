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

import org.bson.Document;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.mongodb.client.FindIterable;

/**
 * @author Christoph Strobl
 * @since 2.0
 */
class FindOperationSupport implements FindOperationBuilder {

	private final MongoTemplate template;

	FindOperationSupport(MongoTemplate template) {

		Assert.notNull(template, "Template must not be null!");
		this.template = template;
	}

	@Override
	public <T> FindOperation<T> query(Class<T> domainType) {

		Assert.notNull(domainType, "DomainType must not be null!");

		return new FindBuilder<>(template, domainType, null, domainType);
	}

	static class FindBuilder<T> implements FindOperation<T>, InCollection<T>, ProjectingTo<T>, FindBy<T> {

		private final MongoTemplate template;
		private final Class<?> domainType;
		private final Class<T> returnType;
		private final String collection;

		private FindBuilder(MongoTemplate template, Class<?> domainType, String collection, Class<T> returnType) {

			this.template = template;
			this.returnType = returnType;
			this.domainType = domainType;
			this.collection = collection;
		}

		@Override
		public ProjectingTo<T> inCollection(String collection) {

			Assert.hasText(collection, "Collection name must not be null nor empty!");

			return new FindBuilder<>(template, domainType, collection, returnType);
		}

		@Override
		public <T1> FindBy<T1> returnResultsAs(Class<T1> returnType) {

			Assert.notNull(returnType, "ReturnType must not be null!");

			return new FindBuilder<>(template, domainType, collection, returnType);
		}

		@Override
		public List<T> findAllBy(Query query) {
			return doFind(query, null);
		}

		@Override
		public Optional<T> findBy(Query query) {

			List<T> result = doFind(query, new DelegatingQueryCursorPreparer(getCursorPreparer(query, null)).limit(2));

			if (ObjectUtils.isEmpty(result)) {
				return Optional.empty();
			}

			if (result.size() > 1) {
				throw new IncorrectResultSizeDataAccessException("Query " + asString() + " returned non unique result.", 1);
			}

			return Optional.of(result.iterator().next());
		}

		@Override
		public Optional<T> findFirstBy(Query query) {

			List<T> result = doFind(query, new DelegatingQueryCursorPreparer(getCursorPreparer(query, null)).limit(1));

			if (ObjectUtils.isEmpty(result)) {
				return Optional.empty();
			}

			return Optional.of(result.iterator().next());
		}

		@Override
		public List<T> findAll() {
			return findAllBy(null);
		}

		private List<T> doFind(Query query, CursorPreparer preparer) {

			Document queryObject = query != null ? query.getQueryObject() : new Document();
			Document fieldsObject = query != null ? query.getFieldsObject() : new Document();

			return template.doFind(
					StringUtils.hasText(collection) ? collection : template.determineCollectionName(domainType), queryObject,
					fieldsObject, domainType, returnType, getCursorPreparer(query, preparer));
		}

		private CursorPreparer getCursorPreparer(Query query, CursorPreparer preparer) {
			return query == null || preparer != null ? preparer : template.new QueryCursorPreparer(query, domainType);
		}

		public String asString() {
			return "";
		}
	}

	static class DelegatingQueryCursorPreparer implements CursorPreparer {

		private final CursorPreparer delegate;
		private Optional<Integer> limit = Optional.empty();

		public DelegatingQueryCursorPreparer(CursorPreparer delegate) {
			this.delegate = delegate;
		}

		@Override
		public FindIterable<Document> prepare(FindIterable<Document> cursor) {

			FindIterable<Document> target = delegate.prepare(cursor);

			if (limit.isPresent()) {
				target = target.limit(limit.get());
			}

			return target;
		}

		CursorPreparer limit(int limit) {
			this.limit = Optional.of(limit);
			return this;
		}
	}

}
