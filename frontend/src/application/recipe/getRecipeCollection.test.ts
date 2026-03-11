import { describe, expect, it, vi, beforeEach } from 'vitest';
import { getRecipeCollection } from './getRecipeCollection';

describe('getRecipeCollection', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
  });

  it('loads top recipes when no category is selected', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue({
      ok: true,
      json: () => Promise.resolve([{ id: 1, title: 'Top', categoryName: 'Desserts', viewCount: 42 }]),
    } as Response);

    const result = await getRecipeCollection();

    expect(fetch).toHaveBeenCalledWith('/api/recipes/top');
    expect(result).toEqual([{ id: 1, title: 'Top', categoryName: 'Desserts', viewCount: 42 }]);
  });

  it('loads category recipes when a category is selected', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue({
      ok: true,
      json: () => Promise.resolve([{ id: 2, title: 'Soup', categoryName: 'Soups', viewCount: 7 }]),
    } as Response);

    const result = await getRecipeCollection(5);

    expect(fetch).toHaveBeenCalledWith('/api/recipes?categoryId=5');
    expect(result[0]?.title).toBe('Soup');
  });
});
