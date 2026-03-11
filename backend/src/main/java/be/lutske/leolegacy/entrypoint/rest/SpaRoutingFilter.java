package be.lutske.leolegacy.entrypoint.rest;

import io.vertx.ext.web.Router;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

@ApplicationScoped
public class SpaRoutingFilter {

    public void init(@Observes Router router) {
        router.get("/*").handler(ctx -> {
            String path = ctx.normalizedPath();
            if (path.startsWith("/api") || path.startsWith("/q/") || path.contains(".")) {
                ctx.next();
                return;
            }
            ctx.reroute("/index.html");
        });
    }
}
