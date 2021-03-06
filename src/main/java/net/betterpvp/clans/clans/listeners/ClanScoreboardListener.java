package net.betterpvp.clans.clans.listeners;

import net.betterpvp.clans.Clans;
import net.betterpvp.clans.clans.Clan;
import net.betterpvp.clans.clans.ClanUtilities;
import net.betterpvp.clans.gamer.Gamer;
import net.betterpvp.clans.gamer.GamerManager;
import net.betterpvp.clans.worldevents.WEManager;

import net.betterpvp.clans.worldevents.WorldEvent;
import net.betterpvp.core.framework.BPVPListener;
import net.betterpvp.core.framework.UpdateEvent;
import net.betterpvp.core.framework.UpdateEvent.UpdateType;
import net.betterpvp.core.utility.UtilFormat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public class ClanScoreboardListener extends BPVPListener<Clans> {

    public ClanScoreboardListener(Clans instance) {
        super(instance);

    }

    @EventHandler
    public void onUpdate(UpdateEvent e) {
        if (e.getType() == UpdateType.SEC) {

            new BukkitRunnable() {
                @Override
                public void run() {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        Scoreboard scoreboard = p.getScoreboard();
                        if (scoreboard != null) {

                            Gamer gamer = GamerManager.getOnlineGamer(p);


                            if (scoreboard.getObjective(DisplaySlot.SIDEBAR) != null) {
                                scoreboard.getObjective(DisplaySlot.SIDEBAR).unregister();
                            }


                            if (gamer != null) {
                                if (gamer.getClient().getSettingAsBoolean("Sidebar")) {
                                    Objective side = scoreboard.registerNewObjective("BetterPvP", "dummy");
                                    side.setDisplaySlot(DisplaySlot.SIDEBAR);
                                    side.setDisplayName(ChatColor.GOLD.toString() + ChatColor.BOLD + "  2020 Season 1  ");

                                    Clan clan = ClanUtilities.getClan(p);

                                    if (clan != null) {
                                        side.getScore(ChatColor.YELLOW.toString() + ChatColor.BOLD + "Clan").setScore(15);
                                        String name = clan.getName() + "§r";

                                        side.getScore(ChatColor.AQUA.toString() + ChatColor.BOLD.toString() + name + "").setScore(14);
                                        side.getScore("§r").setScore(13);

                                        if (clan.getTerritory().size() > 0) {
                                            side.getScore(ChatColor.YELLOW.toString() + ChatColor.BOLD + "Clan Energy").setScore(12);
                                            side.getScore(ChatColor.GREEN.toString() + ChatColor.BOLD + (ClanUtilities.getHoursOfEnergy(clan)) + " hours").setScore(11);
                                            side.getScore("§r§r").setScore(10);
                                        }
                                    }


                                    side.getScore(ChatColor.YELLOW.toString() + ChatColor.BOLD + "Coins").setScore(9);
                                    side.getScore(ChatColor.GOLD.toString() + ChatColor.BOLD + UtilFormat.formatNumber(gamer.getCoins())).setScore(8);

                                    side.getScore("§r§r§r").setScore(7);
                                    side.getScore(ChatColor.YELLOW.toString() + ChatColor.BOLD + "Territory").setScore(6);
                                    Clan standing = ClanUtilities.getClan(p.getLocation());
                                    if (standing != null) {
                                        side.getScore(ClanUtilities.getRelation(clan, standing).getPrimary(true) + standing.getName()).setScore(5);
                                    } else {
                                        side.getScore(ChatColor.GREEN.toString() + ChatColor.BOLD + "Wilderness").setScore(5);
                                    }

                                    if (WEManager.isWorldEventActive()) {
                                        WorldEvent we = WEManager.getActiveWorldEvent();

                                        side.getScore("§r§r§r§r").setScore(4);

                                        side.getScore(ChatColor.WHITE.toString() + ChatColor.BOLD + "World Event").setScore(3);
                                        side.getScore(we.getDisplayName()).setScore(2);

                                    }
                                }
                            }
                        }
                    }
                }
            }.runTaskAsynchronously(getInstance());

        }
    }

}
