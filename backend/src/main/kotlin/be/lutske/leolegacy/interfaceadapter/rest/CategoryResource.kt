package be.lutske.leolegacy.interfaceadapter.rest

import be.lutske.leolegacy.infrastructure.persistence.repository.CategoryRepository
import be.lutske.leolegacy.infrastructure.persistence.repository.RecipeRepository
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType

/**
 * REST resource for recipe categories.
 */
@Path("/api/categories")
class CategoryResource(
    private val categoryRepository: CategoryRepository,
    private val recipeRepository: RecipeRepository
) {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    fun listCategories(): List<CategoryResponse> {
        return categoryRepository.findAllOrderedByName().map { cat ->
            val count = recipeRepository.findByCategoryId(cat.id).size.toLong()
            CategoryResponse(
                id = cat.id,
                name = cat.name,
                recipeCount = count
            )
        }
    }
}
