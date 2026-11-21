package com.santiagolovisotto.proteinaligner.web;

import com.santiagolovisotto.proteinaligner.pdb.PdbCode;
import com.santiagolovisotto.proteinaligner.pdb.PdbLookupService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/pdb")
class PdbController {

    private final PdbLookupService lookup;

    PdbController(PdbLookupService lookup) {
        this.lookup = lookup;
    }

    @GetMapping("/{code}")
    List<PdbEntityResponse> proteins(@PathVariable String code) {
        return lookup.findProteins(new PdbCode(code)).stream()
                .map(PdbEntityResponse::from)
                .toList();
    }
}