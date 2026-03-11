import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import ImportRecipePage from './ImportRecipePage';

describe('ImportRecipePage', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
  });

  it('renders the page title', () => {
    render(
      <MemoryRouter>
        <ImportRecipePage />
      </MemoryRouter>,
    );

    expect(screen.getByText('Import Recipe')).toBeInTheDocument();
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
  });

  it('renders the upload section initially', () => {
    render(
      <MemoryRouter>
        <ImportRecipePage />
      </MemoryRouter>,
    );

    expect(document.getElementById('import-upload-btn')).toBeInTheDocument();
    expect(document.getElementById('import-upload-hint')).toBeInTheDocument();
  });
});
