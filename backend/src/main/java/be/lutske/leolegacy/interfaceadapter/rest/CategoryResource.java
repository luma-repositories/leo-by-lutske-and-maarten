package be.lutske.leolegacy.interfaceadapter.rest;

import be.lutske.leolegacy.application.usecase.ListCategoriesUseCase;
import be.lutske.leolegacy.domain.category.Category;
import be.lutske.leolegacy.interfaceadapter.dto.CategoryResponse;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

@Path("/api/categories")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CategoryResource {

    @Inject
    ListCategoriesUseCase listCategoriesUseCase;

    @GET
    public Response listCategories() {
        List<Category> categories = listCategoriesUseCase.execute();
        return Response.ok(categories.stream()
                .map(CategoryResponse::fromDomain)
                .toList())
                .build();
    }
}