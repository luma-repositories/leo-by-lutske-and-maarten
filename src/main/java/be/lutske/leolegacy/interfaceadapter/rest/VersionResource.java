package be.lutske.leolegacy.interfaceadapter.rest;

import be.lutske.leolegacy.application.usecase.GetCurrentVersionUseCase;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/api/version")
@Produces(MediaType.APPLICATION_JSON)
public class VersionResource {

    private final GetCurrentVersionUseCase getCurrentVersionUseCase;

    public VersionResource(GetCurrentVersionUseCase getCurrentVersionUseCase) {
        this.getCurrentVersionUseCase = getCurrentVersionUseCase;
    }

    @GET
    public VersionResponse getVersion() {
        return new VersionResponse(getCurrentVersionUseCase.execute().value());
    }
}
