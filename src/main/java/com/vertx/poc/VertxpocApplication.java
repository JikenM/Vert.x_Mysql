package com.vertx.poc;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.vertx.core.Vertx;

@SpringBootApplication
public class VertxpocApplication {
	
	@Autowired
	VertxMySQLConnection mySQLConnection;
	
	public static void main(String[] args) {
		SpringApplication.run(VertxpocApplication.class, args);
	}
	
	@PostConstruct
    public void deployVerticle() {
        final Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(mySQLConnection);
    }
}
