package be.lutske.leolegacy.interfaceadapter.rest;

import be.lutske.leolegacy.infrastructure.persistence.entity.RecipeEntity;
import be.lutske.leolegacy.infrastructure.persistence.repository.RecipeRepository;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import java.util.Arrays;
import java.util.List;

/**
 * REST resource for recipes.
 */
@Path("/api/recipes")
public class RecipeResource {

    private final RecipeRepository recipeRepository;

    public RecipeResource(RecipeRepository recipeRepository) {
        this.recipeRepository = recipeRepository;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<RecipeSummaryResponse> listRecipes(@QueryParam("categoryId") Long categoryId) {
        List<RecipeEntity> recipes = (categoryId != null)
                ? recipeRepository.findByCategoryId(categoryId)
                : recipeRepository.listAll();
        return recipes.stream().map(this::toSummary).toList();
    }

    @GET
    @Path("/top")
    @Produces(MediaType.APPLICATION_JSON)
    public List<RecipeSummaryResponse> topRecipes() {
        return recipeRepository.findTopByViewCount(10).stream()
                .map(this::toSummary)
                .toList();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public RecipeDetailResponse getRecipe(@PathParam("id") long id) {
        RecipeEntity recipe = recipeRepository.findById(id);
        if (recipe == null) {
            throw new NotFoundException("Recipe with id " + id + " not found");
        }
        return toDetail(recipe);
    }

    private RecipeSummaryResponse toSummary(RecipeEntity entity) {
        return new RecipeSummaryResponse(
                entity.getId(),
                entity.getTitle(),
                entity.getCategory().getName(),
                entity.getViewCount()
        );
    }

    private RecipeDetailResponse toDetail(RecipeEntity entity) {
        List<String> ingredientList = Arrays.stream(entity.getIngredients().split("\\|"))
                .filter(s -> !s.isBlank())
                .toList();
        return new RecipeDetailResponse(
                entity.getId(),
                entity.getTitle(),
                ingredientList,
                entity.getPreparation(),
                entity.getCategory().getId(),
                entity.getCategory().getName(),
                entity.getViewCount()
        );
    }
}
