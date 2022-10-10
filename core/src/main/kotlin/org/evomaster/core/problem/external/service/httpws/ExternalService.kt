package org.evomaster.core.problem.external.service.httpws

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import java.util.*

/**
 * Represent the external service related information including
 * WireMock server and ExternalServiceInfo collected from SUT.
 */
class ExternalService(
    /**
     * External service information collected from SUT
     */
    private val externalServiceInfo: HttpExternalServiceInfo,
    /**
     * Initiated WireMock server for the external service
     */
    private val wireMockServer: WireMockServer
) {

    /**
     * Return the IP address of WireMock instance
     */
    fun getWireMockAddress(): String {
        return wireMockServer.options.bindAddress()
    }

    /**
     * Return the running port of WireMock instance
     */
    fun getWireMockPort(): Int {
        return wireMockServer.options.portNumber()
    }

    fun getWireMockServer(): WireMockServer {
        return wireMockServer
    }

    fun getRemoteHostName(): String {
        return externalServiceInfo.remoteHostname
    }

    fun getWireMockAbsoluteAddress(): String {
        return getWireMockAddress().plus(":").plus(getWireMockPort())
    }

    /**
     * Returns the signature of the external service.
     * Which is usually contains protocol, remote hostname, and port.
     */
    fun getSignature(): String {
        return externalServiceInfo.signature()
    }

    /**
     * Returns the active stub mappings from the WireMockServer
     */
    fun getStubs(): List<StubMapping> {
        return wireMockServer.stubMappings
    }


    /**
     * To get all the HTTP/S requests made to the WireMock instance
     *
     * TODO: For now watMatched serves no purpose. Should be handled when
     *  handling diff for the received requests
     *
     * TODO: Query parameters are not available under ServeEvent for some
     *  reasons. Need to check why.
     */
    fun getAllServedRequests(): List<HttpExternalServiceRequest> {
        return wireMockServer.allServeEvents.map {
            HttpExternalServiceRequest(
                it.id,
                it.request.method.value(),
                it.request.url,
                it.request.absoluteUrl,
                it.wasMatched,
                getSignature()
            )
        }.toList()
    }

    /**
     * Reset WireMock to clean everything including stubs and
     * requests.
     */
    fun reset() {
        wireMockServer.resetAll()
    }

    /**
     * Reset the served request on the respective WireMock
     * instance.
     */
    fun resetServedRequests() {
        wireMockServer.resetRequests()
    }

    /**
     * To stop the WireMock server
     */
    fun stopWireMockServer() {
        wireMockServer.stop()
    }

    /**
     * Will remove the stub mapping related to the given id
     */
    fun removeStub(stubId: UUID): Boolean {
        val stubMapping = wireMockServer.getStubMapping(stubId)
        if (stubMapping.isPresent) {
            wireMockServer.removeStubMapping(stubMapping.item)
            return true
        }
        return false
    }
}