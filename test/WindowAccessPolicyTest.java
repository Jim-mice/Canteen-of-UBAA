import menu.DishDefinition;
import menu.DishType;
import user.DefaultWindowAccessPolicy;
import user.GroupUser;
import user.RegularUser;
import user.UserGroup;

import java.util.Optional;
import java.util.Set;

public final class WindowAccessPolicyTest {
    private WindowAccessPolicyTest() { }

    public static void run() {
        grantsPublicDishesToRegularAndGroupUsers();
        restrictsHunanDishByMembership();
        supportsAnyConfiguredGroup();
        rejectsNullInputs();
    }

    private static void grantsPublicDishesToRegularAndGroupUsers() {
        var policy = new DefaultWindowAccessPolicy();
        var publicDish = dish(DishType.BEEF_NOODLES, Optional.empty());
        var regular = new RegularUser("U-1", "Regular");
        var group = new GroupUser("U-2", "Group", Set.of(UserGroup.HUNAN));
        TestSupport.assertTrue(policy.canAccess(regular, publicDish), "regular user may access public dish");
        TestSupport.assertTrue(policy.canAccess(group, publicDish), "group user may access public dish");
    }

    private static void restrictsHunanDishByMembership() {
        var policy = new DefaultWindowAccessPolicy();
        var hunanDish = dish(DishType.HUNAN_SPECIALTY, Optional.of(UserGroup.HUNAN));
        TestSupport.assertFalse(policy.canAccess(new RegularUser("U-1", "Regular"), hunanDish),
                "regular user must not access Hunan-only dish");
        TestSupport.assertTrue(policy.canAccess(new GroupUser("U-2", "Hunan", Set.of(UserGroup.HUNAN)), hunanDish),
                "Hunan member may access Hunan-only dish");
    }

    private static void supportsAnyConfiguredGroup() {
        var staff = new UserGroup("STAFF", "Staff");
        var dish = dish(DishType.WONTON, Optional.of(staff));
        var policy = new DefaultWindowAccessPolicy();
        TestSupport.assertTrue(policy.canAccess(new GroupUser("U-1", "Staff", Set.of(staff)), dish),
                "custom group membership should grant access");
        TestSupport.assertFalse(policy.canAccess(new GroupUser("U-2", "Other", Set.of(UserGroup.HUNAN)), dish),
                "unrelated group must not grant access");
    }

    private static void rejectsNullInputs() {
        var policy = new DefaultWindowAccessPolicy();
        TestSupport.assertFalse(policy.canAccess(null, dish(DishType.BEEF_NOODLES, Optional.empty())), "null user");
        TestSupport.assertFalse(policy.canAccess(new RegularUser("U-1", "Regular"), null), "null dish");
    }

    private static DishDefinition dish(DishType type, Optional<UserGroup> requiredGroup) {
        return new DishDefinition(type, type.name(), 1, 2, requiredGroup, new int[]{2});
    }
}
