import type { RecipeSummary } from '../../domain/recipe/RecipeSummary';
import { toRecipeSummary } from '../../infrastructure/api/mappers';
import { fetchRecipesDto, fetchTopRecipesDto } from '../../infrastructure/api/client';

export async function getRecipeCollection(categoryId?: number): Promise<RecipeSummary[]> {
  const dtos = categoryId ? await fetchRecipesDto(categoryId) : await fetchTopRecipesDto();
  return dtos.map(toRecipeSummary);
}
