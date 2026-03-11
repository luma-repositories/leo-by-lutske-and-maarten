package be.lutske.leolegacy.entrypoint.rest;

import be.lutske.leolegacy.entrypoint.rest.dto.VersionResponse;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Path("/api/version")
public class VersionResource {

    @ConfigProperty(name = "app.version")
    String appVersion;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public VersionResponse getVersion() {
        return new VersionResponse(appVersion);
    }
}
