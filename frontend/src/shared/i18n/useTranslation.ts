import en from '../../locales/en.json';

type NestedRecord = { [key: string]: string | NestedRecord };

function resolveTranslation(key: string): string | NestedRecord | undefined {
  const parts = key.split('.');
  let current: string | NestedRecord = en as NestedRecord;

  for (const part of parts) {
    if (typeof current === 'string') {
      return undefined;
    }
    current = current[part];
    if (current === undefined) {
      return undefined;
    }
  }

  return current;
}

export function useTranslation() {
  function t(key: string, values?: Record<string, string>): string {
    const resolved = resolveTranslation(key);
    if (typeof resolved !== 'string') {
      return key;
    }

    if (!values) {
      return resolved;
    }

    return Object.entries(values).reduce(
      (message, [token, value]) => message.replace(`{${token}}`, value),
      resolved,
    );
  }

  return { t };
}
