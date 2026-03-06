import { Routes, Route } from 'react-router-dom';
import Header from './components/Header';
import Footer from './components/Footer';
import HomePage from './pages/HomePage';
import RecipeDetailPage from './pages/RecipeDetailPage';
import './App.css';

export default function App() {
  return (
    <div className="app" id="app-root">
      <Header />
      <div className="app__content" id="app-content">
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/recipes/:id" element={<RecipeDetailPage />} />
        </Routes>
      </div>
      <Footer />
    </div>
  );
}
