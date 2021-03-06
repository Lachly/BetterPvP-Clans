package net.betterpvp.clans.weapon.weapons.legendaries;

import net.betterpvp.clans.Clans;
import net.betterpvp.clans.classes.Energy;
import net.betterpvp.clans.utilities.UtilClans;
import net.betterpvp.clans.weapon.Weapon;
import net.betterpvp.core.utility.UtilItem;
import net.betterpvp.core.utility.UtilMath;
import net.betterpvp.core.utility.recharge.RechargeManager;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.NetherWarts;

public class FarmingRake extends Weapon {

    public FarmingRake(Clans i) {
        super(i, Material.IRON_HOE, (byte) 0, ChatColor.RED + "Rake",
                new String[]{"",
                        ChatColor.GRAY + "Damage: " + ChatColor.YELLOW + "0",
                        ChatColor.GRAY + "Active: " + ChatColor.YELLOW + "Harvest",
                        "",
                        ChatColor.RESET + "This mysterious tool will ",
                        ChatColor.RESET + "automatically harvest and replant any ",
                        ChatColor.RESET + "crops in your vicinity.",
                        ""}, true, 8.0);
    }


    @EventHandler
    public void onInteract(PlayerInteractEvent e) {


        if (e.getPlayer().getItemInHand() == null) return;
        if (e.getPlayer().getItemInHand().getType() != Material.IRON_HOE) return;
        if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (isThisWeapon(e.getPlayer())) {
                skill(e.getPlayer());
            }


        }
    }


    @SuppressWarnings("deprecation")
    public void skill(Player p) {


        if (RechargeManager.getInstance().add(p, getName(), 1.0, true)) {
            if (Energy.use(p, getName(), 25.0, true)) {
                for (int x = -5; x < 5; x++) {
                    for (int z = -5; z < 5; z++) {
                        Location loc = new Location(p.getWorld(), p.getLocation().getX() + x, p.getLocation().getY(), p.getLocation().getZ() + z);
                        if (loc.getBlock() != null || loc.getBlock().getType() != Material.AIR) {
                            Block b = loc.getBlock();
                            if (b.getType() == Material.POTATO) {
                                if (b.getData() == CropState.RIPE.getData()) {
                                    if (p.getInventory().firstEmpty() == -1) {
                                        p.getWorld().dropItem(b.getLocation(), new ItemStack(Material.POTATO_ITEM, UtilMath.randomInt(1, 3)));
                                    } else {
                                        p.getInventory().addItem(UtilClans.updateNames(new ItemStack(Material.POTATO_ITEM, UtilMath.randomInt(1, 3))));
                                    }
                                    p.getWorld().playEffect(b.getLocation(), Effect.STEP_SOUND, 3);
                                    p.getWorld().playEffect(b.getLocation(), Effect.STEP_SOUND, 3);
                                    p.getWorld().playEffect(b.getLocation(), Effect.STEP_SOUND, 3);
                                    b.setType(Material.POTATO);
                                    p.getInventory().removeItem(new ItemStack(Material.POTATO_ITEM, 1));
                                }
                            } else if (b.getType() == Material.CARROT) {
                                if (b.getData() == CropState.RIPE.getData()) {
                                    if (p.getInventory().firstEmpty() == -1) {
                                        p.getWorld().dropItem(b.getLocation(), new ItemStack(Material.CARROT_ITEM, UtilMath.randomInt(1, 3)));
                                    } else {
                                        p.getInventory().addItem(UtilClans.updateNames(new ItemStack(Material.CARROT_ITEM, UtilMath.randomInt(1, 3))));
                                    }
                                    p.getWorld().playEffect(b.getLocation(), Effect.STEP_SOUND, 3);
                                    p.getWorld().playEffect(b.getLocation(), Effect.STEP_SOUND, 3);
                                    p.getWorld().playEffect(b.getLocation(), Effect.STEP_SOUND, 3);
                                    b.setType(Material.CARROT);
                                    p.getInventory().removeItem(new ItemStack(Material.CARROT_ITEM, 1));
                                }

                            } else if (b.getType() == Material.CROPS) {
                                if (b.getData() == CropState.RIPE.getData()) {
                                    p.getInventory().addItem(UtilClans.updateNames(new ItemStack(Material.WHEAT, 1)));
                                    p.getInventory().addItem(UtilClans.updateNames(new ItemStack(Material.SEEDS, UtilMath.randomInt(1, 3))));
                                    p.getWorld().playEffect(b.getLocation(), Effect.STEP_SOUND, 3);
                                    p.getWorld().playEffect(b.getLocation(), Effect.STEP_SOUND, 3);
                                    p.getWorld().playEffect(b.getLocation(), Effect.STEP_SOUND, 3);
                                    b.setType(Material.CROPS);
                                    p.getInventory().removeItem(new ItemStack(Material.SEEDS, 1));
                                }
                            } else if (b.getType() == Material.NETHER_WARTS) {
                                NetherWarts n = (NetherWarts) b.getState().getData();
                                if (n.getState() == NetherWartsState.RIPE) {
                                    if (p.getInventory().firstEmpty() == -1) {
                                        p.getWorld().dropItem(b.getLocation(), new ItemStack(Material.NETHER_STALK, UtilMath.randomInt(2, 4)));
                                    } else {
                                        p.getInventory().addItem(UtilClans.updateNames(new ItemStack(Material.NETHER_STALK, UtilMath.randomInt(2, 4))));
                                    }

                                    p.getWorld().playEffect(b.getLocation(), Effect.STEP_SOUND, 3);
                                    p.getWorld().playEffect(b.getLocation(), Effect.STEP_SOUND, 3);
                                    p.getWorld().playEffect(b.getLocation(), Effect.STEP_SOUND, 3);
                                    b.setType(Material.NETHER_WARTS);
                                    p.getInventory().removeItem(new ItemStack(Material.NETHER_STALK, 1));
                                }
                            }

                        }
                    }
                }

                p.getItemInHand().setDurability((short) (p.getItemInHand().getDurability() + 1));
                if (p.getItemInHand().getDurability() >= 250) {
                    p.getInventory().setItemInHand(new ItemStack(Material.AIR));
                }
            }
        }

    }

}
