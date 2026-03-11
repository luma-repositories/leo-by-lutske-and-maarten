import { Link, useParams } from 'react-router-dom';
import { useRecipeDetail } from '../../application/useRecipes';
import './RecipeDetailPage.css';

export default function RecipeDetailPage() {
  const { id } = useParams<{ id: string }>();
  const { recipe, loading, error } = useRecipeDetail(id);

  if (loading) {
    return (
      <div className="detail-page" id="recipe-detail-page">
        <p className="detail-page__loading" id="recipe-detail-loading">Loading...</p>
      </div>
    );
  }

  if (error || !recipe) {
    return (
      <div className="detail-page" id="recipe-detail-page">
        <p className="detail-page__error" id="recipe-detail-error">Recipe not found.</p>
        <Link to="/" className="detail-page__back" id="recipe-detail-back-error">
          &larr; Back to recipes
        </Link>
      </div>
    );
  }

  return (
    <div className="detail-page" id="recipe-detail-page">
      <Link to={`/?categoryId=${recipe.categoryId}`} className="detail-page__back" id="recipe-detail-back">
        &larr; Back to {recipe.categoryName}
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
            <h2 className="detail-card__section-title">Ingredients</h2>
            <ul className="detail-card__ingredients" id="recipe-detail-ingredients">
              {recipe.ingredients.map((item, idx) => (
                <li key={idx} className="detail-card__ingredient" id={`ingredient-${idx}`}>
                  {item}
                </li>
              ))}
            </ul>
          </section>

          <section className="detail-card__section" id="recipe-detail-preparation-section">
            <h2 className="detail-card__section-title">Preparation</h2>
            <p className="detail-card__preparation" id="recipe-detail-preparation">
              {recipe.preparation}
            </p>
          </section>
        </div>
      </article>
    </div>
  );
}
