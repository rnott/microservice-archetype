#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
/*
 * Copyright 2016 Randy Nott
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ${package}.${artifactId}.foo;

import java.util.HashSet;
import java.util.Set;
import ${package}.${artifactId}.api.Foo;
import ${package}.${artifactId}.util.Mediator;
import ${package}.${artifactId}.util.ClientException;

public class FooMediator implements Mediator<FooEntity, Foo> {

	@Override
	public Foo toBinding( FooEntity entity ) {
		// prevent lazy loading exceptions
		entity = unproxy( entity );

		Foo foo = new Foo();
		foo.id = entity.getId();
		foo.name = entity.getName();
		foo.version = entity.getVersion();

		// check the typed value for one and many to one relationships
		if ( entity.getAnother() == null ) {
			foo.anotherId = entity.getAnotherId();
		} else {
			// mediate the relationship
			foo.another = new FooMediator().toBinding( entity.getAnother() );
		}

		// collections are an all or nothing proposition
		Set<BarEntity> bar = unproxy( entity.getBar() );
		if ( bar != null ) {
			bar.forEach( b -> {
				foo.bar.add( new BarMediator().toBinding( b ) );
			});
		}

		return foo;
	}

	@Override
	public FooEntity toEntity( Foo binding ) {
		return toEntity( binding, new FooEntity() );
	}

	@Override
	public FooEntity toEntity( Foo binding, FooEntity entity ) {
		entity.setId( binding.id );
		entity.setName( binding.name );
		// IMPORTANT: never update the entity version
		if ( binding.another == null ) {
			entity.setAnotherId( binding.anotherId );
		} else {
			// target value must pre-exist
			// cannot cascade insert/update
			throw new ClientException( "'Another' mutations cannot be cascaded" );
		}
		Set<BarEntity> bar = new HashSet<>( binding.bar.size() );
		entity.setBar( bar );
		binding.bar.forEach( b -> {
			bar.add( new BarMediator().toEntity( b ) );
		});
		return entity;
	}
}
