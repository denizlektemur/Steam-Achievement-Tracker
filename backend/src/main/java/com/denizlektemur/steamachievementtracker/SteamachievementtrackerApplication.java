package com.denizlektemur.steamachievementtracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SteamachievementtrackerApplication {
	public static void main(String[] args) {
		SpringApplication.run(SteamachievementtrackerApplication.class, args);
	}
}