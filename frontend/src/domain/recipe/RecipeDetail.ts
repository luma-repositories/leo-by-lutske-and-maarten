export interface RecipeDetail {
  id: number;
  title: string;
  ingredients: string[];
  preparation: string;
  categoryId: number;
  categoryName: string;
  viewCount: number;
}
