package com.example.eduaceai.security;

import com.example.eduaceai.utils.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT filter — nếu header có Bearer token mà token không hợp lệ/hết hạn → short-circuit 401.
 *
 * <p>Trước đây filter silently skip khi token invalid → request đi qua anonymous → Spring default
 * entry point trả 403. Sai semantic RFC 6750 + vi phạm consensus toàn cầu "JWT expired → 401".
 * Frontend handle 401 = auto clear + redirect login; 403 = chỉ toast (user bị stuck).
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String JSON_401_BODY =
            "{\"message\":\"Phiên đăng nhập đã hết hạn\","
                    + "\"errorMessage\":\"Vui lòng đăng nhập lại\","
                    + "\"data\":null}";

    private final JwtUtils jwtUtils;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (!jwtUtils.validateToken(token)) {
                // Token hết hạn hoặc invalid → short-circuit 401 ngay, không đi qua filter chain tiếp
                writeUnauthorized(response);
                return;
            }
            String studentCode = jwtUtils.extractStudentCode(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(studentCode);

            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities()
            );
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authToken);
        }
        filterChain.doFilter(request, response);
    }

    private static void writeUnauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("WWW-Authenticate", "Bearer error=\"invalid_token\"");
        response.getWriter().write(JSON_401_BODY);
    }
}
