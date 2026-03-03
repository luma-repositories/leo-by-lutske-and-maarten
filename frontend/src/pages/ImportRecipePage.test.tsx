import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { apiClient } from '../api/client';
import { ImportRecipePage } from './ImportRecipePage';

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

describe('ImportRecipePage', () => {
  beforeEach(() => {
    vi.resetAllMocks();
    vi.stubGlobal('URL', {
      ...URL,
      createObjectURL: vi.fn(() => 'blob:preview')
    });
  });

  it('shows follow-up form and confirms import when more info is needed', async () => {
    vi.mocked(apiClient.importRecipe).mockResolvedValue({
      kind: 'needs_more_info',
      payload: {
        status: 'NEEDS_MORE_INFO',
        rawText: 'OCR text',
        proposedRecipe: {
          title: 'Soup',
          ingredients: '1 tomato',
          instructions: 'Boil',
          source: 'Imported from image'
        },
        missingFields: ['servings'],
        parseWarnings: ['Needs manual review']
      }
    });

    vi.mocked(apiClient.confirmImportedRecipe).mockResolvedValue({
      kind: 'created',
      recipe: {
        id: 999,
        legacyId: 0,
        title: 'Soup',
        category: 'Imported',
        ingredients: '1 tomato',
        preparation: 'Boil'
      }
    });

    render(
      <MemoryRouter initialEntries={['/recipes/import']}>
        <Routes>
          <Route path="/" element={<p id="home-marker">home</p>} />
          <Route path="/recipes/import" element={<ImportRecipePage />} />
          <Route path="/recipes/:id" element={<p id="import-route-result">detail opened</p>} />
        </Routes>
      </MemoryRouter>
    );

    const fileInput = screen.getByLabelText('Kies afbeelding');
    await userEvent.upload(fileInput, new File(['abc'], 'recipe.png', { type: 'image/png' }));
    await userEvent.click(screen.getByRole('button', { name: 'Upload en scan' }));

    expect(await screen.findByText('Nog extra info nodig')).toBeDefined();
    await userEvent.type(screen.getByLabelText('Porties'), '2');
    await userEvent.click(screen.getByRole('button', { name: 'Bevestig en opslaan' }));

    expect(await screen.findByText(/Recept succesvol geimporteerd/)).toBeDefined();
  });

  it('navigates back to the recipes overview', async () => {
    render(
      <MemoryRouter initialEntries={['/recipes/import']}>
        <Routes>
          <Route path="/" element={<p id="home-marker">home</p>} />
          <Route path="/recipes/import" element={<ImportRecipePage />} />
        </Routes>
      </MemoryRouter>
    );

    await userEvent.click(screen.getByRole('link', { name: 'Terug naar recepten' }));
    expect(await screen.findByText('home')).toBeDefined();
  });

  it('shows backend OCR runtime message on upload failure', async () => {
    vi.mocked(apiClient.importRecipe).mockRejectedValue(
      new Error('OCR runtime unavailable. Install native tesseract and restart the backend.')
    );

    render(
      <MemoryRouter initialEntries={['/recipes/import']}>
        <Routes>
          <Route path="/recipes/import" element={<ImportRecipePage />} />
        </Routes>
      </MemoryRouter>
    );

    const fileInput = screen.getByLabelText('Kies afbeelding');
    await userEvent.upload(fileInput, new File(['abc'], 'recipe.png', { type: 'image/png' }));
    await userEvent.click(screen.getByRole('button', { name: 'Upload en scan' }));

    expect(await screen.findByText('OCR runtime unavailable. Install native tesseract and restart the backend.')).toBeDefined();
  });
});
