package be.lutske.leolegacy.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class CoreModuleSmokeTest {

    @Test
    void testDomainModuleRunsOnJava25() {
        final var version = Runtime.version().feature();
        System.out.println("Detected Java version: " + version);
        assertTrue(version == 25, "Expected Java 25 but got: " + version);
    }
}
