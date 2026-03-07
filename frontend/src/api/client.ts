/** API response types matching the Quarkus REST DTOs. */

export interface CategoryResponse {
  id: number;
  name: string;
  recipeCount: number;
}

export interface RecipeSummaryResponse {
  id: number;
  title: string;
  categoryName: string;
  viewCount: number;
}

export interface RecipeDetailResponse {
  id: number;
  title: string;
  ingredients: string[];
  preparation: string;
  categoryId: number;
  categoryName: string;
  viewCount: number;
}

export interface VersionResponse {
  version: string;
}

/** Fetch helpers — all paths are same-origin (/api/...) */

export async function fetchVersion(): Promise<VersionResponse> {
  const res = await fetch('/api/version');
  if (!res.ok) throw new Error('Failed to fetch version');
  return res.json();
}

export async function fetchCategories(): Promise<CategoryResponse[]> {
  const res = await fetch('/api/categories');
  if (!res.ok) throw new Error('Failed to fetch categories');
  return res.json();
}

export async function fetchRecipes(categoryId?: number): Promise<RecipeSummaryResponse[]> {
  const url = categoryId ? `/api/recipes?categoryId=${categoryId}` : '/api/recipes';
  const res = await fetch(url);
  if (!res.ok) throw new Error('Failed to fetch recipes');
  return res.json();
}

export async function fetchTopRecipes(): Promise<RecipeSummaryResponse[]> {
  const res = await fetch('/api/recipes/top');
  if (!res.ok) throw new Error('Failed to fetch top recipes');
  return res.json();
}

export async function fetchRecipe(id: number): Promise<RecipeDetailResponse> {
  const res = await fetch(`/api/recipes/${id}`);
  if (!res.ok) throw new Error(`Failed to fetch recipe ${id}`);
  return res.json();
}

/** --- Recipe Import types --- */

export interface ProposedRecipeDto {
  title?: string | null;
  ingredients?: string[] | null;
  preparation?: string | null;
  categoryId?: number | null;
  notes?: string | null;
}

export interface ImportNeedsMoreInfoResponse {
  status: 'NEEDS_MORE_INFO';
  rawText: string;
  proposedRecipe: ProposedRecipeDto;
  missingFields: string[];
  parseWarnings: string[];
}

export interface UserOverrides {
  title?: string | null;
  ingredients?: string[] | null;
  preparation?: string | null;
  categoryId?: number | null;
  notes?: string | null;
}

export interface ImportConfirmRequest {
  rawText: string;
  proposedRecipe: ProposedRecipeDto;
  userOverrides?: UserOverrides | null;
}

export type ImportResult =
  | { status: 'created'; recipe: RecipeDetailResponse }
  | { status: 'needs_more_info'; data: ImportNeedsMoreInfoResponse }
  | { status: 'error'; message: string };

/** Upload an image for OCR recipe import. */
export async function importRecipeImage(file: File): Promise<ImportResult> {
  const formData = new FormData();
  formData.append('file', file);

  const res = await fetch('/api/recipes/import', {
    method: 'POST',
    body: formData,
  });

  if (res.status === 201) {
    const recipe: RecipeDetailResponse = await res.json();
    return { status: 'created', recipe };
  }

  if (res.status === 422) {
    const data: ImportNeedsMoreInfoResponse = await res.json();
    return { status: 'needs_more_info', data };
  }

  const errorBody = await res.json().catch(() => ({ error: 'Unknown error' }));
  return { status: 'error', message: errorBody.error || `Import failed (HTTP ${res.status})` };
}

/** Confirm and finalize an imported recipe with user-provided data. */
export async function confirmRecipeImport(request: ImportConfirmRequest): Promise<RecipeDetailResponse> {
  const res = await fetch('/api/recipes/import/confirm', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(request),
  });
  if (!res.ok) throw new Error('Failed to confirm recipe import');
  return res.json();
}
