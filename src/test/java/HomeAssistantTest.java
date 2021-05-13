import net.robiotic.mineassistant.HomeAssistant;

import java.net.URISyntaxException;

public class HomeAssistantTest {
    public static void main(String[] args) throws URISyntaxException {
        if (args.length != 2) {
            System.err.println("Usage: <HA url> <HA token>");
            System.exit(1);
        }

        final HomeAssistant ha = new HomeAssistant(args[0], args[1]);
        ha.connect();
    }
}
