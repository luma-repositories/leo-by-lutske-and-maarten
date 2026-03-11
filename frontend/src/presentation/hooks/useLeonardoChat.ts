import { useMemo, useState } from 'react';
import { chatWithLeonardo } from '../../application/chatbot/chatWithLeonardo';
import type { ChatMessage, ChatMessageContext } from '../../domain/chatbot/ChatbotReply';

interface LeonardoChatState {
  messages: ChatMessage[];
  loading: boolean;
  error: boolean;
  sendMessage: (message: string) => Promise<void>;
}

function createInitialAssistantMessage(initialText: string): ChatMessage {
  return {
    id: 'assistant-initial',
    role: 'assistant',
    text: initialText,
    recommendations: [],
  };
}

export function useLeonardoChat(initialText: string, fallbackErrorText: string): LeonardoChatState {
  const initialMessage = useMemo(() => createInitialAssistantMessage(initialText), [initialText]);
  const [messages, setMessages] = useState<ChatMessage[]>([initialMessage]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(false);

  async function sendMessage(message: string) {
    const trimmedMessage = message.trim();
    if (!trimmedMessage) {
      return;
    }

    setLoading(true);
    setError(false);

    setMessages((currentMessages) => [
      ...currentMessages,
      {
        id: `user-${currentMessages.length + 1}`,
        role: 'user',
        text: trimmedMessage,
        recommendations: [],
      },
    ]);

    try {
      const context: ChatMessageContext[] = messages
        .filter((entry) => entry.role === 'user')
        .slice(-3)
        .map((entry) => ({ role: entry.role, message: entry.text }));
      const reply = await chatWithLeonardo(trimmedMessage, context);
      setMessages((currentMessages) => [
        ...currentMessages,
        {
          id: `assistant-${currentMessages.length + 1}`,
          role: 'assistant',
          text: reply.message,
          recommendations: reply.recommendations,
        },
      ]);
    } catch {
      setError(true);
      setMessages((currentMessages) => [
        ...currentMessages,
        {
          id: `assistant-${currentMessages.length + 1}`,
          role: 'assistant',
          text: fallbackErrorText,
          recommendations: [],
        },
      ]);
    } finally {
      setLoading(false);
    }
  }

  return {
    messages,
    loading,
    error,
    sendMessage,
  };
}
