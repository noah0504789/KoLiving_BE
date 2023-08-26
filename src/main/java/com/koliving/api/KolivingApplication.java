package com.koliving.api;

<<<<<<< HEAD
import com.koliving.api.room.domain.Furnishing;
import com.koliving.api.room.domain.FurnishingType;
import com.koliving.api.room.infra.FurnishingRepository;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.boot.CommandLineRunner;
=======
import jakarta.annotation.PostConstruct;
>>>>>>> 1d10d8b... feat: set jvm timezone to utc
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import java.util.TimeZone;

@SpringBootApplication
@ConfigurationPropertiesScan("com.koliving.api.properties")
public class KolivingApplication {

    public static void main(String[] args) {
        SpringApplication.run(KolivingApplication.class, args);
    }

<<<<<<< HEAD
    @Profile("default")
    @Bean
    CommandLineRunner commandLineRunner(FurnishingRepository furnishingRepository) {
        return args -> {
            initFurnishings(furnishingRepository);
        };
    }

    private void initFurnishings(FurnishingRepository furnishingRepository) {
        final List<Furnishing> furnishings = Arrays.stream(FurnishingType.values())
            .map(Furnishing::valueOf)
            .collect(Collectors.toList());

        furnishingRepository.saveAll(furnishings);
=======
    @PostConstruct
    void started() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
>>>>>>> 1d10d8b... feat: set jvm timezone to utc
    }
}
