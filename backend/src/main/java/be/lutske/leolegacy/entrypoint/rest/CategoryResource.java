package be.lutske.leolegacy.entrypoint.rest;

import be.lutske.leolegacy.usecase.category.ListCategoriesUseCase;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/api/categories")
public class CategoryResource {

    private final ListCategoriesUseCase listCategoriesUseCase;

    public CategoryResource(ListCategoriesUseCase listCategoriesUseCase) {
        this.listCategoriesUseCase = listCategoriesUseCase;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<CategoryResponse> listCategories() {
        return listCategoriesUseCase.execute().stream()
                .map(category -> new CategoryResponse(category.id(), category.name(), category.recipeCount()))
                .toList();
    }
}
