import type { RecipeDetail } from '../../domain/recipe/RecipeDetail';
import { fetchRecipeDto } from '../../infrastructure/api/client';
import { toRecipeDetail } from '../../infrastructure/api/mappers';

export async function getRecipeDetail(id: number): Promise<RecipeDetail> {
  return toRecipeDetail(await fetchRecipeDto(id));
}
