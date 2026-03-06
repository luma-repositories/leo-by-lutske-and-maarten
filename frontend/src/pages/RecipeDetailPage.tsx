import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { type RecipeDetailResponse, fetchRecipe } from '../api/client';
import './RecipeDetailPage.css';

export default function RecipeDetailPage() {
  const { id } = useParams<{ id: string }>();
  const [recipe, setRecipe] = useState<RecipeDetailResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);

  useEffect(() => {
    if (!id) return;
    setLoading(true);
    setError(false);
    fetchRecipe(Number(id))
      .then(setRecipe)
      .catch(() => setError(true))
      .finally(() => setLoading(false));
  }, [id]);

  if (loading) {
    return (
      <div className="detail-page" id="recipe-detail-page">
        <p className="detail-page__loading" id="recipe-detail-loading">Laden...</p>
      </div>
    );
  }

  if (error || !recipe) {
    return (
      <div className="detail-page" id="recipe-detail-page">
        <p className="detail-page__error" id="recipe-detail-error">Recept niet gevonden.</p>
        <Link to="/" className="detail-page__back" id="recipe-detail-back-error">
          &larr; Terug naar recepten
        </Link>
      </div>
    );
  }

  return (
    <div className="detail-page" id="recipe-detail-page">
      <Link to={`/?categoryId=${recipe.categoryId}`} className="detail-page__back" id="recipe-detail-back">
        &larr; Terug naar {recipe.categoryName}
      </Link>

      <article className="detail-card" id="recipe-detail-card">
        <div className="detail-card__accent"></div>
        <div className="detail-card__body">
          <header className="detail-card__header">
            <h1 className="detail-card__title" id="recipe-detail-title">{recipe.title}</h1>
            <span className="detail-card__category" id="recipe-detail-category">
              {recipe.categoryName}
            </span>
          </header>

          <section className="detail-card__section" id="recipe-detail-ingredients-section">
            <h2 className="detail-card__section-title">Benodigdheden</h2>
            <ul className="detail-card__ingredients" id="recipe-detail-ingredients">
              {recipe.ingredients.map((item, idx) => (
                <li key={idx} className="detail-card__ingredient" id={`ingredient-${idx}`}>
                  {item}
                </li>
              ))}
            </ul>
          </section>

          <section className="detail-card__section" id="recipe-detail-preparation-section">
            <h2 className="detail-card__section-title">Bereiding</h2>
            <p className="detail-card__preparation" id="recipe-detail-preparation">
              {recipe.preparation}
            </p>
          </section>
        </div>
      </article>
    </div>
  );
}
