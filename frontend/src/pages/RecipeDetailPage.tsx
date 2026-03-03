import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { apiClient } from '../api/client';
import type { RecipeDetail } from '../types/api';
import { nl } from '../i18n/nl';

export function RecipeDetailPage() {
  const { id } = useParams();
  const [recipe, setRecipe] = useState<RecipeDetail | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!id) {
      return;
    }

    setLoading(true);
    apiClient
      .getRecipe(id)
      .then(setRecipe)
      .catch(() => setRecipe(null))
      .finally(() => setLoading(false));
  }, [id]);

  if (loading) {
    return <p id="recipe-loading">{nl.loading}</p>;
  }

  if (!recipe) {
    return (
      <section className="detail" id="recipe-detail-missing">
        <p>Recept niet gevonden.</p>
        <Link to="/" id="recipe-back-link-missing">{nl.backToList}</Link>
      </section>
    );
  }

  return (
    <section className="detail" id="recipe-detail">
      <Link to="/" className="back-link" id="recipe-back-link">{nl.backToList}</Link>
      <h2 className="detail-title" id="recipe-detail-title">{recipe.title}</h2>
      <p className="detail-category" id="recipe-detail-category">{recipe.category}</p>

      {recipe.description && <p className="detail-description" id="recipe-detail-description">{recipe.description}</p>}
      {recipe.servings && <p className="detail-servings" id="recipe-detail-servings">{nl.servings}: {recipe.servings}</p>}

      <article className="detail-block" id="recipe-detail-ingredients-block">
        <h3>{nl.ingredients}</h3>
        <p id="recipe-detail-ingredients">{recipe.ingredients}</p>
      </article>

      <article className="detail-block" id="recipe-detail-preparation-block">
        <h3>{nl.preparation}</h3>
        <p id="recipe-detail-preparation">{recipe.preparation}</p>
      </article>

      <p className="detail-meta" id="recipe-detail-legacy-ref">
        {nl.sourceRef}: {recipe.legacyId ?? '-'}
      </p>
      {recipe.source && <p className="detail-meta" id="recipe-detail-source">{nl.source}: {recipe.source}</p>}
      {recipe.tags && <p className="detail-meta" id="recipe-detail-tags">{nl.tags}: {recipe.tags}</p>}
    </section>
  );
}
