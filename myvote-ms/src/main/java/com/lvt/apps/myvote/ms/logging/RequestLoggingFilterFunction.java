package com.lvt.apps.myvote.ms.logging;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.client.reactive.ClientHttpRequest;
import org.springframework.http.client.reactive.ClientHttpRequestDecorator;
import org.springframework.lang.NonNull;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.Math.min;

@Slf4j
@RequiredArgsConstructor
public class RequestLoggingFilterFunction implements ExchangeFilterFunction {

    private static final int MAX_BYTES_LOGGED = 4_096;

    private final String externalSystem;

    private final List<String> fieldsToMask;

    /**
     * This method will log the request and response of the external system.
     *
     * @param request ClientRequest
     * @param next ExchangeFunction
     * @return Mono<ClientResponse>
     */
    @Override
    @NonNull
    public Mono<ClientResponse> filter(@NonNull ClientRequest request, @NonNull ExchangeFunction next) {

        AtomicBoolean requestLogged = new AtomicBoolean(false);
        AtomicBoolean responseLogged = new AtomicBoolean(false);

        StringBuilder capturedRequestBody = new StringBuilder();
        StringBuilder capturedResponseBody = new StringBuilder();

        return next.exchange(ClientRequest.from(request).body(new BodyInserter<>() {
             @Override
             @NonNull
             public Mono<Void> insert(@NonNull ClientHttpRequest req, @NonNull Context context) {
                 return request.body().insert(new ClientHttpRequestDecorator(req) {
                     @Override
                     @NonNull
                     public Mono<Void> writeWith(@NonNull Publisher<? extends DataBuffer> body) {
                         return super.writeWith(Flux.from(body).doOnNext(data -> capturedRequestBody.append(extractBytes(data))));
                     }
                 }, context);
             }
            }).build())
            .doOnNext(response -> log.info(getRequestSuccessLogString(request, requestLogged, capturedRequestBody)))
            .doOnError(error -> log.info(getRequestErrorLogString(request, error, requestLogged, capturedResponseBody)))
            .map(response ->
                response.mutate()
                    .body(
                        transformer -> transformer
                            .doOnNext(body -> capturedResponseBody.append(extractBytes(body)))
                            .doOnComplete(() -> log.info(getResponseSuccessLogString(response, responseLogged, capturedResponseBody)))
                            .doOnError(error -> log.info(getResponseErrorLogString(response, error, responseLogged)))
                    )
                    .build()
            );
    }

    /**
     * This method will log the response of the external system.
     * @param response ClientResponse
     * @param error Throwable
     * @param responseLogged AtomicBoolean
     */
    private String getResponseErrorLogString(ClientResponse response, Throwable error, AtomicBoolean responseLogged) {
        if (!responseLogged.getAndSet(true)) {
            StringBuilder logOutput = new StringBuilder(LoggingFormatter.generateResponseHeadingText(externalSystem));
            logOutput.append(LoggingFormatter.formatNameValue("Status", String.valueOf(response.statusCode())));
            logOutput.append(LoggingFormatter.formatNameValue("Error", error.getMessage()));
            return logOutput.toString();
        }
        return null;
    }

    /**
     * This method will log the response of the external system.
     * @param response ClientResponse
     * @param responseLogged AtomicBoolean
     * @param capturedResponseBody StringBuilder
     */
    private String getResponseSuccessLogString(ClientResponse response, AtomicBoolean responseLogged, StringBuilder capturedResponseBody) {
        if (!responseLogged.getAndSet(true)) {
            StringBuilder logOutput = new StringBuilder(LoggingFormatter.generateResponseHeadingText(externalSystem));
            logOutput.append(LoggingFormatter.formatNameValue("Status", String.valueOf(response.statusCode())));
            response.headers().asHttpHeaders().forEach((key, value1) -> value1.forEach(value -> logOutput.append(LoggingFormatter.formatNameValue(key, value))));
            logOutput.append(LoggingFormatter.formatNameValue("Body", capturedResponseBody.toString()));
            return logOutput.toString();
        }
        return null;
    }

    /**
     * This method will log the request of the external system.
     * @param request ClientRequest
     * @param error Throwable
     * @param requestLogged AtomicBoolean
     * @param capturedResponseBody StringBuilder
     */
    private String getRequestErrorLogString(ClientRequest request, Throwable error, AtomicBoolean requestLogged, StringBuilder capturedResponseBody) {
        if (!requestLogged.getAndSet(true)) {
            StringBuilder logOutput = new StringBuilder(LoggingFormatter.generateRequestHeadingText(externalSystem));
            logOutput.append(LoggingFormatter.formatNameValue("Method", request.method().toString()));
            logOutput.append(LoggingFormatter.formatNameValue("URL", String.valueOf(request.url())));
            request.headers().forEach((key, value1) -> value1.forEach(value -> logOutput.append(LoggingFormatter.formatNameValue(key, value))));
            logOutput.append(LoggingFormatter.formatNameValue("Body", capturedResponseBody.toString()));
            logOutput.append(LoggingFormatter.formatNameValue("Error", error.getMessage()));
            return logOutput.toString();
        }
        return null;
    }

    /**
     * This method will create a DoOnNext log string
     * @param request ClientRequest
     * @param requestLogged AtomicBoolean
     * @param capturedRequestBody StringBuilder
     * @return String
     */
    private String getRequestSuccessLogString(ClientRequest request, AtomicBoolean requestLogged, StringBuilder capturedRequestBody) {
        if (!requestLogged.getAndSet(true)) {
            StringBuilder logOutput = new StringBuilder(LoggingFormatter.generateRequestHeadingText(externalSystem));
            logOutput.append(LoggingFormatter.formatNameValue("Method", request.method().toString()));
            logOutput.append(LoggingFormatter.formatNameValue("URL", String.valueOf(request.url())));
            request.headers().forEach((key, value1) -> value1.forEach(value -> logOutput.append(LoggingFormatter.formatNameValue(key, value))));
            logOutput.append(LoggingFormatter.formatNameValue("Body", maskJsonFields(capturedRequestBody.toString(), fieldsToMask)));
            return logOutput.toString();
        }
        return null;
    }

    /**
     * This method will extract the bytes from the DataBuffer.
     *
     * @param data DataBuffer
     * @return String
     */
    private static String extractBytes(DataBuffer data) {

        if(Objects.isNull(data) || data.readableByteCount() == 0) {
            return "";
        }

        int currentReadPosition = data.readPosition();
        var numberOfBytesLogged = min(data.readableByteCount(), MAX_BYTES_LOGGED);
        var bytes = new byte[numberOfBytesLogged];
        data.read(bytes, 0, numberOfBytesLogged);
        data.readPosition(currentReadPosition);

        return new String(bytes);

    }

    /**
     * Mask any fields containing sensitive information that we don't want logged
     *
     * @param json String
     * @param fieldsToMask List<String>
     * @return String
     */
    public static String maskJsonFields(String json, List<String> fieldsToMask) {

        if(Objects.isNull(json)) {
            return null;
        }

        if (fieldsToMask == null || fieldsToMask.isEmpty()) {
            return json;
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(json);
            for (String key : fieldsToMask) {
                JsonNode node = jsonNode.get(key);
                if (Objects.nonNull(node)) {
                    ((ObjectNode) jsonNode).put(key, "*******");
                }
            }
            return objectMapper.writeValueAsString(jsonNode);
        } catch (JsonProcessingException e) {
            log.info(e.toString());
        }
        return "[json parsing error]";
    }

}

