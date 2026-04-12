// frontend/src/application/useCase/useRecipeImportWorkflow.ts
import { Recipe } from '../../domain/index';
import { ApiClient } from '../infrastructure/api/apiClient';
import { Category } from '../../domain/index';

/**
 * Manages the complex workflow for importing a recipe.
 * This service layer coordinates API calls and domain validation.
 */
export class UseRecipeImportWorkflow {
    private apiClient: ApiClient;

    constructor(apiClient: ApiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Executes the full workflow: Calls the backend API to process raw source data,
     * handles potential errors, and returns a validated domain model.
     * @param sourceData - The raw data source (e.g., base64 string, file URL)
     * @returns A fully populated and validated Recipe object.
     */
    public async execute(sourceData: string): Promise<Recipe> {
        console.log("Starting recipe import workflow...");
        
        // 1. Call the backend endpoint responsible for AI processing
        const response = await this.apiClient.processRecipeImport(sourceData);

        if (!response.success || !response.data) {
            throw new Error(`API failed to process import: ${response.message || 'Unknown error'}`);
        }

        const importedRecipe: Recipe = response.data;

        // 2. Post-processing validation/enhancement (e.g., checking for mandatory fields, enriching data)
        if (!importedRecipe.title) {
            throw new Error("Validation failed: Imported recipe must have a title.");
        }
        
        console.log("Workflow completed successfully.");
        return importedRecipe;
    }
}