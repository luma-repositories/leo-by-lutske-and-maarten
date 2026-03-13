package be.lutske.leolegacy.interfaceadapter.rest;

import be.lutske.leolegacy.application.usecase.GetRecipeUseCase;
import be.lutske.leolegacy.application.usecase.ListRecipesUseCase;
import be.lutske.leolegacy.application.usecase.ListRecipesByCategoryUseCase;
import be.lutske.leolegacy.application.usecase.IncrementRecipeViewCountUseCase;
import be.lutske.leolegacy.domain.recipe.Recipe;
import be.lutske.leolegacy.domain.recipe.RecipeId;
import be.lutske.leolegacy.domain.category.CategoryId;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/api/recipes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RecipeResource {

    @Inject
    GetRecipeUseCase getRecipeUseCase;

    @Inject
    ListRecipesUseCase listRecipesUseCase;

    @Inject
    ListRecipesByCategoryUseCase listRecipesByCategoryUseCase;

    @Inject
    IncrementRecipeViewCountUseCase incrementViewCountUseCase;

    @GET
    public Response listRecipes(@QueryParam("categoryId") String categoryId) {
        if (categoryId != null) {
            CategoryId categoryIdObj = new CategoryId(categoryId);
            List<Recipe> recipes = listRecipesByCategoryUseCase.execute(categoryIdObj);
        return Response.ok(recipes).build();
    }
    else {
        List<Recipe> recipes = listRecipesUseCase.execute();
        return Response.ok(recipes).build();
    }

    }

    @GET
    @Path("/top")
    public Response topRecipes() {
        // Assuming this would be handled by a different use case if needed
        // For now, we'll keep it as is but note that this should be refactored
        return Response.status(Response.Status.NOT_IMPLEMENTED).build();
    }

    @GET
    @Path("/{id}")
    public Response getRecipe(@PathParam("id") String id) {
        RecipeId recipeId = new RecipeId(id);
        Recipe recipe = getRecipeUseCase.execute(recipeId);
        incrementViewCountUseCase.execute(recipeId);
        return Response.ok(recipe).build();
    }
}