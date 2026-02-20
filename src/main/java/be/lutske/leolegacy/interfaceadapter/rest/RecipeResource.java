package be.lutske.leolegacy.interfaceadapter.rest;

import be.lutske.leolegacy.application.usecase.GetRecipeDetailUseCase;
import be.lutske.leolegacy.application.usecase.ListRecipesUseCase;
import be.lutske.leolegacy.domain.RecipeDetail;
import be.lutske.leolegacy.domain.RecipeSummary;

import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.stream.Collectors;

@Path("/api/recipes")
@Produces(MediaType.APPLICATION_JSON)
public class RecipeResource {

    private final ListRecipesUseCase listRecipesUseCase;
    private final GetRecipeDetailUseCase getRecipeDetailUseCase;

    public RecipeResource(ListRecipesUseCase listRecipesUseCase, GetRecipeDetailUseCase getRecipeDetailUseCase) {
        this.listRecipesUseCase = listRecipesUseCase;
        this.getRecipeDetailUseCase = getRecipeDetailUseCase;
    }

    @GET
    public List<RecipeSummaryResponse> listRecipes(@QueryParam("category") String categorySlug) {
        return listRecipesUseCase.execute(categorySlug).stream()
                .map(this::toSummaryResponse)
                .collect(Collectors.toList());
    }

    @GET
    @Path("/{id}")
    public RecipeDetailResponse getRecipe(@PathParam("id") Long id) {
        RecipeDetail detail = getRecipeDetailUseCase.execute(id)
                .orElseThrow(() -> new NotFoundException("Recipe not found"));
        return toDetailResponse(detail);
    }

    private RecipeSummaryResponse toSummaryResponse(RecipeSummary recipe) {
        return new RecipeSummaryResponse(recipe.id(), recipe.legacyId(), recipe.title(), recipe.category(), recipe.excerpt());
    }

    private RecipeDetailResponse toDetailResponse(RecipeDetail recipe) {
        return new RecipeDetailResponse(
                recipe.id(),
                recipe.legacyId(),
                recipe.title(),
                recipe.category(),
                recipe.ingredients(),
                recipe.preparation(),
                recipe.pdfSlug()
        );
    }
}
