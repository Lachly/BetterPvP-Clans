package net.betterpvp.clans.clans;

import net.betterpvp.clans.Clans;
import net.betterpvp.clans.clans.events.ChunkClaimEvent;
import net.betterpvp.clans.clans.events.ClanDeleteEvent;
import net.betterpvp.clans.clans.mysql.AllianceRepository;
import net.betterpvp.clans.clans.mysql.ClanRepository;
import net.betterpvp.clans.clans.mysql.EnemyRepository;
import net.betterpvp.clans.clans.mysql.MemberRepository;
import net.betterpvp.clans.effects.EffectManager;
import net.betterpvp.clans.effects.EffectType;
import net.betterpvp.clans.gamer.Gamer;
import net.betterpvp.clans.gamer.GamerManager;
import net.betterpvp.clans.scoreboard.ScoreboardManager;
import net.betterpvp.clans.skills.selector.skills.ranger.Agility;
import net.betterpvp.core.client.Client;
import net.betterpvp.core.client.ClientUtilities;
import net.betterpvp.core.database.Log;
import net.betterpvp.core.punish.Punish.PunishType;
import net.betterpvp.core.punish.PunishManager;
import net.betterpvp.core.utility.UtilFormat;
import net.betterpvp.core.utility.UtilMessage;
import net.betterpvp.core.utility.UtilTime;
import net.betterpvp.core.utility.recharge.RechargeManager;
import org.bukkit.*;
import org.bukkit.entity.Player;

import java.util.*;

public class ClanUtilities {

    public static List<Clan> clans = new ArrayList<Clan>();

    public enum ClanRelation {

        PILLAGE(ChatColor.LIGHT_PURPLE, ChatColor.DARK_PURPLE),
        ALLY(ChatColor.GREEN, ChatColor.DARK_GREEN),
        ALLY_TRUST(ChatColor.DARK_GREEN, ChatColor.GREEN),
        ENEMY(ChatColor.RED, ChatColor.DARK_RED),
        SAFE(ChatColor.AQUA, ChatColor.DARK_AQUA),
        NEUTRAL(ChatColor.YELLOW, ChatColor.GOLD),
        SELF(ChatColor.AQUA, ChatColor.DARK_AQUA);

        private ChatColor primary;
        private ChatColor secondary;

        ClanRelation(ChatColor primary, ChatColor secondary) {
            this.primary = primary;
            this.secondary = secondary;
        }

        public ChatColor getPrimary() {
            return primary;
        }

        public String getPrimary(boolean bold) {
            return primary.toString() + ChatColor.BOLD;
        }

        public ChatColor getSecondary() {
            return secondary;
        }
    }

    public static boolean isClanMember(Player p, Player target) {
        Clan aClan = ClanUtilities.getClan(p);
        Clan bClan = ClanUtilities.getClan(target);

        if (aClan == null || bClan == null) return false;

        return aClan.getName().equalsIgnoreCase(bClan.getName());

    }

    public static ClanRelation getRelation(Clan clanA, Clan clanB) {
        if (clanA == null || clanB == null) {
            return ClanRelation.NEUTRAL;
        } else if (clanA == clanB) {
            return ClanRelation.SELF;
        } else if (clanB.hasTrust(clanA)) {
            return ClanRelation.ALLY_TRUST;
        } else if (clanA.isAllied(clanB)) {
            return ClanRelation.ALLY;
        } else if (clanA.isEnemy(clanB)) {
            return ClanRelation.ENEMY;
        } else if (Pillage.isPillaging(clanA, clanB)) {
            return ClanRelation.PILLAGE;
        } else if (Pillage.isPillaging(clanB, clanA)) {
            return ClanRelation.PILLAGE;
        }

        return ClanRelation.NEUTRAL;
    }

    public static boolean hasAccess(Player p, Location loc) {
        Clan locClan = getClan(loc);
        Clan pClan = getClan(p);
        if (locClan == null) return true;

        if (pClan != null) {
            if (Pillage.isPillaging(pClan, locClan)) {
                return true;
            }
        }

        return getRelation(pClan, locClan) != ClanRelation.ALLY
                && getRelation(pClan, locClan) != ClanRelation.PILLAGE
                && getRelation(pClan, locClan) != ClanRelation.ENEMY
                && getRelation(pClan, locClan) != ClanRelation.NEUTRAL;
    }

    public static Clan searchClan(Player caller, String clan, boolean inform) {
        LinkedList<Clan> matchList = new LinkedList<Clan>();
        for (Clan clans : clans) {
            if (clans.getName().equalsIgnoreCase(clan)) {
                return clans;
            }
            if (clans.getName().toLowerCase().contains(clan.toLowerCase())) {
                matchList.add(clans);
            }
        }
        if (matchList.size() != 1) {
            if (!inform) {
                return null;
            }

            UtilMessage.message(caller, "Clan Search", ChatColor.YELLOW.toString() + matchList.size() + ChatColor.GRAY
                    + " matches for [" + ChatColor.YELLOW + clan + ChatColor.GRAY + "].");

            if (matchList.size() > 0) {
                String matchString = "";
                for (Clan cur : matchList) {
                    matchString = matchString + ChatColor.YELLOW + cur.getName() + ChatColor.GRAY + ", ";
                }

                if (matchString.length() > 1) {
                    matchString = matchString.substring(0, matchString.length() - 2);
                }

                UtilMessage.message(caller, "Clan Search", ChatColor.GRAY + "Matches [" + ChatColor.YELLOW + matchString + ChatColor.GRAY + "].");
            }
            return null;
        }
        return matchList.get(0);
    }

    public static String getMembersList(Clan clan) {
        StringBuilder membersString = new StringBuilder();
        if (clan.getMembers() != null && !clan.getMembers().isEmpty()) {
            for (ClanMember member : clan.getMembers()) {
                Client client = ClientUtilities.getClient(member.getUUID());
                if (client != null) {
                    membersString.append(membersString.length() != 0 ? ChatColor.GRAY + ", " : "")
                            .append(ChatColor.YELLOW + member.getRoleIcon() + UtilFormat.getOnlineStatus(member.getUUID())
                                    + client.getName());
                }
            }
        }
        return membersString.toString();
    }

    public static String getAllianceList(Player player, Clan clan) {
        StringBuilder allyString = new StringBuilder();
        if (!clan.getAlliances().isEmpty()) {
            for (Alliance ally : clan.getAlliances()) {
                allyString.append(allyString.length() != 0 ? ChatColor.GRAY + ", " : "")
                        .append(getRelation(getClan(player), ally.getClan()).getPrimary() + ally.getClan().getName());
            }
        }
        return allyString.toString();
    }

    public static String getEnemyList(Player player, Clan clan) {
        StringBuilder enemyString = new StringBuilder();
        if (!clan.getEnemies().isEmpty()) {
            for (Dominance dom : clan.getEnemies()) {
                enemyString.append(enemyString.length() != 0 ? ChatColor.GRAY + ", " : "")
                        .append(getRelation(getClan(player), dom.getClan()).getPrimary() + dom.getClan().getName());
            }
        }
        return enemyString.toString();
    }

    public static String getEnemyListDom(Player player, Clan clan) {
        StringBuilder enemyString = new StringBuilder();
        if (!clan.getEnemies().isEmpty()) {
            for (Dominance dom : clan.getEnemies()) {
                enemyString.append(enemyString.length() != 0 ? ChatColor.GRAY + ", " : "")
                        .append(getRelation(getClan(player), dom.getClan()).getPrimary() + dom.getClan().getName()
                                + " " + clan.getDominanceString(dom.getClan()));
            }
        }
        return enemyString.toString();
    }

    public static List<String> getClanTooltip(Player player, Clan target) {
        List<String> list = new ArrayList<String>();
        Clan clan = getClan(player);
        list.add(ChatColor.RED + "[Clans] " + getRelation(clan, target).getPrimary() + target.getName() + " Information:");
        list.add(" Age: " + ChatColor.YELLOW + target.getAge());
        list.add(" Territory: " + ChatColor.YELLOW + target.getTerritory().size() + "/" + (target.getMembers().size() + 2));
        list.add(" Allies: " + ChatColor.YELLOW + getAllianceList(player, target));
        list.add(" Enemies: " + ChatColor.YELLOW + getEnemyList(player, target));
        list.add(" Members: " + ChatColor.YELLOW + getMembersList(target));
        return list;
    }

    public static Clan getClan(Player player) {
        return getClan(player.getUniqueId());
    }

    public static Clan getClan(UUID uuid) {
        for (Clan clan : clans) {
            for (ClanMember member : clan.getMembers()) {
                if (member.getUUID().equals(uuid)) {
                    return clan;
                }
            }
        }
        return null;
    }

    public static void addClan(Clan c) {
        clans.add(c);
    }

    public static void removeClan(Clan c) {
        clans.remove(c);
    }

    public static List<Clan> getClans() {
        return clans;
    }

    public static Clan getClan(String string) {


        for (Clan clan : clans) {
            if (clan.getName().equalsIgnoreCase(string)) {
                return clan;
            }
        }

        for (Client client : ClientUtilities.getClients()) {
            if (client.getName().equalsIgnoreCase(string)) {
                for (Clan clan : clans) {
                    for (ClanMember member : clan.getMembers()) {
                        if (member.getUUID().equals(client.getUUID())) {
                            return clan;
                        }
                    }
                }
            }
        }


        return null;
    }

    public static Clan getClan(Chunk chunk) {
        for (Clan clan : clans) {
            if (clan.getTerritory().contains(UtilFormat.chunkToFile(chunk))) {
                return clan;
            }
        }
        return null;
    }

    public static Clan getClan(Location location) {
        return getClan(location.getChunk());
    }

    public static void disbandClan(Player player, Clan clan) {

        UtilMessage.broadcast("Clans", ChatColor.YELLOW + player.getName() + ChatColor.GRAY + " disbanded "
                + ChatColor.YELLOW + "Clan " + clan.getName() + ChatColor.GRAY + ".");

        for (String s : clan.getTerritory()) {
            Chunk c = UtilFormat.stringToChunk(s);
            if (c != null) {
                Bukkit.getPluginManager().callEvent(new ChunkClaimEvent(c));
            }
        }


        Iterator<Alliance> allies = clan.getAlliances().iterator();
        while (allies.hasNext()) {
            Alliance ally = allies.next();
            ally.getClan().getAlliances().remove(ally.getClan().getAlliance(clan));
            AllianceRepository.deleteAlly(clan, ally.getClan());
            AllianceRepository.deleteAlly(ally.getClan(), clan);
            allies.remove();
        }

        Log.write("Clans", clan.getName() + " was disbanded.");
        Log.write("Clans", ChatColor.stripColor(getEnemyListDom(player, clan)));
        if (clan.getEnemies().size() > 0) {
            UtilMessage.broadcast("Clans", "Dominance on disband: " + getEnemyListDom(player, clan));
        }
        Iterator<Dominance> iterator = clan.getEnemies().iterator();
        while (iterator.hasNext()) {
            Dominance next = iterator.next();
            next.getClan().getEnemies().remove(next.getClan().getDominance(clan));
            EnemyRepository.deleteEnemy(next);
            iterator.remove();
        }

        Iterator<ClanMember> members = clan.getMembers().iterator();
        while (members.hasNext()) {
            ClanMember next = members.next();
            if (Bukkit.getPlayer(next.getUUID()) != null) {
                RechargeManager.getInstance().add(Bukkit.getPlayer(next.getUUID()), "Create Clan", 300, true, false);
            }
            MemberRepository.deleteMember(next);
            members.remove();


        }


        //ClanRepository.removeDynmap(clan);
      //  ScoreboardManager.removeClan(clan.getName());
        ClanRepository.deleteClan(clan);

        ClanUtilities.getClans().remove(clan);


    }

    public static void disbandClan(Clan clan) {

        for (String s : clan.getTerritory()) {
            Chunk c = UtilFormat.stringToChunk(s);
            if (c != null) {
                Bukkit.getPluginManager().callEvent(new ChunkClaimEvent(c));
            }
        }


        Iterator<Alliance> allies = clan.getAlliances().iterator();
        while (allies.hasNext()) {
            Alliance ally = allies.next();
            ally.getClan().getAlliances().remove(ally.getClan().getAlliance(clan));
            AllianceRepository.deleteAlly(clan, ally.getClan());
            AllianceRepository.deleteAlly(ally.getClan(), clan);
            allies.remove();
        }


        Log.write("Clans", clan.getName() + " was disbanded.");
        Iterator<Dominance> iterator = clan.getEnemies().iterator();
        while (iterator.hasNext()) {
            Dominance next = iterator.next();
            next.getClan().getEnemies().remove(next.getClan().getDominance(clan));
            EnemyRepository.deleteEnemy(next);
            iterator.remove();
        }

        Iterator<ClanMember> members = clan.getMembers().iterator();
        while (members.hasNext()) {
            ClanMember next = members.next();
            if (Bukkit.getPlayer(next.getUUID()) != null) {

                RechargeManager.getInstance().add(Bukkit.getPlayer(next.getUUID()), "Create Clan", 300, true, false);
            }
            MemberRepository.deleteMember(next);
            members.remove();


        }

        //ClanRepository.removeDynmap(clan);
       // ScoreboardManager.removeClan(clan.getName());
        ClanRepository.deleteClan(clan);
    }

    public static int getHoursOfEnergy(Clan clan) {
        int count = 0;
        if (clan.getTerritory().size() > 0) {
            count = (int) clan.getEnergy() / (clan.getTerritory().size() * 25);
        }

        return count;
    }

    public static long getEnergyTime(Clan clan) {
        return getHoursOfEnergy(clan) * 3600000;
    }

    public static boolean isInTerritory(Player p, String clan) {
        Clan c = ClanUtilities.getClan(clan);
        Clan d = ClanUtilities.getClan(p.getLocation());
        if (c == null || d == null) {
            return false;
        }

        return c == d;


    }

    public static void sort() {

        Collections.sort(clans, new Comparator<Clan>() {

            @Override
            public int compare(Clan a, Clan b) {

                return b.getPoints() - a.getPoints();
            }

        });

    }

    public static boolean canCast(Player p) {
        if (getClan(p.getLocation()) != null) {
            if (getClan(p.getLocation()) instanceof AdminClan) {
                AdminClan a = (AdminClan) getClan(p.getLocation());
                if (a.isSafe()) {
                    UtilMessage.message(p, "Restriction", "You are not allowed to cast abilities here!");
                    return false;
                }
            }
        }

        return true;
    }

    public static boolean canHurt(Player player, Player target) {
        if (target.getGameMode() == GameMode.CREATIVE || target.getGameMode() == GameMode.SPECTATOR) {
            return false;
        }


        if (player.equals(target)) {
            return false;
        }


        if (PunishManager.getPunish(player.getUniqueId(), PunishType.PVPLock) != null) {
            return false;
        }

        if (Agility.getActive().contains(target.getUniqueId())) {
            return false;
        }


        if (Clans.getOptions().isFNG()) {
            return true;
        }



        if (EffectManager.hasEffect(target, EffectType.PROTECTION) || EffectManager.hasEffect(player, EffectType.PROTECTION)) {
            return false;
        }


        if (getRelation(getClan(player), getClan(target)) == ClanRelation.SELF
                || getRelation(getClan(player), getClan(target)) == ClanRelation.ALLY
                || getRelation(getClan(player), getClan(target)) == ClanRelation.ALLY_TRUST) {
            return false;
        }


        Gamer gamer = GamerManager.getOnlineGamer(target);

        if (gamer != null) {
            if (!UtilTime.elapsed(gamer.getLastDamaged(), 15000)) {
                return true;
            }


            Clan targetClan = getClan(target.getLocation());

            if (targetClan != null) {
                if (targetClan instanceof AdminClan) {
                    AdminClan ac = (AdminClan) targetClan;
                    if (ac.isSafe()) {
                        if (ac.getName().contains("Spawn")) {
                            if (target.getLocation().getY() < 100) {
                                return !UtilTime.elapsed(gamer.getLastDamaged(), 60000);
                            }
                        }
                        return false;
                    }

                }
            }
            Clan playerClan = getClan(player.getLocation());
            if (playerClan != null) {
                if (playerClan instanceof AdminClan) {
                    AdminClan ac = (AdminClan) playerClan;
                    return !ac.isSafe();
                }
            }
        }


        return true;
    }

    public static List<String> territoryToString(List<Chunk> territory) {
        List<String> chunks = new ArrayList<String>();
        for (Chunk chunk : territory) {
            chunks.add(UtilFormat.chunkToFile(chunk));
        }

        return chunks;
    }
}
