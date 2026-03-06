package be.lutske.leolegacy.infrastructure.persistence.repository

import be.lutske.leolegacy.infrastructure.persistence.entity.RecipeEntity
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class RecipeRepository : PanacheRepository<RecipeEntity> {

    fun findByCategoryId(categoryId: Long): List<RecipeEntity> =
        list("category.id", categoryId)

    fun findTopByViewCount(limit: Int): List<RecipeEntity> =
        list("ORDER BY viewCount DESC", *arrayOf()).stream().limit(limit.toLong()).toList()
}
