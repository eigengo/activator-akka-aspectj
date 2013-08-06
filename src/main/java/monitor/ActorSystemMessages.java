package monitor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author janmachacek
 */
class ActorSystemMessages {
    private final Map<Long, Integer> messages = new HashMap<>();

    void recordMessage() {
        long second = System.currentTimeMillis() / 1000;
        int count = 0;
        if (messages.containsKey(second)) {
            count = messages.get(second);
        }
        messages.put(second, ++count);
    }

    float average() {
        if (messages.isEmpty()) return 0;
        int total = 0;
        for (Integer i : messages.values()) {
            total += i;
        }

        return total / messages.size();
    }
}
