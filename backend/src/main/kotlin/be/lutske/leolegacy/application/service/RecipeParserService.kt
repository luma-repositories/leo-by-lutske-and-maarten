package be.lutske.leolegacy.application.service

import be.lutske.leolegacy.interfaceadapter.rest.ProposedRecipeDto
import jakarta.enterprise.context.ApplicationScoped

/**
 * Parses raw OCR text into a structured recipe proposal.
 *
 * Heuristics:
 * - Title: first non-empty line, or line in ALL CAPS, or line before "Ingredients"
 * - Ingredients: section between ingredient-heading and instruction-heading
 * - Instructions: section after instruction-heading
 * - If no headings found, attempts to detect ingredient-like lines (quantities/units)
 */
@ApplicationScoped
class RecipeParserService {

    data class ParseResult(
        val proposedRecipe: ProposedRecipeDto,
        val missingFields: List<String>,
        val parseWarnings: List<String>
    )

    companion object {
        // Headings that indicate the start of an ingredients section
        private val INGREDIENT_HEADINGS = listOf(
            "ingredients", "ingredienten", "benodigdheden", "wat heb je nodig",
            "ingredient", "ingrediënten"
        )

        // Headings that indicate the start of an instructions section
        private val INSTRUCTION_HEADINGS = listOf(
            "instructions", "directions", "method", "steps", "preparation",
            "bereiding", "bereidingswijze", "werkwijze", "stappen"
        )

        // Regex to detect ingredient-like lines (starts with number, fraction, or bullet)
        private val INGREDIENT_LINE_PATTERN = Regex(
            """^\s*(\d|½|¼|¾|⅓|⅔|•|[-–—*]|\d+[.,/]\d+)\s*.*""",
            RegexOption.IGNORE_CASE
        )
    }

    /**
     * Parse raw OCR text into a structured recipe proposal.
     */
    fun parse(rawText: String): ParseResult {
        if (rawText.isBlank()) {
            return ParseResult(
                proposedRecipe = ProposedRecipeDto(),
                missingFields = listOf("title", "ingredients", "preparation"),
                parseWarnings = listOf("OCR produced no text")
            )
        }

        val lines = rawText.lines().map { it.trim() }
        val warnings = mutableListOf<String>()

        // Try to find section boundaries using headings
        val ingredientStart = findHeadingIndex(lines, INGREDIENT_HEADINGS)
        val instructionStart = findHeadingIndex(lines, INSTRUCTION_HEADINGS)

        val title: String?
        val ingredients: List<String>?
        val preparation: String?

        if (ingredientStart != null && instructionStart != null && instructionStart > ingredientStart) {
            // Structured recipe with both headings found
            title = extractTitle(lines, ingredientStart)
            ingredients = extractSection(lines, ingredientStart + 1, instructionStart)
            preparation = extractTextBlock(lines, instructionStart + 1, lines.size)
        } else if (ingredientStart != null) {
            // Only ingredient heading found
            title = extractTitle(lines, ingredientStart)
            val remaining = lines.subList(ingredientStart + 1, lines.size)
            val splitPoint = findInstructionBoundary(remaining)
            if (splitPoint != null) {
                ingredients = remaining.subList(0, splitPoint).filter { it.isNotBlank() }
                preparation = remaining.subList(splitPoint, remaining.size)
                    .filter { it.isNotBlank() }.joinToString("\n")
            } else {
                ingredients = remaining.filter { it.isNotBlank() }
                preparation = null
                warnings.add("Could not find instructions section — all text after ingredients heading treated as ingredients")
            }
        } else if (instructionStart != null) {
            // Only instruction heading found
            title = extractTitle(lines, instructionStart)
            ingredients = null
            preparation = extractTextBlock(lines, instructionStart + 1, lines.size)
            warnings.add("Could not find ingredients section")
        } else {
            // No headings found — use heuristics
            warnings.add("No section headings detected — using heuristic parsing")
            title = lines.firstOrNull { it.isNotBlank() }

            val contentLines = if (title != null) {
                lines.drop(lines.indexOf(title) + 1).filter { it.isNotBlank() }
            } else {
                lines.filter { it.isNotBlank() }
            }

            val (ingredientLines, otherLines) = contentLines.partition {
                INGREDIENT_LINE_PATTERN.matches(it)
            }

            ingredients = ingredientLines.ifEmpty { null }
            preparation = otherLines.joinToString("\n").ifBlank { null }

            if (ingredients == null && preparation == null && contentLines.isNotEmpty()) {
                // Can't distinguish — treat everything as preparation
                return ParseResult(
                    proposedRecipe = ProposedRecipeDto(
                        title = title,
                        preparation = contentLines.joinToString("\n")
                    ),
                    missingFields = buildMissingFields(title, null, contentLines.joinToString("\n")),
                    parseWarnings = warnings + "Could not distinguish ingredients from instructions"
                )
            }
        }

        val missingFields = buildMissingFields(title, ingredients, preparation)

        return ParseResult(
            proposedRecipe = ProposedRecipeDto(
                title = title,
                ingredients = ingredients,
                preparation = preparation
            ),
            missingFields = missingFields,
            parseWarnings = warnings
        )
    }

    private fun findHeadingIndex(lines: List<String>, headings: List<String>): Int? {
        return lines.indexOfFirst { line ->
            val normalized = line.lowercase().replace(":", "").trim()
            headings.any { heading -> normalized == heading || normalized.startsWith("$heading ") }
        }.takeIf { it >= 0 }
    }

    private fun extractTitle(lines: List<String>, beforeIndex: Int): String? {
        // Look for ALL CAPS line before the section
        val titleCandidates = lines.subList(0, beforeIndex).filter { it.isNotBlank() }

        // Prefer ALL CAPS line
        val allCapsLine = titleCandidates.firstOrNull { line ->
            line.length > 2 && line == line.uppercase() && line.any { it.isLetter() }
        }
        if (allCapsLine != null) return allCapsLine

        // Otherwise, first non-empty line
        return titleCandidates.firstOrNull()
    }

    private fun extractSection(lines: List<String>, fromIndex: Int, toIndex: Int): List<String> {
        return lines.subList(fromIndex, toIndex).filter { it.isNotBlank() }
    }

    private fun extractTextBlock(lines: List<String>, fromIndex: Int, toIndex: Int): String? {
        val text = lines.subList(fromIndex, toIndex).filter { it.isNotBlank() }.joinToString("\n")
        return text.ifBlank { null }
    }

    private fun findInstructionBoundary(lines: List<String>): Int? {
        // Find the first line that looks like an instruction (longer sentence, not ingredient-like)
        // after a sequence of ingredient-like lines
        var lastIngredientIndex = -1
        for ((index, line) in lines.withIndex()) {
            if (line.isNotBlank() && INGREDIENT_LINE_PATTERN.matches(line)) {
                lastIngredientIndex = index
            }
        }
        return if (lastIngredientIndex >= 0 && lastIngredientIndex < lines.size - 1) {
            lastIngredientIndex + 1
        } else {
            null
        }
    }

    private fun buildMissingFields(title: String?, ingredients: List<String>?, preparation: String?): List<String> {
        val missing = mutableListOf<String>()
        if (title.isNullOrBlank()) missing.add("title")
        if (ingredients.isNullOrEmpty()) missing.add("ingredients")
        if (preparation.isNullOrBlank()) missing.add("preparation")
        return missing
    }
}
