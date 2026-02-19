package be.lutske.leolegacy.application.port;

import be.lutske.leolegacy.domain.AppVersion;

public interface VersionConfigPort {
    AppVersion currentVersion();
}
