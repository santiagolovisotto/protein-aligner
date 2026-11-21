package com.santiagolovisotto.proteinaligner.alignment;

import com.santiagolovisotto.proteinaligner.pdb.PdbEntity;
import com.santiagolovisotto.proteinaligner.pdb.PdbLookupService;

public class AlignProteinsService {

    private final PdbLookupService pdb;
    private final Aligner aligner;
    private final int maxSequenceLength;

    public AlignProteinsService(PdbLookupService pdb, Aligner aligner, int maxSequenceLength) {
        if (maxSequenceLength < 1) {
            throw new IllegalArgumentException("El largo maximo debe ser al menos 1");
        }
        this.pdb = pdb;
        this.aligner = aligner;
        this.maxSequenceLength = maxSequenceLength;
    }

    public ProteinComparison compare(
            ProteinReference queryRef,
            ProteinReference targetRef,
            AlignmentType type,
            AlignmentParameters parameters) {
        PdbEntity query = pdb.findProtein(queryRef.code(), queryRef.entityId());
        PdbEntity target = pdb.findProtein(targetRef.code(), targetRef.entityId());
        ensureWithinLimit(query);
        ensureWithinLimit(target);

        AlignmentResult result = aligner.align(
                new AlignmentRequest(query.sequence(), target.sequence(), type, parameters));
        return new ProteinComparison(query, target, result);
    }

    private void ensureWithinLimit(PdbEntity entity) {
        if (entity.length() > maxSequenceLength) {
            throw new SequenceTooLongException(entity.id(), entity.length(), maxSequenceLength);
        }
    }
}