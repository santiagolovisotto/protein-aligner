package com.santiagolovisotto.proteinaligner.pdb;

public class ProteinEntityNotFoundException extends RuntimeException {

    public ProteinEntityNotFoundException(PdbCode code, int entityId) {
        super("La entidad " + entityId + " de la entrada " + code.value() + " no existe o no es una proteina");
    }
}