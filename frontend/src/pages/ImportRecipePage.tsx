import { ChangeEvent, FormEvent, useMemo, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { apiClient } from '../api/client';
import { nl } from '../i18n/nl';
import type { NeedsMoreInfoResponse, ProposedRecipe, UserOverrides } from '../types/api';

type ImportStage = 'idle' | 'uploading' | 'needs_more_info' | 'confirming' | 'error' | 'success';

export function ImportRecipePage() {
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [stage, setStage] = useState<ImportStage>('idle');
  const [errorMessage, setErrorMessage] = useState<string>('');
  const [needsInfoPayload, setNeedsInfoPayload] = useState<NeedsMoreInfoResponse | null>(null);
  const [proposedRecipe, setProposedRecipe] = useState<ProposedRecipe>({});
  const [userOverrides, setUserOverrides] = useState<UserOverrides>({ notes: '' });
  const [successMessage, setSuccessMessage] = useState<string>('');
  const navigate = useNavigate();

  const previewUrl = useMemo(() => {
    if (!selectedFile) {
      return null;
    }
    return URL.createObjectURL(selectedFile);
  }, [selectedFile]);

  const onFileChange = (event: ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0] ?? null;
    setSelectedFile(file);
    setErrorMessage('');
  };

  const onImportSubmit = async (event: FormEvent) => {
    event.preventDefault();
    if (!selectedFile) {
      setErrorMessage(nl.importFailed);
      return;
    }

    setStage('uploading');
    setErrorMessage('');

    try {
      const result = await apiClient.importRecipe(selectedFile);
      if (result.kind === 'created') {
        const ingredientsCount = countIngredients(result.recipe.ingredients);
        setSuccessMessage(`${nl.importSuccess} (${result.recipe.title}, ${ingredientsCount} ${nl.ingredients.toLowerCase()})`);
        setStage('success');
        window.setTimeout(() => navigate(`/recipes/${result.recipe.id}`), 900);
        return;
      }

      setNeedsInfoPayload(result.payload);
      setProposedRecipe(result.payload.proposedRecipe);
      setUserOverrides({ notes: '' });
      setStage('needs_more_info');
    } catch (error) {
      setStage('error');
      setErrorMessage(getErrorMessage(error));
    }
  };

  const onConfirmSubmit = async (event: FormEvent) => {
    event.preventDefault();
    if (!needsInfoPayload) {
      return;
    }

    setStage('confirming');
    setErrorMessage('');

    try {
      const result = await apiClient.confirmImportedRecipe(needsInfoPayload.rawText, proposedRecipe, userOverrides);
      if (result.kind === 'created') {
        const ingredientsCount = countIngredients(result.recipe.ingredients);
        setSuccessMessage(`${nl.importSuccess} (${result.recipe.title}, ${ingredientsCount} ${nl.ingredients.toLowerCase()})`);
        setStage('success');
        window.setTimeout(() => navigate(`/recipes/${result.recipe.id}`), 900);
        return;
      }

      setNeedsInfoPayload(result.payload);
      setProposedRecipe(result.payload.proposedRecipe);
      setStage('needs_more_info');
    } catch (error) {
      setStage('error');
      setErrorMessage(getErrorMessage(error));
    }
  };

  return (
    <section className="content import-page" id="import-page">
      <Link className="back-link" id="import-back-link" to="/">{nl.backToRecipes}</Link>
      <h2 className="section-title" id="import-page-title">{nl.importRecipe}</h2>
      <p className="import-description" id="import-page-description">{nl.importDescription}</p>

      <form className="import-upload-form" id="import-upload-form" onSubmit={onImportSubmit}>
        <label className="import-label" id="import-file-label" htmlFor="import-file-input">{nl.chooseImage}</label>
        <input
          className="import-file-input"
          id="import-file-input"
          type="file"
          accept="image/png,image/jpg,image/jpeg,image/webp"
          onChange={onFileChange}
        />

        {previewUrl && (
          <img className="import-preview" id="import-image-preview" src={previewUrl} alt="import preview" />
        )}

        <button
          className="import-submit-button"
          id="import-submit-button"
          type="submit"
          disabled={stage === 'uploading' || stage === 'confirming'}
        >
          {nl.uploadAndScan}
        </button>
      </form>

      {(stage === 'uploading' || stage === 'confirming') && (
        <p className="import-loading" id="import-loading-message">{nl.processingImport}</p>
      )}

      {(stage === 'error' || (stage === 'idle' && errorMessage)) && (
        <p className="import-error" id="import-error-message">{errorMessage}</p>
      )}

      {stage === 'success' && (
        <p className="import-success" id="import-success-message">{successMessage}</p>
      )}

      {stage === 'needs_more_info' && needsInfoPayload && (
        <section className="import-needs-info" id="import-needs-info-section">
          <h3 className="import-needs-info-title" id="import-needs-info-title">{nl.missingDataTitle}</h3>
          <p className="import-needs-info-description" id="import-needs-info-description">{nl.missingDataDescription}</p>

          <label className="import-label" id="import-raw-text-label" htmlFor="import-raw-text">{nl.ocrRawText}</label>
          <textarea
            className="import-textarea"
            id="import-raw-text"
            value={needsInfoPayload.rawText}
            readOnly
            rows={8}
          />

          <div className="import-warnings" id="import-warning-list">
            <h4 className="import-warnings-title" id="import-warning-title">{nl.parseWarnings}</h4>
            {needsInfoPayload.parseWarnings.map((warning, index) => (
              <p className="import-warning-item" id={`import-warning-${index}`} key={`warning-${index}`}>{warning}</p>
            ))}
          </div>

          <form className="import-confirm-form" id="import-confirm-form" onSubmit={onConfirmSubmit}>
            <label className="import-label" id="import-title-label" htmlFor="import-title-input">{nl.title}</label>
            <input
              className="import-input"
              id="import-title-input"
              value={userOverrides.title ?? proposedRecipe.title ?? ''}
              onChange={(event) => setUserOverrides((current) => ({ ...current, title: event.target.value }))}
            />

            <label className="import-label" id="import-description-label" htmlFor="import-description-input">{nl.description}</label>
            <textarea
              className="import-textarea"
              id="import-description-input"
              value={userOverrides.description ?? proposedRecipe.description ?? ''}
              onChange={(event) => setUserOverrides((current) => ({ ...current, description: event.target.value }))}
              rows={2}
            />

            <label className="import-label" id="import-servings-label" htmlFor="import-servings-input">{nl.servings}</label>
            <input
              className="import-input"
              id="import-servings-input"
              type="number"
              min={1}
              value={userOverrides.servings ?? proposedRecipe.servings ?? ''}
              onChange={(event) => {
                const value = event.target.value;
                setUserOverrides((current) => ({ ...current, servings: value ? Number(value) : undefined }));
              }}
            />

            <label className="import-label" id="import-ingredients-label" htmlFor="import-ingredients-input">{nl.ingredients}</label>
            <textarea
              className="import-textarea"
              id="import-ingredients-input"
              value={userOverrides.ingredients ?? proposedRecipe.ingredients ?? ''}
              onChange={(event) => setUserOverrides((current) => ({ ...current, ingredients: event.target.value }))}
              rows={6}
            />

            <label className="import-label" id="import-instructions-label" htmlFor="import-instructions-input">{nl.instructions}</label>
            <textarea
              className="import-textarea"
              id="import-instructions-input"
              value={userOverrides.instructions ?? proposedRecipe.instructions ?? ''}
              onChange={(event) => setUserOverrides((current) => ({ ...current, instructions: event.target.value }))}
              rows={6}
            />

            <label className="import-label" id="import-tags-label" htmlFor="import-tags-input">{nl.tags}</label>
            <input
              className="import-input"
              id="import-tags-input"
              value={userOverrides.tags ?? proposedRecipe.tags ?? ''}
              onChange={(event) => setUserOverrides((current) => ({ ...current, tags: event.target.value }))}
            />

            <label className="import-label" id="import-source-label" htmlFor="import-source-input">{nl.source}</label>
            <input
              className="import-input"
              id="import-source-input"
              value={userOverrides.source ?? proposedRecipe.source ?? ''}
              onChange={(event) => setUserOverrides((current) => ({ ...current, source: event.target.value }))}
            />

            <label className="import-label" id="import-notes-label" htmlFor="import-notes-input">{nl.notes}</label>
            <textarea
              className="import-textarea"
              id="import-notes-input"
              value={userOverrides.notes ?? ''}
              onChange={(event) => setUserOverrides((current) => ({ ...current, notes: event.target.value }))}
              rows={3}
            />

            <button className="import-confirm-button" id="import-confirm-button" type="submit">{nl.confirmAndSave}</button>
          </form>
        </section>
      )}
    </section>
  );
}

function getErrorMessage(error: unknown): string {
  if (error instanceof Error && error.message.trim().length > 0) {
    return error.message;
  }
  return nl.importFailed;
}

function countIngredients(ingredients: string): number {
  return ingredients
    .split(/\n|,/)
    .map((item) => item.trim())
    .filter((item) => item.length > 0).length;
}
