package com.ao.wallet

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.fge.jsonpatch.StandardJsonPatchFactory
import org.springframework.data.jpa.repository.JpaRepository

val objectMapper: ObjectMapper = jacksonObjectMapper().findAndRegisterModules()

fun String.patch(field: String, value: Any?, operation: String = "replace"): String {
    val patch = createPatch(field, value, operation)
    val jsonPatch = StandardJsonPatchFactory.create().fromJson(patch)
    return jsonPatch.apply(this.asJsonNode().deepCopy()).toString()
}

fun String.asJsonNode(): JsonNode = objectMapper.readTree(this)

fun createPatch(path: String, value: Any?, operation: String = "replace"): JsonNode {
    return """[{ 
                 "op": "$operation", 
                 "path": "$path", 
                 "value": ${formatValue(value)}
              }]""".asJsonNode()
}

private fun formatValue(value: Any?): String? {
    return when (value) {
        null -> null
        is Number, is Boolean, is JsonNode -> value.toString()
        else -> '"' + value.toString() + '"'
    }
}

fun <T, ID> JpaRepository<T, ID>.single(): T = findAll().single()