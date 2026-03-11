import type { ChatMessageContext } from '../../domain/chatbot/ChatbotReply';

export interface CategoryResponseDto {
  id: number;
  name: string;
  recipeCount: number;
}

export interface RecipeSummaryResponseDto {
  id: number;
  title: string;
  categoryName: string;
  viewCount: number;
}

export interface RecipeDetailResponseDto {
  id: number;
  title: string;
  ingredients: string[];
  preparation: string;
  categoryId: number;
  categoryName: string;
  viewCount: number;
}

export interface VersionResponseDto {
  version: string;
}

export interface ChatbotRecommendationDto {
  recipeId: number;
  title: string;
  categoryName: string;
  matchReason: string;
}

export interface ChatbotReplyResponseDto {
  author: string;
  message: string;
  recommendations: ChatbotRecommendationDto[];
}

export interface ChatbotMessageContextDto {
  role: string;
  message: string;
}

export interface ProposedRecipeDto {
  title?: string | null;
  description?: string | null;
  servings?: string | null;
  ingredients?: string[] | null;
  preparation?: string | null;
  categoryId?: number | null;
  notes?: string | null;
  source?: string | null;
  tags?: string[] | null;
}

export interface ImportExtractionResponseDto {
  status: 'COMPLETE' | 'NEEDS_MORE_INFO';
  rawModelResponse?: string | null;
  proposedRecipe: ProposedRecipeDto;
  missingFields: string[];
  warnings: string[];
}

export interface UserOverridesDto {
  title?: string | null;
  ingredients?: string[] | null;
  preparation?: string | null;
  categoryId?: number | null;
  notes?: string | null;
  servings?: string | null;
  description?: string | null;
}

export interface ImportConfirmRequestDto {
  rawModelResponse?: string | null;
  proposedRecipe: ProposedRecipeDto;
  userOverrides?: UserOverridesDto | null;
}

export async function fetchVersionDto(): Promise<VersionResponseDto> {
  const response = await fetch('/api/version');
  if (!response.ok) {
    throw new Error('Failed to fetch version');
  }
  return response.json();
}

export async function fetchCategoriesDto(): Promise<CategoryResponseDto[]> {
  const response = await fetch('/api/categories');
  if (!response.ok) {
    throw new Error('Failed to fetch categories');
  }
  return response.json();
}

export async function fetchRecipesDto(categoryId?: number): Promise<RecipeSummaryResponseDto[]> {
  const url = categoryId ? `/api/recipes?categoryId=${categoryId}` : '/api/recipes';
  const response = await fetch(url);
  if (!response.ok) {
    throw new Error('Failed to fetch recipes');
  }
  return response.json();
}

export async function fetchTopRecipesDto(): Promise<RecipeSummaryResponseDto[]> {
  const response = await fetch('/api/recipes/top');
  if (!response.ok) {
    throw new Error('Failed to fetch top recipes');
  }
  return response.json();
}

export async function fetchRecipeDto(id: number): Promise<RecipeDetailResponseDto> {
  const response = await fetch(`/api/recipes/${id}`);
  if (!response.ok) {
    throw new Error(`Failed to fetch recipe ${id}`);
  }
  return response.json();
}

export async function fetchChatbotReplyDto(message: string, context: ChatMessageContext[]): Promise<ChatbotReplyResponseDto> {
  const response = await fetch('/api/chatbot/messages', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ message, context }),
  });

  if (!response.ok) {
    throw new Error('Failed to fetch chatbot reply');
  }

  return response.json();
}

export async function importRecipeImageDto(file: File): Promise<
  { status: 'extracted'; data: ImportExtractionResponseDto } | { status: 'error'; message: string }
> {
  const formData = new FormData();
  formData.append('file', file);

  const response = await fetch('/api/recipes/import', {
    method: 'POST',
    body: formData,
  });

  if (response.status === 200) {
    return { status: 'extracted', data: await response.json() };
  }

  const errorBody = await response.json().catch(() => ({ error: 'Unknown error' }));
  return { status: 'error', message: errorBody.error || `Import failed (HTTP ${response.status})` };
}

export async function confirmRecipeImportDto(request: ImportConfirmRequestDto): Promise<RecipeDetailResponseDto> {
  const response = await fetch('/api/recipes/import/confirm', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(request),
  });
  if (!response.ok) {
    throw new Error('Failed to confirm recipe import');
  }
  return response.json();
}
