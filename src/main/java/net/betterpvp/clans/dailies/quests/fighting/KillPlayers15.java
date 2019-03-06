package net.betterpvp.clans.dailies.quests.fighting;

import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;

import net.betterpvp.clans.Clans;
import net.betterpvp.clans.dailies.progression.Progress;
import net.betterpvp.clans.dailies.progression.types.GeneralProgression;
import net.betterpvp.clans.dailies.quests.General;
import net.betterpvp.clans.gamer.combat.CombatLogs;
import net.betterpvp.clans.gamer.combat.LogManager;
import net.md_5.bungee.api.ChatColor;

public class KillPlayers15 extends General{

	public KillPlayers15(Clans i) {
		super(i, "Kill 15 Players", new String[]{
				ChatColor.GRAY + "Kill a total of " + ChatColor.GREEN + "15" + ChatColor.GRAY + " players."
		});

	}

	@Override
	public int getRequiredAmount() {
		// TODO Auto-generated method stub
		return 15;
	}


	@EventHandler
	public void onKill(PlayerDeathEvent e){
		if(isActive()){
			CombatLogs cl = LogManager.getKiller(e.getEntity());
			if(cl != null){
				Progress p = getQuestProgression(cl.getDamager().getUniqueId(), getName());
				if(p != null){
					if(!p.isComplete()){
						if(p instanceof GeneralProgression){
							GeneralProgression gp = (GeneralProgression) p;
							gp.addCurrentAmount();

							if(gp.getCurrentAmount() >= gp.getRequiredAmount()){
								gp.onComplete(cl.getDamager().getUniqueId());
							}
						}
					}
				}

			}
		}
	}

}