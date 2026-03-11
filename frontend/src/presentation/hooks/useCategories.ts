import { useEffect, useState } from 'react';
import { getCategories } from '../../application/category/getCategories';
import type { Category } from '../../domain/category/Category';

export function useCategories() {
  const [categories, setCategories] = useState<Category[]>([]);

  useEffect(() => {
    getCategories()
      .then(setCategories)
      .catch(() => setCategories([]));
  }, []);

  return categories;
}
