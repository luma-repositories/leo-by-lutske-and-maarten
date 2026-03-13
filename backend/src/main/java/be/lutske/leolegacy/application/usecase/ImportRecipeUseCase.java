package be.lutske.leolegacy.application.usecase;

import be.lutske.leolegacy.domain.entity.CategoryEntity;
import be.lutske.leolegacy.domain.entity.RecipeEntity;
import be.lutske.leolegacy.infrastructure.persistence.repository.CategoryRepository;
import be.lutske.leolegacy.infrastructure.persistence.repository.RecipeRepository;
import be.lutske.leolegacy.interfaceadapter.rest.RecipeImportDtos.ProposedRecipeDto;
import java.time.Instant;

public class ImportRecipeUseCase {
    private static final long DEFAULT_IMPORT_CATEGORY_ID = 15L;
    private final RecipeRepository recipeRepository;
    private final CategoryRepository categoryRepository;

    public ImportRecipeUseCase(RecipeRepository recipeRepository, CategoryRepository categoryRepository) {
        this.recipeRepository = recipeRepository;
        this.categoryRepository = categoryRepository;
    }

    public RecipeEntity execute(ProposedRecipeDto proposal, String rawModelResponse) {
        long categoryId = proposal.categoryId() != null ? proposal.categoryId() : DEFAULT_IMPORT_CATEGORY_ID;
        CategoryEntity category = categoryRepository.findById(categoryId);
        if (category == null) {
            category = categoryRepository.findById(DEFAULT_IMPORT_CATEGORY_ID);
        }
        if (category == null) {
            throw new IllegalStateException("Default import category not found");
        }

        RecipeEntity recipe = new RecipeEntity();
        recipe.setTitle(proposal.title() != null ? proposal.title() : "Untitled Recipe");
        recipe.setIngredients(proposal.ingredients() != null ? String.join("|", proposal.ingredients()) : "");
        recipe.setPreparation(proposal.preparation() != null ? proposal.preparation() : "");
        recipe.setCategory(category);
        recipe.setViewCount(0);
        recipe.setSource(proposal.source() != null ? proposal.source() : "Imported from image");
        recipe.setCreatedAt(Instant.now());

        if (rawModelResponse != null) {
            recipe.setImportMetadata(rawModelResponse);
        }

        recipeRepository.persist(recipe);
        return recipe;
    }
}