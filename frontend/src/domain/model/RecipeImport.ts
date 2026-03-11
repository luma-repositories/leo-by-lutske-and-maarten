/**
 * Domain model for the recipe import flow.
 */

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

export interface ExtractionResult {
  status: 'COMPLETE' | 'NEEDS_MORE_INFO';
  rawModelResponse?: string | null;
  proposedRecipe: ProposedRecipe;
  missingFields: string[];
  warnings: string[];
}

export interface UserOverrides {
  title?: string | null;
  ingredients?: string[] | null;
  preparation?: string | null;
  categoryId?: number | null;
  notes?: string | null;
  servings?: string | null;
  description?: string | null;
}

export interface ImportConfirmRequest {
  rawModelResponse?: string | null;
  proposedRecipe: ProposedRecipe;
  userOverrides?: UserOverrides | null;
}

export type ImportResult =
  | { status: 'extracted'; data: ExtractionResult }
  | { status: 'error'; message: string };
