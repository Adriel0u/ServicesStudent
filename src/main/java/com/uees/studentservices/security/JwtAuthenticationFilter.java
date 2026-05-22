package com.uees.studentservices.security;

import com.uees.studentservices.repository.StudentRepository;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String HEADER = "Authorization";
    private static final String PREFIX = "Bearer ";

    private final JwtService jwt;
    private final StudentRepository students;

    public JwtAuthenticationFilter(JwtService jwt, StudentRepository students) {
        this.jwt = jwt;
        this.students = students;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws ServletException, IOException {
        String authHeader = request.getHeader(HEADER);
        if (authHeader == null || !authHeader.startsWith(PREFIX)) {
            chain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(PREFIX.length());
        try {
            if (!jwt.isTokenValid(token)) {
                chain.doFilter(request, response);
                return;
            }

            String type = jwt.extractTokenType(token);
            if (!JwtService.TOKEN_TYPE_ACCESS.equals(type)) {
                chain.doFilter(request, response);
                return;
            }

            UUID studentId = jwt.extractStudentId(token);
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                students.findById(studentId).ifPresent(student -> {
                    StudentPrincipal principal = new StudentPrincipal(student);
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                });
            }
        } catch (JwtException | IllegalArgumentException ignored) {
            SecurityContextHolder.clearContext();
        }

        chain.doFilter(request, response);
    }
}
