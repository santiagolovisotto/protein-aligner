package com.santiagolovisotto.proteinaligner.web;

import com.santiagolovisotto.proteinaligner.pdb.PdbEntity;

import java.util.List;

public record PdbEntityResponse(
        String id,
        int entityId,
        List<String> chains,
        String name,
        String organism,
        int length) {

    static PdbEntityResponse from(PdbEntity entity) {
        return new PdbEntityResponse(
                entity.id(),
                entity.entityId(),
                entity.chains(),
                entity.name(),
                entity.organism(),
                entity.length());
    }
}