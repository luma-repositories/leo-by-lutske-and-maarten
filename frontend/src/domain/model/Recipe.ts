/**
 * Domain model for a recipe summary (list views).
 */
export interface RecipeSummary {
  id: number;
  title: string;
  categoryName: string;
  viewCount: number;
}

/**
 * Domain model for a full recipe detail.
 */
export interface RecipeDetail {
  id: number;
  title: string;
  ingredients: string[];
  preparation: string;
  categoryId: number;
  categoryName: string;
  viewCount: number;
}
