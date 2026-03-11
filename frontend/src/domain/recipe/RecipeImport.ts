export interface ProposedRecipe {
  title?: string | null;
  description?: string | null;
  servings?: string | null;
  ingredients?: string[] | null;
  preparation?: string | null;
  categoryId?: number | null;
  notes?: string | null;
  source?: string | null;
  tags?: string[] | null;
}

export interface RecipeImportExtraction {
  status: 'COMPLETE' | 'NEEDS_MORE_INFO';
  rawModelResponse?: string | null;
  proposedRecipe: ProposedRecipe;
  missingFields: string[];
  warnings: string[];
}

export interface UserRecipeOverrides {
  title?: string | null;
  ingredients?: string[] | null;
  preparation?: string | null;
  categoryId?: number | null;
  notes?: string | null;
  servings?: string | null;
  description?: string | null;
}

export interface ConfirmRecipeImportCommand {
  rawModelResponse?: string | null;
  proposedRecipe: ProposedRecipe;
  userOverrides?: UserRecipeOverrides | null;
}

export type RecipeImageImportResult =
  | { status: 'extracted'; data: RecipeImportExtraction }
  | { status: 'error'; message: string };
