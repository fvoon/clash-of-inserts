package com.moneylion.fvoon.clashofinserts;

import org.springframework.boot.SpringApplication;

public class TestClashOfInsertsApplication {

	public static void main(String[] args) {
		SpringApplication.from(ClashOfInsertsApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
