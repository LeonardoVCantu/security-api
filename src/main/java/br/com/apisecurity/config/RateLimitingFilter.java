package br.com.apisecurity.config;
import br.com.apisecurity.dto.ApiErrorResponseDto;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {


    private final ObjectMapper objectMapper;
    @Value("${api.security.rate-limit.capacity}")
    private int capacity;

    @Value("${api.security.rate-limit.refill-tokens}")
    private int refillTokens;

    @Value("${api.security.rate-limit.duration-minutes}")
    private int durationMinutes;

    private final Map<String, Bucket> nativeCahce = new ConcurrentHashMap<>();

    public RateLimitingFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    private Bucket createNewBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.classic(capacity,
                        Refill.intervally(refillTokens, Duration.ofMinutes(durationMinutes))))
                .build();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String ip = getClientIP(request);

        Bucket bucket = nativeCahce.computeIfAbsent(ip, k -> createNewBucket());

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            sendRateLimitResponse(response);
        }
    }

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty()) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }

    private void sendRateLimitResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType("application/json;charset=UTF-8");

        ApiErrorResponseDto errorResponse = new ApiErrorResponseDto(
                "Limite de requisições excedido. Por favor, aguarde antes de tentar novamente.",
                HttpStatus.TOO_MANY_REQUESTS.value(),
                HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase(),
                java.time.LocalDateTime.now(),
                null
        );

        String jsonPayload = objectMapper.writeValueAsString(errorResponse);

        response.getWriter().write(jsonPayload);
    }
}