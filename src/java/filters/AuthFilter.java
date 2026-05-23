package filters;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * AuthFilter is a Jakarta Servlet filter that protects all secured pages
 * by checking whether the user is logged in before allowing access.
 *
 * I implemented this filter because the assignment requires: "any attempts
 * to access a function page of the system without login will be redirected
 * to the login page." This is demonstrated in section 4 of the demo document.
 *
 * The filter intercepts every request to .xhtml pages and checks the session
 * for the "loggedInUser" attribute. If the attribute is absent, the user is
 * redirected to the login page. Public pages (index, login, registration,
 * email verification, recovery) are excluded from this check.
 */
@WebFilter(filterName = "AuthFilter", urlPatterns = {"*.xhtml"})
public class AuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // No initialisation needed for this filter.
    }

    /**
     * Checks the session for a logged-in user attribute before allowing the
     * request to continue. Public pages are whitelisted and always permitted.
     *
     * @param request  the incoming servlet request
     * @param response the servlet response
     * @param chain    the filter chain to continue if access is allowed
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpSession session = httpRequest.getSession(false);

        String requestURI = httpRequest.getRequestURI();

        // Determine if the requested page is a public page that does not
        // require authentication. These pages are part of the registration,
        // login and account recovery flows.
        boolean isPublicPage = isPublicPage(requestURI);

        boolean isLoggedIn = (session != null)
                && (session.getAttribute("loggedInUser") != null);

        if (isLoggedIn || isPublicPage) {
            // User is authenticated or the page is public - allow the request.
            chain.doFilter(request, response);
        } else {
            // User is not authenticated and the page requires login.
            // Redirect to the login page as required by the assignment spec.
            String contextPath = httpRequest.getContextPath();
            httpResponse.sendRedirect(contextPath + "/login.xhtml");
        }
    }

    /**
     * Determines whether a request URI refers to a page that is accessible
     * without logging in. Public pages include the index page, login page,
     * registration pages, email verification, and account recovery pages.
     *
     * @param uri the request URI to check
     * @return true if the page is publicly accessible without login
     */
    private boolean isPublicPage(String uri) {
        return uri.contains("/index.xhtml")
                || uri.contains("/login.xhtml")
                || uri.contains("/email.xhtml")
                || uri.contains("/register.xhtml")
                || uri.contains("/emailRecovery.xhtml")
                || uri.contains("/recovery.xhtml")
                || uri.contains("javax.faces")
                || uri.contains("jakarta.faces")
                || uri.contains("/resources/")
                || uri.endsWith(".css")
                || uri.endsWith(".js")
                || uri.endsWith(".png")
                || uri.endsWith(".jpg");
    }

    @Override
    public void destroy() {
        // No cleanup needed for this filter.
    }
}
