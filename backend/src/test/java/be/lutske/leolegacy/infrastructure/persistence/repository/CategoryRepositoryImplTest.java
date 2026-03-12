package be.lutske.leolegacy.infrastructure.persistence.repository;

import be.lutske.leolegacy.domain.category.Category;
import be.lutske.leolegacy.domain.category.CategoryRepository;
import be.lutske.leolegacy.infrastructure.persistence.entity.CategoryEntity;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CategoryRepositoryImplTest {

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private CategoryRepositoryImpl repository;

    @Test
    public void testFindById() {
        // Arrange
        Long id = 1L;
        CategoryEntity entity = new CategoryEntity();
        entity.setId(id);
        entity.setName("Test Category");
        
        when(entityManager.find(CategoryEntity.class, id)).thenReturn(entity);

        // Act
        Optional<Category> result = repository.findById(id);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Test Category", result.get().getName());
        verify(entityManager, times(1)).find(CategoryEntity.class, id);
    }

    @Test
    public void testFindById_NotFound() {
        // Arrange
        Long id = 1L;
        when(entityManager.find(CategoryEntity.class, id)).thenReturn(null);

        // Act
        Optional<Category> result = repository.findById(id);

        // Assert
        assertFalse(result.isPresent());
        verify(entityManager, times(1)).find(CategoryEntity.class, id);
    }

    @Test
    public void testSave() {
        // Arrange
        Category category = new Category();
        category.setId(1L);
        category.setName("New Category");
        
        CategoryEntity entity = new CategoryEntity();
        entity.setId(1L);
        entity.setName("New Category");
        
        when(entityManager.merge(any(CategoryEntity.class))).thenReturn(entity);

        // Act
        Category saved = repository.save(category);

        // Assert
        assertEquals("New Category", saved.getName());
        verify(entityManager, times(1)).merge(any(CategoryEntity.class));
    }

    @Test
    public void testDeleteById() {
        // Arrange
        Long id = 1L;
        CategoryEntity entity = new CategoryEntity();
        entity.setId(id);
        
        when(entityManager.find(CategoryEntity.class, id)).thenReturn(entity);

        // Act
        repository.deleteById(id);

        // Assert
        verify(entityManager, times(1)).remove(entity);
    }

    @Test
    public void testFindAll() {
        // Arrange
        CategoryEntity entity1 = new CategoryEntity();
        entity1.setId(1L);
        entity1.setName("Category 1");
        
        CategoryEntity entity2 = new CategoryEntity();
        entity2.setId(2L);
        entity2.setName("Category 2");
        
        TypedQuery<CategoryEntity> query = Mockito.mock(TypedQuery.class);
        when(entityManager.createQuery("SELECT e FROM CategoryEntity e", CategoryEntity.class)).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(entity1, entity2));

        // Act
        List<Category> categories = repository.findAll();

        // Assert
        assertEquals(2, categories.size());
        assertEquals("Category 1", categories.get(0).getName());
        assertEquals("Category 2", categories.get(1).getName());
        verify(entityManager, times(1)).createQuery("SELECT e FROM CategoryEntity e", CategoryEntity.class);
    }

    @Test
    public void testFindByName() {
        // Arrange
        String name = "Test";
        CategoryEntity entity = new CategoryEntity();
        entity.setId(1L);
        entity.setName(name);
        
        Specification<CategoryEntity> spec = Mockito.mock(Specification.class);
        when(spec.toPredicate(any(), any(), any())).thenReturn(null);
        
        TypedQuery<CategoryEntity> query = Mockito.mock(TypedQuery.class);
        when(entityManager.createQuery(spec.toPredicate(any(), any(), any()), CategoryEntity.class)).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(entity));

        // Act
        List<Category> categories = repository.findByName(name);

        // Assert
        assertEquals(1, categories.size());
        assertEquals(name, categories.get(0).getName());
        verify(spec, times(1)).toPredicate(any(), any(), any());
    }
}