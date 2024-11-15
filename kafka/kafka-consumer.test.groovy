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

import org.citrusframework.kafka.message.KafkaMessage
import org.citrusframework.spi.Resources

import static org.citrusframework.actions.SendMessageAction.Builder.send
import static org.citrusframework.camel.dsl.CamelSupport.camel
import static org.citrusframework.testcontainers.actions.TestcontainersActionBuilder.testcontainers

name "KafkaConsumerTest"
description "Sample test in Groovy"

given:
    variables {
        key = "citrus:randomNumber(4)"
        message = "Hello Kafka"
    }

given:
    $(testcontainers()
            .kafka()
            .start()
    )

when:
    $(camel().jbang()
            .run()
            .integrationName("kafka-consumer")
            .integration(Resources.create("KafkaConsumer.java"))
            .withSystemProperties(Resources.create("application.properties"))
    )

then:
    $(send()
        .endpoint('kafka:demo-topic?server=${CITRUS_TESTCONTAINERS_KAFKA_BOOTSTRAP_SERVERS}')
        .message(new KafkaMessage('${message}', Collections.singletonMap("messageId", '${key}'))
                .messageKey('${key}'))
    )

then:
    $(camel().jbang()
            .verify("kafka-consumer")
            .waitForLogMessage('${message}')
    )
