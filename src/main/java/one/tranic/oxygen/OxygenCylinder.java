package one.tranic.oxygen;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public final class OxygenCylinder extends JavaPlugin implements Listener {
    private static OxygenCylinder instance;
    Map<UUID, Boolean> playerInteracted = new HashMap<>();
    private Metrics metrics;

    public static OxygenCylinder getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        metrics = new Metrics(this, 24165);

        Config.reload();
        this.getServer().getPluginManager().registerEvents(this, this);

        try {
            Field commandMapField = Bukkit.getPluginManager().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            SimpleCommandMap commandMap = (SimpleCommandMap) commandMapField.get(Bukkit.getPluginManager());

            commandMap.register("oxygen-reload", "oxygencylinder", new OxygenCommand());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onDisable() {
        if (metrics != null) metrics.shutdown();
    }

    @EventHandler
    public void onPlayerLeft(PlayerQuitEvent event) {
        playerInteracted.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onBottleInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!player.isInWater()) return;
        EquipmentSlot hand = event.getHand();
        if (hand == null) return;

        ItemStack stack;
        if (hand.name().equals("HAND")) {
            stack = player.getInventory().getItemInMainHand();
            if (!stack.getType().equals(Material.GLASS_BOTTLE)) return;
        } else {
            // Ignore the off-hand
            return;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        Action action = event.getAction();

        // In order to solve a bug, an additional judgment is required
        if (action == Action.RIGHT_CLICK_AIR) {
            playerInteracted.put(player.getUniqueId(), true);
            return;
        }

        if (action != Action.RIGHT_CLICK_BLOCK) {
            if (playerInteracted.getOrDefault(player.getUniqueId(), false)) {
                playerInteracted.put(player.getUniqueId(), false);
                return;
            }
            if (ThreadLocalRandom.current().nextDouble() < Config.getChanceGlassBottleBreaksWithFireTypeInOffhand()) {
                if (item.getAmount() > 1) item.setAmount(item.getAmount() - 1);
                else player.getInventory().remove(item);
                player.playSound(player.getLocation(), Sound.BLOCK_GLASS_BREAK, 1f, 1f);
            } else {
                ItemStack offhandStack = player.getInventory().getItemInOffHand();
                if (!isFireSource(offhandStack.getType())) {
                    if (item.getAmount() > 1) item.setAmount(item.getAmount() - 1);
                    else player.getInventory().remove(item);
                    ItemStack i = new ItemStack(Material.POTION);
                    PotionMeta meta = (PotionMeta) i.getItemMeta();
                    if (meta == null) return;
                    meta.setBasePotionData(new PotionData(PotionType.WATER));
                    i.setItemMeta(meta);
                    player.getInventory().addItem(i);
                }
            }

            int maxAir = player.getMaximumAir();
            int air = player.getRemainingAir();

            if (air >= maxAir) return;

            int newAir = air + Config.getAmountOfAirInBottles();
            if (newAir > maxAir) newAir = maxAir;
            player.setRemainingAir(newAir);
        }
    }

    private boolean isFireSource(Material material) {
        return material == Material.FLINT_AND_STEEL || material == Material.FIRE_CHARGE || material == Material.LAVA_BUCKET;
    }
}