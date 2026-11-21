package com.santiagolovisotto.proteinaligner.alignment;

import com.santiagolovisotto.proteinaligner.pdb.PdbLookupService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class AlignmentConfiguration {

    @Bean
    AlignProteinsService alignProteinsService(
            PdbLookupService pdb,
            Aligner aligner,
            @Value("${app.alignment.max-sequence-length:2000}") int maxSequenceLength) {
        return new AlignProteinsService(pdb, aligner, maxSequenceLength);
    }
}