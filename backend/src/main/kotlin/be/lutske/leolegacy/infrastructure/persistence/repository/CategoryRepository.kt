package be.lutske.leolegacy.infrastructure.persistence.repository

import be.lutske.leolegacy.infrastructure.persistence.entity.CategoryEntity
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class CategoryRepository : PanacheRepository<CategoryEntity> {

    fun findAllOrderedByName(): List<CategoryEntity> =
        list("ORDER BY name")
}
