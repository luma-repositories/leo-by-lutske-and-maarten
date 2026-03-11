package be.lutske.leolegacy.entrypoint.rest;

import be.lutske.leolegacy.domain.model.Recipe;
import be.lutske.leolegacy.entrypoint.rest.dto.RecipeDetailResponse;
import be.lutske.leolegacy.entrypoint.rest.dto.RecipeSummaryResponse;
import be.lutske.leolegacy.usecase.GetRecipeDetail;
import be.lutske.leolegacy.usecase.ListRecipes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/api/recipes")
public class RecipeResource {

    private final ListRecipes listRecipes;
    private final GetRecipeDetail getRecipeDetail;

    public RecipeResource(ListRecipes listRecipes, GetRecipeDetail getRecipeDetail) {
        this.listRecipes = listRecipes;
        this.getRecipeDetail = getRecipeDetail;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<RecipeSummaryResponse> listRecipes(@QueryParam("categoryId") Long categoryId) {
        return listRecipes.execute(categoryId).stream().map(this::toSummary).toList();
    }

    @GET
    @Path("/top")
    @Produces(MediaType.APPLICATION_JSON)
    public List<RecipeSummaryResponse> topRecipes() {
        return listRecipes.topByViewCount(10).stream().map(this::toSummary).toList();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public RecipeDetailResponse getRecipe(@PathParam("id") long id) {
        return getRecipeDetail.execute(id)
                .map(this::toDetail)
                .orElseThrow(() -> new NotFoundException("Recipe with id " + id + " not found"));
    }

    private RecipeSummaryResponse toSummary(Recipe r) {
        return new RecipeSummaryResponse(r.getId(), r.getTitle(),
                r.getCategory() != null ? r.getCategory().getName() : null, r.getViewCount());
    }

    private RecipeDetailResponse toDetail(Recipe r) {
        return new RecipeDetailResponse(r.getId(), r.getTitle(), r.getIngredients(),
                r.getPreparation(),
                r.getCategory() != null ? r.getCategory().getId() : 0,
                r.getCategory() != null ? r.getCategory().getName() : null,
                r.getViewCount());
    }
}
