package edu.buaa.canteen.repository;

import edu.buaa.canteen.domain.user.User;
import java.util.Optional;

public interface UserRepository { Optional<User> findById(String id); User save(User user); }
