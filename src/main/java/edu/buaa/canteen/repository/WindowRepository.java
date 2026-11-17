package edu.buaa.canteen.repository;

import edu.buaa.canteen.domain.menu.DishType;
import edu.buaa.canteen.domain.resource.Window;
import java.util.List;
import java.util.Optional;

public interface WindowRepository { Optional<Window> findById(String id); List<Window> findByDishType(DishType dishType); List<Window> findAll(); }
