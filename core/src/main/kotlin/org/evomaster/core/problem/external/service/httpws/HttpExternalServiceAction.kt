package org.evomaster.core.problem.external.service.httpws

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.common.Metadata.metadata
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import org.evomaster.core.problem.external.service.ApiExternalServiceAction
import org.evomaster.core.problem.external.service.httpws.param.HttpWsResponseParam
import org.evomaster.core.search.StructuralElement
import org.evomaster.core.search.gene.Gene

/**
 * Action to execute the external service related need
 * to handle the external service calls.
 *
 * Typically, handle WireMock responses
 */
class HttpExternalServiceAction(

    /**
     * Received request to the respective WireMock instance
     *
     * TODO: Need to expand the properties further in future
     *  depending on the need
     */
    val request: HttpExternalServiceRequest,

    /**
     * currently, we support response with json format
     * then use ObjectGene now,
     * might extend it later
     */
    response: HttpWsResponseParam = HttpWsResponseParam(),

    /**
     * WireMock server which received the request
     */
    val externalService: ExternalService,
    active: Boolean = false,
    used: Boolean = false,
    private val id: Long,
    localId: String
) : ApiExternalServiceAction(response, active, used, localId) {

    companion object {
        private fun buildResponse(template: String): HttpWsResponseParam {
            // TODO: refactor later
            return HttpWsResponseParam()
        }
    }

    constructor(
        request: HttpExternalServiceRequest,
        template: String,
        externalService: ExternalService,
        id: Long,
        localId: String = NONE_ACTION_COMPONENT_ID
    ) :
            this(request, buildResponse(template), externalService, id = id, localId = localId)

    /**
     * UUID generated by WireMock is used under ExternalServiceRequest
     * is used as ID for action.
     *
     * TODO: After the ID refactor, this needs to be changed.
     */
    override fun getName(): String {
        return request.id.toString()
    }

    override fun seeTopGenes(): List<out Gene> {
        return response.genes
    }

    override fun shouldCountForFitnessEvaluations(): Boolean {
        return false
    }

    /**
     * Each external service will have a WireMock instance representing that
     * so when the ExternalServiceAction is copied, same instance will be passed
     * into the copy too. Otherwise, we have to manage multiple instances for the
     * same external service.
     */
    override fun copyContent(): StructuralElement {
        return HttpExternalServiceAction(
            request,
            response.copy() as HttpWsResponseParam,
            externalService,
            active,
            used,
            id,
            localId = getLocalId()
        )
    }

    /**
     * This will the stub for WireMock based on the response.
     *
     * Will use the absolute URL as the key. If there is a stub exists
     * for that absolute URL will remove it before adding a new one.
     *
     * If the action is inactive ([active] is false) this will make changed to
     * the WireMock for the respective [HttpExternalServiceRequest]
     *
     * TODO: This has to moved separetly to have extensive features
     *  in future.
     */
    fun buildResponse() {
        val wm = externalService.getWireMockServer()
        val mappings = wm.stubMappings
        val m = mappings.size
        if (!active) {
            return
        }

        val existingStubs = externalService.getWireMockServer()
            .findStubMappingsByMetadata(matchingJsonPath("$.url", containing(request.absoluteURL)))

        if (existingStubs.isNotEmpty()) {
            removeStub(existingStubs)
        }

        externalService.getWireMockServer().stubFor(
            get(urlMatching(request.url))
                .atPriority(1)
                .willReturn(
                    aResponse()
                        .withStatus(viewStatus())
                        .withBody(viewResponse())
                )
                .withMetadata(
                    metadata()
                        .attr("url", request.absoluteURL)
                )
        )
    }

    /**
     * Remove all the existing stubs from WireMock.
     * Will be used before building new response.
     */
    fun removeStub(stubs: List<StubMapping>) {
        stubs.forEach { stub ->
            externalService.getWireMockServer().removeStub(stub)
        }
    }

    private fun viewStatus(): Int {
        return (response as HttpWsResponseParam).status.getValueAsRawString().toInt()
    }

    private fun viewResponse(): String {
        // TODO: Need to extend further to handle the response body based on the
        //  unmarshalled object inside SUT using the ParsedDto information.
        return (response as HttpWsResponseParam).response.getValueAsRawString()
    }

}