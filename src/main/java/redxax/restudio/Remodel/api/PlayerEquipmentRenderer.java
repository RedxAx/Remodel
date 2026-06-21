package redxax.restudio.Remodel.api;

@FunctionalInterface
public interface PlayerEquipmentRenderer {
    void render(PlayerEquipmentSlot slot, Object item);

    default void render(PlayerEquipmentSlot slot, Object item, String attachment) {
        render(slot, item);
    }
}
