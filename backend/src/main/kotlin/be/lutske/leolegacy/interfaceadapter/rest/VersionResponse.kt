package be.lutske.leolegacy.interfaceadapter.rest

/**
 * JSON response DTO for the version endpoint.
 *
 * Example: { "version": "1.2.3" }
 */
data class VersionResponse(
    val version: String
)
