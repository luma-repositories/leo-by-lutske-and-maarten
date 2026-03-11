package be.lutske.leolegacy.domain.port;

import be.lutske.leolegacy.domain.model.Category;

import java.util.List;
import java.util.Optional;

/**
 * Port for category persistence. Infrastructure provides the implementation.
 */
public interface CategoryRepository {

    List<Category> findAllOrderedByName();

    Optional<Category> findById(long id);
}
