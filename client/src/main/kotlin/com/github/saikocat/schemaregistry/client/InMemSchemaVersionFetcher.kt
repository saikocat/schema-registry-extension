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
package com.github.saikocat.schemaregistry.client

import io.confluent.kafka.schemaregistry.client.SchemaVersionFetcher
import io.confluent.kafka.schemaregistry.client.rest.entities.Schema
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

typealias SubjectAndVersion = Pair<String, Int>

/**
 * InMemSchemaVersionFetcher is a thread-safe in memory schema version fetcher with an ability to
 * add/update new entries into its cache.
 */
class InMemSchemaVersionFetcher(
    initialData: Map<SubjectAndVersion, Schema> = emptyMap<SubjectAndVersion, Schema>()
) : SchemaVersionFetcher {
    val resolvedCache: ConcurrentMap<SubjectAndVersion, Schema> =
        ConcurrentHashMap(initialData.toMutableMap())

    /** getByVersion returns a Schema from the in memory cache */
    override fun getByVersion(
        subject: String, version: Int, lookupDeletedSchema: Boolean
    ): Schema? {
        return resolvedCache.get(SubjectAndVersion(subject, version))
    }

    /** updateCache defines a method to update/add a subject version and schema */
    fun updateCache(sv: SubjectAndVersion, schema: Schema) = resolvedCache.put(sv, schema)

    /** updateCache defines a method to mass add/update from a map of subject version and schema */
    fun updateCache(source: Map<SubjectAndVersion, Schema>) = resolvedCache.putAll(source)

    /** clearCache removes all entries from existing cache */
    @Synchronized fun clearCache() = resolvedCache.clear()
}
