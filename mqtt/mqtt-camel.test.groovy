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

import static org.apache.camel.builder.endpoint.StaticEndpointBuilders.pahoMqtt5
import static org.citrusframework.actions.CreateVariablesAction.Builder.createVariables
import static org.citrusframework.actions.SendMessageAction.Builder.send
import static org.citrusframework.camel.dsl.CamelSupport.camel
import static org.citrusframework.testcontainers.actions.TestcontainersActionBuilder.testcontainers

name "MqttCamelTest"
description "Sample test in Groovy"

given:
    $(createVariables()
        .variable("mqtt.topic", "temperature")
        .variable("mqtt.client.id", "mqtt-citrus-client")
    )

given:
    $(testcontainers()
            .container()
            .start()
            .image("eclipse-mosquitto:latest")
            .containerName("mqtt")
            .serviceName("mqtt")
            .addExposedPort(1883)
            .addPortBinding("1883:1883")
            .withVolumeMount("conf/", "/mosquitto/config")
            .autoRemove(true)
    )

when:
    $(camel().jbang()
            .run()
            .autoRemove(true)
            .integrationName("mqtt-camel")
            .integration(Resources.create("MqttCamel.java"))
            .withSystemProperties(Resources.create("application.properties"))
    )

then:
    $(camel().camelContext().start())
    $(camel()
        .send()
        .endpoint(pahoMqtt5('${mqtt.topic}')
                        .brokerUrl('tcp://localhost:${CITRUS_TESTCONTAINERS_MQTT_PORT}')
                        .clientId('${mqtt.client.id}')::getRawUri)
        .message()
        .body("""
            {
              "value": 21
            }
            """)
    )

then:
    $(camel().jbang()
            .verify("mqtt-camel")
            .waitForLogMessage('Warm temperature at 21')
    )

then:
    $(camel()
        .send()
        .endpoint(pahoMqtt5('${mqtt.topic}')
                        .brokerUrl('tcp://localhost:${CITRUS_TESTCONTAINERS_MQTT_PORT}')
                        .clientId('${mqtt.client.id}')::getRawUri)
        .message()
        .body("""
            {
              "value": 7
            }
            """)
    )

then:
    $(camel().jbang()
            .verify("mqtt-camel")
            .waitForLogMessage('Cold temperature at 7')
    )
