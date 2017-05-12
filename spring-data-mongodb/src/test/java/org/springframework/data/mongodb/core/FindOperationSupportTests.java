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

import static org.assertj.core.api.Assertions.*;
import static org.springframework.data.mongodb.core.query.Criteria.*;
import static org.springframework.data.mongodb.core.query.Query.*;

import lombok.Data;

import org.junit.Before;
import org.junit.Test;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

import com.mongodb.MongoClient;

/**
 * @author Christoph Strobl
 */
public class FindOperationSupportTests {

	private static final String STAR_WARS = "star-wars";
	MongoTemplate template;

	Person han;
	Person luke;

	@Before
	public void setUp() {

		template = new MongoTemplate(new SimpleMongoDbFactory(new MongoClient(), "FindOperationSupportTests"));
		template.dropCollection(STAR_WARS);

		han = new Person();
		han.firstname = "han";
		han.id = "id-1";

		luke = new Person();
		luke.firstname = "luke";
		luke.id = "id-2";

		template.save(han);
		template.save(luke);
	}

	@Test(expected = IllegalArgumentException.class) // DATAMONGO-1563
	public void domainTypeIsRequired() {
		template.query(null);
	}

	@Test(expected = IllegalArgumentException.class) // DATAMONGO-1563
	public void returnTypeIsRequiredOnSet() {
		template.query(Person.class).returnResultsAs(null);
	}

	@Test(expected = IllegalArgumentException.class) // DATAMONGO-1563
	public void collectionIsRequiredOnSet() {
		template.query(Person.class).inCollection(null);
	}

	@Test // DATAMONGO-1563
	public void findAll() {
		assertThat(template.query(Person.class).findAll()).containsExactlyInAnyOrder(han, luke);
	}

	@Test // DATAMONGO-1563
	public void findAllWithCollection() {
		assertThat(template.query(Human.class).inCollection(STAR_WARS).findAll()).hasSize(2);
	}

	@Test // DATAMONGO-1563
	public void findAllWithProjection() {

		assertThat(template.query(Person.class).returnResultsAs(Jedi.class).findAll()).hasOnlyElementsOfType(Jedi.class)
				.hasSize(2);
	}

	@Test // DATAMONGO-1563
	public void findAllBy() {

		assertThat(template.query(Person.class).findAllBy(query(where("firstname").is("luke"))))
				.containsExactlyInAnyOrder(luke);
	}

	@Test // DATAMONGO-1563
	public void findAllByWithCollectionUsingMappingInformation() {

		assertThat(template.query(Jedi.class).inCollection(STAR_WARS).findAllBy(query(where("name").is("luke"))))
				.hasSize(1).hasOnlyElementsOfType(Jedi.class);
	}

	@Test // DATAMONGO-1563
	public void findAllByWithCollection() {
		assertThat(template.query(Human.class).inCollection(STAR_WARS).findAllBy(query(where("firstname").is("luke"))))
				.hasSize(1);
	}

	@Test // DATAMONGO-1563
	public void findAllByWithProjection() {

		assertThat(template.query(Person.class).returnResultsAs(Jedi.class).findAllBy(query(where("firstname").is("luke"))))
				.hasOnlyElementsOfType(Jedi.class).hasSize(1);
	}

	@Test // DATAMONGO-1563
	public void findBy() {
		assertThat(template.query(Person.class).findBy(query(where("firstname").is("luke")))).contains(luke);
	}

	@Test // DATAMONGO-1563
	public void findByNoMatch() {
		assertThat(template.query(Person.class).findBy(query(where("firstname").is("spock")))).isNotPresent();
	}

	@Test(expected = IncorrectResultSizeDataAccessException.class) // DATAMONGO-1563
	public void findByTooManyResults() {
		template.query(Person.class).findBy(query(where("firstname").in("han", "luke")));
	}

	@Data
	@org.springframework.data.mongodb.core.mapping.Document(collection = STAR_WARS)
	static class Person {
		@Id String id;
		String firstname;
	}

	@Data
	static class Human {
		@Id String id;
	}

	@Data
	static class Jedi {

		@Field("firstname") String name;
	}

}
