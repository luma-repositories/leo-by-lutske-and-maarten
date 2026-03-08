import { useState, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  type ImportNeedsMoreInfoResponse,
  type ImportConfirmRequest,
  importRecipeImage,
  confirmRecipeImport,
} from '../api/client';
import { useTranslation } from '../i18n/useTranslation';
import './ImportRecipePage.css';

export default function ImportRecipePage() {
  const navigate = useNavigate();
  const fileInputRef = useRef<HTMLInputElement>(null);
  const { t } = useTranslation();

  // Upload state
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [previewUrl, setPreviewUrl] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Needs-more-info state
  const [needsMoreInfo, setNeedsMoreInfo] = useState<ImportNeedsMoreInfoResponse | null>(null);
  const [editTitle, setEditTitle] = useState('');
  const [editIngredients, setEditIngredients] = useState('');
  const [editPreparation, setEditPreparation] = useState('');
  const [editNotes, setEditNotes] = useState('');
  const [confirming, setConfirming] = useState(false);

  function handleFileSelect(e: React.ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0];
    if (!file) return;

    setSelectedFile(file);
    setError(null);
    setNeedsMoreInfo(null);

    // Create preview URL
    const url = URL.createObjectURL(file);
    setPreviewUrl(url);
  }

  async function handleUpload() {
    if (!selectedFile) return;

    setLoading(true);
    setError(null);

    const result = await importRecipeImage(selectedFile);

    setLoading(false);

    if (result.status === 'created') {
      navigate(`/recipes/${result.recipe.id}`);
    } else if (result.status === 'needs_more_info') {
      setNeedsMoreInfo(result.data);
      // Pre-fill form with proposed values
      setEditTitle(result.data.proposedRecipe.title ?? '');
      setEditIngredients(
        result.data.proposedRecipe.ingredients?.join('\n') ?? ''
      );
      setEditPreparation(result.data.proposedRecipe.preparation ?? '');
      setEditNotes('');
    } else {
      setError(result.message);
    }
  }

  async function handleConfirm() {
    if (!needsMoreInfo) return;

    setConfirming(true);
    setError(null);

    const ingredientsList = editIngredients
      .split('\n')
      .map((s) => s.trim())
      .filter((s) => s.length > 0);

    const request: ImportConfirmRequest = {
      rawModelResponse: needsMoreInfo.rawModelResponse,
      proposedRecipe: needsMoreInfo.proposedRecipe,
      userOverrides: {
        title: editTitle || null,
        ingredients: ingredientsList.length > 0 ? ingredientsList : null,
        preparation: editPreparation || null,
        notes: editNotes || null,
      },
    };

    try {
      const recipe = await confirmRecipeImport(request);
      navigate(`/recipes/${recipe.id}`);
    } catch {
      setError(t('import.errorSaveFailed'));
    } finally {
      setConfirming(false);
    }
  }

  function handleReset() {
    setSelectedFile(null);
    setPreviewUrl(null);
    setError(null);
    setNeedsMoreInfo(null);
    setEditTitle('');
    setEditIngredients('');
    setEditPreparation('');
    setEditNotes('');
    if (fileInputRef.current) {
      fileInputRef.current.value = '';
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

      {/* Error message */}
      {error && (
        <div className="import-page__error" id="import-error" role="alert">
          {error}
        </div>
      )}

      {/* Upload section */}
      {!needsMoreInfo && (
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

          {/* Image preview */}
          {previewUrl && (
            <div className="import-upload__preview" id="import-preview">
              <img
                src={previewUrl}
                alt="Voorbeeld van het gekozen recept"
                className="import-upload__preview-img"
                id="import-preview-img"
              />
            </div>
          )}

          {/* Upload button */}
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

          {/* Loading indicator */}
          {loading && (
            <div className="import-upload__loading" id="import-loading">
              <div className="import-upload__spinner"></div>
              <p>{t('import.loadingText')}</p>
            </div>
          )}
        </div>
      )}

      {/* Needs more info section */}
      {needsMoreInfo && (
        <div className="import-review" id="import-review-section">
          {/* Warnings */}
          {needsMoreInfo.warnings.length > 0 && (
            <div className="import-review__warnings" id="import-warnings">
              <h3>{t('import.warningsTitle')}</h3>
              <ul>
                {needsMoreInfo.warnings.map((w, i) => (
                  <li key={i} id={`import-warning-${i}`}>{w}</li>
                ))}
              </ul>
            </div>
          )}

          {/* Missing fields indicator */}
          <div className="import-review__missing" id="import-missing-fields">
            <h3>{t('import.missingFieldsTitle')}</h3>
            <p>
              {needsMoreInfo.missingFields.map((f) => {
                return t(`import.fieldLabels.${f}`) || f;
              }).join(', ')}
            </p>
          </div>

          {/* Raw model response */}
          {needsMoreInfo.rawModelResponse && (
            <div className="import-review__raw" id="import-raw-text-section">
              <h3>{t('import.rawResponseTitle')}</h3>
              <textarea
                readOnly
                value={needsMoreInfo.rawModelResponse}
                className="import-review__raw-textarea"
                id="import-raw-text"
                rows={8}
              />
            </div>
          )}

          {/* Editable form */}
          <div className="import-review__form" id="import-edit-form">
            <h3>{t('import.editFormTitle')}</h3>

            <label className="import-review__label" htmlFor="import-edit-title">
              {t('import.labelTitle')}
            </label>
            <input
              type="text"
              id="import-edit-title"
              className="import-review__input"
              value={editTitle}
              onChange={(e) => setEditTitle(e.target.value)}
              placeholder={t('import.placeholderTitle')}
            />

            <label className="import-review__label" htmlFor="import-edit-ingredients">
              {t('import.labelIngredients')}
            </label>
            <textarea
              id="import-edit-ingredients"
              className="import-review__textarea"
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
              className="import-review__textarea"
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

          {/* Confirm actions */}
          <div className="import-review__actions">
            <button
              onClick={handleConfirm}
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
