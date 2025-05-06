package ro.unibuc.hello.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import io.micrometer.core.annotation.Timed;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ro.unibuc.hello.data.SessionEntity;
import ro.unibuc.hello.exception.InvalidSessionException;
import ro.unibuc.hello.service.SessionsService;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private SessionsService sessionService;

    @Override
    @Timed(value = "auctions.sessions.verify_session.time", description = "Time taken to verify session")
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        // Allow static resources and other handlers
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;

        // Check if method or class is marked as @PublicEndpoint
        if (handlerMethod.hasMethodAnnotation(PublicEndpoint.class) ||
            handlerMethod.getBeanType().isAnnotationPresent(PublicEndpoint.class)) {
            return true; // Allow public routes without authentication
        }

        // Extract session ID from header
        String sessionId = request.getHeader("X-Session-Id");

        if (sessionId == null) {
            throw new InvalidSessionException("Missing session id");
        }

        SessionEntity session = sessionService.getValidSession(sessionId);
        request.setAttribute("authenticatedUserId", session.getUser().getId());
        return true;
    }
}
