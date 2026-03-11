import { render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import RecipeDetailPage from './RecipeDetailPage';

const mockRecipe = {
  id: 42,
  title: 'Chocolate Mousse',
  ingredients: ['200 g chocolate', '4 eggs', '50 g sugar'],
  preparation: 'Melt the chocolate.',
  categoryId: 7,
  categoryName: 'Desserts',
  viewCount: 100,
};

describe('RecipeDetailPage', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
  });

  it('renders recipe detail after loading', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue({
      ok: true,
      json: () => Promise.resolve(mockRecipe),
    } as Response);

    render(
      <MemoryRouter initialEntries={['/recipes/42']}>
        <Routes>
          <Route path="/recipes/:id" element={<RecipeDetailPage />} />
        </Routes>
      </MemoryRouter>,
    );

    await waitFor(() => {
      expect(screen.getByText('Chocolate Mousse')).toBeInTheDocument();
    });
  });

  it('shows loading state initially', () => {
    vi.spyOn(globalThis, 'fetch').mockImplementation(() => new Promise(() => {}));

    render(
      <MemoryRouter initialEntries={['/recipes/42']}>
        <Routes>
          <Route path="/recipes/:id" element={<RecipeDetailPage />} />
        </Routes>
      </MemoryRouter>,
    );

    expect(screen.getByText('Loading...')).toBeInTheDocument();
  });

  it('shows error state when recipe not found', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue({
      ok: false,
      status: 404,
    } as Response);

    render(
      <MemoryRouter initialEntries={['/recipes/999']}>
        <Routes>
          <Route path="/recipes/:id" element={<RecipeDetailPage />} />
        </Routes>
      </MemoryRouter>,
    );

    await waitFor(() => {
      expect(screen.getByText('Recipe not found.')).toBeInTheDocument();
    });
  });

  it('has correct id attributes for e2e testing', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue({
      ok: true,
      json: () => Promise.resolve(mockRecipe),
    } as Response);

    render(
      <MemoryRouter initialEntries={['/recipes/42']}>
        <Routes>
          <Route path="/recipes/:id" element={<RecipeDetailPage />} />
        </Routes>
      </MemoryRouter>,
    );

    await waitFor(() => {
      expect(document.getElementById('recipe-detail-page')).toBeInTheDocument();
      expect(document.getElementById('recipe-detail-title')).toBeInTheDocument();
      expect(document.getElementById('recipe-detail-ingredients')).toBeInTheDocument();
      expect(document.getElementById('recipe-detail-preparation')).toBeInTheDocument();
    });
  });
});
