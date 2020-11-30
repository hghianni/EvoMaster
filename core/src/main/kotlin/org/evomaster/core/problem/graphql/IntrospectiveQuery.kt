package org.evomaster.core.problem.graphql

import org.evomaster.core.problem.graphql.schema.SchemaObj
import org.evomaster.core.remote.SutProblemException
import org.glassfish.jersey.client.ClientConfig
import org.glassfish.jersey.client.ClientProperties
import org.glassfish.jersey.client.HttpUrlConnectorProvider
import javax.ws.rs.client.Client
import javax.ws.rs.client.ClientBuilder
import javax.ws.rs.client.Entity
import javax.ws.rs.core.MediaType


class IntrospectiveQuery {

    private val clientConfiguration = ClientConfig()
            .property(ClientProperties.CONNECT_TIMEOUT, 10_000)
            .property(ClientProperties.READ_TIMEOUT, 10_000)
            //workaround bug in Jersey client
            .property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true)
            .property(ClientProperties.FOLLOW_REDIRECTS, false)


    /**
     * Client library to make HTTP calls. To get the schema from a GraphQL API, we need to make
     * an HTTP call that has, as body payload, and introspective query
     */
    private var client: Client = ClientBuilder.newClient(clientConfiguration)


    fun fetchSchema(
            /**
             * The endpoint URL of where we can query the GraphQL SUT
             */
            graphQlEndpoint: String) : String{

        /*
            Body payload for the introspective query
         */
        val query = Entity.entity("""
                    {
                        "query": {
                            TODO
                        }
                    }
                """.trimIndent(), MediaType.APPLICATION_JSON_TYPE)

        //TODO check if TCP problems
        val response = client.target(graphQlEndpoint)
                .request("application/json")
                .buildPost(query)
                .invoke()

        //TODO check status code, and any other problem inside GraphQL response

        if(response.status != 200){
            throw SutProblemException("Failed to retrieve GraphQL schema. Status code: ${response.status}")
        }

        /*
            Extract the body from response as a string
         */
        val body = response.readEntity(String::class.java)

        return body
    }
}