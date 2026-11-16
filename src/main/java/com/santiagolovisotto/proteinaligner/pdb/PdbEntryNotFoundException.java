package com.santiagolovisotto.proteinaligner.pdb;

public class PdbEntryNotFoundException extends RuntimeException {

    public PdbEntryNotFoundException(PdbCode code) {
        super("No se encontro la entrada PDB " + code.value());
    }
}