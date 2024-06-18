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
package gov.cdc.izgateway.transformation.camel.routers;

import gov.cdc.izgateway.transformation.services.Hl7TransformerService;
import org.apache.camel.Exchange;
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

      onException(Exception.class)
              .process(exchange -> {
                  Exception exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
                  log.error("Exception caught during processing: ", exception);
                  // You can add more custom handling logic here
              })
              .handled(true); // This will prevent the exception from being propagated further

    /*
     * This route will take the incoming message (the IZGHub wsdl
     * endpoint used for the transformation service)  and transform it
     * using the hl7TransformerService. The transformed message will
     * then be sent to the IZ Gateway Hub.  The response from
     * the IZ Gateway Hub will be transformed using the
     * hl7TransformerService.
     */
    from("direct:izghubTransform")
        .bean(hl7TransformerService)
        .to("izghub:IISHubService")
        .bean(hl7TransformerService);

//    from("file:/Users/cahilp/temp/hl7?noop=true")
//      .to("direct:izghubTransform")
//      .to("file:/Users/cahilp/temp/hl7/processed?fileName=${date:now:yyyyMMddHHmmssSSS}.txt");



    /* This would be cool */
    /*
    from("direct:fhirTransform")
        .bean(hl7TransformerService)
        .to("izghub:IISHubService")
        .bean(hl7TransformerService);
    */
  }

}
