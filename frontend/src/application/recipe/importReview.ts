import type { ConfirmRecipeImportCommand, RecipeImportExtraction } from '../../domain/recipe/RecipeImport';

export interface EditableRecipeImport {
  title: string;
  ingredients: string;
  preparation: string;
  notes: string;
}

export function createEditableRecipeImport(extraction: RecipeImportExtraction): EditableRecipeImport {
  return {
    title: extraction.proposedRecipe.title ?? '',
    ingredients: extraction.proposedRecipe.ingredients?.join('\n') ?? '',
    preparation: extraction.proposedRecipe.preparation ?? '',
    notes: '',
  };
}

export function buildConfirmRecipeImportCommand(
  extraction: RecipeImportExtraction,
  editable: EditableRecipeImport,
): ConfirmRecipeImportCommand {
  const ingredients = editable.ingredients
    .split('\n')
    .map((item) => item.trim())
    .filter((item) => item.length > 0);

  return {
    rawModelResponse: extraction.rawModelResponse,
    proposedRecipe: extraction.proposedRecipe,
    userOverrides: {
      title: editable.title || null,
      ingredients: ingredients.length > 0 ? ingredients : null,
      preparation: editable.preparation || null,
      notes: editable.notes || null,
    },
  };
}
