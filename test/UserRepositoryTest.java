import user.GroupUser;
import user.InMemoryUserRepository;
import user.RegularUser;
import user.User;
import user.UserGroup;

import java.util.HashSet;
import java.util.Set;

public final class UserRepositoryTest {
    private UserRepositoryTest() { }

    public static void run() {
        savesAndFindsRegularAndGroupUsers();
        missingUsersReturnEmpty();
        savingSameIdReplacesTheRecord();
        membershipIsDefensivelyCopiedAndImmutable();
        rejectsNullUser();
    }

    private static void savesAndFindsRegularAndGroupUsers() {
        var repository = new InMemoryUserRepository();
        var regular = new RegularUser("U-1", "Regular");
        var group = new GroupUser("U-2", "Group", Set.of(UserGroup.HUNAN));
        TestSupport.assertEquals(regular, repository.save(regular), "regular save result");
        TestSupport.assertEquals(group, repository.save(group), "group save result");
        TestSupport.assertEquals(regular, repository.findById("U-1").orElseThrow(), "regular lookup");
        TestSupport.assertEquals(group, repository.findById("U-2").orElseThrow(), "group lookup");
    }

    private static void missingUsersReturnEmpty() {
        var repository = new InMemoryUserRepository();
        TestSupport.assertTrue(repository.findById("missing").isEmpty(), "unknown user should be empty");
        TestSupport.assertTrue(repository.findById(null).isEmpty(), "null lookup should be empty");
    }

    private static void savingSameIdReplacesTheRecord() {
        var repository = new InMemoryUserRepository();
        repository.save(new RegularUser("U-1", "Before"));
        var replacement = new GroupUser("U-1", "After", Set.of(UserGroup.HUNAN));
        repository.save(replacement);
        TestSupport.assertEquals(replacement, repository.findById("U-1").orElseThrow(), "updated user record");
    }

    private static void membershipIsDefensivelyCopiedAndImmutable() {
        var groups = new HashSet<>(Set.of(UserGroup.HUNAN));
        var user = new GroupUser("U-1", "Group", groups);
        groups.clear();
        TestSupport.assertTrue(user.groups().contains(UserGroup.HUNAN), "constructor must copy memberships");
        TestSupport.assertThrows(UnsupportedOperationException.class,
                () -> user.groups().add(new UserGroup("STAFF", "Staff")), "membership view must be immutable");
    }

    private static void rejectsNullUser() {
        var repository = new InMemoryUserRepository();
        TestSupport.assertThrows(IllegalArgumentException.class, () -> repository.save(null), "null user must be rejected");
        TestSupport.assertThrows(IllegalArgumentException.class,
                () -> repository.save(new InvalidIdUser(null)), "null ID must be rejected");
        TestSupport.assertThrows(IllegalArgumentException.class,
                () -> repository.save(new InvalidIdUser(" ")), "blank ID must be rejected");
    }

    private static final class InvalidIdUser extends User {
        private final String reportedId;
        private InvalidIdUser(String reportedId) { super("valid-constructor-id", "Invalid", Set.of()); this.reportedId = reportedId; }
        @Override public String id() { return reportedId; }
    }
}
