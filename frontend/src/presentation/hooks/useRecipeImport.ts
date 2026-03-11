import { useMemo, useRef, useState } from 'react';
import { confirmRecipeImport, importRecipeImage } from '../../application/recipe/importRecipe';
import {
  buildConfirmRecipeImportCommand,
  createEditableRecipeImport,
  type EditableRecipeImport,
} from '../../application/recipe/importReview';
import type { RecipeImportExtraction } from '../../domain/recipe/RecipeImport';

export function useRecipeImport(onConfirmSuccess: (recipeId: number) => void, saveErrorMessage: string) {
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [previewUrl, setPreviewUrl] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [extraction, setExtraction] = useState<RecipeImportExtraction | null>(null);
  const [editable, setEditable] = useState<EditableRecipeImport>({
    title: '',
    ingredients: '',
    preparation: '',
    notes: '',
  });
  const [confirming, setConfirming] = useState(false);

  const canConfirm = useMemo(() => editable.title.trim().length > 0, [editable.title]);

  function handleFileSelect(event: React.ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0];
    if (!file) {
      return;
    }

    if (previewUrl) {
      URL.revokeObjectURL(previewUrl);
    }

    setSelectedFile(file);
    setError(null);
    setExtraction(null);
    setEditable({ title: '', ingredients: '', preparation: '', notes: '' });
    setPreviewUrl(URL.createObjectURL(file));
  }

  async function handleUpload() {
    if (!selectedFile) {
      return;
    }

    setLoading(true);
    setError(null);
    const result = await importRecipeImage(selectedFile);
    setLoading(false);

    if (result.status === 'error') {
      setError(result.message);
      return;
    }

    setExtraction(result.data);
    setEditable(createEditableRecipeImport(result.data));
  }

  async function handleConfirm() {
    if (!extraction) {
      return;
    }

    setConfirming(true);
    setError(null);

    try {
      const recipe = await confirmRecipeImport(buildConfirmRecipeImportCommand(extraction, editable));
      onConfirmSuccess(recipe.id);
    } catch {
      setError(saveErrorMessage);
    } finally {
      setConfirming(false);
    }
  }

  function handleReset() {
    if (previewUrl) {
      URL.revokeObjectURL(previewUrl);
    }

    setSelectedFile(null);
    setPreviewUrl(null);
    setError(null);
    setExtraction(null);
    setEditable({ title: '', ingredients: '', preparation: '', notes: '' });

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
    extraction,
    editable,
    confirming,
    canConfirm,
    setEditable,
    handleFileSelect,
    handleUpload,
    handleConfirm,
    handleReset,
  };
}
