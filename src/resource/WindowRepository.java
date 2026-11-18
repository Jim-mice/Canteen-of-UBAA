package resource;

import menu.DishType;
import java.util.List;
import java.util.Optional;

public interface WindowRepository { Optional<Window> findById(String id); List<Window> findByDishType(DishType dishType); List<Window> findAll(); }
