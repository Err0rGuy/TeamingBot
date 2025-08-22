package org.linker.plnm;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GroupLinkerBotApplication {

    public static void main(String[] args) {
        var app = new SpringApplication(GroupLinkerBotApplication.class);
        app.setBannerMode(Banner.Mode.OFF);
        app.run(args);
        System.out.println("Application started....");
    }

}
