import { useNavigate } from 'react-router-dom';
import { useRecipeImport } from '../../application/useRecipeImport';
import { useTranslation } from '../../i18n/useTranslation';
import './ImportRecipePage.css';

export default function ImportRecipePage() {
  const navigate = useNavigate();
  const { t } = useTranslation();
  const {
    fileInputRef,
    selectedFile,
    previewUrl,
    loading,
    error,
    setError,
    extraction,
    editTitle,
    setEditTitle,
    editIngredients,
    setEditIngredients,
    editPreparation,
    setEditPreparation,
    editNotes,
    setEditNotes,
    confirming,
    handleFileSelect,
    handleUpload,
    handleConfirm,
    handleReset,
  } = useRecipeImport();

  async function onConfirm() {
    const recipeId = await handleConfirm();
    if (recipeId) {
      navigate(`/recipes/${recipeId}`);
    } else {
      setError(t('import.errorSaveFailed'));
    }
  }

  return (
    <div className="import-page" id="import-recipe-page">
      <h1 className="import-page__title" id="import-page-title">
        {t('import.pageTitle')}
      </h1>
      <p className="import-page__subtitle" id="import-page-subtitle">
        {t('import.pageSubtitle')}
      </p>

      {error && (
        <div className="import-page__error" id="import-error" role="alert">
          {error}
        </div>
      )}

      {!extraction && (
        <div className="import-upload" id="import-upload-section">
          <div className="import-upload__dropzone" id="import-dropzone">
            <input
              ref={fileInputRef}
              type="file"
              accept="image/png,image/jpeg,image/jpg,image/webp"
              onChange={handleFileSelect}
              className="import-upload__input"
              id="import-file-input"
            />
            <p className="import-upload__hint" id="import-upload-hint">
              {t('import.fileHint')}
            </p>
          </div>

          {previewUrl && (
            <div className="import-upload__preview" id="import-preview">
              <img
                src={previewUrl}
                alt="Preview of selected recipe"
                className="import-upload__preview-img"
                id="import-preview-img"
              />
            </div>
          )}

          <div className="import-upload__actions">
            <button
              onClick={handleUpload}
              disabled={!selectedFile || loading}
              className="import-upload__btn import-upload__btn--primary"
              id="import-upload-btn"
            >
              {loading ? t('import.uploadBtnLoading') : t('import.uploadBtn')}
            </button>
            {selectedFile && (
              <button
                onClick={handleReset}
                disabled={loading}
                className="import-upload__btn import-upload__btn--secondary"
                id="import-reset-btn"
              >
                {t('import.resetBtn')}
              </button>
            )}
          </div>

          {loading && (
            <div className="import-upload__loading" id="import-loading">
              <div className="import-upload__spinner"></div>
              <p>{t('import.loadingText')}</p>
            </div>
          )}
        </div>
      )}

      {extraction && (
        <div className="import-review" id="import-review-section">
          <div
            className={`import-review__status import-review__status--${extraction.status === 'COMPLETE' ? 'complete' : 'incomplete'}`}
            id="import-status-banner"
          >
            {extraction.status === 'COMPLETE'
              ? t('import.extractionComplete')
              : t('import.extractionIncomplete')}
          </div>

          {extraction.warnings.length > 0 && (
            <div className="import-review__warnings" id="import-warnings">
              <h3>{t('import.warningsTitle')}</h3>
              <ul>
                {extraction.warnings.map((w, i) => (
                  <li key={i} id={`import-warning-${i}`}>{w}</li>
                ))}
              </ul>
            </div>
          )}

          {extraction.missingFields.length > 0 && (
            <div className="import-review__missing" id="import-missing-fields">
              <h3>{t('import.missingFieldsTitle')}</h3>
              <p>
                {extraction.missingFields.map((f) => {
                  return t(`import.fieldLabels.${f}`) || f;
                }).join(', ')}
              </p>
            </div>
          )}

          {extraction.rawModelResponse && (
            <details className="import-review__raw" id="import-raw-text-section">
              <summary>{t('import.rawResponseTitle')}</summary>
              <textarea
                readOnly
                value={extraction.rawModelResponse}
                className="import-review__raw-textarea"
                id="import-raw-text"
                rows={8}
              />
            </details>
          )}

          <div className="import-review__form" id="import-edit-form">
            <h3>{t('import.editFormTitle')}</h3>

            <label className="import-review__label" htmlFor="import-edit-title">
              {t('import.labelTitle')}
            </label>
            <input
              type="text"
              id="import-edit-title"
              className={`import-review__input ${extraction.missingFields.includes('title') ? 'import-review__input--missing' : ''}`}
              value={editTitle}
              onChange={(e) => setEditTitle(e.target.value)}
              placeholder={t('import.placeholderTitle')}
            />

            <label className="import-review__label" htmlFor="import-edit-ingredients">
              {t('import.labelIngredients')}
            </label>
            <textarea
              id="import-edit-ingredients"
              className={`import-review__textarea ${extraction.missingFields.includes('ingredients') ? 'import-review__textarea--missing' : ''}`}
              value={editIngredients}
              onChange={(e) => setEditIngredients(e.target.value)}
              placeholder={t('import.placeholderIngredients')}
              rows={6}
            />

            <label className="import-review__label" htmlFor="import-edit-preparation">
              {t('import.labelPreparation')}
            </label>
            <textarea
              id="import-edit-preparation"
              className={`import-review__textarea ${extraction.missingFields.includes('preparation') ? 'import-review__textarea--missing' : ''}`}
              value={editPreparation}
              onChange={(e) => setEditPreparation(e.target.value)}
              placeholder={t('import.placeholderPreparation')}
              rows={6}
            />

            <label className="import-review__label" htmlFor="import-edit-notes">
              {t('import.labelNotes')}
            </label>
            <textarea
              id="import-edit-notes"
              className="import-review__textarea"
              value={editNotes}
              onChange={(e) => setEditNotes(e.target.value)}
              placeholder={t('import.placeholderNotes')}
              rows={3}
            />
          </div>

          <div className="import-review__actions">
            <button
              onClick={onConfirm}
              disabled={confirming || !editTitle.trim()}
              className="import-upload__btn import-upload__btn--primary"
              id="import-confirm-btn"
            >
              {confirming ? t('import.confirmBtnLoading') : t('import.confirmBtn')}
            </button>
            <button
              onClick={handleReset}
              disabled={confirming}
              className="import-upload__btn import-upload__btn--secondary"
              id="import-cancel-btn"
            >
              {t('import.cancelBtn')}
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
