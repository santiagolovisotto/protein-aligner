package com.santiagolovisotto.proteinaligner.web;

import com.santiagolovisotto.proteinaligner.alignment.SequenceTooLongException;
import com.santiagolovisotto.proteinaligner.pdb.InvalidPdbCodeException;
import com.santiagolovisotto.proteinaligner.pdb.NoProteinEntitiesException;
import com.santiagolovisotto.proteinaligner.pdb.PdbEntryNotFoundException;
import com.santiagolovisotto.proteinaligner.pdb.PdbUnavailableException;
import com.santiagolovisotto.proteinaligner.pdb.ProteinEntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(InvalidPdbCodeException.class)
    ProblemDetail invalidCode(InvalidPdbCodeException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(InvalidRequestException.class)
    ProblemDetail invalidRequest(InvalidRequestException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail invalidBody(MethodArgumentNotValidException e) {
        String detail = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ProblemDetail unreadableBody(HttpMessageNotReadableException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "El cuerpo de la solicitud no es un JSON valido o tiene un valor no reconocido");
    }

    @ExceptionHandler(PdbEntryNotFoundException.class)
    ProblemDetail entryNotFound(PdbEntryNotFoundException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(ProteinEntityNotFoundException.class)
    ProblemDetail proteinEntityNotFound(ProteinEntityNotFoundException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(NoProteinEntitiesException.class)
    ProblemDetail noProteins(NoProteinEntitiesException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(422), e.getMessage());
    }

    @ExceptionHandler(SequenceTooLongException.class)
    ProblemDetail tooLong(SequenceTooLongException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(422), e.getMessage());
    }

    @ExceptionHandler(UnsupportedOperationException.class)
    ProblemDetail notImplemented(UnsupportedOperationException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_IMPLEMENTED, e.getMessage());
    }

    @ExceptionHandler(PdbUnavailableException.class)
    ProblemDetail pdbUnavailable(PdbUnavailableException e) {
        log.warn("RCSB PDB no disponible: {}", e.getMessage(), e);
        return ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage());
    }
}