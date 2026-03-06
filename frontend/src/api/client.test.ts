import { describe, it, expect, vi, beforeEach } from 'vitest';
import {
  fetchVersion,
  fetchCategories,
  fetchRecipes,
  fetchTopRecipes,
  fetchRecipe,
} from './client';

describe('API client', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
  });

  describe('fetchVersion', () => {
    it('calls /api/version and returns version data', async () => {
      vi.spyOn(globalThis, 'fetch').mockResolvedValue({
        ok: true,
        json: () => Promise.resolve({ version: '1.2.3' }),
      } as Response);

      const result = await fetchVersion();

      expect(fetch).toHaveBeenCalledWith('/api/version');
      expect(result).toEqual({ version: '1.2.3' });
    });

    it('throws on non-ok response', async () => {
      vi.spyOn(globalThis, 'fetch').mockResolvedValue({
        ok: false,
        status: 500,
      } as Response);

      await expect(fetchVersion()).rejects.toThrow('Failed to fetch version');
    });
  });

  describe('fetchCategories', () => {
    it('calls /api/categories and returns category list', async () => {
      const mockData = [{ id: 1, name: 'Soepen', recipeCount: 16 }];
      vi.spyOn(globalThis, 'fetch').mockResolvedValue({
        ok: true,
        json: () => Promise.resolve(mockData),
      } as Response);

      const result = await fetchCategories();

      expect(fetch).toHaveBeenCalledWith('/api/categories');
      expect(result).toEqual(mockData);
    });

    it('throws on non-ok response', async () => {
      vi.spyOn(globalThis, 'fetch').mockResolvedValue({
        ok: false,
        status: 500,
      } as Response);

      await expect(fetchCategories()).rejects.toThrow('Failed to fetch categories');
    });
  });

  describe('fetchRecipes', () => {
    it('calls /api/recipes without filter when no categoryId', async () => {
      vi.spyOn(globalThis, 'fetch').mockResolvedValue({
        ok: true,
        json: () => Promise.resolve([]),
      } as Response);

      await fetchRecipes();

      expect(fetch).toHaveBeenCalledWith('/api/recipes');
    });

    it('calls /api/recipes with categoryId filter', async () => {
      vi.spyOn(globalThis, 'fetch').mockResolvedValue({
        ok: true,
        json: () => Promise.resolve([]),
      } as Response);

      await fetchRecipes(5);

      expect(fetch).toHaveBeenCalledWith('/api/recipes?categoryId=5');
    });

    it('throws on non-ok response', async () => {
      vi.spyOn(globalThis, 'fetch').mockResolvedValue({
        ok: false,
        status: 500,
      } as Response);

      await expect(fetchRecipes()).rejects.toThrow('Failed to fetch recipes');
    });
  });

  describe('fetchTopRecipes', () => {
    it('calls /api/recipes/top', async () => {
      vi.spyOn(globalThis, 'fetch').mockResolvedValue({
        ok: true,
        json: () => Promise.resolve([]),
      } as Response);

      await fetchTopRecipes();

      expect(fetch).toHaveBeenCalledWith('/api/recipes/top');
    });

    it('throws on non-ok response', async () => {
      vi.spyOn(globalThis, 'fetch').mockResolvedValue({
        ok: false,
        status: 500,
      } as Response);

      await expect(fetchTopRecipes()).rejects.toThrow('Failed to fetch top recipes');
    });
  });

  describe('fetchRecipe', () => {
    it('calls /api/recipes/{id} and returns recipe detail', async () => {
      const mockRecipe = {
        id: 42,
        title: 'Chocolademousse',
        ingredients: ['chocolade', 'eieren'],
        preparation: 'Smelt de chocolade.',
        categoryId: 7,
        categoryName: 'Nagerechten',
        viewCount: 100,
      };
      vi.spyOn(globalThis, 'fetch').mockResolvedValue({
        ok: true,
        json: () => Promise.resolve(mockRecipe),
      } as Response);

      const result = await fetchRecipe(42);

      expect(fetch).toHaveBeenCalledWith('/api/recipes/42');
      expect(result).toEqual(mockRecipe);
    });

    it('throws on non-ok response', async () => {
      vi.spyOn(globalThis, 'fetch').mockResolvedValue({
        ok: false,
        status: 404,
      } as Response);

      await expect(fetchRecipe(999)).rejects.toThrow('Failed to fetch recipe 999');
    });
  });
});
