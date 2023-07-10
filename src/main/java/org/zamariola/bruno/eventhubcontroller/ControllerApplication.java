package org.zamariola.bruno.eventhubcontroller;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@ConfigurationPropertiesScan("org.zamariola.bruno.eventhubcontroller.properties")
public class ControllerApplication {

  public static void main(String[] args) {
    SpringApplication.run(ControllerApplication.class, args);
  }
}
