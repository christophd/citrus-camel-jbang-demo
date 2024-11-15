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

package server

import org.citrusframework.openapi.OpenApiSpecification
import org.citrusframework.spi.Resources
import org.springframework.http.HttpStatus

import static org.citrusframework.camel.dsl.CamelSupport.camel
import static org.citrusframework.openapi.actions.OpenApiActionBuilder.openapi

name "PetstoreTest"
description "Sample test in Groovy"

given:
    variables {
        petId = "1000"
    }

when:
    $(camel().jbang()
        .run()
        .integrationName("petstore")
        .integration(Resources.create("Petstore.java"))
        .addResource("petstore-api.json")
        .withSystemProperties(Resources.create("application.properties"))
    )

OpenApiSpecification petstoreApi = OpenApiSpecification.from("http://localhost:8080/openapi");

then:
    $(openapi().specification(petstoreApi)
            .client("http://localhost:8080/petstore")
            .send("getPetById")
    )

then:
    $(openapi().specification(petstoreApi)
            .client("http://localhost:8080/petstore")
            .receive("getPetById", HttpStatus.OK)
    )
