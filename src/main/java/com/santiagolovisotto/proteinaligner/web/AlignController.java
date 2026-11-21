package com.santiagolovisotto.proteinaligner.web;

import com.santiagolovisotto.proteinaligner.alignment.AlignProteinsService;
import com.santiagolovisotto.proteinaligner.alignment.AlignmentParameters;
import com.santiagolovisotto.proteinaligner.alignment.ProteinReference;
import com.santiagolovisotto.proteinaligner.pdb.PdbCode;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/align")
class AlignController {

    private final AlignProteinsService service;

    AlignController(AlignProteinsService service) {
        this.service = service;
    }

    @PostMapping
    ComparisonResponse align(@Valid @RequestBody AlignRequestBody body) {
        return ComparisonResponse.from(service.compare(
                reference(body.query()),
                reference(body.target()),
                body.type(),
                parameters(body)));
    }

    private static ProteinReference reference(AlignRequestBody.ProteinRefBody ref) {
        return new ProteinReference(new PdbCode(ref.code()), ref.entityId());
    }

    private static AlignmentParameters parameters(AlignRequestBody body) {
        AlignmentParameters defaults = AlignmentParameters.defaults();
        try {
            return new AlignmentParameters(
                    body.matrix() != null ? body.matrix() : defaults.matrix(),
                    body.gapOpen() != null ? body.gapOpen() : defaults.gapOpen(),
                    body.gapExtension() != null ? body.gapExtension() : defaults.gapExtension());
        } catch (IllegalArgumentException e) {
            throw new InvalidRequestException(e.getMessage());
        }
    }
}