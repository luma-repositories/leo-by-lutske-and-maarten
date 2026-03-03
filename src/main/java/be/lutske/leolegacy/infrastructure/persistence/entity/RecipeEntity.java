package be.lutske.leolegacy.infrastructure.persistence.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "recipes")
public class RecipeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "legacy_id", unique = true)
    public Integer legacyId;

    @Column(nullable = false, length = 255)
    public String title;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    public CategoryEntity category;

    @Column(nullable = false, columnDefinition = "TEXT")
    public String ingredients;

    @Column(nullable = false, columnDefinition = "TEXT")
    public String preparation;

    @Column(name = "pdf_slug", length = 255)
    public String pdfSlug;

    @Column(name = "description", columnDefinition = "TEXT")
    public String description;

    @Column(name = "servings")
    public Integer servings;

    @Column(name = "source", length = 255)
    public String source;

    @Column(name = "tags", columnDefinition = "TEXT")
    public String tags;

    @Column(name = "ocr_raw_text", columnDefinition = "TEXT")
    public String ocrRawText;

    @Column(name = "import_notes", columnDefinition = "TEXT")
    public String importNotes;

    @Column(name = "created_at", nullable = false)
    public LocalDateTime createdAt;
}
