package edu.buaa.canteen;

import edu.buaa.canteen.config.CanteenConfiguration;

/** Minimal command-line entry point for the architecture scaffold. */
public final class CanteenApplication {
    private CanteenApplication() { }
    public static void main(String[] args) {
        var configuration = CanteenConfiguration.defaults();
        System.out.printf("Canteen of UBAA 架构已启动：%d 个窗口，%d 名厨师。%n",
                configuration.windows().size(), configuration.totalChefCount());
    }
}
