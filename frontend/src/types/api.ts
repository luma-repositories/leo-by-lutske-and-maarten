export interface Category {
  id: number;
  slug: string;
  name: string;
  recipeCount: number;
}

export interface RecipeSummary {
  id: number;
  legacyId: number;
  title: string;
  category: string;
  excerpt: string;
}

export interface RecipeDetail {
  id: number;
  legacyId: number;
  title: string;
  category: string;
  ingredients: string;
  preparation: string;
  pdfSlug?: string;
}

export interface VersionResponse {
  version: string;
}
