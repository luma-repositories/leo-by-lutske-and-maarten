// frontend/src/infrastructure/api/apiClient.ts
import { Recipe, ApiResponse } from '../domain';

// A wrapper class to handle all backend communication.
export class ApiClient {
    private baseUrl: string;

    constructor(baseUrl: string = '/api/v1') {
        this.baseUrl = baseUrl;
    }

    /**
     * Mock implementation simulating a call to the backend's AI ingestion endpoint 
     * (e.g., POST /api/v1/recipes/import).
     * @param sourceData The raw string data to process.
     * @returns A promise resolving to the ApiResponse containing the Domain Recipe object.
     */
    public async processRecipeImport(sourceData: string): Promise<ApiResponse<Recipe>> {
        console.log(`[API Client] Sending ${sourceData.substring(0, 30)}... to backend for AI processing.`);
        
        // --- MOCKING BEHAVIOR ---
        // In a real implementation, this would be an Axios/Fetch call:
        // const response = await fetch(`${this.baseUrl}/recipes/import`, { method: 'POST', body: sourceData, headers: ... });
        // return response.json();
        
        await new Promise(resolve => setTimeout(resolve, 200)); // Simulate network latency

        // Mock success payload structure matching backend domain output
        const mockRecipe: Recipe = {
            id: 1001,
            title: "Mocked AI Generated Recipe Title",
            ingredientsContent: "Mock ingredients list from AI analysis.",
            preparationContent: "Detailed steps based on OCR/image text.",
            category: { id: 1, name: "Imported" },
            viewCount: 0,
            source: "MockImage.png",
            createdAt: new Date(),
        };

        return {
            success: true,
            data: mockRecipe,
            message: "Recipe processed and mapped successfully."
        };
    }

    /**
     * Mock retrieval of a recipe detail by ID.
     */
    public async getRecipe(id: number): Promise<ApiResponse<Recipe>> {
        console.log(`[API Client] Fetching recipe ${id}...`);
        await new Promise(resolve => setTimeout(resolve, 50)); // Simulate network latency

        const mockRecipe: Recipe = {
            id: id,
            title: "The Masterpiece Recipe",
            ingredientsContent: "Mock ingredients.",
            preparationContent: "Mock steps.",
            category: { id: 1, name: "Italian" },
            viewCount: 5, // Mocking pre-incremented view count
            source: "UserEntry",
            createdAt: new Date(),
        };

        return {
            success: true,
            data: mockRecipe,
            message: "Recipe details fetched successfully."
        };
    }
}