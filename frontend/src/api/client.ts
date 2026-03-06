/** API response types matching the Quarkus REST DTOs. */

export interface CategoryResponse {
  id: number;
  name: string;
  recipeCount: number;
}

export interface RecipeSummaryResponse {
  id: number;
  title: string;
  categoryName: string;
  viewCount: number;
}

export interface RecipeDetailResponse {
  id: number;
  title: string;
  ingredients: string[];
  preparation: string;
  categoryId: number;
  categoryName: string;
  viewCount: number;
}

export interface VersionResponse {
  version: string;
}

/** Fetch helpers — all paths are same-origin (/api/...) */

export async function fetchVersion(): Promise<VersionResponse> {
  const res = await fetch('/api/version');
  if (!res.ok) throw new Error('Failed to fetch version');
  return res.json();
}

export async function fetchCategories(): Promise<CategoryResponse[]> {
  const res = await fetch('/api/categories');
  if (!res.ok) throw new Error('Failed to fetch categories');
  return res.json();
}

export async function fetchRecipes(categoryId?: number): Promise<RecipeSummaryResponse[]> {
  const url = categoryId ? `/api/recipes?categoryId=${categoryId}` : '/api/recipes';
  const res = await fetch(url);
  if (!res.ok) throw new Error('Failed to fetch recipes');
  return res.json();
}

export async function fetchTopRecipes(): Promise<RecipeSummaryResponse[]> {
  const res = await fetch('/api/recipes/top');
  if (!res.ok) throw new Error('Failed to fetch top recipes');
  return res.json();
}

export async function fetchRecipe(id: number): Promise<RecipeDetailResponse> {
  const res = await fetch(`/api/recipes/${id}`);
  if (!res.ok) throw new Error(`Failed to fetch recipe ${id}`);
  return res.json();
}
