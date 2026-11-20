package com.santiagolovisotto.proteinaligner.pdb;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

public class RcsbPdbEntrySource implements PdbEntrySource {

    private final HttpClient client;
    private final String baseUrl;
    private final Duration requestTimeout;

    private static final List<String> NOT_FOUND_MESSAGES = List.of(
            "No valid PDB IDs",
            "No fasta files were found");

    public RcsbPdbEntrySource(String baseUrl, Duration connectTimeout, Duration requestTimeout) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.requestTimeout = requestTimeout;
        this.client = HttpClient.newBuilder()
                .connectTimeout(connectTimeout)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    @Override
    public List<PdbEntity> fetchEntities(PdbCode code) {
        HttpRequest request = HttpRequest.newBuilder(
                        URI.create(baseUrl + "/fasta/entry/" + code.value() + "/download"))
                .timeout(requestTimeout)
                .header("Accept", "text/x-fasta, text/plain")
                .GET()
                .build();

        HttpResponse<String> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new PdbUnavailableException("No se pudo consultar RCSB PDB", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PdbUnavailableException("La consulta a RCSB PDB fue interrumpida", e);
        }

        int status = response.statusCode();
        if (status == 404) {
            throw new PdbEntryNotFoundException(code);
        }
        if (status != 200) {
            throw new PdbUnavailableException("RCSB PDB respondio con estado " + status);
        }

        String body = response.body();
        if (body.isBlank() || NOT_FOUND_MESSAGES.stream().anyMatch(body::contains)) {
            throw new PdbEntryNotFoundException(code);
        }
        return RcsbFastaParser.parse(code, body);
    }
}