package be.lutske.leolegacy.usecases;

import be.lutske.leolegacy.domain.ProposedRecipe;
import be.lutske.leolegacy.domain.UserOverrides;

public record ConfirmRecipeImportCommand(String rawModelResponse,
                                        ProposedRecipe proposedRecipe,
                                        UserOverrides userOverrides) {
}