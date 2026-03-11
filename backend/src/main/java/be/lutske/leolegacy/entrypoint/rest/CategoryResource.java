package be.lutske.leolegacy.entrypoint.rest;

import be.lutske.leolegacy.entrypoint.rest.dto.CategoryResponse;
import be.lutske.leolegacy.usecase.ListCategories;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/api/categories")
public class CategoryResource {

    private final ListCategories listCategories;

    public CategoryResource(ListCategories listCategories) {
        this.listCategories = listCategories;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<CategoryResponse> listCategories() {
        return listCategories.execute().stream()
                .map(cwc -> new CategoryResponse(
                        cwc.category().getId(),
                        cwc.category().getName(),
                        cwc.recipeCount()))
                .toList();
    }
}
