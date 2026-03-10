package be.lutske.leolegacy.interfaceadapter.rest;

import be.lutske.leolegacy.infrastructure.persistence.repository.CategoryRepository;
import be.lutske.leolegacy.infrastructure.persistence.repository.RecipeRepository;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

/**
 * REST resource for recipe categories.
 */
@Path("/api/categories")
public class CategoryResource {

    private final CategoryRepository categoryRepository;
    private final RecipeRepository recipeRepository;

    public CategoryResource(CategoryRepository categoryRepository, RecipeRepository recipeRepository) {
        this.categoryRepository = categoryRepository;
        this.recipeRepository = recipeRepository;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<CategoryResponse> listCategories() {
        return categoryRepository.findAllOrderedByName().stream()
                .map(cat -> new CategoryResponse(
                        cat.getId(),
                        cat.getName(),
                        recipeRepository.findByCategoryId(cat.getId()).size()
                ))
                .toList();
    }
}
