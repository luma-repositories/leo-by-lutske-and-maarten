import { render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import App from './App';
import { apiClient } from './api/client';

vi.mock('./api/client', () => ({
  apiClient: {
    getVersion: vi.fn(),
    listCategories: vi.fn(),
    listRecipes: vi.fn(),
    getRecipe: vi.fn()
  }
}));

describe('App', () => {
  beforeEach(() => {
    vi.resetAllMocks();
  });

  it('renders app shell and recipe overview smoke flow', async () => {
    vi.mocked(apiClient.getVersion).mockResolvedValue({ version: '1.0.0-test' });
    vi.mocked(apiClient.listCategories).mockResolvedValue([
      { id: 1, slug: 'soepen', name: 'Soepen', recipeCount: 1 }
    ]);
    vi.mocked(apiClient.listRecipes).mockResolvedValue([
      {
        id: 100,
        legacyId: 10,
        title: 'Tomatensoep',
        category: 'Soepen',
        excerpt: 'Snelle soep'
      }
    ]);

    render(
      <MemoryRouter initialEntries={['/']}>
        <App />
      </MemoryRouter>
    );

    expect(await screen.findByText('Leo Legacy Recepten')).toBeDefined();

    await waitFor(() => {
      expect(screen.getByText('Tomatensoep')).toBeDefined();
      expect(screen.getByText('1.0.0-test')).toBeDefined();
    });
  });
});
