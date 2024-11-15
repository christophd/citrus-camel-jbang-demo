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
import org.citrusframework.testcontainers.aws2.LocalStackContainer

import static org.apache.camel.builder.endpoint.StaticEndpointBuilders.aws2S3
import static org.citrusframework.actions.CreateVariablesAction.Builder.createVariables
import static org.citrusframework.actions.SendMessageAction.Builder.send
import static org.citrusframework.camel.dsl.CamelSupport.camel
import static org.citrusframework.testcontainers.actions.TestcontainersActionBuilder.testcontainers

name "AwsS3ConsumerTest"
description "Sample test in Groovy"

given:
    $(createVariables()
            .variable("aws.s3.bucketNameOrArn", "citrus-camel-demo")
            .variable("aws.s3.key", "hello.txt")
            .variable("aws.s3.message", "Hello Camel!")
    )

given:
    $(testcontainers()
            .localstack()
            .start()
            .withService(LocalStackContainer.Service.S3)
    )

given:
    $(camel().camelContext().start())

given:
    $(camel().bind().component("amazonS3Client", Resources.create("amazonS3Client.groovy")))

when:
    $(camel().jbang()
            .run()
            .integrationName("aws-s3-consumer")
            .integration(Resources.create("AwsS3Consumer.java"))
            .withSystemProperties(Resources.create("application.properties"))
    )

then:
    $(camel()
            .send()
            .endpoint(aws2S3('${aws.s3.bucketNameOrArn}')
                        .advanced()
                        .amazonS3Client('#amazonS3Client')::getRawUri)
            .message()
            .body('${aws.s3.message}')
            .header("CamelAwsS3Key", '${aws.s3.key}')
    )

then:
    $(camel().jbang()
            .verify("aws-s3-consumer")
            .waitForLogMessage('''
                Body: { "message": "${aws.s3.message}" }
                ''')
    )
