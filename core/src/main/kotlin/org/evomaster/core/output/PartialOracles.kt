package org.evomaster.core.output


import org.evomaster.core.output.oracles.ImplementedOracle
import org.evomaster.core.output.oracles.SchemaOracle
import org.evomaster.core.output.oracles.SupportedCodeOracle
import org.evomaster.core.problem.rest.RestCallAction
import org.evomaster.core.problem.rest.RestCallResult
import org.evomaster.core.problem.rest.RestIndividual
import org.evomaster.core.search.EvaluatedAction
import org.evomaster.core.search.EvaluatedIndividual

/**
 * [PartialOracles] are meant to be a way to handle different types of soft assertions/expectations (name may change in future)
 *
 * The idea is that any new type of expectation is a partial oracle (again, name may change, if anything more appropriate
 * emerges). For example, if a return object is specified in the Swagger for a given endpoint with a given status code,
 * then the object returned should have the same structure as the Swagger reference (see [responseStructure] below.
 *
 * They are "partial" because failing such a test is not necessarily indicative of a bug, as it could be some sort
 * or shortcut or something (and since REST semantics are not strictly enforced, it cannot be an assert). Nevertheless,
 * it would be a break with expected semantics and could be indicative of a fault or design problem.
 *
 * The [PartialOracles] would (in future) each receive their own variable to turn on or off, and only those selected
 * would be added to the code. So they should be independent from each other. The idea is that, either during generation
 * or during execution, the user can decide if certain partial oracles are of interest at the moment, and turn then
 * on or off as required.
 *
 */

class PartialOracles {
    private lateinit var objectGenerator: ObjectGenerator
    private lateinit var format: OutputFormat
    //private var oracles = mutableListOf(SupportedCodeOracle())
    private var oracles = mutableListOf(SupportedCodeOracle(), SchemaOracle())
    private val expectationsMasterSwitch = "ems"

    fun variableDeclaration(lines: Lines, format: OutputFormat, active: MutableMap<String, Boolean>){
        for (oracle in oracles){
            if(active.get(oracle.getName()) == true) {
                        oracle.variableDeclaration(lines, format)
            }
        }
    }

    fun addExpectations(call: RestCallAction, lines: Lines, res: RestCallResult, name: String, format: OutputFormat) {
        val generates = oracles.any {
            it.generatesExpectation(call, res)
        }
        if (!generates) return
        lines.add("expectationHandler.expect($expectationsMasterSwitch)")
        lines.indented {
            for (oracle in oracles) { oracle.addExpectations(call, lines, res, name, format) }
            if (format.isJava()) { lines.append(";") }
        }
    }


    fun setGenerator(objGen: ObjectGenerator){
        objectGenerator = objGen
        oracles.forEach {
            it.setObjectGenerator(objectGenerator)
        }
    }

    fun setFormat(format: OutputFormat = OutputFormat.KOTLIN_JUNIT_5){
        this.format = format
    }

    fun selectForClustering(action: EvaluatedAction): Boolean{
        if (::objectGenerator.isInitialized){
            return oracles.any { oracle ->
                oracle.selectForClustering(action)
            }
        }
        else return false;
    }

    fun generatesExpectation(individual: EvaluatedIndividual<RestIndividual>): Boolean{
        return oracles.any { oracle ->
            individual.evaluatedActions().any {
                oracle.generatesExpectation(
                        (it.action as RestCallAction),
                        (it.result as RestCallResult)
                )
            }
        }
    }

    fun failByOracle(individuals: MutableList<EvaluatedIndividual<RestIndividual>>): MutableMap<String, MutableList<EvaluatedIndividual<RestIndividual>>>{
        val oracleInds = mutableMapOf<String, MutableList<EvaluatedIndividual<RestIndividual>>>()
        oracles.forEach { oracle ->
            val failindInds = individuals.filter {
                it.evaluatedActions().any { oracle.selectForClustering(it) }
            }.toMutableList()
            failindInds.sortBy { it.evaluatedActions().size }
            oracleInds.put(oracle.getName(), failindInds)
        }
        return oracleInds
    }

    fun activeOracles(individuals: MutableList<EvaluatedIndividual<RestIndividual>>): MutableMap<String, Boolean>{
        val active = mutableMapOf<String, Boolean>()
        oracles.forEach { oracle ->
            active.put(oracle.getName(), individuals.any { individual ->
                individual.evaluatedActions().any {
                    oracle.generatesExpectation(
                            (it.action as RestCallAction),
                            (it.result as RestCallResult)
                    )
                } })
        }
        return active
    }

    fun adjustName(): MutableList<ImplementedOracle>{
        return oracles.filter { it.adjustName() != null }.toMutableList()
    }

}