package com.santiagolovisotto.proteinaligner.pdb;

import java.util.List;

public class PdbLookupService {

    private final PdbEntrySource source;

    public PdbLookupService(PdbEntrySource source) {
        this.source = source;
    }

    public List<PdbEntity> findProteins(PdbCode code) {
        List<PdbEntity> proteins = source.fetchEntities(code).stream()
                .filter(PdbEntity::isProtein)
                .toList();
        if (proteins.isEmpty()) {
            throw new NoProteinEntitiesException(code);
        }
        return proteins;
    }
}