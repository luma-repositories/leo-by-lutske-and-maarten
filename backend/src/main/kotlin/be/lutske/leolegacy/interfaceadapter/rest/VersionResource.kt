package be.lutske.leolegacy.interfaceadapter.rest

import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import org.eclipse.microprofile.config.inject.ConfigProperty

/**
 * REST resource that exposes the application version.
 *
 * The version value is read from application.properties via MicroProfile Config.
 */
@Path("/api/version")
class VersionResource(
    @ConfigProperty(name = "app.version")
    private val appVersion: String
) {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    fun getVersion(): VersionResponse {
        return VersionResponse(version = appVersion)
    }
}
