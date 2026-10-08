package be.lutske.leolegacy.domain;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IngredientConverterTest {

    private final IngredientConverter converter = new IngredientConverter();

    @Test
    void testGramToOunces() {
        IngredientConversion result = converter.convert("200 g sugar");
        assertEquals("7.1 oz", result.originalQuantity());
        assertEquals("7.1 oz (200 g) sugar", result.displayValue());
        assertEquals("oz", result.unit());
    }

    @Test
    void testKgToLbs() {
        IngredientConversion result = converter.convert("1 kg potatoes");
        assertEquals("3.3 lb", result.displayValue());
    }

    @Test
    void testMillilitersToCups() {
        IngredientConversion result = converter.convert("500 ml milk");
        assertEquals("2.1 cups", result.displayValue());
    }

    @Test
    void testLitersToCups() {
        IngredientConversion result = converter.convert("1 liter stock");
        assertEquals("4.2 cups", result.displayValue());
    }

    @Test
    void testVagueQuantity() {
        IngredientConversion result = converter.convert("q.b. sale");
        assertEquals("q.b.", result.displayValue());
        assertEquals("false", String.valueOf(result.converted()));
    }

    @Test
    void testCups() {
        IngredientConversion result = converter.convert("2 cups flour");
        assertEquals("1.0 cups", result.displayValue());
    }

    @Test
    void testTenMillilitersMilk() {
        IngredientConversion result = converter.convert("100 ml milk");
        assertEquals("0.4 cups", result.displayValue());
    }

    @Test
    void testKgWithDecimalComma() {
        IngredientConversion result = converter.convert("1,5 kg potatoes");
        assertEquals("3.3 lb", result.displayValue());
    }

    @Test
    void testEggs() {
        IngredientConversion result = converter.convert("4 eggs");
        assertEquals("4 eggs", result.displayValue());
        assertEquals("false", String.valueOf(result.converted()));
    }

    @Test
    void testToTaste() {
        IngredientConversion result = converter.convert("salt to taste");
        assertEquals("q.b. taste", result.displayValue());
        assertEquals("false", String.valueOf(result.converted()));
    }

    @Test
    void testNullInput() {
        IngredientConversion result = converter.convert(null);
        assertEquals(null, result.originalQuantity());
        assertEquals(null, result.convertedQuantity());
        assertEquals(null, result.unit());
        assertEquals(null, result.ingredientName());
        assertEquals("", result.displayValue());
        assertEquals(false, result.converted());
    }

    @Test
    void testBlankInput() {
        IngredientConversion result = converter.convert("   ");
        assertEquals(null, result.originalQuantity());
        assertEquals("", result.displayValue());
    }

    @Test
    void testUppercaseMetric() {
        IngredientConversion result = converter.convert("200 G sugar");
        assertEquals("7.1 oz", result.displayValue());
    }

    @Test
    void testNoIngredientName() {
        IngredientConversion result = converter.convert("200 g");
        assertEquals(null, result.ingredientName());
    }

    @Test
    void testUnrecognizedMetric() {
        IngredientConversion result = converter.convert("200 grammo sugar");
        assertEquals("200 grammo sugar", result.displayValue());
        assertEquals("false", String.valueOf(result.converted()));
    }

    @Test
    void testUnrecognizedImperial() {
        IngredientConversion result = converter.convert("1 litre milk");
        assertEquals("1 litre milk", result.displayValue());
        assertEquals("false", String.valueOf(result.converted()));
    }

    @Test
    void testConvertAll() {
        List<String> ingredients = List.of("salt", "flour", "200 g sugar");
        List<IngredientConversion> results = converter.convertAll(ingredients);
        assertEquals(3, results.size());
    }

    @Test
    void testConvertNullList() {
        List<IngredientConversion> results = converter.convertAll(null);
        assertTrue(results.isEmpty());
    }

    @Test
    void testConvertConvertedIngredient() {
        IngredientConversion first = converter.convert("200 g sugar");
        IngredientConversion second = converter.convert(first.displayValue());
        assertEquals(first.displayValue(), second.displayValue());
    }
}