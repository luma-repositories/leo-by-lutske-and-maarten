import { useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { type RecipeSummaryResponse, fetchRecipes, fetchTopRecipes } from '../api/client';
import CategorySidebar from '../components/CategorySidebar';
import './HomePage.css';

export default function HomePage() {
  const [recipes, setRecipes] = useState<RecipeSummaryResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchParams] = useSearchParams();
  const categoryId = searchParams.get('categoryId');

  useEffect(() => {
    const fetcher = categoryId
      ? fetchRecipes(Number(categoryId))
      : fetchTopRecipes();

    fetcher
      .then(setRecipes)
      .catch(() => setRecipes([]))
      .finally(() => setLoading(false));
  }, [categoryId]);

  return (
    <div className="home" id="home-page">
      <div className="home__sidebar">
        <CategorySidebar />
      </div>
      <main className="home__main" id="recipe-list-container">
        <h2 className="home__heading" id="recipe-list-heading">
          {categoryId ? 'Recepten' : 'Populairste recepten'}
        </h2>
        {loading ? (
          <p className="home__loading" id="recipe-list-loading">Laden...</p>
        ) : recipes.length === 0 ? (
          <p className="home__empty" id="recipe-list-empty">Geen recepten gevonden.</p>
        ) : (
          <ul className="recipe-grid" id="recipe-grid">
            {recipes.map((recipe) => (
              <li key={recipe.id} className="recipe-card" id={`recipe-card-${recipe.id}`}>
                <Link to={`/recipes/${recipe.id}`} className="recipe-card__link" id={`recipe-link-${recipe.id}`}>
                  <div className="recipe-card__accent"></div>
                  <div className="recipe-card__body">
                    <h3 className="recipe-card__title" id={`recipe-title-${recipe.id}`}>
                      {recipe.title}
                    </h3>
                    <div className="recipe-card__meta">
                      <span className="recipe-card__category" id={`recipe-category-${recipe.id}`}>
                        {recipe.categoryName}
                      </span>
                      {recipe.viewCount > 0 && (
                        <span className="recipe-card__views" id={`recipe-views-${recipe.id}`}>
                          {recipe.viewCount.toLocaleString('nl-BE')} bekeken
                        </span>
                      )}
                    </div>
                  </div>
                </Link>
              </li>
            ))}
          </ul>
        )}
      </main>
    </div>
  );
}
