package org.evomaster.core.search.gene

import org.apache.commons.lang3.StringEscapeUtils
import org.evomaster.core.output.OutputFormat
import org.evomaster.core.search.service.Randomness


class StringGene(
        name: String,
        var value: String = "foo",
        /** Inclusive */
        val minLength: Int = 0,
        /** Inclusive */
        val maxLength: Int = 16
) : Gene(name) {

    /*
        Even if through mutation we can get large string, we should
        avoid sampling very large strings by default
     */
    private val maxForRandomizantion = 16

    override fun copy(): Gene {
        val copy = StringGene(name, value, minLength, maxLength)
        return copy
    }


    override fun randomize(randomness: Randomness, forceNewValue: Boolean, allGenes: List<Gene>) {

        //TODO much more would need to be done here to handle strings...
        value = randomness.nextWordString(minLength, Math.min(maxLength, maxForRandomizantion))
    }

    override fun getValueAsPrintableString(previousGenes: List<Gene>, mode: String?, targetFormat: OutputFormat?): String {
        val rawValue = getValueAsRawString()
        if (mode != null && mode.equals("xml")) {
            return StringEscapeUtils.escapeXml(rawValue)
        } else {
            when {
                // TODO this code should be refactored with other getValueAsPrintableString() methods
                (targetFormat == null) -> return "\"$rawValue\""
                targetFormat.isKotlin() -> return "\"$rawValue\""
                        .replace("\\", "\\\\")
                        .replace("$", "\\$")
                else -> return "\"$rawValue\""
                        .replace("\\", "\\\\")
            }
        }
    }

    override fun getValueAsRawString(): String {
        return value
    }

    override fun copyValueFrom(other: Gene) {
        if (other !is StringGene) {
            throw IllegalArgumentException("Invalid gene type ${other.javaClass}")
        }
        this.value = other.value
    }

    override fun containsSameValueAs(other: Gene): Boolean {
        if (other !is StringGene) {
            throw IllegalArgumentException("Invalid gene type ${other.javaClass}")
        }
        return this.value == other.value
    }

}