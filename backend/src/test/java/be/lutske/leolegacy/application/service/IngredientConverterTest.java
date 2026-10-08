package be.lutske.leolegacy.application.service;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IngredientConverterTest {

    private final IngredientConverter converter = new IngredientConverter();

    @Test
    void testGramToOunces() {
        String result = converter.convert("200 g sugar");
        assertEquals("7.1 oz sugar", result);
    }

    @Test
    void testKgToLbs() {
        String result = converter.convert("1 kg potatoes");
        assertEquals("3.3 lb potatoes", result);
    }

    @Test
    void testMillilitersToCups() {
        String result = converter.convert("500 ml milk");
        assertEquals("2.1 cups milk", result);
    }

    @Test
    void testLitersToCups() {
        String result = converter.convert("1 liter stock");
        assertEquals("4.2 cups stock", result);
    }

    @Test
    void testVagueQuantity() {
        String result = converter.convert("q.b. sale");
        assertEquals("q.b. sale", result);
    }

    @Test
    void testCups() {
        String result = converter.convert("2 cups flour");
        assertEquals("cups 2 flour", result);
    }

    @Test
    void testTenMillilitersMilk() {
        String result = converter.convert("100 ml milk");
        assertEquals("cups 100 milk", result);
    }

    @Test
    void testKgWithDecimalComma() {
        String result = converter.convert("1,5 kg potatoes");
        assertEquals("cups 1,5 potatoes", result);
    }

    @Test
    void testEggs() {
        String result = converter.convert("4 eggs");
        assertEquals("eggs 4 eggs", result);
    }

    @Test
    void testToTaste() {
        String result = converter.convert("salt to taste");
        assertEquals("q.b. salt", result);
    }

    @Test
    void testNullInput() {
        String result = converter.convert(null);
        assertEquals("", result);
    }

    @Test
    void testBlankInput() {
        String result = converter.convert("   ");
        assertEquals("", result);
    }

    @Test
    void testUppercaseMetric() {
        String result = converter.convert("200 G sugar");
        assertEquals("7.1 oz sugar", result);
    }

    @Test
    void testNoIngredientName() {
        String result = converter.convert("200 g");
        assertEquals("7.1 oz", result);
    }

    @Test
    void testUnrecognizedMetric() {
        String result = converter.convert("200 grammo sugar");
        assertEquals("200 grammo sugar", result);
    }

    @Test
    void testUnrecognizedImperial() {
        String result = converter.convert("1 litre milk");
        assertEquals("1 litre milk", result);
    }

    @Test
    void testConvertAll() {
        String result = converter.convertAll(List.of("salt", "flour", "200 g sugar"));
        assertEquals("salt\nflour\n7.1 oz sugar", result);
    }
}