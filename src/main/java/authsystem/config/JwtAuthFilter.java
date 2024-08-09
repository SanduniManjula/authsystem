package authsystem.config;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface JwtAuthFilter {
    void doFilterInternal(HttpServletRequest request,
                          HttpServletResponse response,
                          FilterChain filterChain) throws ServletException, IOException;
}
