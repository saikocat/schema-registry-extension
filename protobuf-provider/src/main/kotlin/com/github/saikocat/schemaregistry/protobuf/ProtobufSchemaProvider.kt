/*
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
package com.github.saikocat.schemaregistry.protobuf

import io.confluent.kafka.schemaregistry.ParsedSchema
import io.confluent.kafka.schemaregistry.SchemaProvider
import io.confluent.kafka.schemaregistry.client.SchemaVersionFetcher
import io.confluent.kafka.schemaregistry.client.rest.entities.Schema
import io.confluent.kafka.schemaregistry.client.rest.entities.SchemaReference
import io.confluent.kafka.schemaregistry.protobuf.ProtobufSchema
import java.util.Optional
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * ProtobufSchemaProvider allows injection of a SchemaVersionFetcher without going through the
 * config string like AbstractSchemaProvider.
 */
class ProtobufSchemaProvider(val fetcher: SchemaVersionFetcher) : SchemaProvider {
    fun schemaVersionFetcher(): SchemaVersionFetcher = fetcher

    override fun schemaType(): String = ProtobufSchema.TYPE

    override fun parseSchema(
        schemaString: String, references: List<SchemaReference>
    ): Optional<ParsedSchema> {
        try {
            return Optional.of(
                ProtobufSchema(schemaString, references, resolveReferences(references), null, null))
        } catch (e: Exception) {
            log.error("Could not parse Protobuf schema", e)
            return Optional.empty()
        }
    }

    fun resolveReferences(references: List<SchemaReference>?): Map<String, String> {
        if (references == null) {
            return java.util.Collections.emptyMap()
        }
        val result = java.util.LinkedHashMap<String, String>()
        resolveReferences(references, result)
        return result
    }

    private fun resolveReferences(
        references: List<SchemaReference>, schemas: MutableMap<String, String>
    ) {
        references.forEach { reference ->
            if (reference.getName() == null ||
                reference.getSubject() == null ||
                reference.getVersion() == null) {
                throw IllegalStateException("Invalid reference: $reference")
            }
            val subject: String = reference.getSubject()
            if (!schemas.containsKey(reference.getName())) {
                val schema: Schema? =
                    schemaVersionFetcher().getByVersion(subject, reference.getVersion(), true)
                if (schema == null) {
                    throw IllegalStateException(
                        "No schema reference found for subject \"$subject\" and version ${reference.getVersion()}")
                }
                if (reference.getVersion() == -1) {
                    // Update the version with the latest
                    reference.setVersion(schema.getVersion())
                }
                resolveReferences(schema.getReferences(), schemas)
                schemas.put(reference.getName(), schema.getSchema())
            }
        }
    }

    companion object {
        private final val log: Logger =
            LoggerFactory.getLogger(ProtobufSchemaProvider::class.java.enclosingClass)
    }
}
