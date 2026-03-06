package be.lutske.leolegacy.interfaceadapter.rest

import be.lutske.leolegacy.infrastructure.persistence.entity.RecipeEntity
import be.lutske.leolegacy.infrastructure.persistence.repository.RecipeRepository
import jakarta.ws.rs.GET
import jakarta.ws.rs.NotFoundException
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.QueryParam
import jakarta.ws.rs.core.MediaType

/**
 * REST resource for recipes.
 */
@Path("/api/recipes")
class RecipeResource(
    private val recipeRepository: RecipeRepository
) {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    fun listRecipes(@QueryParam("categoryId") categoryId: Long?): List<RecipeSummaryResponse> {
        val recipes: List<RecipeEntity> = if (categoryId != null) {
            recipeRepository.findByCategoryId(categoryId)
        } else {
            recipeRepository.listAll()
        }
        return recipes.map { it.toSummary() }
    }

    @GET
    @Path("/top")
    @Produces(MediaType.APPLICATION_JSON)
    fun topRecipes(): List<RecipeSummaryResponse> {
        return recipeRepository.findTopByViewCount(10).map { it.toSummary() }
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    fun getRecipe(@PathParam("id") id: Long): RecipeDetailResponse {
        val recipe = recipeRepository.findById(id)
            ?: throw NotFoundException("Recipe with id $id not found")
        return recipe.toDetail()
    }

    private fun RecipeEntity.toSummary() = RecipeSummaryResponse(
        id = this.id,
        title = this.title,
        categoryName = this.category.name,
        viewCount = this.viewCount
    )

    private fun RecipeEntity.toDetail() = RecipeDetailResponse(
        id = this.id,
        title = this.title,
        ingredients = this.ingredients.split("|").filter { it.isNotBlank() },
        preparation = this.preparation,
        categoryId = this.category.id,
        categoryName = this.category.name,
        viewCount = this.viewCount
    )
}
