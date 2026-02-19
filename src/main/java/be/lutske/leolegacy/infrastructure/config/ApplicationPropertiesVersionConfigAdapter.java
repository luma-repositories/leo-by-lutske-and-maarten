package be.lutske.leolegacy.infrastructure.config;

import be.lutske.leolegacy.application.port.VersionConfigPort;
import be.lutske.leolegacy.domain.AppVersion;
import javax.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class ApplicationPropertiesVersionConfigAdapter implements VersionConfigPort {

    @ConfigProperty(name = "app.version")
    String configuredVersion;

    @Override
    public AppVersion currentVersion() {
        return new AppVersion(configuredVersion);
    }
}
