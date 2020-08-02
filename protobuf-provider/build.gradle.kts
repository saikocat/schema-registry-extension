dependencies {
    project(":client")

    api("io.confluent:kafka-protobuf-provider:${DepVersions.schemaRegistry}")
}
