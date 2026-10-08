# 1. Title

Preserve recipe browsing and import workflows

## 2. User Story

As a home cook
I want to continue browsing recipes and importing recipes from images without changes to the established workflows
So that I can reliably find cooking instructions and add reviewed recipes to the collection.

## 3. Context

Application changes must not disrupt the existing recipe browsing and import experience or alter previously saved recipes.

Users can discover popular recipes, browse categories, read recipe details, and import a recipe from an image. Importing always includes a review step: even a complete extraction must not become a saved recipe until the user confirms it.

This story defines the functional preservation scope. It does not introduce new screens, permissions, recipe editing after saving, or additional recipe information beyond the existing experience.

The preservation baseline is the current observable behavior described below. In particular, imported content is requested in English and always presented for review; older descriptions of automatic saving or preserving the original language are not the intended baseline.

## 4. Screens / User Journey

### Screen: Home Page

- User opens the home page and sees up to ten recipes with the highest recorded view counts, ordered from highest to lowest.
- Recipe cards display the title and category. Positive view counts are displayed; zero counts are not.
- User selects a category in the category sidebar and sees recipes belonging to that category.
- Categories appear alphabetically with their recipe counts.
- While recipes are loading, a loading message appears. An empty result displays “No recipes found.”

### Screen: Recipe Detail Page

- User selects a recipe card and sees its title, category, ingredients, and preparation instructions.
- User can return to the recipe's category.
- If the recipe cannot be displayed, the page shows “Recipe not found.” and a link back to the home page.

### Screen: Import Recipe Page — Image Selection

- User selects a PNG, JPG, JPEG, or WEBP image and sees a preview.
- User starts extraction or resets the selection.
- Extraction cannot start without a selected image.
- While extraction is in progress, the page displays progress feedback and prevents another extraction submission.

### Screen: Import Recipe Page — Review

- After successful extraction, the page displays an editable proposal, completeness status, warnings, and missing required fields where applicable.
- User can edit the title, ingredients, preparation, and optional notes.
- User can expand the extracted source text when it is available.
- User confirms the proposal or cancels and returns to image selection.
- Successful confirmation opens the saved recipe's detail page.

## 5. Functional Requirements

1. Existing recipes, category assignments, ingredient order, and preparation text remain available without unintended changes.
2. Popular recipe discovery, category browsing, displayed counts, recipe details, and return navigation retain the behavior described above. Merely opening a recipe does not introduce a new view-counting behavior.
3. Import accepts the supported image formats up to and including 10 MB. An unsupported format or an image above this limit is rejected with an explanatory message and does not create a recipe.
4. Extraction proposes English recipe content and identifies missing or uncertain information. It must not invent information that cannot be read from the image.
5. Complete, incomplete, and empty extraction results all require user review. Extraction alone never adds a recipe to the collection.
6. Title, ingredients, and preparation are required for confirmation. The confirmation action is unavailable while the title is blank or contains only spaces.
7. On confirmation, ingredient entries are read one per line; surrounding spaces and empty lines are removed. Non-empty user corrections replace proposed values. Under the existing review behavior, clearing all ingredients or all preparation falls back to the proposed value; if neither provides the required content, confirmation fails.
8. Recognized metric ingredient quantities retain the existing imperial conversion and original metric amount. For example, “200 g dark chocolate” becomes “7.1 oz (200 g) dark chocolate.” Already imperial, vague, or unrecognized quantities remain unchanged. Confirming a converted proposal must not add a second conversion.
9. Non-empty user notes appear after the saved preparation, separated by a blank line and prefixed with “Notes: ”.
10. Recipes imported through the current review screen are assigned to the default Imported category. No category-selection control is introduced by this story.
11. Cancelling review clears the selected image, preview, proposal, edits, and errors without saving a recipe. Resetting image selection also clears the selection and preview.
12. If saving fails, the user remains on the review screen, sees a failure message, retains the entered corrections, and can try again. No successful-save outcome is shown unless the recipe was saved.

## 6. Acceptance Criteria

### AC1 — Browse popular recipes

Given more than ten recipes with distinct recorded view counts exist,
When the user opens the home page without selecting a category,
Then the ten highest-ranked recipes appear in descending view-count order with their titles and categories.

### AC2 — Browse a category

Given categories contain known recipes,
When the user selects a category,
Then only recipes from that category appear, and the sidebar displays the correct category counts in alphabetical category order.

### AC3 — Read a recipe

Given an existing recipe appears in a recipe list,
When the user opens it,
Then its title, category, ordered ingredients, and preparation match the saved recipe, and returning to its category displays that category's recipes.

### AC4 — Review before saving

Given a supported image within the size limit produces a complete extraction,
When extraction finishes,
Then an editable proposal appears, the result is marked complete, and no recipe has been added to the collection.

### AC5 — Correct incomplete results

Given extraction cannot identify one or more required fields,
When the review screen appears,
Then the missing fields and available warnings are shown, available content remains editable, and the user can supply the missing information before confirmation.

### AC6 — Reject unsupported uploads

Given an image exceeds 10 MB or its format is unsupported,
When the user attempts extraction,
Then an explanatory rejection message appears and no recipe is created.

### AC7 — Confirm reviewed content

Given a proposal contains valid required content and the user supplies corrections and notes,
When the user confirms it,
Then the saved recipe reflects the correction and conversion rules, includes the notes, belongs to the Imported category, and opens on its detail page.

### AC8 — Cancel or recover from a failed save

Given the user is reviewing an unsaved proposal,
When the user cancels,
Then the page returns to empty image selection without saving;
And when a confirmation attempt instead fails,
Then a failure message appears while the review content remains available for correction or retry.

## 7. Functional Test Scenarios

### Test: Popular recipes and category navigation

Steps:
1. Prepare more than ten recipes with distinct view counts across several categories.
2. Open the home page.
3. Select a category containing recipes.
4. Select a category containing no recipes.

Expected result:
- The home page displays the ten most-viewed recipes in descending order.
- Category names are alphabetical and counts match their contents.
- Filtering shows only the selected category's recipes.
- The empty category displays “No recipes found.”

### Test: Read existing recipe details

Steps:
1. Open an existing recipe from a category list.
2. Compare the title, category, ingredients, and preparation with the saved content.
3. Use the return-to-category link.
4. Open a recipe that does not exist.

Expected result:
- Existing content and ingredient order are unchanged.
- Return navigation restores the appropriate category list.
- A nonexistent recipe displays “Recipe not found.” with a home-page link.

### Test: Complete extraction still requires review

Steps:
1. Open Import Recipe and verify extraction is unavailable without an image.
2. Select a readable supported image within the size limit.
3. Start extraction and wait for completion.
4. Leave the proposal unconfirmed and inspect the collection.

Expected result:
- A preview and extraction progress feedback appear.
- The complete proposal is editable and marked complete.
- Extracted content is proposed in English.
- No recipe is added before confirmation.

### Test: Incomplete or unrelated image

Steps:
1. Import an image with readable ingredients but no preparation instructions.
2. Inspect the missing-field indicators and warnings.
3. Repeat with an image that contains no recipe.
4. Supply a title, ingredients, and preparation manually and confirm.

Expected result:
- Readable content remains available for review.
- Missing required content and available warnings are shown.
- Neither image creates a recipe automatically.
- A valid manually completed proposal can be saved.

### Test: Image format and size boundaries

Steps:
1. Attempt extraction with an unsupported format.
2. Attempt extraction with a supported image larger than 10 MB.
3. Attempt extraction with a valid supported image exactly 10 MB in size.

Expected result:
- The unsupported and oversized files are rejected with explanatory messages.
- The image exactly at the limit is not rejected for size.
- Rejected files do not create recipes.

### Test: Save corrections, conversions, and notes

Steps:
1. Extract a recipe and change its title and preparation.
2. Enter “200 g dark chocolate” and “4 eggs” on separate ingredient lines, including surrounding spaces and an empty line.
3. Add the note “Serve chilled.” and confirm.
4. Reopen the saved recipe from the Imported category.

Expected result:
- The corrected title and preparation are saved.
- Ingredients display “7.1 oz (200 g) dark chocolate” and “4 eggs” in order, without a blank ingredient.
- Preparation ends with a blank line followed by “Notes: Serve chilled.”
- The recipe remains accessible after leaving its detail page.

### Test: Required fields and existing fallback behavior

Steps:
1. Clear the title of a complete proposal, then enter only spaces.
2. Restore a valid title, clear all ingredient text and preparation, and confirm.
3. Repeat with an incomplete proposal that has no proposed ingredients or preparation.

Expected result:
- A blank or whitespace-only title prevents confirmation.
- Cleared ingredients and preparation fall back to available proposed values.
- If required content is absent from both the proposal and corrections, the recipe is not saved and review remains available.

### Test: Cancellation and save failure

Steps:
1. Edit a proposal and cancel review.
2. Start a new import and edit the proposal.
3. Attempt confirmation when saving cannot complete.

Expected result:
- Cancellation returns to image selection with no previous image, proposal, edits, or errors, and creates no recipe.
- Failed saving displays a failure message, retains the latest edits, and does not navigate to a saved recipe.

## 8. Edge Cases

- Fewer than ten recipes exist: show all available recipes on the popular list.
- Equal view counts: preserve descending ranking without introducing a new tie-breaking requirement.
- A category is empty: show the existing empty-list message.
- A recipe cannot be displayed: show the existing recipe-not-found state and return navigation.
- Extraction yields no readable recipe content: show an incomplete editable proposal rather than saving an empty recipe.
- An image contains several recipes: do not introduce a multiple-recipe selection or batch-save workflow; any proposed content remains subject to review.
- Ingredients already contain imperial quantities or vague quantities such as “to taste”: retain them without another conversion.
- A user clears all ingredients or preparation: retain the existing proposal fallback behavior, not a new deletion behavior.
- Optional notes are blank: do not append an empty notes section.
- No default Imported category is available: saving must fail rather than report a successful import.
- Extraction failure must not be mistaken for a completed extraction or a saved recipe. Broader changes to retry behavior are outside this preservation story.
