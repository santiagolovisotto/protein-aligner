package com.santiagolovisotto.proteinaligner.pdb;

import java.util.List;

/**
 * Fuente de entidades de una entrada PDB.
 *
 * @throws PdbEntryNotFoundException si el codigo no existe en la base de datos
 * @throws PdbUnavailableException   si no se pudo consultar la fuente (red, servicio caido, respuesta rara)
 */
public interface PdbEntrySource {

    List<PdbEntity> fetchEntities(PdbCode code);
}