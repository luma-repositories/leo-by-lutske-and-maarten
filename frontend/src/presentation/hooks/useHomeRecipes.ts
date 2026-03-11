import { useEffect, useReducer } from 'react';
import { getRecipeCollection } from '../../application/recipe/getRecipeCollection';
import type { RecipeSummary } from '../../domain/recipe/RecipeSummary';

interface HomeRecipesState {
  recipes: RecipeSummary[];
  loading: boolean;
}

type HomeRecipesAction =
  | { type: 'load_started' }
  | { type: 'load_succeeded'; recipes: RecipeSummary[] }
  | { type: 'load_failed' };

function reducer(state: HomeRecipesState, action: HomeRecipesAction): HomeRecipesState {
  switch (action.type) {
    case 'load_started':
      return { ...state, loading: true };
    case 'load_succeeded':
      return { recipes: action.recipes, loading: false };
    case 'load_failed':
      return { recipes: [], loading: false };
  }
}

export function useHomeRecipes(categoryId?: number) {
  const [state, dispatch] = useReducer(reducer, { recipes: [], loading: true });

  useEffect(() => {
    let active = true;
    dispatch({ type: 'load_started' });

    getRecipeCollection(categoryId)
      .then((recipes) => {
        if (active) {
          dispatch({ type: 'load_succeeded', recipes });
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
  }, [categoryId]);

  return state;
}
