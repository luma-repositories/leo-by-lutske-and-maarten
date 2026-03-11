import type { AppVersion } from '../../domain/app/AppVersion';
import { toAppVersion } from '../../infrastructure/api/mappers';
import { fetchVersionDto } from '../../infrastructure/api/client';

export async function getAppVersion(): Promise<AppVersion> {
  return toAppVersion(await fetchVersionDto());
}
