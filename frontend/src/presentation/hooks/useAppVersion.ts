import { useEffect, useState } from 'react';
import { getAppVersion } from '../../application/app/getAppVersion';

export function useAppVersion() {
  const [version, setVersion] = useState('');

  useEffect(() => {
    getAppVersion()
      .then((appVersion) => setVersion(appVersion.version))
      .catch(() => setVersion('unknown'));
  }, []);

  return version;
}
