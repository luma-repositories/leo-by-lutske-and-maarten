import { Link, useParams } from 'react-router-dom';
import { useRecipeDetail } from '../hooks/useRecipeDetail';
import { useTranslation } from '../../shared/i18n/useTranslation';
import '../../pages/RecipeDetailPage.css';

export default function RecipeDetailPage() {
  const { id } = useParams<{ id: string }>();
  const parsedId = id ? Number(id) : undefined;
  const { recipe, loading, error } = useRecipeDetail(parsedId);
  const { t } = useTranslation();

  if (loading) {
    return (
      <div className="detail-page" id="recipe-detail-page">
        <p className="detail-page__loading" id="recipe-detail-loading">{t('common.loading')}</p>
      </div>
    );
  }

  if (error || !recipe) {
    return (
      <div className="detail-page" id="recipe-detail-page">
        <p className="detail-page__error" id="recipe-detail-error">{t('detail.notFound')}</p>
        <Link to="/" className="detail-page__back" id="recipe-detail-back-error">
          {t('detail.backToRecipes')}
        </Link>
      </div>
    );
  }

  return (
    <div className="detail-page" id="recipe-detail-page">
      <Link to={`/?categoryId=${recipe.categoryId}`} className="detail-page__back" id="recipe-detail-back">
        {t('detail.backToCategory', { category: recipe.categoryName })}
      </Link>

      <article className="detail-card" id="recipe-detail-card">
        <div className="detail-card__accent"></div>
        <div className="detail-card__body">
          <header className="detail-card__header" id="recipe-detail-header">
            <h1 className="detail-card__title" id="recipe-detail-title">{recipe.title}</h1>
            <span className="detail-card__category" id="recipe-detail-category">
              {recipe.categoryName}
            </span>
          </header>

          <section className="detail-card__section" id="recipe-detail-ingredients-section">
            <h2 className="detail-card__section-title" id="recipe-detail-ingredients-title">
              {t('detail.ingredients')}
            </h2>
            <ul className="detail-card__ingredients" id="recipe-detail-ingredients">
              {recipe.ingredients.map((item, index) => (
                <li key={index} className="detail-card__ingredient" id={`ingredient-${index}`}>
                  {item}
                </li>
              ))}
            </ul>
          </section>

          <section className="detail-card__section" id="recipe-detail-preparation-section">
            <h2 className="detail-card__section-title" id="recipe-detail-preparation-title">
              {t('detail.preparation')}
            </h2>
            <p className="detail-card__preparation" id="recipe-detail-preparation">
              {recipe.preparation}
            </p>
          </section>
        </div>
      </article>
    </div>
  );
}
