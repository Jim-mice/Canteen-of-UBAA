package user;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/** In-memory user storage keyed by the unique user ID. */
public final class InMemoryUserRepository implements UserRepository {
    private final Map<String, User> users = new LinkedHashMap<>();

    @Override
    public Optional<User> findById(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(users.get(id));
    }

    /** Replaces any existing user record with the same ID. */
    @Override
    public User save(User user) {
        if (user == null) {
            throw new IllegalArgumentException("user must not be null");
        }
        String id = user.id();
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("user ID must not be null or blank");
        }
        users.put(id, user);
        return user;
    }
}
