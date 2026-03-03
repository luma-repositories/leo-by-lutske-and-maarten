import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { apiClient } from '../api/client';
import { nl } from '../i18n/nl';
import { RecipesListPage } from './RecipesListPage';

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

describe('RecipesListPage', () => {
  beforeEach(() => {
    vi.resetAllMocks();
    vi.mocked(apiClient.listCategories).mockResolvedValue([]);
  });

  it('shows load error when recipes API is unavailable', async () => {
    vi.mocked(apiClient.listRecipes).mockRejectedValue(new Error('backend unavailable'));

    render(
      <MemoryRouter initialEntries={['/']}>
        <Routes>
          <Route path="/" element={<RecipesListPage />} />
        </Routes>
      </MemoryRouter>
    );

    expect(await screen.findByText(nl.recipesLoadError)).toBeDefined();
    expect(screen.queryByText(nl.noRecipes)).toBeNull();
  });

  it('retries recipe loading after an API failure', async () => {
    vi.mocked(apiClient.listRecipes)
      .mockRejectedValueOnce(new Error('temporary issue'))
      .mockResolvedValueOnce([
        {
          id: 7,
          legacyId: 700,
          title: 'Minestrone',
          category: 'Soepen',
          excerpt: 'Groentensoep'
        }
      ]);

    render(
      <MemoryRouter initialEntries={['/']}>
        <Routes>
          <Route path="/" element={<RecipesListPage />} />
        </Routes>
      </MemoryRouter>
    );

    const retryButton = await screen.findByRole('button', { name: nl.retry });
    await userEvent.click(retryButton);

    expect(await screen.findByText('Minestrone')).toBeDefined();
  });
});
