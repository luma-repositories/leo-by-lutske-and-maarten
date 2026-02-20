package be.lutske.leolegacy.interfaceadapter.rest;

import be.lutske.leolegacy.application.usecase.ListCategoriesUseCase;
import be.lutske.leolegacy.domain.Category;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.stream.Collectors;

@Path("/api/categories")
@Produces(MediaType.APPLICATION_JSON)
public class CategoryResource {

    private final ListCategoriesUseCase listCategoriesUseCase;

    public CategoryResource(ListCategoriesUseCase listCategoriesUseCase) {
        this.listCategoriesUseCase = listCategoriesUseCase;
    }

    @GET
    public List<CategoryResponse> listCategories() {
        return listCategoriesUseCase.execute().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private CategoryResponse toResponse(Category category) {
        return new CategoryResponse(category.id(), category.slug(), category.name(), category.recipeCount());
    }
}
