package be.lutske.leolegacy.infrastructure.persistence.repository;

import be.lutske.leolegacy.domain.recipe.Recipe;
import be.lutske.leolegacy.domain.recipe.RecipeRepository;
import be.lutske.leolegacy.infrastructure.persistence.entity.RecipeEntity;
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

public class RecipeRepositoryImplTest {

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private RecipeRepositoryImpl repository;

    @Test
    public void testFindById() {
        // Arrange
        Long id = 1L;
        RecipeEntity entity = new RecipeEntity();
        entity.setId(id);
        entity.setTitle("Test Recipe");
        
        when(entityManager.find(RecipeEntity.class, id)).thenReturn(entity);

        // Act
        Optional<Recipe> result = repository.findById(id);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Test Recipe", result.get().getTitle());
        verify(entityManager, times(1)).find(RecipeEntity.class, id);
    }

    @Test
    public void testFindById_NotFound() {
        // Arrange
        Long id = 1L;
        when(entityManager.find(RecipeEntity.class, id)).thenReturn(null);

        // Act
        Optional<Recipe> result = repository.findById(id);

        // Assert
        assertFalse(result.isPresent());
        verify(entityManager, times(1)).find(RecipeEntity.class, id);
    }

    @Test
    public void testSave() {
        // Arrange
        Recipe recipe = new Recipe();
        recipe.setId(1L);
        recipe.setTitle("New Recipe");
        
        RecipeEntity entity = new RecipeEntity();
        entity.setId(1L);
        entity.setTitle("New Recipe");
        
        when(entityManager.merge(any(RecipeEntity.class))).thenReturn(entity);

        // Act
        Recipe saved = repository.save(recipe);

        // Assert
        assertEquals("New Recipe", saved.getTitle());
        verify(entityManager, times(1)).merge(any(RecipeEntity.class));
    }

    @Test
    public void testDeleteById() {
        // Arrange
        Long id = 1L;
        RecipeEntity entity = new RecipeEntity();
        entity.setId(id);
        
        when(entityManager.find(RecipeEntity.class, id)).thenReturn(entity);

        // Act
        repository.deleteById(id);

        // Assert
        verify(entityManager, times(1)).remove(entity);
    }

    @Test
    public void testFindAll() {
        // Arrange
        RecipeEntity entity1 = new RecipeEntity();
        entity1.setId(1L);
        entity1.setTitle("Recipe 1");
        
        RecipeEntity entity2 = new RecipeEntity();
        entity2.setId(2L);
        entity2.setTitle("Recipe 2");
        
        TypedQuery<RecipeEntity> query = Mockito.mock(TypedQuery.class);
        when(entityManager.createQuery("SELECT e FROM RecipeEntity e", RecipeEntity.class)).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(entity1, entity2));

        // Act
        List<Recipe> recipes = repository.findAll();

        // Assert
        assertEquals(2, recipes.size());
        assertEquals("Recipe 1", recipes.get(0).getTitle());
        assertEquals("Recipe 2", recipes.get(1).getTitle());
        verify(entityManager, times(1)).createQuery("SELECT e FROM RecipeEntity e", RecipeEntity.class);
    }

    @Test
    public void testFindByTitle() {
        // Arrange
        String title = "Test";
        RecipeEntity entity = new RecipeEntity();
        entity.setId(1L);
        entity.setTitle(title);
        
        Specification<RecipeEntity> spec = Mockito.mock(Specification.class);
        when(spec.toPredicate(any(), any(), any())).thenReturn(null);
        
        TypedQuery<RecipeEntity> query = Mockito.mock(TypedQuery.class);
        when(entityManager.createQuery(spec.toPredicate(any(), any(), any()), RecipeEntity.class)).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(entity));

        // Act
        List<Recipe> recipes = repository.findByTitle(title);

        // Assert
        assertEquals(1, recipes.size());
        assertEquals(title, recipes.get(0).getTitle());
        verify(spec, times(1)).toPredicate(any(), any(), any());
    }
}