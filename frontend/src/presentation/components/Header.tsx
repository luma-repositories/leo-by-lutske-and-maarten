import { Link } from 'react-router-dom';
import { useAppVersion } from '../hooks/useAppVersion';
import { useTranslation } from '../../shared/i18n/useTranslation';
import '../../components/Header.css';

export default function Header() {
  const version = useAppVersion();
  const { t } = useTranslation();

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
            <span className="header__subtitle">{t('header.subtitle')}</span>
          </Link>
          <div className="header__actions">
            <Link to="/import" className="header__import-btn" id="header-import-btn">
              {t('header.import')}
            </Link>
            {version && (
              <span className="header__version" id="app-version">
                v{version}
              </span>
            )}
          </div>
        </div>
      </div>
    </header>
  );
}
