package com.santiagolovisotto.proteinaligner.pdb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Convierte la respuesta FASTA de RCSB (una entrada por entidad) en PdbEntity.
 * Formato: >4HHB_1|Chains A, C|Hemoglobin subunit alpha|Homo sapiens (9606)
 */
final class RcsbFastaParser {

    private RcsbFastaParser() {
    }

    static List<PdbEntity> parse(PdbCode code, String fasta) {
        List<PdbEntity> entities = new ArrayList<>();
        String header = null;
        StringBuilder sequence = new StringBuilder();

        for (String rawLine : fasta.split("\\R")) {
            String line = rawLine.trim();
            if (line.isEmpty()) {
                continue;
            }
            if (line.startsWith(">")) {
                if (header != null) {
                    entities.add(toEntity(code, header, sequence.toString()));
                }
                header = line.substring(1);
                sequence.setLength(0);
            } else {
                if (header == null) {
                    throw new PdbUnavailableException("Respuesta inesperada de RCSB: secuencia sin encabezado");
                }
                sequence.append(line);
            }
        }
        if (header != null) {
            entities.add(toEntity(code, header, sequence.toString()));
        }
        return List.copyOf(entities);
    }

    private static PdbEntity toEntity(PdbCode code, String header, String sequence) {
        String[] parts = header.split("\\|", -1);
        if (parts.length < 4) {
            throw new PdbUnavailableException("Respuesta inesperada de RCSB: encabezado con formato desconocido");
        }
        int entityId = parseEntityId(code, parts[0].trim());
        List<String> chains = parseChains(parts[1]);
        String name = String.join("|", Arrays.copyOfRange(parts, 2, parts.length - 1));
        String organism = parts[parts.length - 1];
        try {
            return new PdbEntity(code, entityId, chains, name, organism, sequence);
        } catch (IllegalArgumentException e) {
            throw new PdbUnavailableException("Respuesta inesperada de RCSB: entidad con datos invalidos", e);
        }
    }

    private static int parseEntityId(PdbCode code, String id) {
        String prefix = code.value() + "_";
        if (!id.regionMatches(true, 0, prefix, 0, prefix.length())) {
            throw new PdbUnavailableException("Respuesta inesperada de RCSB: la entidad no corresponde a " + code.value());
        }
        try {
            return Integer.parseInt(id.substring(prefix.length()));
        } catch (NumberFormatException e) {
            throw new PdbUnavailableException("Respuesta inesperada de RCSB: numero de entidad invalido", e);
        }
    }

    private static List<String> parseChains(String field) {
        String cleaned = field.trim().replaceFirst("^Chains?\\s+", "");
        List<String> chains = Arrays.stream(cleaned.split(","))
                .map(String::trim)
                .filter(chain -> !chain.isEmpty())
                .toList();
        if (chains.isEmpty()) {
            throw new PdbUnavailableException("Respuesta inesperada de RCSB: entidad sin cadenas");
        }
        return chains;
    }
}