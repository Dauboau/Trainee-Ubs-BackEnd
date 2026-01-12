package com.ubs.ExpenseManager.security.filter;

import com.ubs.ExpenseManager.security.jwt.CustomUserDetailsService;
import com.ubs.ExpenseManager.security.jwt.JwtService;
import com.ubs.ExpenseManager.utils.Constants.Jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final AuthenticationEntryPoint authenticationEntryPoint;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {
        String authHeader = getAuthHeader(request);
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith(Jwt.JWT_BEARER)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(Jwt.JWT_BEARER.length());

        try {
            String login = jwtService.extractLogin(token);
            if (userNotAuthenticated(login)) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(login);
                if (jwtService.isTokenValid(token, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = buildAuthToken(userDetails,
                        request);
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException ex) {
            authenticationEntryPoint.commence(
                request,
                response,
                new BadCredentialsException("Token expired")
            );
        } catch (JwtException ex) {
            SecurityContextHolder.clearContext();
            authenticationEntryPoint.commence(
                request,
                response,
                new BadCredentialsException("Invalid token", ex)
            );
        }
    }

    private String getAuthHeader(HttpServletRequest request) {
        String headerToken = request.getHeader(Jwt.JWT_AUTHORIZATION);
        if (StringUtils.hasText(headerToken)) {
            return headerToken;
        }
        return null;
    }

    private boolean userNotAuthenticated(String userName) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (StringUtils.hasText(userName) && authentication == null);
    }

    private UsernamePasswordAuthenticationToken buildAuthToken(UserDetails userDetails,
        HttpServletRequest request) {
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
            userDetails, null, userDetails.getAuthorities());
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        return authToken;
    }
}