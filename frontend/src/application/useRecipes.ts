import { useEffect, useState } from 'react';
import type { RecipeSummary, RecipeDetail } from '../domain/model';
import { fetchRecipes, fetchTopRecipes, fetchRecipe } from '../infrastructure/api/apiClient';

/**
 * Application hook: manages recipe list state.
 */
export function useRecipeList(categoryId: string | null) {
  const [recipes, setRecipes] = useState<RecipeSummary[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    setLoading(true);
    const fetcher = categoryId
      ? fetchRecipes(Number(categoryId))
      : fetchTopRecipes();

    fetcher
      .then(setRecipes)
      .catch(() => setRecipes([]))
      .finally(() => setLoading(false));
  }, [categoryId]);

  return { recipes, loading };
}

/**
 * Application hook: manages single recipe detail state.
 */
export function useRecipeDetail(id: string | undefined) {
  const [recipe, setRecipe] = useState<RecipeDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);

  useEffect(() => {
    if (!id) return;
    setLoading(true);
    setError(false);
    fetchRecipe(Number(id))
      .then(setRecipe)
      .catch(() => setError(true))
      .finally(() => setLoading(false));
  }, [id]);

  return { recipe, loading, error };
}
