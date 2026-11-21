package com.santiagolovisotto.proteinaligner.web;

import com.santiagolovisotto.proteinaligner.pdb.InvalidPdbCodeException;
import com.santiagolovisotto.proteinaligner.pdb.NoProteinEntitiesException;
import com.santiagolovisotto.proteinaligner.pdb.PdbEntryNotFoundException;
import com.santiagolovisotto.proteinaligner.pdb.PdbUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(InvalidPdbCodeException.class)
    ProblemDetail invalidCode(InvalidPdbCodeException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(PdbEntryNotFoundException.class)
    ProblemDetail entryNotFound(PdbEntryNotFoundException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(NoProteinEntitiesException.class)
    ProblemDetail noProteins(NoProteinEntitiesException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(422), e.getMessage());
    }

    @ExceptionHandler(PdbUnavailableException.class)
    ProblemDetail pdbUnavailable(PdbUnavailableException e) {
        log.warn("RCSB PDB no disponible: {}", e.getMessage(), e);
        return ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage());
    }
}