import { Routes, Route } from 'react-router-dom';
import Header from './presentation/components/Header';
import Footer from './presentation/components/Footer';
import HomePage from './presentation/pages/HomePage';
import RecipeDetailPage from './presentation/pages/RecipeDetailPage';
import ImportRecipePage from './presentation/pages/ImportRecipePage';
import './App.css';

export default function App() {
  return (
    <div className="app" id="app-root">
      <Header />
      <div className="app__content" id="app-content">
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/recipes/:id" element={<RecipeDetailPage />} />
          <Route path="/import" element={<ImportRecipePage />} />
        </Routes>
      </div>
      <Footer />
    </div>
  );
}
