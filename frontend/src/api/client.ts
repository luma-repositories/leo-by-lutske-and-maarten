import type { Category, RecipeDetail, RecipeSummary, VersionResponse } from '../types/api';

async function getJson<T>(path: string): Promise<T> {
  const response = await fetch(path);
  if (!response.ok) {
    throw new Error(`Request failed: ${response.status}`);
  }
  return response.json() as Promise<T>;
}

export const apiClient = {
  listCategories: (): Promise<Category[]> => getJson<Category[]>('/api/categories'),
  listRecipes: (categorySlug?: string): Promise<RecipeSummary[]> => {
    const params = categorySlug ? `?category=${encodeURIComponent(categorySlug)}` : '';
    return getJson<RecipeSummary[]>(`/api/recipes${params}`);
  },
  getRecipe: (id: string): Promise<RecipeDetail> => getJson<RecipeDetail>(`/api/recipes/${id}`),
  getVersion: (): Promise<VersionResponse> => getJson<VersionResponse>('/api/version')
};
