package com.santiagolovisotto.proteinaligner.pdb;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
class PdbConfiguration {

    @Bean
    PdbEntrySource pdbEntrySource(
            @Value("${app.pdb.base-url:https://rcsb.org}") String baseUrl,
            @Value("${app.pdb.connect-timeout-seconds:5}") int connectTimeoutSeconds,
            @Value("${app.pdb.request-timeout-seconds:10}") int requestTimeoutSeconds,
            @Value("${app.pdb.cache-max-entries:100}") int cacheMaxEntries) {
        PdbEntrySource rcsb = new RcsbPdbEntrySource(
                baseUrl,
                Duration.ofSeconds(connectTimeoutSeconds),
                Duration.ofSeconds(requestTimeoutSeconds));
        return new CachingPdbEntrySource(rcsb, cacheMaxEntries);
    }

    @Bean
    PdbLookupService pdbLookupService(PdbEntrySource source) {
        return new PdbLookupService(source);
    }
}