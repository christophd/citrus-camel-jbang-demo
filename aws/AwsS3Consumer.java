/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
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

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.spi.DataType;

public class AwsS3Consumer extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        from("kamelet:aws-s3-source?" +
                "bucketNameOrArn={{aws.s3.bucketNameOrArn}}&" +
                "region={{aws.s3.region}}&" +
                "overrideEndpoint=true&" +
                "forcePathStyle=true&" +
                "uriEndpointOverride={{aws.s3.uriEndpointOverride}}&" +
                "accessKey={{aws.s3.accessKey}}&" +
                "secretKey={{aws.s3.secretKey}}")
            .transform(new DataType("aws2-s3:application-cloudevents"))
            .split(body().tokenize("\n"))
            .filter(simple("${body} != \"\""))
            .setBody()
            .simple("""
                { "message": "${body}" }
                """)
            .to("log:info");
    }
}
