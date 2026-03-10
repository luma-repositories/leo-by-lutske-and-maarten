import { useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { type CategoryResponse, fetchCategories } from '../api/client';
import './CategorySidebar.css';

export default function CategorySidebar() {
  const [categories, setCategories] = useState<CategoryResponse[]>([]);
  const [searchParams] = useSearchParams();
  const activeCategoryId = searchParams.get('categoryId');

  useEffect(() => {
    fetchCategories()
      .then(setCategories)
      .catch(() => setCategories([]));
  }, []);

  return (
    <aside className="sidebar" id="category-sidebar">
      <h2 className="sidebar__title" id="sidebar-title">Recipe Book</h2>
      <nav className="sidebar__nav">
        <ul className="sidebar__list" id="category-list">
          <li className="sidebar__item" id="category-item-all">
            <Link
              to="/"
              className={`sidebar__link ${!activeCategoryId ? 'sidebar__link--active' : ''}`}
              id="category-link-all"
            >
              All Recipes
            </Link>
          </li>
          {categories.map((cat) => (
            <li className="sidebar__item" key={cat.id} id={`category-item-${cat.id}`}>
              <Link
                to={`/?categoryId=${cat.id}`}
                className={`sidebar__link ${activeCategoryId === String(cat.id) ? 'sidebar__link--active' : ''}`}
                id={`category-link-${cat.id}`}
              >
                {cat.name}
                <span className="sidebar__count" id={`category-count-${cat.id}`}>
                  {cat.recipeCount}
                </span>
              </Link>
            </li>
          ))}
        </ul>
      </nav>
    </aside>
  );
}
