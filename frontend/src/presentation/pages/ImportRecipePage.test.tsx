import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import ImportRecipePage from './ImportRecipePage';

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

/** Helper: mock a 200 extraction response */
function mockExtractionResponse(overrides: Record<string, unknown> = {}) {
  return {
    ok: true,
    status: 200,
    json: () =>
      Promise.resolve({
        status: 'COMPLETE',
        rawModelResponse: '{"title":"Chocolate Mousse"}',
        proposedRecipe: {
          title: 'Chocolate Mousse',
          ingredients: ['200 g dark chocolate', '4 eggs'],
          preparation: 'Melt chocolate.\nFold in eggs.',
        },
        missingFields: [],
        warnings: [],
        ...overrides,
      }),
  } as Response;
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

    expect(screen.getByText('Import Recipe')).toBeInTheDocument();
    expect(screen.getByText(/Upload a photo of a recipe/)).toBeInTheDocument();
  });

  it('has correct id attributes for e2e testing', () => {
    render(
      <MemoryRouter>
        <ImportRecipePage />
      </MemoryRouter>,
    );

    expect(document.getElementById('import-recipe-page')).toBeInTheDocument();
    expect(document.getElementById('import-page-title')).toBeInTheDocument();
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
    expect(uploadBtn.disabled).toBe(true);
  });

  it('enables upload button after selecting a file', async () => {
    const user = userEvent.setup();

    render(
      <MemoryRouter>
        <ImportRecipePage />
      </MemoryRouter>,
    );

    const fileInput = document.getElementById('import-file-input') as HTMLInputElement;
    await user.upload(fileInput, createMockFile());

    const uploadBtn = document.getElementById('import-upload-btn') as HTMLButtonElement;
    expect(uploadBtn.disabled).toBe(false);
  });

  it('shows loading state during upload', async () => {
    const user = userEvent.setup();

    vi.spyOn(globalThis, 'fetch').mockImplementation(() => new Promise(() => {}));

    render(
      <MemoryRouter>
        <ImportRecipePage />
      </MemoryRouter>,
    );

    const fileInput = document.getElementById('import-file-input') as HTMLInputElement;
    await user.upload(fileInput, createMockFile());
    await user.click(document.getElementById('import-upload-btn')!);

    expect(screen.getByText('Recognizing...')).toBeInTheDocument();
    expect(document.getElementById('import-loading')).toBeInTheDocument();
  });

  it('always shows editable form after upload — even for complete extraction', async () => {
    const user = userEvent.setup();

    vi.spyOn(globalThis, 'fetch').mockResolvedValue(mockExtractionResponse());

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

    // Should NOT auto-navigate — form should be shown
    expect(mockNavigate).not.toHaveBeenCalled();

    // Status banner shows COMPLETE
    expect(document.getElementById('import-status-banner')).toBeInTheDocument();
    expect(screen.getByText(/All fields were extracted successfully/)).toBeInTheDocument();

    // Form is pre-filled
    const titleInput = document.getElementById('import-edit-title') as HTMLInputElement;
    expect(titleInput.value).toBe('Chocolate Mousse');

    const ingredientsTextarea = document.getElementById('import-edit-ingredients') as HTMLTextAreaElement;
    expect(ingredientsTextarea.value).toBe('200 g dark chocolate\n4 eggs');

    // Confirm button is present
    expect(document.getElementById('import-confirm-btn')).toBeInTheDocument();
  });

  it('shows NEEDS_MORE_INFO status with missing fields highlighted', async () => {
    const user = userEvent.setup();

    vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      mockExtractionResponse({
        status: 'NEEDS_MORE_INFO',
        proposedRecipe: {
          title: 'Partial Recipe',
          ingredients: null,
          preparation: null,
        },
        missingFields: ['ingredients', 'preparation'],
        warnings: ['Could not detect ingredients section'],
      }),
    );

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

    // Status banner shows incomplete
    expect(screen.getByText(/Some fields could not be extracted/)).toBeInTheDocument();

    // Missing fields indicator
    expect(document.getElementById('import-missing-fields')).toBeInTheDocument();

    // Warnings
    expect(document.getElementById('import-warnings')).toBeInTheDocument();
    expect(screen.getByText('Could not detect ingredients section')).toBeInTheDocument();
  });

  it('shows error when image is not a recipe', async () => {
    const user = userEvent.setup();

    vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      mockExtractionResponse({
        status: 'NEEDS_MORE_INFO',
        proposedRecipe: { title: null, ingredients: null, preparation: null },
        missingFields: ['title', 'ingredients', 'preparation'],
        warnings: ['Image does not appear to contain a recipe'],
      }),
    );

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

    expect(screen.getByText('Image does not appear to contain a recipe')).toBeInTheDocument();
  });

  it('shows error message on provider failure (502)', async () => {
    const user = userEvent.setup();

    vi.spyOn(globalThis, 'fetch').mockResolvedValue({
      ok: false,
      status: 502,
      json: () => Promise.resolve({ error: 'AI extraction failed: Provider unavailable' }),
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
      expect(document.getElementById('import-error')).toBeInTheDocument();
      expect(screen.getByText('AI extraction failed: Provider unavailable')).toBeInTheDocument();
    });
  });

  it('navigates to recipe detail after confirming', async () => {
    const user = userEvent.setup();

    let callCount = 0;
    vi.spyOn(globalThis, 'fetch').mockImplementation(() => {
      callCount++;
      if (callCount === 1) {
        return Promise.resolve(mockExtractionResponse());
      }
      return Promise.resolve({
        ok: true,
        status: 201,
        json: () =>
          Promise.resolve({
            id: 100,
            title: 'Chocolate Mousse',
            ingredients: ['200 g dark chocolate', '4 eggs'],
            preparation: 'Melt chocolate.\nFold in eggs.',
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

    const fileInput = document.getElementById('import-file-input') as HTMLInputElement;
    await user.upload(fileInput, createMockFile());
    await user.click(document.getElementById('import-upload-btn')!);

    await waitFor(() => {
      expect(document.getElementById('import-review-section')).toBeInTheDocument();
    });

    await user.click(document.getElementById('import-confirm-btn')!);

    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalledWith('/recipes/100');
    });
  });

  it('disables confirm button when title is empty', async () => {
    const user = userEvent.setup();

    vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      mockExtractionResponse({
        proposedRecipe: { title: null, ingredients: null, preparation: null },
        missingFields: ['title', 'ingredients', 'preparation'],
      }),
    );

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
        return Promise.resolve(mockExtractionResponse());
      }
      return Promise.resolve({ ok: false, status: 500 } as Response);
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

    await user.click(document.getElementById('import-confirm-btn')!);

    await waitFor(() => {
      expect(document.getElementById('import-error')).toBeInTheDocument();
      expect(screen.getByText('Save failed. Please try again.')).toBeInTheDocument();
    });
  });

  it('resets form when cancel is clicked in review mode', async () => {
    const user = userEvent.setup();

    vi.spyOn(globalThis, 'fetch').mockResolvedValue(mockExtractionResponse());

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

    await user.click(document.getElementById('import-cancel-btn')!);

    expect(document.getElementById('import-review-section')).not.toBeInTheDocument();
    expect(document.getElementById('import-upload-section')).toBeInTheDocument();
  });

  it('shows raw model response in collapsible details', async () => {
    const user = userEvent.setup();

    vi.spyOn(globalThis, 'fetch').mockResolvedValue(mockExtractionResponse());

    render(
      <MemoryRouter>
        <ImportRecipePage />
      </MemoryRouter>,
    );

    const fileInput = document.getElementById('import-file-input') as HTMLInputElement;
    await user.upload(fileInput, createMockFile());
    await user.click(document.getElementById('import-upload-btn')!);

    await waitFor(() => {
      expect(document.getElementById('import-raw-text-section')).toBeInTheDocument();
    });

    expect(document.getElementById('import-raw-text')).toBeInTheDocument();
  });
});
