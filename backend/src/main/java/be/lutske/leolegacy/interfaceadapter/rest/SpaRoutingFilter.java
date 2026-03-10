package be.lutske.leolegacy.interfaceadapter.rest;

import io.vertx.ext.web.Router;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

/**
 * SPA routing filter for the React frontend.
 *
 * Redirects all non-API, non-file requests to index.html so that
 * React Router can handle client-side routing.
 */
@ApplicationScoped
public class SpaRoutingFilter {

    public void init(@Observes Router router) {
        router.get("/*").handler(ctx -> {
            String path = ctx.normalizedPath();

            // Let API calls and static files (with extensions) pass through
            if (path.startsWith("/api") || path.startsWith("/q/") || path.contains(".")) {
                ctx.next();
                return;
            }

            // Reroute everything else to index.html for React Router
            ctx.reroute("/index.html");
        });
    }
}
