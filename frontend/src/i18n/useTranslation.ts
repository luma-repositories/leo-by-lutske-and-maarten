import nl from '../locales/nl.json';

type NestedRecord = { [key: string]: string | NestedRecord };

/**
 * Simple i18n hook for the Leo Legacy application.
 *
 * Returns a `t(key)` function that resolves dot-separated keys
 * against the Dutch locale file (e.g. "import.pageTitle").
 *
 * Usage:
 *   const { t } = useTranslation();
 *   <h1>{t('import.pageTitle')}</h1>
 */
export function useTranslation() {
  function t(key: string): string {
    const parts = key.split('.');
    let current: string | NestedRecord = nl as NestedRecord;

    for (const part of parts) {
      if (typeof current === 'string') return key; // key too deep
      current = (current as NestedRecord)[part];
      if (current === undefined) return key; // key not found — return key as fallback
    }

    return typeof current === 'string' ? current : key;
  }

  return { t };
}
