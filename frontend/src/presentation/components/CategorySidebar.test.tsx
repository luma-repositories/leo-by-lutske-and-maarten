import { render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import CategorySidebar from './CategorySidebar';

const mockCategories = [
  { id: 1, name: 'Soups', recipeCount: 16 },
  { id: 2, name: 'Pastas', recipeCount: 8 },
  { id: 3, name: 'Gebak', recipeCount: 12 },
];

describe('CategorySidebar', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
  });

  it('renders the sidebar title', () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue({
      ok: true,
      json: () => Promise.resolve(mockCategories),
    } as Response);

    render(
      <MemoryRouter>
        <CategorySidebar />
      </MemoryRouter>,
    );

    expect(screen.getByText('Recipe Book')).toBeInTheDocument();
  });

  it('renders "All Recipes" link', () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue({
      ok: true,
      json: () => Promise.resolve(mockCategories),
    } as Response);

    render(
      <MemoryRouter>
        <CategorySidebar />
      </MemoryRouter>,
    );

    expect(screen.getByText('All Recipes')).toBeInTheDocument();
  });

  it('renders categories after fetching', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue({
      ok: true,
      json: () => Promise.resolve(mockCategories),
    } as Response);

    render(
      <MemoryRouter>
        <CategorySidebar />
      </MemoryRouter>,
    );

    await waitFor(() => {
      expect(screen.getByText('Soups')).toBeInTheDocument();
      expect(screen.getByText('Pastas')).toBeInTheDocument();
      expect(screen.getByText('Gebak')).toBeInTheDocument();
    });
  });

  it('displays recipe counts for each category', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue({
      ok: true,
      json: () => Promise.resolve(mockCategories),
    } as Response);

    render(
      <MemoryRouter>
        <CategorySidebar />
      </MemoryRouter>,
    );

    await waitFor(() => {
      expect(screen.getByText('16')).toBeInTheDocument();
      expect(screen.getByText('8')).toBeInTheDocument();
      expect(screen.getByText('12')).toBeInTheDocument();
    });
  });

  it('has correct id attributes for e2e testing', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue({
      ok: true,
      json: () => Promise.resolve(mockCategories),
    } as Response);

    render(
      <MemoryRouter>
        <CategorySidebar />
      </MemoryRouter>,
    );

    expect(document.getElementById('category-sidebar')).toBeInTheDocument();
    expect(document.getElementById('sidebar-title')).toBeInTheDocument();
    expect(document.getElementById('category-list')).toBeInTheDocument();

    await waitFor(() => {
      expect(document.getElementById('category-item-1')).toBeInTheDocument();
      expect(document.getElementById('category-link-1')).toBeInTheDocument();
      expect(document.getElementById('category-count-1')).toBeInTheDocument();
    });
  });

  it('highlights active category based on URL search params', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue({
      ok: true,
      json: () => Promise.resolve(mockCategories),
    } as Response);

    render(
      <MemoryRouter initialEntries={['/?categoryId=2']}>
        <CategorySidebar />
      </MemoryRouter>,
    );

    await waitFor(() => {
      const activeLink = document.getElementById('category-link-2');
      expect(activeLink).toBeInTheDocument();
      expect(activeLink?.className).toContain('sidebar__link--active');
    });
  });

  it('renders empty list gracefully when fetch fails', async () => {
    vi.spyOn(globalThis, 'fetch').mockRejectedValue(new Error('Network error'));

    render(
      <MemoryRouter>
        <CategorySidebar />
      </MemoryRouter>,
    );

    expect(document.getElementById('category-sidebar')).toBeInTheDocument();
    expect(screen.getByText('All Recipes')).toBeInTheDocument();
  });
});
