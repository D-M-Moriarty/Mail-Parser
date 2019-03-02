package com.darren.mail.parser;

import com.darren.mail.parser.pbmapi.PbmApi;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

//import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
public class Application {
  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

  @Bean
  public PbmApi pbmApi() {
    return new PbmApi();
  }

}