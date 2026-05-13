package com.app.filter;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebInitParam;
import java.io.IOException;

/**
 * Character Encoding Filter for Enterprise Document Manager.
 * - Forces UTF-8 encoding for all HTTP requests (POST parameters, etc.)
 * - Sets UTF-8 encoding for HTTP responses (HTML, JSON, etc.)
 * - Prevents character corruption for non-ASCII characters (e.g., é, ñ, 中文)
 * - Should be the first filter in the chain.
 */
@WebFilter(
    urlPatterns = "/*",
    initParams = {
        @WebInitParam(name = "requestEncoding", value = "UTF-8"),
        @WebInitParam(name = "responseEncoding", value = "UTF-8")
    }
)
public class CharacterEncodingFilter implements Filter {

    private String requestEncoding = "UTF-8";
    private String responseEncoding = "UTF-8";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Override default encoding with init parameters if provided
        String reqEnc = filterConfig.getInitParameter("requestEncoding");
        if (reqEnc != null && !reqEnc.isEmpty()) {
            requestEncoding = reqEnc;
        }
        String respEnc = filterConfig.getInitParameter("responseEncoding");
        if (respEnc != null && !respEnc.isEmpty()) {
            responseEncoding = respEnc;
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        // Set request encoding if not already set
        if (request.getCharacterEncoding() == null) {
            request.setCharacterEncoding(requestEncoding);
        }

        // Set response encoding
        response.setCharacterEncoding(responseEncoding);
        // Optionally set content type header (can be overridden by servlets/JSPs)
        response.setContentType("text/html; charset=" + responseEncoding);

        // Continue the filter chain
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // No resources to release
    }
}
