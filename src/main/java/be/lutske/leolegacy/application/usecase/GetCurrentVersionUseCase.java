package be.lutske.leolegacy.application.usecase;

import be.lutske.leolegacy.application.port.VersionConfigPort;
import be.lutske.leolegacy.domain.AppVersion;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class GetCurrentVersionUseCase {

    private final VersionConfigPort versionConfigPort;

    public GetCurrentVersionUseCase(VersionConfigPort versionConfigPort) {
        this.versionConfigPort = versionConfigPort;
    }

    public AppVersion execute() {
        return versionConfigPort.currentVersion();
    }
}
