import { useEffect, useMemo, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { apiClient } from '../api/client';
import type { Category, RecipeSummary } from '../types/api';
import { nl } from '../i18n/nl';

export function RecipesListPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const [categories, setCategories] = useState<Category[]>([]);
  const [recipes, setRecipes] = useState<RecipeSummary[]>([]);
  const [loading, setLoading] = useState(true);
  const [hasLoadError, setHasLoadError] = useState(false);
  const [reloadToken, setReloadToken] = useState(0);

  const selectedCategory = searchParams.get('category') ?? '';

  useEffect(() => {
    apiClient.listCategories().then(setCategories).catch(() => setCategories([]));
  }, []);

  useEffect(() => {
    setLoading(true);
    setHasLoadError(false);
    apiClient
      .listRecipes(selectedCategory || undefined)
      .then((data) => setRecipes(data))
      .catch(() => {
        setRecipes([]);
        setHasLoadError(true);
      })
      .finally(() => setLoading(false));
  }, [selectedCategory, reloadToken]);

  const sortedCategories = useMemo(() => categories.slice().sort((a, b) => a.name.localeCompare(b.name)), [categories]);

  return (
    <div className="layout" id="recipes-list-layout">
      <aside className="sidebar" id="recipes-sidebar">
        <h2 className="section-title" id="categories-title">{nl.categories}</h2>
        <button
          className={`category-pill ${selectedCategory === '' ? 'active' : ''}`}
          id="category-all"
          onClick={() => setSearchParams({})}
        >
          {nl.allCategories}
        </button>
        {sortedCategories.map((category) => (
          <button
            key={category.id}
            className={`category-pill ${selectedCategory === category.slug ? 'active' : ''}`}
            id={`category-${category.slug}`}
            onClick={() => setSearchParams({ category: category.slug })}
          >
            {category.name} ({category.recipeCount})
          </button>
        ))}
      </aside>

      <section className="content" id="recipes-content">
        <div className="recipes-title-row" id="recipes-title-row">
          <h2 className="section-title" id="recipes-title">{nl.recipes}</h2>
          <Link className="import-link" id="recipes-import-link" to="/recipes/import">{nl.importRecipe}</Link>
        </div>
        {loading ? (
          <p id="recipes-loading">{nl.loading}</p>
        ) : hasLoadError ? (
          <div className="recipes-error" id="recipes-error-block">
            <p id="recipes-error-message">{nl.recipesLoadError}</p>
            <button
              className="retry-button"
              id="recipes-retry-button"
              onClick={() => setReloadToken((current) => current + 1)}
            >
              {nl.retry}
            </button>
          </div>
        ) : recipes.length === 0 ? (
          <p id="recipes-empty">{nl.noRecipes}</p>
        ) : (
          <ul className="recipe-grid" id="recipes-grid">
            {recipes.map((recipe) => (
              <li key={recipe.id} className="recipe-card" id={`recipe-card-${recipe.id}`}>
                <Link to={`/recipes/${recipe.id}`} className="recipe-link" id={`recipe-link-${recipe.id}`}>
                  <h3 className="recipe-title">{recipe.title}</h3>
                  <p className="recipe-meta">{recipe.category}</p>
                  <p className="recipe-excerpt">{recipe.excerpt}</p>
                </Link>
              </li>
            ))}
          </ul>
        )}
      </section>
    </div>
  );
}
