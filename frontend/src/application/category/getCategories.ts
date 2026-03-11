import type { Category } from '../../domain/category/Category';
import { toCategory } from '../../infrastructure/api/mappers';
import { fetchCategoriesDto } from '../../infrastructure/api/client';

export async function getCategories(): Promise<Category[]> {
  return (await fetchCategoriesDto()).map(toCategory);
}
