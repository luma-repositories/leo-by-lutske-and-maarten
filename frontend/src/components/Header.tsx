import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { fetchVersion } from '../api/client';
import './Header.css';

export default function Header() {
  const [version, setVersion] = useState<string>('');

  useEffect(() => {
    fetchVersion()
      .then((data) => setVersion(data.version))
      .catch(() => setVersion('onbekend'));
  }, []);

  return (
    <header className="header" id="app-header">
      <div className="header__inner">
        <div className="header__flag-bar" id="header-flag-bar">
          <span className="header__flag-green"></span>
          <span className="header__flag-white"></span>
          <span className="header__flag-red"></span>
        </div>
        <div className="header__content">
          <Link to="/" className="header__brand" id="header-brand">
            <h1 className="header__title">Leo Legacy</h1>
            <span className="header__subtitle">Recepten</span>
          </Link>
          {version && (
            <span className="header__version" id="app-version">
              v{version}
            </span>
          )}
        </div>
      </div>
    </header>
  );
}
