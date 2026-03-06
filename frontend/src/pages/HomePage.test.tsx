import { render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import HomePage from './HomePage';

const mockTopRecipes = [
  { id: 1, title: 'Chocolademousse', categoryName: 'Nagerechten', viewCount: 5432 },
  { id: 2, title: 'Champignonsoep', categoryName: 'Soepen', viewCount: 3210 },
];

const mockCategoryRecipes = [
  { id: 10, title: 'Uiensoep', categoryName: 'Soepen', viewCount: 100 },
  { id: 11, title: 'Pompoensoep', categoryName: 'Soepen', viewCount: 200 },
];

const mockCategories = [
  { id: 1, name: 'Soepen', recipeCount: 16 },
];

describe('HomePage', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
  });

  it('shows "Populairste recepten" heading when no category is selected', async () => {
    vi.spyOn(globalThis, 'fetch').mockImplementation((url) => {
      const urlStr = typeof url === 'string' ? url : url.toString();
      if (urlStr.includes('/api/recipes/top')) {
        return Promise.resolve({
          ok: true,
          json: () => Promise.resolve(mockTopRecipes),
        } as Response);
      }
      if (urlStr.includes('/api/categories')) {
        return Promise.resolve({
          ok: true,
          json: () => Promise.resolve(mockCategories),
        } as Response);
      }
      return Promise.resolve({
        ok: true,
        json: () => Promise.resolve([]),
      } as Response);
    });

    render(
      <MemoryRouter initialEntries={['/']}>
        <HomePage />
      </MemoryRouter>,
    );

    await waitFor(() => {
      expect(screen.getByText('Populairste recepten')).toBeInTheDocument();
    });
  });

  it('shows "Recepten" heading when a category is selected', async () => {
    vi.spyOn(globalThis, 'fetch').mockImplementation((url) => {
      const urlStr = typeof url === 'string' ? url : url.toString();
      if (urlStr.includes('/api/recipes') && urlStr.includes('categoryId=1')) {
        return Promise.resolve({
          ok: true,
          json: () => Promise.resolve(mockCategoryRecipes),
        } as Response);
      }
      if (urlStr.includes('/api/categories')) {
        return Promise.resolve({
          ok: true,
          json: () => Promise.resolve(mockCategories),
        } as Response);
      }
      return Promise.resolve({
        ok: true,
        json: () => Promise.resolve([]),
      } as Response);
    });

    render(
      <MemoryRouter initialEntries={['/?categoryId=1']}>
        <HomePage />
      </MemoryRouter>,
    );

    await waitFor(() => {
      expect(screen.getByText('Recepten')).toBeInTheDocument();
    });
  });

  it('renders recipe cards after loading', async () => {
    vi.spyOn(globalThis, 'fetch').mockImplementation((url) => {
      const urlStr = typeof url === 'string' ? url : url.toString();
      if (urlStr.includes('/api/recipes/top')) {
        return Promise.resolve({
          ok: true,
          json: () => Promise.resolve(mockTopRecipes),
        } as Response);
      }
      if (urlStr.includes('/api/categories')) {
        return Promise.resolve({
          ok: true,
          json: () => Promise.resolve(mockCategories),
        } as Response);
      }
      return Promise.resolve({
        ok: true,
        json: () => Promise.resolve([]),
      } as Response);
    });

    render(
      <MemoryRouter initialEntries={['/']}>
        <HomePage />
      </MemoryRouter>,
    );

    await waitFor(() => {
      expect(screen.getByText('Chocolademousse')).toBeInTheDocument();
      expect(screen.getByText('Champignonsoep')).toBeInTheDocument();
    });
  });

  it('shows loading state initially', () => {
    vi.spyOn(globalThis, 'fetch').mockImplementation(
      () => new Promise(() => {}), // never resolves
    );

    render(
      <MemoryRouter initialEntries={['/']}>
        <HomePage />
      </MemoryRouter>,
    );

    expect(screen.getByText('Laden...')).toBeInTheDocument();
  });

  it('shows empty state when no recipes found', async () => {
    vi.spyOn(globalThis, 'fetch').mockImplementation((url) => {
      const urlStr = typeof url === 'string' ? url : url.toString();
      if (urlStr.includes('/api/recipes/top')) {
        return Promise.resolve({
          ok: true,
          json: () => Promise.resolve([]),
        } as Response);
      }
      if (urlStr.includes('/api/categories')) {
        return Promise.resolve({
          ok: true,
          json: () => Promise.resolve(mockCategories),
        } as Response);
      }
      return Promise.resolve({
        ok: true,
        json: () => Promise.resolve([]),
      } as Response);
    });

    render(
      <MemoryRouter initialEntries={['/']}>
        <HomePage />
      </MemoryRouter>,
    );

    await waitFor(() => {
      expect(screen.getByText('Geen recepten gevonden.')).toBeInTheDocument();
    });
  });

  it('has correct id attributes for e2e testing', async () => {
    vi.spyOn(globalThis, 'fetch').mockImplementation((url) => {
      const urlStr = typeof url === 'string' ? url : url.toString();
      if (urlStr.includes('/api/recipes/top')) {
        return Promise.resolve({
          ok: true,
          json: () => Promise.resolve(mockTopRecipes),
        } as Response);
      }
      if (urlStr.includes('/api/categories')) {
        return Promise.resolve({
          ok: true,
          json: () => Promise.resolve(mockCategories),
        } as Response);
      }
      return Promise.resolve({
        ok: true,
        json: () => Promise.resolve([]),
      } as Response);
    });

    render(
      <MemoryRouter initialEntries={['/']}>
        <HomePage />
      </MemoryRouter>,
    );

    expect(document.getElementById('home-page')).toBeInTheDocument();
    expect(document.getElementById('recipe-list-container')).toBeInTheDocument();

    await waitFor(() => {
      expect(document.getElementById('recipe-grid')).toBeInTheDocument();
      expect(document.getElementById('recipe-card-1')).toBeInTheDocument();
      expect(document.getElementById('recipe-title-1')).toBeInTheDocument();
    });
  });

  it('displays view counts for recipes with views', async () => {
    vi.spyOn(globalThis, 'fetch').mockImplementation((url) => {
      const urlStr = typeof url === 'string' ? url : url.toString();
      if (urlStr.includes('/api/recipes/top')) {
        return Promise.resolve({
          ok: true,
          json: () => Promise.resolve(mockTopRecipes),
        } as Response);
      }
      if (urlStr.includes('/api/categories')) {
        return Promise.resolve({
          ok: true,
          json: () => Promise.resolve(mockCategories),
        } as Response);
      }
      return Promise.resolve({
        ok: true,
        json: () => Promise.resolve([]),
      } as Response);
    });

    render(
      <MemoryRouter initialEntries={['/']}>
        <HomePage />
      </MemoryRouter>,
    );

    await waitFor(() => {
      expect(screen.getByText(/5\.432 bekeken/)).toBeInTheDocument();
    });
  });
});
