package be.lutske.leolegacy.entrypoint.rest;

import be.lutske.leolegacy.usecase.recipe.GetRecipeUseCase;
import be.lutske.leolegacy.usecase.recipe.ListRecipesUseCase;
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

    private final ListRecipesUseCase listRecipesUseCase;
    private final GetRecipeUseCase getRecipeUseCase;

    public RecipeResource(ListRecipesUseCase listRecipesUseCase, GetRecipeUseCase getRecipeUseCase) {
        this.listRecipesUseCase = listRecipesUseCase;
        this.getRecipeUseCase = getRecipeUseCase;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<RecipeSummaryResponse> listRecipes(@QueryParam("categoryId") Long categoryId) {
        return listRecipesUseCase.listAllOrByCategory(categoryId).stream()
                .map(RestRecipeMapper::toSummaryResponse)
                .toList();
    }

    @GET
    @Path("/top")
    @Produces(MediaType.APPLICATION_JSON)
    public List<RecipeSummaryResponse> topRecipes() {
        return listRecipesUseCase.listTopRecipes(10).stream()
                .map(RestRecipeMapper::toSummaryResponse)
                .toList();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public RecipeDetailResponse getRecipe(@PathParam("id") long id) {
        return getRecipeUseCase.execute(id)
                .map(RestRecipeMapper::toDetailResponse)
                .orElseThrow(() -> new NotFoundException("Recipe with id " + id + " not found"));
    }
}
