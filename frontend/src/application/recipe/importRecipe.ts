import type { ConfirmRecipeImportCommand, RecipeImageImportResult } from '../../domain/recipe/RecipeImport';
import type { RecipeDetail } from '../../domain/recipe/RecipeDetail';
import { confirmRecipeImportDto, importRecipeImageDto } from '../../infrastructure/api/client';
import { toImportConfirmRequestDto, toRecipeDetail, toRecipeImageImportResult } from '../../infrastructure/api/mappers';

export async function importRecipeImage(file: File): Promise<RecipeImageImportResult> {
  return toRecipeImageImportResult(await importRecipeImageDto(file));
}

export async function confirmRecipeImport(command: ConfirmRecipeImportCommand): Promise<RecipeDetail> {
  return toRecipeDetail(await confirmRecipeImportDto(toImportConfirmRequestDto(command)));
}
