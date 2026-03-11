import type { ChatbotReply, ChatMessageContext } from '../../domain/chatbot/ChatbotReply';
import { fetchChatbotReplyDto } from '../../infrastructure/api/client';
import { toChatbotReply } from '../../infrastructure/api/mappers';

export async function chatWithLeonardo(message: string, context: ChatMessageContext[]): Promise<ChatbotReply> {
  const dto = await fetchChatbotReplyDto(message, context);
  return toChatbotReply(dto);
}
