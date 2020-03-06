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

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;
import ${package}.${artifactId}.api.Foo;
import ${package}.${artifactId}.util.Field;
import ${package}.${artifactId}.util.ClientException;

@Path("foos")
@Produces(APPLICATION_JSON_VALUE)
public class FooResource {

	@Inject
	private FooService ${artifactId};

	@Inject
	private UriInfo uriInfo;

	@Context
	private Request request;

	@GET
	public Response query() {
		final Set<Foo> result = new HashSet<>();
		${artifactId}.fetchAll().forEach( f -> {
			result.add( new FooMediator().toBinding( f ) );
		});
		return Response
			.ok( result )
			.build();
	}

	@GET
	@Path("{id}")
	public Response fetch( @PathParam("id") final String id, @QueryParam("expand") final String expansions ) {
		final List<Field> fields = expansions == null ? null : Field.newInstance( expansions ).subfields();
		final FooEntity entity = ${artifactId}.fetch( id, fields );
		final Foo foo = new FooMediator().toBinding( entity );
		final EntityTag etag = generateResourceTag( entity );
		Response.ResponseBuilder builder;
		if ( (builder = request.evaluatePreconditions( entity.getUpdateDate() )) != null ) {
			// not modified (LastModified)
			return builder.build();
		} else if ( (builder = request.evaluatePreconditions( etag )) != null ) {
			// not modified (Etag)
			return builder.build();
		}

		// cache settings are different depending on the use case
		CacheControl cache = new CacheControl();
		cache.setProxyRevalidate( true );
		cache.setMustRevalidate( true );
		cache.setMaxAge( 30 );
		cache.setSMaxAge( 30 );

		// respond with content
		return Response
			.ok( foo )
			.lastModified( entity.getUpdateDate() )
			.tag( etag )
			.cacheControl( cache )
			.build();
	}

	private boolean modificationsReturnContent = true;  // or false if you prefer 201/204 responses

	@POST
	public Response create( final Foo foo ) {
		final FooEntity entity = ${artifactId}.create( new FooMediator().toEntity( foo ) );
		final Foo result = new FooMediator().toBinding( entity );

		ResponseBuilder builder;
		if ( modificationsReturnContent ) {
			// add entity body
			builder = Response.ok( result );
		} else {
			// no body
			builder = Response.created( uriInfo.getRequestUriBuilder().path( result.id ).build() );
		}
		return builder
			.lastModified( entity.getUpdateDate() )
			.tag( generateResourceTag( entity ) )
			.build();
	}	

	@PUT
	@Path("{id}")
	public Response update( @PathParam("id") final String id, final Foo foo ) {
		if ( ! id.contentEquals( foo.id ) ) {
			throw new ClientException( "Entity and path ID must match" );
		}
		final FooMediator mediator = new FooMediator();
		final FooEntity entity = ${artifactId}.fetch( id );

		/*
		 *  Version conflict detection.
		 *  
		 * the version of the resource passed by the client
		 * MUST be the current version otherwise the changes
		 * are based on an outdated version
		 */
		if ( foo.version != entity.getVersion() ) {
			throw new IllegalStateException( "The data submitted is based on an outdated version " + foo.version + ", expected " + entity.getVersion() );
		}

		ResponseBuilder builder;
		if ( modificationsReturnContent ) {
			// add entity body
			final Foo result = mediator.toBinding( ${artifactId}.update( mediator.toEntity( foo, entity ) ) );
			builder = Response.ok( result );			
		} else {
			// no body
			builder = Response.noContent();
		}
		return builder
			.lastModified( entity.getUpdateDate() )
			.tag( generateResourceTag( entity ) )
			.build();
	}	

	private EntityTag generateResourceTag( final FooEntity entity ) {
		// use hash or version
		return new EntityTag( String.valueOf( entity.getVersion() ) );
	}

	private EntityTag generateResourceTag( final Object obj ) {
		// use hash or version
		return new EntityTag( String.valueOf( obj.hashCode() ) );
	}
}
