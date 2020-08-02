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
package com.github.saikocat.schemaregistry.client.utils

import io.confluent.kafka.schemaregistry.ParsedSchema
import io.confluent.kafka.schemaregistry.SchemaProvider
import io.confluent.kafka.schemaregistry.client.rest.entities.Schema
import io.confluent.kafka.schemaregistry.client.rest.entities.SchemaString
import io.confluent.kafka.schemaregistry.utils.JacksonMapper
import java.util.Optional

/**
 * RestSchemaHelper provides conversions for REST json string into Schema and SchemaString entities
 */
object RestSchemaHelper {
    /**
     * parseRestSchema converts REST json response into Schema entities Schema(subject, version, id,
     * schemaType, references, schema).
     */
    fun parseRestSchema(json: String): Schema {
        return JacksonMapper.INSTANCE.readValue(json, Schema::class.java)
    }

    /**
     * toSchemaString converts a Schema into a SchemaString as most provider works on SchemaString
     * to parse into a ParseSchema.
     */
    fun toSchemaString(restSchema: Schema): SchemaString {
        return SchemaString().apply {
            schemaType = restSchema.schemaType
            schemaString = restSchema.schema
            references = restSchema.references
            maxId = restSchema.id
        }
    }

    /** toParsedSchema converts a rest schema into an optional ParsedSchema. */
    fun toParsedSchema(restSchema: Schema, provider: SchemaProvider): Optional<ParsedSchema> {
        val schemaString = toSchemaString(restSchema)
        return toParsedSchema(schemaString, provider)
    }

    fun toParsedSchema(schemaStringJson: String, provider: SchemaProvider): Optional<ParsedSchema> {
        val schemaString = SchemaString.fromJson(schemaStringJson)
        return toParsedSchema(schemaString, provider)
    }

    fun toParsedSchema(
        schemaString: SchemaString, provider: SchemaProvider
    ): Optional<ParsedSchema> {
        // TODO: can throw unknown schemaType with wrong provider
        return provider.parseSchema(schemaString.getSchemaString(), schemaString.getReferences())
    }
}
