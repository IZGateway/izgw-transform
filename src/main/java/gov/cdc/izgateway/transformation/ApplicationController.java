package gov.cdc.izgateway.transformation;

import lombok.extern.java.Log;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Log
@RestController
public class ApplicationController {

  @GetMapping("/hello")
  public String transform() {
    return "Hello from ApplicationController!";
  }
}
