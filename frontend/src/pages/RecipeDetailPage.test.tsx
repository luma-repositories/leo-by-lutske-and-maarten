import { render, screen } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { apiClient } from '../api/client';
import { RecipeDetailPage } from './RecipeDetailPage';

vi.mock('../api/client', () => ({
  apiClient: {
    getVersion: vi.fn(),
    listCategories: vi.fn(),
    listRecipes: vi.fn(),
    getRecipe: vi.fn(),
    importRecipe: vi.fn(),
    confirmImportedRecipe: vi.fn()
  }
}));

describe('RecipeDetailPage', () => {
  beforeEach(() => {
    vi.resetAllMocks();
  });

  it('renders recipe details for a known recipe id', async () => {
    vi.mocked(apiClient.getRecipe).mockResolvedValue({
      id: 42,
      legacyId: 4242,
      title: 'Pasta met basilicum',
      category: 'Pastas',
      ingredients: 'Pasta, basilicum, olijfolie',
      preparation: 'Kook de pasta en meng met de rest.',
      pdfSlug: 'rcpt_pdf_Pasta_met_basilicum'
    });

    render(
      <MemoryRouter initialEntries={['/recipes/42']}>
        <Routes>
          <Route path="/recipes/:id" element={<RecipeDetailPage />} />
        </Routes>
      </MemoryRouter>
    );

    expect(await screen.findByText('Pasta met basilicum')).toBeDefined();
    expect(screen.getByText('Pastas')).toBeDefined();
    expect(screen.getByText('Pasta, basilicum, olijfolie')).toBeDefined();
    expect(screen.getByText('Kook de pasta en meng met de rest.')).toBeDefined();
  });
});
