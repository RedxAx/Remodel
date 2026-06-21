package redxax.restudio.Remodel.api;

import java.util.EnumMap;
import java.util.Map;

public final class PlayerEquipment {
    private final EnumMap<PlayerEquipmentSlot, Object> items = new EnumMap<>(PlayerEquipmentSlot.class);

    public static PlayerEquipment empty() {
        return new PlayerEquipment();
    }

    public static PlayerEquipment of(Map<?, ?> values) {
        PlayerEquipment equipment = new PlayerEquipment();
        if (values == null || values.isEmpty()) {
            return equipment;
        }
        equipment.set(PlayerEquipmentSlot.HEAD, first(values, "helmet", "head"));
        equipment.set(PlayerEquipmentSlot.CHEST, first(values, "chestplate", "chest"));
        equipment.set(PlayerEquipmentSlot.LEGS, first(values, "leggings", "legs"));
        equipment.set(PlayerEquipmentSlot.FEET, first(values, "boots", "feet"));
        equipment.set(PlayerEquipmentSlot.MAIN_HAND, first(values, "mainHand", "main_hand", "mainhand", "hand"));
        equipment.set(PlayerEquipmentSlot.OFF_HAND, first(values, "offHand", "off_hand", "offhand"));
        return equipment;
    }

    public PlayerEquipment set(PlayerEquipmentSlot slot, Object item) {
        if (slot == null) {
            return this;
        }
        if (item == null) {
            items.remove(slot);
        } else {
            items.put(slot, item);
        }
        return this;
    }

    public Object get(PlayerEquipmentSlot slot) {
        return items.get(slot);
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public Map<PlayerEquipmentSlot, Object> asMap() {
        return Map.copyOf(items);
    }

    private static Object first(Map<?, ?> values, String... keys) {
        for (String key : keys) {
            Object value = values.get(key);
            if (value instanceof String string && string.isBlank()) {
                continue;
            }
            if (value != null) {
                return value;
            }
        }
        return null;
    }
}
