package com.santiagolovisotto.proteinaligner.pdb;

public class NoProteinEntitiesException extends RuntimeException {

    public NoProteinEntitiesException(PdbCode code) {
        super("La entrada PDB " + code.value() + " no contiene proteinas");
    }
}