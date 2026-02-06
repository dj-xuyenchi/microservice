package com.gatewayservice.config.security;

import lombok.Getter;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Getter
public class CachedBodyServerHttpRequestDecorator extends ServerHttpRequestDecorator {

    private final byte[] cachedBody;

    public CachedBodyServerHttpRequestDecorator(ServerHttpRequest delegate, byte[] cachedBody) {
        super(delegate);
        this.cachedBody = cachedBody;
    }

    @Override
    public Flux<DataBuffer> getBody() {
        // Dùng DefaultDataBufferFactory để wrap byte[]
        return Flux.defer(() -> {
            DataBuffer buffer = new DefaultDataBufferFactory().wrap(cachedBody);
            return Flux.just(buffer);
        });
    }

    public String getCachedBodyAsString() {
        return new String(cachedBody, StandardCharsets.UTF_8);
    }

    public static Mono<CachedBodyServerHttpRequestDecorator> wrap(ServerWebExchange exchange) {
        HttpMethod method = exchange.getRequest().getMethod();

        if (method == HttpMethod.GET || method == HttpMethod.DELETE) {
            return Mono.just(new CachedBodyServerHttpRequestDecorator(exchange.getRequest(), new byte[0]));
        }

        return DataBufferUtils.join(exchange.getRequest().getBody())
                .map(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);
                    return new CachedBodyServerHttpRequestDecorator(exchange.getRequest(), bytes);
                })
                .switchIfEmpty(Mono.defer(() ->
                        Mono.just(new CachedBodyServerHttpRequestDecorator(exchange.getRequest(), new byte[0]))
                ));
    }
}

