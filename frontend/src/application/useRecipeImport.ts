import { useState, useRef } from 'react';
import type { ExtractionResult, ImportConfirmRequest } from '../domain/model';
import { importRecipeImage, confirmRecipeImport } from '../infrastructure/api/apiClient';

/**
 * Application hook: manages the recipe import workflow.
 * Encapsulates upload, extraction review, and confirmation orchestration.
 */
export function useRecipeImport() {
  const fileInputRef = useRef<HTMLInputElement>(null);

  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [previewUrl, setPreviewUrl] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const [extraction, setExtraction] = useState<ExtractionResult | null>(null);
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
    setExtraction(null);
    setPreviewUrl(URL.createObjectURL(file));
  }

  async function handleUpload() {
    if (!selectedFile) return;
    setLoading(true);
    setError(null);

    const result = await importRecipeImage(selectedFile);
    setLoading(false);

    if (result.status === 'extracted') {
      setExtraction(result.data);
      setEditTitle(result.data.proposedRecipe.title ?? '');
      setEditIngredients(result.data.proposedRecipe.ingredients?.join('\n') ?? '');
      setEditPreparation(result.data.proposedRecipe.preparation ?? '');
      setEditNotes('');
    } else {
      setError(result.message);
    }
  }

  async function handleConfirm(): Promise<number | null> {
    if (!extraction) return null;

    setConfirming(true);
    setError(null);

    const ingredientsList = editIngredients
      .split('\n')
      .map((s) => s.trim())
      .filter((s) => s.length > 0);

    const request: ImportConfirmRequest = {
      rawModelResponse: extraction.rawModelResponse,
      proposedRecipe: extraction.proposedRecipe,
      userOverrides: {
        title: editTitle || null,
        ingredients: ingredientsList.length > 0 ? ingredientsList : null,
        preparation: editPreparation || null,
        notes: editNotes || null,
      },
    };

    try {
      const recipe = await confirmRecipeImport(request);
      return recipe.id;
    } catch {
      return null;
    } finally {
      setConfirming(false);
    }
  }

  function handleReset() {
    setSelectedFile(null);
    setPreviewUrl(null);
    setError(null);
    setExtraction(null);
    setEditTitle('');
    setEditIngredients('');
    setEditPreparation('');
    setEditNotes('');
    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
  }

  return {
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
  };
}
