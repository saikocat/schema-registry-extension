# schema-registry-extension
Confluent Schema Registry Extension

## Client package
* `InMemSchemaVersionFetcher` is a thread-safe in memory schema version fetcher
  with an ability to add/update new entries into its cache.
* `RestSchemaHelper` utility provides conversions for REST json string into
  `Schema`, `SchemaString`, and `ParsedSchema` entities

## Protobuf-Provider
* `ProtobufSchemaProvider` allows injection of a `SchemaVersionFetcher` without
  going through the config string like `AbstractSchemaProvider`.
