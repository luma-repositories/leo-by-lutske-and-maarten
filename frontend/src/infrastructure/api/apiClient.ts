import type { Category } from '../../domain/model/Category';
import type { RecipeSummary, RecipeDetail } from '../../domain/model/Recipe';
import type {
  ExtractionResult,
  ImportConfirmRequest,
  ImportResult,
} from '../../domain/model/RecipeImport';

/**
 * Infrastructure layer: HTTP API client.
 * All fetch logic is isolated here — no business logic.
 */

export interface VersionResponse {
  version: string;
}

export async function fetchVersion(): Promise<VersionResponse> {
  const res = await fetch('/api/version');
  if (!res.ok) throw new Error('Failed to fetch version');
  return res.json();
}

export async function fetchCategories(): Promise<Category[]> {
  const res = await fetch('/api/categories');
  if (!res.ok) throw new Error('Failed to fetch categories');
  return res.json();
}

export async function fetchRecipes(categoryId?: number): Promise<RecipeSummary[]> {
  const url = categoryId ? `/api/recipes?categoryId=${categoryId}` : '/api/recipes';
  const res = await fetch(url);
  if (!res.ok) throw new Error('Failed to fetch recipes');
  return res.json();
}

export async function fetchTopRecipes(): Promise<RecipeSummary[]> {
  const res = await fetch('/api/recipes/top');
  if (!res.ok) throw new Error('Failed to fetch top recipes');
  return res.json();
}

export async function fetchRecipe(id: number): Promise<RecipeDetail> {
  const res = await fetch(`/api/recipes/${id}`);
  if (!res.ok) throw new Error(`Failed to fetch recipe ${id}`);
  return res.json();
}

export async function importRecipeImage(file: File): Promise<ImportResult> {
  const formData = new FormData();
  formData.append('file', file);

  const res = await fetch('/api/recipes/import', {
    method: 'POST',
    body: formData,
  });

  if (res.status === 200) {
    const data: ExtractionResult = await res.json();
    return { status: 'extracted', data };
  }

  const errorBody = await res.json().catch(() => ({ error: 'Unknown error' }));
  return { status: 'error', message: errorBody.error || `Import failed (HTTP ${res.status})` };
}

export async function confirmRecipeImport(request: ImportConfirmRequest): Promise<RecipeDetail> {
  const res = await fetch('/api/recipes/import/confirm', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(request),
  });
  if (!res.ok) throw new Error('Failed to confirm recipe import');
  return res.json();
}
