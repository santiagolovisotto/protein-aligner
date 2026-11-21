package com.santiagolovisotto.proteinaligner.pdb;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Decorador que recuerda las ultimas consultas exitosas (cache LRU en memoria).
 * Los errores no se guardan: el siguiente intento vuelve a consultar la fuente.
 */
public class CachingPdbEntrySource implements PdbEntrySource {

    private final PdbEntrySource delegate;
    private final Map<PdbCode, List<PdbEntity>> cache;

    public CachingPdbEntrySource(PdbEntrySource delegate, int maxEntries) {
        if (maxEntries < 1) {
            throw new IllegalArgumentException("El tamano de la cache debe ser al menos 1");
        }
        this.delegate = delegate;
        this.cache = Collections.synchronizedMap(new LinkedHashMap<>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<PdbCode, List<PdbEntity>> eldest) {
                return size() > maxEntries;
            }
        });
    }

    @Override
    public List<PdbEntity> fetchEntities(PdbCode code) {
        List<PdbEntity> cached = cache.get(code);
        if (cached != null) {
            return cached;
        }
        List<PdbEntity> fetched = delegate.fetchEntities(code);
        cache.put(code, fetched);
        return fetched;
    }
}