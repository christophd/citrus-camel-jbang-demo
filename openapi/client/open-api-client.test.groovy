/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package client

import org.citrusframework.openapi.OpenApiSpecification
import org.citrusframework.spi.Resources
import org.springframework.http.HttpStatus

import static org.citrusframework.camel.dsl.CamelSupport.camel
import static org.citrusframework.openapi.actions.OpenApiActionBuilder.openapi
import static org.citrusframework.script.GroovyAction.Builder.groovy

name "OpenApiClientTest"
description "Sample test in Groovy"

given:
    variables {
        petId = "1000"
    }

given:
    $(groovy()
        .script(Resources.create("petstoreServer.groovy"))
    )

when:
    $(camel().jbang()
        .run()
        .integrationName("openapi-client")
        .integration(Resources.create("OpenApiClient.java"))
        .addResource("petstore-api.json")
        .withSystemProperties(Resources.create("application.properties"))
    )

OpenApiSpecification petstoreApi = OpenApiSpecification.from("petstore-api.json");

then:
    $(openapi().specification(petstoreApi)
            .server("petstoreServer")
            .receive("addPet")
    )

then:
    $(openapi().specification(petstoreApi)
            .server("petstoreServer")
            .send("addPet", HttpStatus.CREATED)
    )
