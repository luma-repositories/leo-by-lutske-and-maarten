package be.lutske.leolegacy.port.out;

import be.lutske.leolegacy.domain.category.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryQueryPort {

    List<Category> findAllOrderedByName();

    Optional<Category> findById(long id);
}
