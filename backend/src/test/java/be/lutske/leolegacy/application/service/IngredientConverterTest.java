package be.lutske.leolegacy.application.service;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IngredientConverterTest {

    private final IngredientConverter converter = new IngredientConverter();

    @Test
    void convertsGramsToOunces() {
        var result = converter.convert("200 g sugar");

        assertEquals("7.1 oz (200 g) sugar", result.displayValue());
        assertEquals("200 g", result.originalQuantity());
        assertEquals("7.1 oz", result.convertedQuantity());
        assertEquals("sugar", result.ingredientName());
        assertTrue(result.converted());
    }

    @Test
    void convertsKilogramsToPounds() {
        var result = converter.convert("1 kg potatoes");

        assertEquals("2.2 lb (1 kg) potatoes", result.displayValue());
        assertEquals("1 kg", result.originalQuantity());
        assertEquals("2.2 lb", result.convertedQuantity());
        assertTrue(result.converted());
    }

    @Test
    void convertsMillilitersToCups() {
        var result = converter.convert("500 ml milk");

        assertEquals("2.1 cups (500 ml) milk", result.displayValue());
        assertEquals("500 ml", result.originalQuantity());
        assertEquals("2.1 cups", result.convertedQuantity());
    }

    @Test
    void convertsLitersToCups() {
        var result = converter.convert("1 liter stock");

        assertEquals("4.2 cups (1 liter) stock", result.displayValue());
        assertEquals("1 liter", result.originalQuantity());
        assertEquals("4.2 cups", result.convertedQuantity());
    }

    @Test
    void leavesVagueQuantitiesUnchanged() {
        var result = converter.convert("q.b. sale");

        assertEquals("q.b. sale", result.displayValue());
        assertFalse(result.converted());
    }

    @Test
    void leavesImperialUnitsUnchanged() {
        var result = converter.convert("2 cups flour");

        assertEquals("2 cups flour", result.displayValue());
        assertFalse(result.converted());
    }

    @Test
    void convertsListAndKeepsDisplayFormat() {
        List<String> inputs = List.of("200 g sugar", "1 liter milk");
        var results = converter.convertAll(inputs);

        assertEquals(2, results.size());
        assertEquals("7.1 oz (200 g) sugar", results.getFirst().displayValue());
        assertEquals("4.2 cups (1 liter) milk", results.get(1).displayValue());
    }
}

