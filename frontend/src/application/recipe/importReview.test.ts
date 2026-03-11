import { describe, expect, it } from 'vitest';
import { buildConfirmRecipeImportCommand, createEditableRecipeImport } from './importReview';

describe('importReview', () => {
  it('creates editable state from extracted recipe data', () => {
    const editable = createEditableRecipeImport({
      status: 'COMPLETE',
      rawModelResponse: 'raw',
      proposedRecipe: {
        title: 'Chocolate Mousse',
        ingredients: ['200 g chocolate', '4 eggs'],
        preparation: 'Melt\nFold',
      },
      missingFields: [],
      warnings: [],
    });

    expect(editable).toEqual({
      title: 'Chocolate Mousse',
      ingredients: '200 g chocolate\n4 eggs',
      preparation: 'Melt\nFold',
      notes: '',
    });
  });

  it('builds a confirm command with normalized ingredients', () => {
    const command = buildConfirmRecipeImportCommand(
      {
        status: 'NEEDS_MORE_INFO',
        rawModelResponse: 'raw',
        proposedRecipe: { title: 'Draft', ingredients: null, preparation: null },
        missingFields: ['ingredients'],
        warnings: [],
      },
      {
        title: 'Draft',
        ingredients: '200 g flour\n\n2 eggs\n',
        preparation: 'Mix',
        notes: 'Add vanilla',
      },
    );

    expect(command.userOverrides?.ingredients).toEqual(['200 g flour', '2 eggs']);
    expect(command.userOverrides?.notes).toBe('Add vanilla');
  });
});
