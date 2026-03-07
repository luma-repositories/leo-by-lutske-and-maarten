import { describe, it, expect, vi, beforeEach } from 'vitest';
import {
  fetchVersion,
  fetchCategories,
  fetchRecipes,
  fetchTopRecipes,
  fetchRecipe,
  importRecipeImage,
  confirmRecipeImport,
} from './client';
import type { ImportConfirmRequest } from './client';

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

  describe('importRecipeImage', () => {
    it('returns created status with recipe on 201', async () => {
      const mockRecipe = {
        id: 99,
        title: 'Imported Recipe',
        ingredients: ['flour', 'eggs'],
        preparation: 'Mix and bake.',
        categoryId: 15,
        categoryName: 'Geimporteerd',
        viewCount: 0,
      };

      vi.spyOn(globalThis, 'fetch').mockResolvedValue({
        ok: true,
        status: 201,
        json: () => Promise.resolve(mockRecipe),
      } as Response);

      const file = new File(['image-data'], 'recipe.jpg', { type: 'image/jpeg' });
      const result = await importRecipeImage(file);

      expect(fetch).toHaveBeenCalledWith('/api/recipes/import', {
        method: 'POST',
        body: expect.any(FormData),
      });
      expect(result).toEqual({ status: 'created', recipe: mockRecipe });
    });

    it('returns needs_more_info status on 422', async () => {
      const mockData = {
        status: 'NEEDS_MORE_INFO',
        rawText: 'Some OCR text',
        proposedRecipe: { title: 'Test', ingredients: null, preparation: null },
        missingFields: ['ingredients', 'preparation'],
        parseWarnings: ['Could not detect ingredients'],
      };

      vi.spyOn(globalThis, 'fetch').mockResolvedValue({
        ok: false,
        status: 422,
        json: () => Promise.resolve(mockData),
      } as Response);

      const file = new File(['image-data'], 'recipe.png', { type: 'image/png' });
      const result = await importRecipeImage(file);

      expect(result).toEqual({ status: 'needs_more_info', data: mockData });
    });

    it('returns error status on other failure codes', async () => {
      vi.spyOn(globalThis, 'fetch').mockResolvedValue({
        ok: false,
        status: 400,
        json: () => Promise.resolve({ error: 'File too large' }),
      } as Response);

      const file = new File(['image-data'], 'recipe.jpg', { type: 'image/jpeg' });
      const result = await importRecipeImage(file);

      expect(result).toEqual({ status: 'error', message: 'File too large' });
    });

    it('returns error with fallback message when JSON parse fails', async () => {
      vi.spyOn(globalThis, 'fetch').mockResolvedValue({
        ok: false,
        status: 500,
        json: () => Promise.reject(new Error('Invalid JSON')),
      } as Response);

      const file = new File(['image-data'], 'recipe.jpg', { type: 'image/jpeg' });
      const result = await importRecipeImage(file);

      // The catch block in importRecipeImage falls back to { error: 'Unknown error' }
      expect(result).toEqual({ status: 'error', message: 'Unknown error' });
    });
  });

  describe('confirmRecipeImport', () => {
    it('sends JSON body and returns recipe on success', async () => {
      const mockRecipe = {
        id: 100,
        title: 'Confirmed Recipe',
        ingredients: ['flour'],
        preparation: 'Mix well.',
        categoryId: 15,
        categoryName: 'Geimporteerd',
        viewCount: 0,
      };

      vi.spyOn(globalThis, 'fetch').mockResolvedValue({
        ok: true,
        status: 201,
        json: () => Promise.resolve(mockRecipe),
      } as Response);

      const request: ImportConfirmRequest = {
        rawText: 'OCR text',
        proposedRecipe: { title: 'Confirmed Recipe', ingredients: ['flour'], preparation: null },
        userOverrides: { preparation: 'Mix well.' },
      };

      const result = await confirmRecipeImport(request);

      expect(fetch).toHaveBeenCalledWith('/api/recipes/import/confirm', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(request),
      });
      expect(result).toEqual(mockRecipe);
    });

    it('throws on non-ok response', async () => {
      vi.spyOn(globalThis, 'fetch').mockResolvedValue({
        ok: false,
        status: 400,
      } as Response);

      const request: ImportConfirmRequest = {
        rawText: 'OCR text',
        proposedRecipe: { title: null },
        userOverrides: {},
      };

      await expect(confirmRecipeImport(request)).rejects.toThrow('Failed to confirm recipe import');
    });
  });
});
