import type { AppVersion } from '../../domain/app/AppVersion';
import type { ChatbotReply } from '../../domain/chatbot/ChatbotReply';
import type { Category } from '../../domain/category/Category';
import type {
  ConfirmRecipeImportCommand,
  RecipeImageImportResult,
  RecipeImportExtraction,
} from '../../domain/recipe/RecipeImport';
import type { RecipeDetail } from '../../domain/recipe/RecipeDetail';
import type { RecipeSummary } from '../../domain/recipe/RecipeSummary';
import type {
  CategoryResponseDto,
  ChatbotReplyResponseDto,
  ImportConfirmRequestDto,
  ImportExtractionResponseDto,
  RecipeDetailResponseDto,
  RecipeSummaryResponseDto,
  VersionResponseDto,
} from './client';

export function toAppVersion(dto: VersionResponseDto): AppVersion {
  return { version: dto.version };
}

export function toCategory(dto: CategoryResponseDto): Category {
  return { id: dto.id, name: dto.name, recipeCount: dto.recipeCount };
}

export function toRecipeSummary(dto: RecipeSummaryResponseDto): RecipeSummary {
  return {
    id: dto.id,
    title: dto.title,
    categoryName: dto.categoryName,
    viewCount: dto.viewCount,
  };
}

export function toChatbotReply(dto: ChatbotReplyResponseDto): ChatbotReply {
  return {
    author: dto.author,
    message: dto.message,
    recommendations: dto.recommendations.map((recommendation) => ({
      id: recommendation.recipeId,
      title: recommendation.title,
      categoryName: recommendation.categoryName,
      viewCount: 0,
      matchReason: recommendation.matchReason,
    })),
  };
}

export function toRecipeDetail(dto: RecipeDetailResponseDto): RecipeDetail {
  return {
    id: dto.id,
    title: dto.title,
    ingredients: dto.ingredients,
    preparation: dto.preparation,
    categoryId: dto.categoryId,
    categoryName: dto.categoryName,
    viewCount: dto.viewCount,
  };
}

export function toRecipeImportExtraction(dto: ImportExtractionResponseDto): RecipeImportExtraction {
  return {
    status: dto.status,
    rawModelResponse: dto.rawModelResponse,
    proposedRecipe: dto.proposedRecipe,
    missingFields: dto.missingFields,
    warnings: dto.warnings,
  };
}

export function toRecipeImageImportResult(
  result: { status: 'extracted'; data: ImportExtractionResponseDto } | { status: 'error'; message: string },
): RecipeImageImportResult {
  if (result.status === 'error') {
    return result;
  }
  return { status: 'extracted', data: toRecipeImportExtraction(result.data) };
}

export function toImportConfirmRequestDto(command: ConfirmRecipeImportCommand): ImportConfirmRequestDto {
  return {
    rawModelResponse: command.rawModelResponse,
    proposedRecipe: command.proposedRecipe,
    userOverrides: command.userOverrides,
  };
}
