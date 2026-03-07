package be.lutske.leolegacy.infrastructure.persistence.entity

import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntityBase
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "recipe")
class RecipeEntity : PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    lateinit var title: String

    @Column(columnDefinition = "TEXT")
    lateinit var ingredients: String

    @Column(columnDefinition = "TEXT")
    lateinit var preparation: String

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    lateinit var category: CategoryEntity

    @Column(name = "view_count")
    var viewCount: Int = 0

    @Column(name = "source")
    var source: String? = null

    @Column(name = "created_at")
    var createdAt: Instant? = null

    constructor()

    constructor(
        id: Long,
        title: String,
        ingredients: String,
        preparation: String,
        category: CategoryEntity,
        viewCount: Int = 0,
        source: String? = null,
        createdAt: Instant? = null
    ) {
        this.id = id
        this.title = title
        this.ingredients = ingredients
        this.preparation = preparation
        this.category = category
        this.viewCount = viewCount
        this.source = source
        this.createdAt = createdAt
    }
}
