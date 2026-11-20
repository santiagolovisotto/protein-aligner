package com.santiagolovisotto.proteinaligner.pdb;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RcsbPdbEntrySourceTest {

    private static final PdbCode HEMOGLOBINA = new PdbCode("4HHB");
    private static final String RUTA = "/fasta/entry/4HHB/download";

    private HttpServer server;
    private String baseUrl;

    @BeforeEach
    void iniciarServidor() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.start();
        baseUrl = "http://localhost:" + server.getAddress().getPort();
    }

    @AfterEach
    void detenerServidor() {
        server.stop(0);
    }

    private void responder(int estado, String cuerpo) {
        server.createContext(RUTA, exchange -> {
            byte[] bytes = cuerpo.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(estado, bytes.length == 0 ? -1 : bytes.length);
            if (bytes.length > 0) {
                try (OutputStream out = exchange.getResponseBody()) {
                    out.write(bytes);
                }
            }
            exchange.close();
        });
    }

    private RcsbPdbEntrySource fuente() {
        return new RcsbPdbEntrySource(baseUrl, Duration.ofSeconds(2), Duration.ofSeconds(2));
    }

    @Test
    void obtieneLasEntidadesDeUnaRespuestaCorrecta() {
        responder(200, """
                >4HHB_1|Chains A, C|Hemoglobin subunit alpha|Homo sapiens (9606)
                VLSPADKTNV
                >4HHB_2|Chains B, D|Hemoglobin subunit beta|Homo sapiens (9606)
                VHLTPEEKSA
                """);

        List<PdbEntity> entidades = fuente().fetchEntities(HEMOGLOBINA);

        assertEquals(2, entidades.size());
        assertEquals("4HHB_1", entidades.get(0).id());
        assertEquals("VHLTPEEKSA", entidades.get(1).sequence());
    }

    @Test
    void unaEntradaInexistenteDaNoEncontrada() {
        responder(404, "");

        assertThrows(PdbEntryNotFoundException.class, () -> fuente().fetchEntities(HEMOGLOBINA));
    }

    @Test
    void unaRespuestaVaciaSeTrataComoEntradaInexistente() {
        responder(200, "");

        assertThrows(PdbEntryNotFoundException.class, () -> fuente().fetchEntities(HEMOGLOBINA));
    }

    @Test
    void unErrorDelServidorDaServicioNoDisponible() {
        responder(500, "error");
        assertThrows(PdbUnavailableException.class, () -> fuente().fetchEntities(HEMOGLOBINA));
    }

    @Test
    void unaRespuestaConFormatoRaroDaServicioNoDisponible() {
        responder(200, "esto no es un FASTA");

        assertThrows(PdbUnavailableException.class, () -> fuente().fetchEntities(HEMOGLOBINA));
    }

    @Test
    void unServidorCaidoDaServicioNoDisponible() {
        responder(200, ">4HHB_1|Chains A|Prueba|Homo sapiens (9606)\nVLSPADK\n");
        RcsbPdbEntrySource fuente = fuente();
        server.stop(0);

        assertThrows(PdbUnavailableException.class, () -> fuente.fetchEntities(HEMOGLOBINA));
    }

    @Test
    void unServidorLentoDaServicioNoDisponiblePorTimeout() {
        server.createContext(RUTA, exchange -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            exchange.close();
        });
        RcsbPdbEntrySource fuente =
                new RcsbPdbEntrySource(baseUrl, Duration.ofSeconds(2), Duration.ofMillis(200));

        assertThrows(PdbUnavailableException.class, () -> fuente.fetchEntities(HEMOGLOBINA));
    }
}