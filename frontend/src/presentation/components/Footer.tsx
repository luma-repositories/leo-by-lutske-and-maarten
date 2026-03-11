import { useTranslation } from '../../shared/i18n/useTranslation';
import '../../components/Footer.css';

export default function Footer() {
  const { t } = useTranslation();

  return (
    <footer className="footer" id="app-footer">
      <div className="footer__inner">
        <div className="footer__flag-bar" id="footer-flag-bar">
          <span className="footer__flag-green"></span>
          <span className="footer__flag-white"></span>
          <span className="footer__flag-red"></span>
        </div>
        <div className="footer__content">
          <p className="footer__text" id="footer-text">
            {t('footer.tagline')}
          </p>
        </div>
      </div>
    </footer>
  );
}
