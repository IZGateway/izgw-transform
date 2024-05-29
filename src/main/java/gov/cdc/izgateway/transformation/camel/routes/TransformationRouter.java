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
package gov.cdc.izgateway.transformation.camel.routes;

//import gov.cdc.izgateway.transformation.services.Hl7TransformerService;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TransformationRouter extends RouteBuilder {


    /**
    * Create a route that does something simple (for now) using the current transformation model that the team has created.
    * Take Austin's example, and implement it here  Each step should know how to process itself.
    * Camel route should define the pipeline of steps.  And keep track of the state of the message and the state of the pipeline.
    */
    @Override
    public void configure() throws Exception {

        // Since HubController is itself a listener, this route will be
        // the entry point for the camel route initiated from HubController
        from("direct:izghub")
          .log("Hello from TransformationRouter!");

    }
}
