import { useState } from 'react';
import type { FormEvent } from 'react';
import { Link } from 'react-router-dom';
import { useLeonardoChat } from '../hooks/useLeonardoChat';
import { useTranslation } from '../../shared/i18n/useTranslation';
import '../../components/RecipeChatbot.css';

export default function RecipeChatbot() {
  const { t } = useTranslation();
  const { messages, loading, error, sendMessage } = useLeonardoChat(
    t('chatbot.initialMessage'),
    t('chatbot.error'),
  );
  const [draft, setDraft] = useState('');
  const suggestionPrompts = [
    t('chatbot.suggestions.tomato'),
    t('chatbot.suggestions.chicken'),
    t('chatbot.suggestions.veggie'),
    t('chatbot.suggestions.light'),
    t('chatbot.suggestions.spicy'),
    t('chatbot.suggestions.ovenDish'),
  ];

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const message = draft.trim();
    if (!message) {
      return;
    }

    setDraft('');
    await sendMessage(message);
  }

  async function handleSuggestionClick(prompt: string) {
    setDraft('');
    await sendMessage(prompt);
  }

  return (
    <section className="chatbot" id="recipe-chatbot">
      <div className="chatbot__header" id="recipe-chatbot-header">
        <p className="chatbot__eyebrow" id="recipe-chatbot-eyebrow">{t('chatbot.eyebrow')}</p>
        <h2 className="chatbot__title" id="recipe-chatbot-title">{t('chatbot.title')}</h2>
        <p className="chatbot__description" id="recipe-chatbot-description">{t('chatbot.description')}</p>
      </div>

      <div className="chatbot__messages" id="recipe-chatbot-messages">
        {messages.map((message, index) => (
          <article
            key={message.id}
            className={`chatbot__message chatbot__message--${message.role}`}
            id={`recipe-chatbot-message-${index}`}
          >
            <p className="chatbot__message-label" id={`recipe-chatbot-message-label-${index}`}>
              {message.role === 'assistant' ? t('chatbot.assistantName') : t('chatbot.youLabel')}
            </p>
            <p className="chatbot__message-text" id={`recipe-chatbot-message-text-${index}`}>{message.text}</p>

            {message.recommendations.length > 0 && (
              <ul className="chatbot__recommendations" id={`recipe-chatbot-recommendations-${index}`}>
                {message.recommendations.map((recommendation) => (
                  <li
                    key={recommendation.id}
                    className="chatbot__recommendation"
                    id={`recipe-chatbot-recommendation-${recommendation.id}`}
                  >
                    <Link
                      to={`/recipes/${recommendation.id}`}
                      className="chatbot__recommendation-link"
                      id={`recipe-chatbot-recommendation-link-${recommendation.id}`}
                    >
                      <span
                        className="chatbot__recommendation-title"
                        id={`recipe-chatbot-recommendation-title-${recommendation.id}`}
                      >
                        {recommendation.title}
                      </span>
                      <span
                        className="chatbot__recommendation-category"
                        id={`recipe-chatbot-recommendation-category-${recommendation.id}`}
                      >
                        {recommendation.categoryName}
                      </span>
                    </Link>
                    <p
                      className="chatbot__recommendation-reason"
                      id={`recipe-chatbot-recommendation-reason-${recommendation.id}`}
                    >
                      {recommendation.matchReason}
                    </p>
                  </li>
                ))}
              </ul>
            )}
          </article>
        ))}
      </div>

      <form className="chatbot__form" id="recipe-chatbot-form" onSubmit={handleSubmit}>
        <label className="chatbot__label" htmlFor="recipe-chatbot-input" id="recipe-chatbot-input-label">
          {t('chatbot.inputLabel')}
        </label>
        <div className="chatbot__input-row" id="recipe-chatbot-input-row">
          <input
            id="recipe-chatbot-input"
            className="chatbot__input"
            value={draft}
            onChange={(event) => setDraft(event.target.value)}
            placeholder={t('chatbot.placeholder')}
          />
          <button
            type="submit"
            className="chatbot__submit"
            id="recipe-chatbot-submit"
            disabled={loading}
          >
            {loading ? t('chatbot.sending') : t('chatbot.send')}
          </button>
        </div>
        <div className="chatbot__suggestions" id="recipe-chatbot-suggestions">
          {suggestionPrompts.map((prompt, index) => (
            <button
              key={prompt}
              type="button"
              className="chatbot__suggestion"
              id={`recipe-chatbot-suggestion-${index}`}
              onClick={() => void handleSuggestionClick(prompt)}
              disabled={loading}
            >
              {prompt}
            </button>
          ))}
        </div>
        {error && <p className="chatbot__error" id="recipe-chatbot-error">{t('chatbot.error')}</p>}
      </form>
    </section>
  );
}
