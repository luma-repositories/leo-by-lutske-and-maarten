import type {
  Category,
  NeedsMoreInfoResponse,
  ProposedRecipe,
  RecipeDetail,
  RecipeImportResult,
  RecipeSummary,
  UserOverrides,
  VersionResponse
} from '../types/api';

async function getJson<T>(path: string): Promise<T> {
  const response = await fetch(path);
  if (!response.ok) {
    throw new Error(await getApiErrorMessage(response, `Request failed: ${response.status}`));
  }
  return response.json() as Promise<T>;
}

async function getApiErrorMessage(response: Response, fallback: string): Promise<string> {
  try {
    const payload = (await response.json()) as { message?: string };
    if (payload.message && payload.message.trim().length > 0) {
      return payload.message;
    }
    return fallback;
  } catch {
    return fallback;
  }
}

export const apiClient = {
  listCategories: (): Promise<Category[]> => getJson<Category[]>('/api/categories'),
  listRecipes: (categorySlug?: string): Promise<RecipeSummary[]> => {
    const params = categorySlug ? `?category=${encodeURIComponent(categorySlug)}` : '';
    return getJson<RecipeSummary[]>(`/api/recipes${params}`);
  },
  getRecipe: (id: string): Promise<RecipeDetail> => getJson<RecipeDetail>(`/api/recipes/${id}`),
  getVersion: (): Promise<VersionResponse> => getJson<VersionResponse>('/api/version'),
  importRecipe: async (file: File): Promise<RecipeImportResult> => {
    const formData = new FormData();
    formData.append('file', file);

    const response = await fetch('/api/recipes/import', {
      method: 'POST',
      body: formData
    });

    if (response.status === 201) {
      return { kind: 'created', recipe: (await response.json()) as RecipeDetail };
    }

    if (response.status === 422) {
      return { kind: 'needs_more_info', payload: (await response.json()) as NeedsMoreInfoResponse };
    }

    throw new Error(await getApiErrorMessage(response, `Import failed: ${response.status}`));
  },
  confirmImportedRecipe: async (
    rawText: string,
    proposedRecipe: ProposedRecipe,
    userOverrides: UserOverrides
  ): Promise<RecipeImportResult> => {
    const response = await fetch('/api/recipes/import/confirm', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ rawText, proposedRecipe, userOverrides })
    });

    if (response.status === 201) {
      return { kind: 'created', recipe: (await response.json()) as RecipeDetail };
    }

    if (response.status === 422) {
      return { kind: 'needs_more_info', payload: (await response.json()) as NeedsMoreInfoResponse };
    }

    throw new Error(await getApiErrorMessage(response, `Import confirmation failed: ${response.status}`));
  }
};
