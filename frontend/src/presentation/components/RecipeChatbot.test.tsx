import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import RecipeChatbot from './RecipeChatbot';

describe('RecipeChatbot', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
  });

  it('introduces Leonardo on first render', () => {
    render(
      <MemoryRouter>
        <RecipeChatbot />
      </MemoryRouter>,
    );

    expect(screen.getByText('Meet Leonardo')).toBeInTheDocument();
    expect(screen.getByText('Hello, I am Leonardo. Tell me what you feel like eating and I will help you find a recipe.')).toBeInTheDocument();
  });

  it('sends a question and renders recipe recommendations with links', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue({
      ok: true,
      json: () => Promise.resolve({
        author: 'Leonardo',
        message: 'Here are tomato-based ideas.',
        recommendations: [
          { recipeId: 37, title: 'Pizza with mushrooms', categoryName: 'Pasta', matchReason: 'Matches: tomato.' },
        ],
      }),
    } as Response);

    const user = userEvent.setup();
    render(
      <MemoryRouter>
        <RecipeChatbot />
      </MemoryRouter>,
    );

    await user.type(screen.getByLabelText('What are you in the mood for?'), 'I would like a tomato based dish');
    await user.click(screen.getByRole('button', { name: 'Ask Leonardo' }));

    await waitFor(() => {
      expect(screen.getByText('Here are tomato-based ideas.')).toBeInTheDocument();
    });

    const recommendationLink = document.getElementById('recipe-chatbot-recommendation-link-37') as HTMLAnchorElement;
    expect(recommendationLink).toBeInTheDocument();
    expect(recommendationLink.getAttribute('href')).toBe('/recipes/37');
    expect(screen.getByText('Pizza with mushrooms')).toBeInTheDocument();
    expect(screen.getByText('Matches: tomato.')).toBeInTheDocument();
  });

  it('has ids for e2e testing hooks', () => {
    render(
      <MemoryRouter>
        <RecipeChatbot />
      </MemoryRouter>,
    );

    expect(document.getElementById('recipe-chatbot')).toBeInTheDocument();
    expect(document.getElementById('recipe-chatbot-form')).toBeInTheDocument();
    expect(document.getElementById('recipe-chatbot-input')).toBeInTheDocument();
    expect(document.getElementById('recipe-chatbot-submit')).toBeInTheDocument();
    expect(document.getElementById('recipe-chatbot-suggestions')).toBeInTheDocument();
    expect(document.getElementById('recipe-chatbot-suggestion-0')).toBeInTheDocument();
  });

  it('sends a predefined suggestion chip when clicked', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue({
      ok: true,
      json: () => Promise.resolve({
        author: 'Leonardo',
        message: 'Here is a spicy idea.',
        recommendations: [
          { recipeId: 32, title: 'Devilishly delicious turkey drumsticks', categoryName: 'Main dishes', matchReason: 'Matches: spicy.' },
        ],
      }),
    } as Response);

    const user = userEvent.setup();
    render(
      <MemoryRouter>
        <RecipeChatbot />
      </MemoryRouter>,
    );

    await user.click(screen.getByRole('button', { name: 'Give me something spicy' }));

    await waitFor(() => {
      expect(screen.getByText('Here is a spicy idea.')).toBeInTheDocument();
    });

    expect(fetch).toHaveBeenCalledWith('/api/chatbot/messages', expect.objectContaining({
      method: 'POST',
    }));
  });

  it('sends recent user context for follow-up questions', async () => {
    vi.spyOn(globalThis, 'fetch')
      .mockResolvedValueOnce({
        ok: true,
        json: () => Promise.resolve({
          author: 'Leonardo',
          message: 'Here are tomato-based ideas.',
          recommendations: [],
        }),
      } as Response)
      .mockResolvedValueOnce({
        ok: true,
        json: () => Promise.resolve({
          author: 'Leonardo',
          message: 'Here is a vegetarian one.',
          recommendations: [],
        }),
      } as Response);

    const user = userEvent.setup();
    render(
      <MemoryRouter>
        <RecipeChatbot />
      </MemoryRouter>,
    );

    await user.type(screen.getByLabelText('What are you in the mood for?'), 'I would like a tomato based dish');
    await user.click(screen.getByRole('button', { name: 'Ask Leonardo' }));
    await waitFor(() => expect(screen.getByText('Here are tomato-based ideas.')).toBeInTheDocument());

    await user.type(screen.getByLabelText('What are you in the mood for?'), 'make it vegetarian');
    await user.click(screen.getByRole('button', { name: 'Ask Leonardo' }));
    await waitFor(() => expect(screen.getByText('Here is a vegetarian one.')).toBeInTheDocument());

    expect(fetch).toHaveBeenLastCalledWith('/api/chatbot/messages', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        message: 'make it vegetarian',
        context: [{ role: 'user', message: 'I would like a tomato based dish' }],
      }),
    });
  });
});
