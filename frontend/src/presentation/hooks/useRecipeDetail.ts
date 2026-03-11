import { useEffect, useReducer } from 'react';
import { getRecipeDetail } from '../../application/recipe/getRecipeDetail';
import type { RecipeDetail } from '../../domain/recipe/RecipeDetail';

interface RecipeDetailState {
  recipe: RecipeDetail | null;
  loading: boolean;
  error: boolean;
}

type RecipeDetailAction =
  | { type: 'load_started' }
  | { type: 'load_succeeded'; recipe: RecipeDetail }
  | { type: 'load_failed' };

function reducer(_state: RecipeDetailState, action: RecipeDetailAction): RecipeDetailState {
  switch (action.type) {
    case 'load_started':
      return { recipe: null, loading: true, error: false };
    case 'load_succeeded':
      return { recipe: action.recipe, loading: false, error: false };
    case 'load_failed':
      return { recipe: null, loading: false, error: true };
  }
}

export function useRecipeDetail(id?: number) {
  const [state, dispatch] = useReducer(reducer, { recipe: null, loading: true, error: false });

  useEffect(() => {
    let active = true;

    if (!id) {
      dispatch({ type: 'load_failed' });
      return () => {
        active = false;
      };
    }

    dispatch({ type: 'load_started' });
    getRecipeDetail(id)
      .then((recipe) => {
        if (active) {
          dispatch({ type: 'load_succeeded', recipe });
        }
      })
      .catch(() => {
        if (active) {
          dispatch({ type: 'load_failed' });
        }
      });

    return () => {
      active = false;
    };
  }, [id]);

  return state;
}
