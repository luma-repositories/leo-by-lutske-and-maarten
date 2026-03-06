import { render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import Header from './Header';

describe('Header', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
  });

  it('renders the brand title', () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue({
      ok: true,
      json: () => Promise.resolve({ version: '1.0.0' }),
    } as Response);

    render(
      <MemoryRouter>
        <Header />
      </MemoryRouter>,
    );

    expect(screen.getByText('Leo Legacy')).toBeInTheDocument();
    expect(screen.getByText('Recepten')).toBeInTheDocument();
  });

  it('has correct id and class attributes for e2e testing', () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue({
      ok: true,
      json: () => Promise.resolve({ version: '1.0.0' }),
    } as Response);

    render(
      <MemoryRouter>
        <Header />
      </MemoryRouter>,
    );

    expect(document.getElementById('app-header')).toBeInTheDocument();
    expect(document.getElementById('header-brand')).toBeInTheDocument();
    expect(document.getElementById('header-flag-bar')).toBeInTheDocument();
  });

  it('displays the version after fetching', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue({
      ok: true,
      json: () => Promise.resolve({ version: '2.5.0' }),
    } as Response);

    render(
      <MemoryRouter>
        <Header />
      </MemoryRouter>,
    );

    await waitFor(() => {
      expect(screen.getByText('v2.5.0')).toBeInTheDocument();
    });

    expect(document.getElementById('app-version')).toBeInTheDocument();
  });

  it('displays "onbekend" when version fetch fails', async () => {
    vi.spyOn(globalThis, 'fetch').mockRejectedValue(new Error('Network error'));

    render(
      <MemoryRouter>
        <Header />
      </MemoryRouter>,
    );

    await waitFor(() => {
      expect(screen.getByText('vonbekend')).toBeInTheDocument();
    });
  });

  it('renders the Italian flag bar with three stripes', () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue({
      ok: true,
      json: () => Promise.resolve({ version: '1.0.0' }),
    } as Response);

    render(
      <MemoryRouter>
        <Header />
      </MemoryRouter>,
    );

    const flagBar = document.getElementById('header-flag-bar');
    expect(flagBar).toBeInTheDocument();
    expect(flagBar?.children).toHaveLength(3);
  });

  it('has a link to the homepage', () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue({
      ok: true,
      json: () => Promise.resolve({ version: '1.0.0' }),
    } as Response);

    render(
      <MemoryRouter>
        <Header />
      </MemoryRouter>,
    );

    const brandLink = document.getElementById('header-brand') as HTMLAnchorElement;
    expect(brandLink).toBeInTheDocument();
    expect(brandLink.getAttribute('href')).toBe('/');
  });
});
