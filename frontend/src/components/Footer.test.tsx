import { render, screen } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import Footer from './Footer';

describe('Footer', () => {
  it('renders the footer text', () => {
    render(<Footer />);

    expect(
      screen.getByText(/Leo Legacy/),
    ).toBeInTheDocument();
    expect(
      screen.getByText(/een kooksite voor lekkerbekken/),
    ).toBeInTheDocument();
  });

  it('has correct id attributes for e2e testing', () => {
    render(<Footer />);

    expect(document.getElementById('app-footer')).toBeInTheDocument();
    expect(document.getElementById('footer-text')).toBeInTheDocument();
    expect(document.getElementById('footer-flag-bar')).toBeInTheDocument();
  });

  it('renders the Italian flag bar with three stripes', () => {
    render(<Footer />);

    const flagBar = document.getElementById('footer-flag-bar');
    expect(flagBar).toBeInTheDocument();
    expect(flagBar?.children).toHaveLength(3);
  });
});
