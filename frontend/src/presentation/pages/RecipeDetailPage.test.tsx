import { render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import RecipeDetailPage from './RecipeDetailPage';

const mockRecipe = {
  id: 42,
  title: 'Chocolademousse',
  ingredients: ['200 g pure chocolade', '4 eieren', '50 g suiker'],
  preparation: 'Smelt de chocolade au bain-marie. Klop de eiwitten stijf met de suiker.',
  categoryId: 7,
  categoryName: 'Nagerechten',
  viewCount: 5432,
};

function renderWithRoute(id: string) {
  return render(
    <MemoryRouter initialEntries={[`/recipes/${id}`]}>
      <Routes>
        <Route path="/recipes/:id" element={<RecipeDetailPage />} />
      </Routes>
    </MemoryRouter>,
  );
}

describe('RecipeDetailPage', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
  });

  it('shows loading state initially', () => {
    vi.spyOn(globalThis, 'fetch').mockImplementation(
      () => new Promise(() => {}), // never resolves
    );

    renderWithRoute('42');

    expect(screen.getByText('Loading...')).toBeInTheDocument();
    expect(document.getElementById('recipe-detail-loading')).toBeInTheDocument();
  });

  it('renders recipe title after loading', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue({
      ok: true,
      json: () => Promise.resolve(mockRecipe),
    } as Response);

    renderWithRoute('42');

    await waitFor(() => {
      expect(screen.getByText('Chocolademousse')).toBeInTheDocument();
    });
  });

  it('renders ingredients list', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue({
      ok: true,
      json: () => Promise.resolve(mockRecipe),
    } as Response);

    renderWithRoute('42');

    await waitFor(() => {
      expect(screen.getByText('200 g pure chocolade')).toBeInTheDocument();
      expect(screen.getByText('4 eieren')).toBeInTheDocument();
      expect(screen.getByText('50 g suiker')).toBeInTheDocument();
    });
  });

  it('renders preparation text', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue({
      ok: true,
      json: () => Promise.resolve(mockRecipe),
    } as Response);

    renderWithRoute('42');

    await waitFor(() => {
      expect(
        screen.getByText(/Smelt de chocolade au bain-marie/),
      ).toBeInTheDocument();
    });
  });

  it('renders category name as badge', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue({
      ok: true,
      json: () => Promise.resolve(mockRecipe),
    } as Response);

    renderWithRoute('42');

    await waitFor(() => {
      expect(screen.getByText('Nagerechten')).toBeInTheDocument();
    });
  });

  it('renders section headings in English', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue({
      ok: true,
      json: () => Promise.resolve(mockRecipe),
    } as Response);

    renderWithRoute('42');

    await waitFor(() => {
      expect(screen.getByText('Ingredients')).toBeInTheDocument();
      expect(screen.getByText('Preparation')).toBeInTheDocument();
    });
  });

  it('shows error state when recipe not found', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue({
      ok: false,
      status: 404,
    } as Response);

    renderWithRoute('999999');

    await waitFor(() => {
      expect(screen.getByText('Recipe not found.')).toBeInTheDocument();
    });
  });

  it('has correct id attributes for e2e testing', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue({
      ok: true,
      json: () => Promise.resolve(mockRecipe),
    } as Response);

    renderWithRoute('42');

    await waitFor(() => {
      expect(document.getElementById('recipe-detail-page')).toBeInTheDocument();
      expect(document.getElementById('recipe-detail-card')).toBeInTheDocument();
      expect(document.getElementById('recipe-detail-title')).toBeInTheDocument();
      expect(document.getElementById('recipe-detail-category')).toBeInTheDocument();
      expect(document.getElementById('recipe-detail-ingredients-section')).toBeInTheDocument();
      expect(document.getElementById('recipe-detail-preparation-section')).toBeInTheDocument();
      expect(document.getElementById('recipe-detail-back')).toBeInTheDocument();
    });
  });

  it('back link navigates to category page', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue({
      ok: true,
      json: () => Promise.resolve(mockRecipe),
    } as Response);

    renderWithRoute('42');

    await waitFor(() => {
      const backLink = document.getElementById('recipe-detail-back') as HTMLAnchorElement;
      expect(backLink).toBeInTheDocument();
      expect(backLink.getAttribute('href')).toBe('/?categoryId=7');
      expect(backLink.textContent).toContain('Back to Nagerechten');
    });
  });
});
