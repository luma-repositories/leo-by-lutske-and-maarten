import { useEffect, useState } from 'react';
import type { Category } from '../domain/model';
import { fetchCategories } from '../infrastructure/api/apiClient';

/**
 * Application hook: manages category list state.
 */
export function useCategories() {
  const [categories, setCategories] = useState<Category[]>([]);

  useEffect(() => {
    fetchCategories()
      .then(setCategories)
      .catch(() => setCategories([]));
  }, []);

  return { categories };
}
