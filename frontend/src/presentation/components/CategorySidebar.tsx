import { Link, useSearchParams } from 'react-router-dom';
import { useCategories } from '../hooks/useCategories';
import { useTranslation } from '../../shared/i18n/useTranslation';
import '../../components/CategorySidebar.css';

export default function CategorySidebar() {
  const categories = useCategories();
  const [searchParams] = useSearchParams();
  const activeCategoryId = searchParams.get('categoryId');
  const { t } = useTranslation();

  return (
    <aside className="sidebar" id="category-sidebar">
      <h2 className="sidebar__title" id="sidebar-title">{t('sidebar.title')}</h2>
      <nav className="sidebar__nav" id="sidebar-nav">
        <ul className="sidebar__list" id="category-list">
          <li className="sidebar__item" id="category-item-all">
            <Link
              to="/"
              className={`sidebar__link ${!activeCategoryId ? 'sidebar__link--active' : ''}`}
              id="category-link-all"
            >
              {t('sidebar.allRecipes')}
            </Link>
          </li>
          {categories.map((category) => (
            <li className="sidebar__item" key={category.id} id={`category-item-${category.id}`}>
              <Link
                to={`/?categoryId=${category.id}`}
                className={`sidebar__link ${activeCategoryId === String(category.id) ? 'sidebar__link--active' : ''}`}
                id={`category-link-${category.id}`}
              >
                {category.name}
                <span className="sidebar__count" id={`category-count-${category.id}`}>
                  {category.recipeCount}
                </span>
              </Link>
            </li>
          ))}
        </ul>
      </nav>
    </aside>
  );
}
