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
package gov.cdc.izgateway.transformation.camel;

import gov.cdc.izgateway.transformation.services.Hl7TransformerService;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TransformationRouter extends RouteBuilder {

  //@Autowired SampleTransform3 sampleTransform;
  @Autowired
  Hl7TransformerService hl7TransformerService;

  @Override
  public void configure() throws Exception {
    /**
     * This route will take the incoming message (the IZGHub wsdl
     * endpoint used for the transformation service)  and transform it
     * using the hl7TransformerService. The transformed message will
     * then be sent to the IZ Gateway Hub.  The response from
     * the IZ Gateway Hub will be transformed using the
     * hl7TransformerService.
     */
    // THIS WAS WORKING - June 11 2024
    from("direct:izghubTransformX")
        .bean(hl7TransformerService);

      // Work on this next:
      from("direct:izghubTransform")
              .bean(hl7TransformerService)
              .to("izghub:IISHubService")
              .bean(hl7TransformerService);
              // TODO: handle the response transformation next .bean(hl7TransformerService);

//    // what would be cool:
//        from("direct:fhirTransform")
//                .bean(hl7TransformerService)
//                .to("izghub:IISHubService")
//                .bean(hl7TransformerService);
//        .bean(hl7TransformerService)

  }
}
