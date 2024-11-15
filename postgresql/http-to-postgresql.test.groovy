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

import org.citrusframework.spi.Resources
import org.springframework.http.HttpStatus

import static org.citrusframework.actions.CreateVariablesAction.Builder.createVariables
import static org.citrusframework.actions.ExecuteSQLAction.Builder.sql
import static org.citrusframework.camel.dsl.CamelSupport.camel
import static org.citrusframework.http.actions.HttpActionBuilder.http
import static org.citrusframework.testcontainers.actions.TestcontainersActionBuilder.testcontainers

name "HttpToPostgreSQLTest"
description "Sample test in Groovy"

given:
    $(createVariables()
            .variable("id", "citrus:randomNumber(4)")
            .variable("headline", "Camel rocks!")
    )

given:
    $(testcontainers()
            .postgreSQL()
            .start()
            .initScript(Resources.create("db.init.sql"))
    )

when:
    $(camel().jbang()
            .run()
            .withSystemProperties(Resources.create("application.properties"))
            .integrationName("http-to-postgresql")
            .integration(Resources.create("HttpToPostgreSQL.java"))
    )

then:
    $(http().client("http://localhost:8080")
            .send()
            .post("/headline")
            .message()
            .body('''
                { "id": ${id}, "headline": "${headline}" }
                ''')
            .contentType("application/json")
    )

then:
    $(http().client("http://localhost:8080")
            .receive()
            .response(HttpStatus.OK)
            .message().body("Headline created!")
    )

then:
    $(sql()
        .query()
        .statement("SELECT headline FROM headlines WHERE ID=${id}")
        .validate("HEADLINE", "${headline}")
    )
