export interface Category {
  id: number;
  slug: string;
  name: string;
  recipeCount: number;
}

export interface RecipeSummary {
  id: number;
  legacyId: number | null;
  title: string;
  category: string;
  excerpt: string;
}

export interface RecipeDetail {
  id: number;
  legacyId: number | null;
  title: string;
  category: string;
  description?: string;
  servings?: number;
  ingredients: string;
  preparation: string;
  pdfSlug?: string;
  tags?: string;
  source?: string;
}

export interface ProposedRecipe {
  title?: string;
  description?: string;
  servings?: number;
  ingredients?: string;
  instructions?: string;
  tags?: string;
  source?: string;
}

export interface UserOverrides {
  title?: string;
  description?: string;
  servings?: number;
  ingredients?: string;
  instructions?: string;
  tags?: string;
  source?: string;
  notes?: string;
}

export interface NeedsMoreInfoResponse {
  status: 'NEEDS_MORE_INFO';
  rawText: string;
  proposedRecipe: ProposedRecipe;
  missingFields: string[];
  parseWarnings: string[];
}

export type RecipeImportResult =
  | { kind: 'created'; recipe: RecipeDetail }
  | { kind: 'needs_more_info'; payload: NeedsMoreInfoResponse };

export interface VersionResponse {
  version: string;
}
