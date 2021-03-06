package net.betterpvp.clans.skills.selector.skills.assassin;

import net.betterpvp.clans.Clans;
import net.betterpvp.clans.classes.Energy;
import net.betterpvp.clans.classes.events.CustomDamageEvent;
import net.betterpvp.clans.skills.Types;
import net.betterpvp.clans.skills.selector.skills.Skill;
import net.betterpvp.core.framework.UpdateEvent;
import net.betterpvp.core.framework.UpdateEvent.UpdateType;
import net.betterpvp.core.utility.UtilBlock;
import net.betterpvp.core.utility.UtilMessage;
import net.betterpvp.core.utility.UtilTime;
import net.betterpvp.core.utility.UtilVelocity;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.WeakHashMap;

public class Evade extends Skill {

    private Set<Player> evading = new HashSet<>();
    private WeakHashMap<Player, Long> gap = new WeakHashMap<>();
    private WeakHashMap<Player, Long> delay = new WeakHashMap<>();


    public Evade(Clans i) {
        super(i, "Evade", "Assassin", getSwords,
                rightClick, 3,
                false, true);

    }

    @EventHandler
    public void onDamage(CustomDamageEvent e) {
        if (e.getDamager() instanceof Player) {
            if (evading.contains((Player) e.getDamager())) {
                e.setCancelled("Skill: Evade");
            }
        }
    }


    @Override
    public String[] getDescription(int level) {

        return new String[]{
                "Right click with Sword to toggle.",
                "",
                "While evading you block attacks, and",
                "teleport behind the attacker.",
                "Crouch and Evade to teleport backwards.",
                "",
                "2 second internal cooldown.",
                "",
                "Energy / second: " + ChatColor.GREEN + (getEnergy(level))};
    }

    @Override
    public double getRecharge(int level) {

        return 2;
    }

    @Override
    public float getEnergy(int level) {

        return (float) (6 - ((level - 1) * 1));
    }

    @Override
    public Types getType() {

        return Types.SWORD;
    }

    @Override
    public void activateSkill(Player p) {
        if (!evading.contains(p)) {

            evading.add(p);
            gap.put(p, System.currentTimeMillis());

        }
    }


    @Override
    public boolean usageCheck(Player player) {
        if (player.getLocation().getBlock().getType() == Material.WATER
                || player.getLocation().getBlock().getType() == Material.STATIONARY_WATER) {
            UtilMessage.message(player, "Skill", "You cannot use " + ChatColor.GREEN
                    + getName() + ChatColor.GRAY + " in water.");
            return false;
        }
        return true;
    }


    @EventHandler
    public void onEvade(CustomDamageEvent e) {
        if (e.getCause() != DamageCause.ENTITY_ATTACK) return;
        if (e.getDamagee() instanceof Player) {
            if (e.getDamager() instanceof LivingEntity) {
                LivingEntity ent = (LivingEntity) e.getDamager();
                Player p = (Player) e.getDamagee();


                if (evading.contains(p)) {

                    if (hasSkill(p, this)) {

                        if (ent != null) {
                            if (!delay.containsKey(p)) {
                                delay.put(p, 0L);
                            }

                            e.setKnockback(false);
                            e.setCancelled("Skill Evade");
                            if (UtilTime.elapsed(delay.get(p), 500)) {
                                for (int i = 0; i < 3; i++) {
                                    p.getWorld().playEffect(p.getLocation(), Effect.SMOKE, 5);
                                }
                                Location target = null;
                                if (p.isSneaking()) {
                                    target = FindLocationBack(ent, p);
                                } else {
                                    target = FindLocationBehind(ent, p);
                                }
                                if (target != null) {
                                    p.teleport(target);
                                }

                                UtilMessage.message(p, getClassType(), "You used " + getName());
                                if (ent instanceof Player) {
                                    Player temp = (Player) ent;
                                    UtilMessage.message(temp, getName(), p.getName() + " used evade!");
                                }

                                delay.put(p, System.currentTimeMillis());
                            }
                        }

                    }
                }


            }
        }

        if (e.getDamager() instanceof Player) {
            Player d = (Player) e.getDamager();
            if (evading.contains(d)) {
                if (hasSkill(d, this)) {
                    e.setCancelled("Cant attack while evading");
                }
            }
        }

    }

    @EventHandler
    public void onUpdateEffect(UpdateEvent e) {
        if (e.getType() == UpdateEvent.UpdateType.TICK_2) {
            Iterator<Player> it = evading.iterator();
            while (it.hasNext()) {
                Player p = it.next();
                if (p.isBlocking()) {
                    p.getWorld().playEffect(p.getLocation(), Effect.STEP_SOUND, 7);
                }
            }
        }
    }

    @EventHandler
    public void onUpdate(UpdateEvent e) {
        if (e.getType() == UpdateType.TICK) {
            Iterator<Player> it = evading.iterator();
            while (it.hasNext()) {
                Player p = it.next();
                if (p.isBlocking()) {
                    if (!Energy.use(p, getName(), getEnergy(getLevel(p)) / 2, true)) {
                        it.remove();
                    } else if (!hasSkill(p, this)) {
                        it.remove();
                    } else if (!hasSwordInMainHand(p)) {
                        it.remove();
                    } else {


                    }
                } else {
                    if (gap.containsKey(p)) {
                        if (UtilTime.elapsed(gap.get(p), 250)) {
                            gap.remove(p);
                            it.remove();

                        }
                    }

                }
            }

        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        evading.remove(e.getEntity());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        evading.remove(e.getPlayer());
    }

    private Location FindLocationBehind(LivingEntity damager, Player damagee) {
        double curMult = 0.0D;
        double maxMult = 1.5D;

        double rate = 0.1D;

        Location lastValid = damager.getLocation();
        Location lastValid2 = damagee.getLocation();
        while (curMult <= maxMult) {
            Vector vec = UtilVelocity.getTrajectory(damagee, damager).multiply(curMult);
            Location loc = damagee.getLocation().add(vec);


            if (loc.getBlock().getType().name().contains("DOOR") || loc.getBlock().getType().name().contains("GATE")) {

                return lastValid2;
            }


            if ((!UtilBlock.airFoliage(loc.getBlock())) || (!UtilBlock.airFoliage(loc.getBlock().getRelative(BlockFace.UP)))) {

                Block b2 = loc.add(0, 1, 0).getBlock();
                if (UtilBlock.airFoliage(b2) && UtilBlock.airFoliage(b2.getRelative(BlockFace.UP))) {

                    break;
                }

                return lastValid2;
            }


            curMult += rate;
        }

        curMult = 0.0D;

        while (curMult <= maxMult) {
            Vector vec = UtilVelocity.getTrajectory(damager, damagee).multiply(curMult);
            Location loc = damager.getLocation().subtract(vec);

            if (loc.getBlock().getType().name().contains("DOOR") || loc.getBlock().getType().name().contains("GATE")) {
                UtilVelocity.velocity(damagee, UtilVelocity.getTrajectory(damagee, damager), 0.3, false, 0, 0.1, 0.2, false);
                return lastValid;
            }

            if ((!UtilBlock.airFoliage(loc.getBlock())) || (!UtilBlock.airFoliage(loc.getBlock().getRelative(BlockFace.UP)))) {
                return lastValid;
            }
            lastValid = loc;

            curMult += rate;
        }

        return lastValid;
    }

    private Location FindLocationBack(LivingEntity damager, Player damagee) {
        double curMult = 0.0D;
        double maxMult = 3.0D;

        double rate = 0.1D;

        Location lastValid = damagee.getLocation();

        while (curMult <= maxMult) {

            Vector vec = UtilVelocity.getTrajectory(damager, damagee).multiply(curMult);
            Location loc = damagee.getLocation().add(vec);

            if (loc.getBlock().getType().name().contains("DOOR") || loc.getBlock().getType().name().contains("GATE")) {
                UtilVelocity.velocity(damagee, UtilVelocity.getTrajectory(damagee, damager), 0.3, false, 0, 0.1, 0.2, false);
                return lastValid;
            }


            if ((!UtilBlock.airFoliage(loc.getBlock())) || (!UtilBlock.airFoliage(loc.getBlock().getRelative(BlockFace.UP)))) {
                return lastValid;
            }
            lastValid = loc;

            curMult += rate;
        }

        return lastValid;
    }


}
