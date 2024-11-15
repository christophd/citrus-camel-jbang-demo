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

import org.citrusframework.kubernetes.ClusterType
import org.citrusframework.spi.Resources

import static org.citrusframework.camel.dsl.CamelSupport.camel
import static org.citrusframework.knative.actions.KnativeActionBuilder.knative

name "KnativeSourceTest"
description "Sample test in Groovy"

given:
    variables {
        timerMessage = "Hello Knative!"
    }

given:
    $(knative()
        .brokers()
        .create("default")
        .clusterType(ClusterType.LOCAL)
    )

when:
    $(camel().jbang()
            .run()
            .integrationName("knative-source")
            .integration(Resources.create("KnativeSource.java"))
            .withSystemProperties(Resources.create("application.properties"))
            .withSystemProperty("timer.message", 'citrus:urlEncode(${timerMessage})')
            .withEnv("K_SINK", "http://localhost:8080")
    )

then:
    $(knative()
        .event()
        .receive()
        .serviceName("default")
        .eventData('${timerMessage}')
        .attribute("ce-id", "@notNull()@")
        .attribute("ce-type", "org.apache.camel.event.messages")
        .attribute("ce-source", "org.apache.camel")
        .attribute("Content-Type", "text/plain")
    )
