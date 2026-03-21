# Prompt

Refactor the recipe parsing/formatting code so that European recipes are automatically enriched with English unit conversions.

Goal:
When importing recipes, always keep BOTH:
- the original metric value (as found in the source)
- the converted English value

Display format:
- English unit comes first
- Original metric value is preserved in parentheses
- Example:
    - 1 liter milk -> 4.2 cups (1 liter) milk
    - 500 g flour -> 17.6 oz (500 g) flour

Import behavior:
- During import, store both values:
    - originalValue (e.g. "1 liter")
    - convertedValue (e.g. "4.2 cups")
- Do not overwrite or lose the original value
- Ensure both values are available for display, editing, and export

Requirements:
- Detect and convert:
    - grams -> ounces
    - kilograms -> pounds
    - milliliters -> cups or fluid ounces
    - liters -> cups or quarts
- Preserve the rest of the ingredient text
- Do not convert:
    - vague quantities (e.g. “q.b.”, “to taste”, “1 bunch”)
    - already imperial units

Behavior:
- Replace metric quantity with:
    - converted English unit + (original metric)
- Examples:
    - "200 g sugar" -> "7.1 oz (200 g) sugar"
    - "1 kg potatoes" -> "2.2 lb (1 kg) potatoes"
    - "500 ml milk" -> "2.1 cups (500 ml) milk"
    - "1 liter stock" -> "4.2 cups (1 liter) stock"

Data model:
- Ensure ingredient structure supports:
    - originalQuantity (String)
    - convertedQuantity (String)
    - unit (optional structured form)
    - ingredientName

Rounding:
- ounces: 1 decimal
- pounds: 1 decimal
- cups: max 2 decimals, prefer readable values
- fluid ounces: 1 decimal

Implementation:
- Create a clean utility method, e.g.:
    - String convertToEnglishUnits(String ingredientLine)
- Also provide a structured variant:
    - Ingredient convertIngredient(Ingredient input)
- Extract conversion constants
- Keep logic simple and maintainable

Testing:
Add tests for:
- correct display format (English first, metric in parentheses)
- correct storage of both original and converted values
- grams -> ounces
- kilograms -> pounds
- milliliters -> cups
- liters -> cups
- no conversion for unsupported or vague inputs

Code quality:
- Keep it clean code
- Avoid large conditional blocks
- Use clear naming
- Keep methods small and focused