// frontend/src/domain/index.ts
// Defines core, UI-agnostic types used across the entire frontend application.

export type CategoryId = string | number;

export interface Category {
    id: CategoryId;
    name: string;
}

export interface Recipe {
    // Use string type for content to mimic unstructured text from AI, 
    // relying on explicit mapping instead of complex object shapes.
    id: string | number | null; 
    title: string;
    ingredientsContent: string;
    preparationContent: string;
    category: Category;
    viewCount: number;
    source: string;
    createdAt: Date;
}

// API response/payload standard type
export interface ApiResponse<T> {
    success: boolean;
    data: T;
    message: string;
}