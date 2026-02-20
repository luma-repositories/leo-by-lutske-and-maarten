import { useEffect, useState } from 'react';
import { Navigate, Route, Routes } from 'react-router-dom';
import { apiClient } from './api/client';
import { nl } from './i18n/nl';
import { RecipeDetailPage } from './pages/RecipeDetailPage';
import { RecipesListPage } from './pages/RecipesListPage';

function App() {
  const [version, setVersion] = useState<string>(nl.unknownVersion);

  useEffect(() => {
    apiClient
      .getVersion()
      .then((result) => setVersion(result.version || nl.unknownVersion))
      .catch(() => setVersion(nl.unknownVersion));
  }, []);

  return (
    <main className="app-shell" id="app-shell">
      <header className="app-header" id="app-header">
        <div className="brand-mark" id="brand-mark" aria-hidden="true" />
        <div className="title-wrap" id="title-wrap">
          <h1 id="app-title">{nl.appTitle}</h1>
          <p id="app-subtitle">{nl.subtitle}</p>
        </div>
      </header>

      <Routes>
        <Route path="/" element={<RecipesListPage />} />
        <Route path="/recipes/:id" element={<RecipeDetailPage />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>

      <footer className="app-footer" id="app-footer">
        <span id="app-version-label">API versie:</span>
        <strong id="app-version-value">{version}</strong>
      </footer>
    </main>
  );
}

export default App;
