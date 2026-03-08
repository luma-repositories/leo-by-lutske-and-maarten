import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import ImportRecipePage from './ImportRecipePage';

// Mock react-router-dom's useNavigate
const mockNavigate = vi.fn();
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

function createMockFile(name = 'recipe.jpg', type = 'image/jpeg', size = 1024): File {
  const content = new Uint8Array(size);
  return new File([content], name, { type });
}

describe('ImportRecipePage', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
    mockNavigate.mockReset();
  });

  it('renders the page title and subtitle', () => {
    render(
      <MemoryRouter>
        <ImportRecipePage />
      </MemoryRouter>,
    );

    expect(screen.getByText('Recept importeren')).toBeInTheDocument();
    expect(
      screen.getByText(/Upload een foto van een recept/),
    ).toBeInTheDocument();
  });

  it('has correct id attributes for e2e testing', () => {
    render(
      <MemoryRouter>
        <ImportRecipePage />
      </MemoryRouter>,
    );

    expect(document.getElementById('import-recipe-page')).toBeInTheDocument();
    expect(document.getElementById('import-page-title')).toBeInTheDocument();
    expect(document.getElementById('import-page-subtitle')).toBeInTheDocument();
    expect(document.getElementById('import-upload-section')).toBeInTheDocument();
    expect(document.getElementById('import-file-input')).toBeInTheDocument();
    expect(document.getElementById('import-upload-btn')).toBeInTheDocument();
  });

  it('shows upload button disabled when no file is selected', () => {
    render(
      <MemoryRouter>
        <ImportRecipePage />
      </MemoryRouter>,
    );

    const uploadBtn = document.getElementById('import-upload-btn') as HTMLButtonElement;
    expect(uploadBtn).toBeInTheDocument();
    expect(uploadBtn.disabled).toBe(true);
  });

  it('shows file hint text', () => {
    render(
      <MemoryRouter>
        <ImportRecipePage />
      </MemoryRouter>,
    );

    expect(
      screen.getByText(/Kies een afbeelding.*max 10 MB/),
    ).toBeInTheDocument();
  });

  it('enables upload button after selecting a file', async () => {
    const user = userEvent.setup();

    render(
      <MemoryRouter>
        <ImportRecipePage />
      </MemoryRouter>,
    );

    const fileInput = document.getElementById('import-file-input') as HTMLInputElement;
    const file = createMockFile();

    await user.upload(fileInput, file);

    const uploadBtn = document.getElementById('import-upload-btn') as HTMLButtonElement;
    expect(uploadBtn.disabled).toBe(false);
  });

  it('shows reset button after selecting a file', async () => {
    const user = userEvent.setup();

    render(
      <MemoryRouter>
        <ImportRecipePage />
      </MemoryRouter>,
    );

    // No reset button initially
    expect(document.getElementById('import-reset-btn')).not.toBeInTheDocument();

    const fileInput = document.getElementById('import-file-input') as HTMLInputElement;
    await user.upload(fileInput, createMockFile());

    // Reset button appears
    expect(document.getElementById('import-reset-btn')).toBeInTheDocument();
    expect(screen.getByText('Opnieuw kiezen')).toBeInTheDocument();
  });

  it('shows loading state during upload', async () => {
    const user = userEvent.setup();

    // Mock fetch that never resolves
    vi.spyOn(globalThis, 'fetch').mockImplementation(
      () => new Promise(() => {}),
    );

    render(
      <MemoryRouter>
        <ImportRecipePage />
      </MemoryRouter>,
    );

    const fileInput = document.getElementById('import-file-input') as HTMLInputElement;
    await user.upload(fileInput, createMockFile());

    const uploadBtn = document.getElementById('import-upload-btn') as HTMLButtonElement;
    await user.click(uploadBtn);

    expect(screen.getByText('Bezig met herkennen...')).toBeInTheDocument();
    expect(document.getElementById('import-loading')).toBeInTheDocument();
    expect(screen.getByText('Afbeelding wordt geanalyseerd door AI...')).toBeInTheDocument();
  });

  it('navigates to recipe detail on successful import (201)', async () => {
    const user = userEvent.setup();

    vi.spyOn(globalThis, 'fetch').mockResolvedValue({
      ok: true,
      status: 201,
      json: () =>
        Promise.resolve({
          id: 99,
          title: 'Imported Recipe',
          ingredients: ['flour', 'eggs'],
          preparation: 'Mix and bake.',
          categoryId: 15,
          categoryName: 'Geimporteerd',
          viewCount: 0,
        }),
    } as Response);

    render(
      <MemoryRouter>
        <ImportRecipePage />
      </MemoryRouter>,
    );

    const fileInput = document.getElementById('import-file-input') as HTMLInputElement;
    await user.upload(fileInput, createMockFile());

    const uploadBtn = document.getElementById('import-upload-btn') as HTMLButtonElement;
    await user.click(uploadBtn);

    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalledWith('/recipes/99');
    });
  });

  it('shows needs-more-info form on 422 response', async () => {
    const user = userEvent.setup();

    vi.spyOn(globalThis, 'fetch').mockResolvedValue({
      ok: false,
      status: 422,
      json: () =>
        Promise.resolve({
          status: 'NEEDS_MORE_INFO',
          rawModelResponse: '{"title":"Proposed Title"}',
          proposedRecipe: {
            title: 'Proposed Title',
            ingredients: ['ingredient 1', 'ingredient 2'],
            preparation: null,
          },
          missingFields: ['preparation'],
          warnings: ['Could not detect preparation section'],
        }),
    } as Response);

    render(
      <MemoryRouter>
        <ImportRecipePage />
      </MemoryRouter>,
    );

    const fileInput = document.getElementById('import-file-input') as HTMLInputElement;
    await user.upload(fileInput, createMockFile());

    const uploadBtn = document.getElementById('import-upload-btn') as HTMLButtonElement;
    await user.click(uploadBtn);

    await waitFor(() => {
      expect(document.getElementById('import-review-section')).toBeInTheDocument();
    });

    // Check raw model response is displayed
    expect(document.getElementById('import-raw-text')).toBeInTheDocument();

    // Check form fields are pre-filled
    const titleInput = document.getElementById('import-edit-title') as HTMLInputElement;
    expect(titleInput.value).toBe('Proposed Title');

    const ingredientsTextarea = document.getElementById('import-edit-ingredients') as HTMLTextAreaElement;
    expect(ingredientsTextarea.value).toBe('ingredient 1\ningredient 2');

    // Check missing fields indicator
    expect(document.getElementById('import-missing-fields')).toBeInTheDocument();
    expect(screen.getByText('Bereiding')).toBeInTheDocument();

    // Check warnings
    expect(document.getElementById('import-warnings')).toBeInTheDocument();
    expect(screen.getByText('Could not detect preparation section')).toBeInTheDocument();

    // Check confirm button
    expect(document.getElementById('import-confirm-btn')).toBeInTheDocument();
    expect(screen.getByText('Bevestigen & opslaan')).toBeInTheDocument();

    // Check cancel button
    expect(document.getElementById('import-cancel-btn')).toBeInTheDocument();
    expect(screen.getByText('Annuleren')).toBeInTheDocument();
  });

  it('shows error message on failed import', async () => {
    const user = userEvent.setup();

    vi.spyOn(globalThis, 'fetch').mockResolvedValue({
      ok: false,
      status: 400,
      json: () =>
        Promise.resolve({
          error: 'Bestand is te groot',
        }),
    } as Response);

    render(
      <MemoryRouter>
        <ImportRecipePage />
      </MemoryRouter>,
    );

    const fileInput = document.getElementById('import-file-input') as HTMLInputElement;
    await user.upload(fileInput, createMockFile());

    const uploadBtn = document.getElementById('import-upload-btn') as HTMLButtonElement;
    await user.click(uploadBtn);

    await waitFor(() => {
      expect(document.getElementById('import-error')).toBeInTheDocument();
      expect(screen.getByText('Bestand is te groot')).toBeInTheDocument();
    });
  });

  it('shows error message on 502 provider failure', async () => {
    const user = userEvent.setup();

    vi.spyOn(globalThis, 'fetch').mockResolvedValue({
      ok: false,
      status: 502,
      json: () =>
        Promise.resolve({
          error: 'AI extraction failed: Provider unavailable',
        }),
    } as Response);

    render(
      <MemoryRouter>
        <ImportRecipePage />
      </MemoryRouter>,
    );

    const fileInput = document.getElementById('import-file-input') as HTMLInputElement;
    await user.upload(fileInput, createMockFile());

    const uploadBtn = document.getElementById('import-upload-btn') as HTMLButtonElement;
    await user.click(uploadBtn);

    await waitFor(() => {
      expect(document.getElementById('import-error')).toBeInTheDocument();
      expect(screen.getByText('AI extraction failed: Provider unavailable')).toBeInTheDocument();
    });
  });

  it('navigates to recipe detail after confirming import', async () => {
    const user = userEvent.setup();

    // First call: 422 needs more info
    // Second call: 201 confirmed
    let callCount = 0;
    vi.spyOn(globalThis, 'fetch').mockImplementation(() => {
      callCount++;
      if (callCount === 1) {
        return Promise.resolve({
          ok: false,
          status: 422,
          json: () =>
            Promise.resolve({
              status: 'NEEDS_MORE_INFO',
              rawModelResponse: '{"title":"My Recipe"}',
              proposedRecipe: {
                title: 'My Recipe',
                ingredients: ['flour'],
                preparation: null,
              },
              missingFields: ['preparation'],
              warnings: [],
            }),
        } as Response);
      }
      return Promise.resolve({
        ok: true,
        status: 201,
        json: () =>
          Promise.resolve({
            id: 100,
            title: 'My Recipe',
            ingredients: ['flour'],
            preparation: 'Mix well.',
            categoryId: 15,
            categoryName: 'Geimporteerd',
            viewCount: 0,
          }),
      } as Response);
    });

    render(
      <MemoryRouter>
        <ImportRecipePage />
      </MemoryRouter>,
    );

    // Upload file
    const fileInput = document.getElementById('import-file-input') as HTMLInputElement;
    await user.upload(fileInput, createMockFile());
    await user.click(document.getElementById('import-upload-btn')!);

    // Wait for review form
    await waitFor(() => {
      expect(document.getElementById('import-review-section')).toBeInTheDocument();
    });

    // Fill in preparation
    const prepTextarea = document.getElementById('import-edit-preparation') as HTMLTextAreaElement;
    await user.type(prepTextarea, 'Mix well.');

    // Click confirm
    await user.click(document.getElementById('import-confirm-btn')!);

    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalledWith('/recipes/100');
    });
  });

  it('disables confirm button when title is empty', async () => {
    const user = userEvent.setup();

    vi.spyOn(globalThis, 'fetch').mockResolvedValue({
      ok: false,
      status: 422,
      json: () =>
        Promise.resolve({
          status: 'NEEDS_MORE_INFO',
          rawModelResponse: '{}',
          proposedRecipe: {
            title: null,
            ingredients: null,
            preparation: null,
          },
          missingFields: ['title', 'ingredients', 'preparation'],
          warnings: [],
        }),
    } as Response);

    render(
      <MemoryRouter>
        <ImportRecipePage />
      </MemoryRouter>,
    );

    const fileInput = document.getElementById('import-file-input') as HTMLInputElement;
    await user.upload(fileInput, createMockFile());
    await user.click(document.getElementById('import-upload-btn')!);

    await waitFor(() => {
      expect(document.getElementById('import-review-section')).toBeInTheDocument();
    });

    const confirmBtn = document.getElementById('import-confirm-btn') as HTMLButtonElement;
    expect(confirmBtn.disabled).toBe(true);
  });

  it('shows error when confirm fails', async () => {
    const user = userEvent.setup();

    let callCount = 0;
    vi.spyOn(globalThis, 'fetch').mockImplementation(() => {
      callCount++;
      if (callCount === 1) {
        return Promise.resolve({
          ok: false,
          status: 422,
          json: () =>
            Promise.resolve({
              status: 'NEEDS_MORE_INFO',
              rawModelResponse: '{"title":"Test Recipe"}',
              proposedRecipe: {
                title: 'Test Recipe',
                ingredients: ['item'],
                preparation: null,
              },
              missingFields: ['preparation'],
              warnings: [],
            }),
        } as Response);
      }
      // Confirm call fails
      return Promise.resolve({
        ok: false,
        status: 500,
      } as Response);
    });

    render(
      <MemoryRouter>
        <ImportRecipePage />
      </MemoryRouter>,
    );

    const fileInput = document.getElementById('import-file-input') as HTMLInputElement;
    await user.upload(fileInput, createMockFile());
    await user.click(document.getElementById('import-upload-btn')!);

    await waitFor(() => {
      expect(document.getElementById('import-review-section')).toBeInTheDocument();
    });

    // Fill preparation and confirm
    const prepTextarea = document.getElementById('import-edit-preparation') as HTMLTextAreaElement;
    await user.type(prepTextarea, 'Some preparation');
    await user.click(document.getElementById('import-confirm-btn')!);

    await waitFor(() => {
      expect(document.getElementById('import-error')).toBeInTheDocument();
      expect(screen.getByText('Opslaan mislukt. Probeer opnieuw.')).toBeInTheDocument();
    });
  });

  it('resets form when cancel is clicked in review mode', async () => {
    const user = userEvent.setup();

    vi.spyOn(globalThis, 'fetch').mockResolvedValue({
      ok: false,
      status: 422,
      json: () =>
        Promise.resolve({
          status: 'NEEDS_MORE_INFO',
          rawModelResponse: '{"title":"Test"}',
          proposedRecipe: {
            title: 'Test',
            ingredients: ['item'],
            preparation: null,
          },
          missingFields: ['preparation'],
          warnings: [],
        }),
    } as Response);

    render(
      <MemoryRouter>
        <ImportRecipePage />
      </MemoryRouter>,
    );

    const fileInput = document.getElementById('import-file-input') as HTMLInputElement;
    await user.upload(fileInput, createMockFile());
    await user.click(document.getElementById('import-upload-btn')!);

    await waitFor(() => {
      expect(document.getElementById('import-review-section')).toBeInTheDocument();
    });

    // Click cancel
    await user.click(document.getElementById('import-cancel-btn')!);

    // Review section should be gone, upload section should be back
    expect(document.getElementById('import-review-section')).not.toBeInTheDocument();
    expect(document.getElementById('import-upload-section')).toBeInTheDocument();
  });
});
