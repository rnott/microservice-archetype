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

package ${package}.service.foo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import ${package}.service.AbstractTest;
import ${package}.service.api.Foo;
import org.testng.annotations.Test;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;

/**
 * Test the resource.
 * <p>
 * @see FooResource
 */
@DatabaseSetup("/fixtures/foo.xml")
@DatabaseTearDown( value = "/fixtures/foo.xml", type = DatabaseOperation.DELETE_ALL )
public class FooResourceTest extends AbstractTest {

	public FooResourceTest() {
		super( "foos" );
	}

	@Test
	public void collection() {
		List<String> expected = new ArrayList<>( 3 );
		expected.add( "1" ); expected.add( "2" ); expected.add( "3" );
		Response response = getBaseTarget()
			.request( MediaType.APPLICATION_JSON )
			.get();
		assert response != null : "Response is NULL";
		assert success( response )
			: "Unexpected status code: " + response.getStatus() + " : " + response.readEntity( String.class ) ;
		List<Foo> value = response.readEntity( new GenericType<List<Foo>>(){} );
		assert value != null : "Value is NULL";
		assert value.size() == 3 : "Unexpected entry count: " + value.size();
		value.forEach( entry -> {
			assert expected.contains( entry.id ) : "Unexpected id: " + entry.id;
			expected.remove( entry.id );
		});
	}

	@Test
	public void identity() {
		Response response = getBaseTarget()
			.path( "1" )
			.request( MediaType.APPLICATION_JSON )
			.get();
		assert response != null : "Response is NULL";
		assert success( response )
			: "Unexpected status code: " + response.getStatus() + " : " + response.readEntity( String.class ) ;
		Foo value = response.readEntity( Foo.class );
		assert value != null : "Value is NULL";
		assert "1".equals( value.id ) : "Unexpected value: " + value.id;
		verifyTimeLastModified( response );
	}

	@Test
	public void expand() {
		List<String> bars = new ArrayList<>( 3 );
		bars.add( "1" ); bars.add( "2" ); bars.add( "3" );
		Response response = getBaseTarget()
			.path( "3" )
			.queryParam( "expand", "another bar barList" )
			.request( MediaType.APPLICATION_JSON )
			.get();
		assert response != null : "Response is NULL";
		assert success( response )
			: "Unexpected status code: " + response.getStatus() + " : " + response.readEntity( String.class ) ;
		Foo value = response.readEntity( Foo.class );
		assert value != null : "Value is NULL";
		assert "3".equals( value.id ) : "Unexpected value: " + value.id;
		assert value.another != null : "Another is NULL";
		assert "2".contentEquals( value.another.id ) : "Unexpected another ID: " + value.another.id;
		assert value.bar != null : "Bar is NULL";
		assert value.bar.size() == 3 : "Unexpected bar count: " + value.bar.size();
		value.bar.forEach( b -> {
			assert bars.contains( b.id ) : "Unexpected bar ID: " + b.id;
		});
		verifyTimeLastModified( response );
	}

	@Test
	public void create() {
		Foo foo = new Foo();
		foo.id = "10";
		foo.name = "Ten";
		Response response = getBaseTarget()
			.request( MediaType.APPLICATION_JSON )
			.post( Entity.entity( foo, MediaType.APPLICATION_JSON ) );
		assert response != null : "Response is NULL";
		assert success( response )
			: "Unexpected status code: " + response.getStatus() + " : " + response.readEntity( String.class ) ;
		verifyTimeLastModified( response );
	}

	@Test
	public void update() {
		Foo foo = new Foo();
		foo.id = "1";
		foo.name = "OnePrime";
		foo.version = 1;
		Response response = getBaseTarget()
			.path( "1" )
			.request( MediaType.APPLICATION_JSON )
			.put( Entity.entity( foo, MediaType.APPLICATION_JSON ) );
		assert response != null : "Response is NULL";
		assert success( response )
			: "Unexpected status code: " + response.getStatus() + " : " + response.readEntity( String.class );
		verifyTimeLastModified( response );
	}

	@Test
	public void conflict() {
		Foo foo = new Foo();
		foo.id = "1";
		foo.name = "OnePrime";
		foo.version = 0;
		Response response = getBaseTarget()
			.path( "1" )
			.request( MediaType.APPLICATION_JSON )
			.put( Entity.entity( foo, MediaType.APPLICATION_JSON ) );
		assert response != null : "Response is NULL";
		assert response.getStatus() == 409
			: "Unexpected status code: " + response.getStatus() + " : " + response.readEntity( String.class );
	}

	private void verifyTimeLastModified( Response response ) {
		Date tlm = response.getLastModified();
		assert tlm != null : "No last modified header";
		assert tlm.before( new Date() ) : "Last modified is in the future";
	}
}
