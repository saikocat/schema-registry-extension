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

import io.confluent.kafka.schemaregistry.client.rest.entities.Schema
import io.confluent.kafka.schemaregistry.client.rest.entities.SchemaString
import java.util.stream.Stream
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class RestSchemaHelperTest {

    @ParameterizedTest
    @MethodSource("restJsonAndSchemaProvider")
    fun testParseRestSchema(
        scenario: String,
        json: String,
        expected: Schema,
        @Suppress("UNUSED_PARAMETER")
        ignored: Any
    ) {
        val actual = RestSchemaHelper.parseRestSchema(json)
        assertEquals(expected, actual, scenario)
    }

    @ParameterizedTest
    @MethodSource("restJsonAndExceptionProvider")
    fun testParseRestSchemaException(
        scenario: String, json: String, exceptionClass: Class<Exception>
    ) {
        assertThrows(
            exceptionClass,
            fun() {
                RestSchemaHelper.parseRestSchema(json)
            },
            scenario)
    }

    @ParameterizedTest
    @MethodSource("restJsonAndSchemaProvider")
    fun testToSchemaString(
        scenario: String,
        @Suppress("UNUSED_PARAMETER")
        json: String,
        restSchema: Schema,
        expected: SchemaString
    ) {
        val actual = RestSchemaHelper.toSchemaString(restSchema)
        assertEquals(expected.toJson(), actual.toJson(), scenario)
    }

    // TODO: refactor this
    companion object {
        @JvmStatic
        fun restJsonAndSchemaProvider(): Stream<Arguments> {
            val restSchema =
                Schema(
                    "3rdparty-google.protobuf.timestamp.proto",
                    1,
                    1,
                    "PROTOBUF",
                    emptyList(),
                    "syntax = \"proto3\";\r\n\r\npackage google.protobuf;\r\n\r\noption java_package = \"com.google.protobuf\";\r\noption java_outer_classname = \"TimestampProto\";\r\noption java_multiple_files = true;\r\n\r\nmessage Timestamp {\r\n  int64 seconds = 1;\r\n  int32 nanos = 2;\r\n}")
            return Stream.of(
                Arguments.of(
                    "Empty Json",
                    "{}",
                    Schema(null, null, null, null, emptyList(), null),
                    SchemaString()),
                Arguments.of(
                    "Valid Protobuf Json",
                    """
                    {
                        "id": ${restSchema.id},
                        "version": ${restSchema.version},
                        "subject": "${restSchema.subject}",
                        "schemaType": "${restSchema.schemaType}",
                        "schema": "syntax = \"proto3\";\r\n\r\npackage google.protobuf;\r\n\r\noption java_package = \"com.google.protobuf\";\r\noption java_outer_classname = \"TimestampProto\";\r\noption java_multiple_files = true;\r\n\r\nmessage Timestamp {\r\n  int64 seconds = 1;\r\n  int32 nanos = 2;\r\n}"
                    }
                    """,
                    restSchema,
                    SchemaString().apply {
                        schemaType = restSchema.schemaType
                        schemaString = restSchema.schema
                        references = restSchema.references
                        maxId = restSchema.id
                    }))
        }

        @JvmStatic
        fun restJsonAndExceptionProvider(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(
                    "Invalid Json",
                    "",
                    com.fasterxml.jackson.databind.exc.MismatchedInputException::class.java))
        }
    }
}
