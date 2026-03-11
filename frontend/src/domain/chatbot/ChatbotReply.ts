import type { RecipeSummary } from '../recipe/RecipeSummary';

export interface ChatbotRecommendation extends RecipeSummary {
  matchReason: string;
}

export interface ChatbotReply {
  author: string;
  message: string;
  recommendations: ChatbotRecommendation[];
}

export interface ChatMessage {
  id: string;
  role: 'assistant' | 'user';
  text: string;
  recommendations: ChatbotRecommendation[];
}

export interface ChatMessageContext {
  role: 'assistant' | 'user';
  message: string;
}
