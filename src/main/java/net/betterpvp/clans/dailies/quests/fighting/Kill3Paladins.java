package net.betterpvp.clans.dailies.quests.fighting;

import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;

import net.betterpvp.clans.Clans;
import net.betterpvp.clans.classes.Role;
import net.betterpvp.clans.dailies.progression.Progress;
import net.betterpvp.clans.dailies.progression.types.GeneralProgression;
import net.betterpvp.clans.dailies.quests.General;
import net.betterpvp.clans.gamer.combat.CombatLogs;
import net.betterpvp.clans.gamer.combat.LogManager;
import net.md_5.bungee.api.ChatColor;

public class Kill3Paladins extends General{

	public Kill3Paladins(Clans i) {
		super(i, "Kill 3 Paladins", new String[]{
				ChatColor.GRAY + "Kill a total of " + ChatColor.GREEN + "3" + ChatColor.GRAY + " Paladins."
		});

	}

	@Override
	public int getRequiredAmount() {
		// TODO Auto-generated method stub
		return 3;
	}


	@EventHandler
	public void onKill(PlayerDeathEvent e){
		if(isActive()){
			CombatLogs cl = LogManager.getKiller(e.getEntity());
			if(cl != null){
				if(Role.getRole(e.getEntity()) != null && Role.getRole(e.getEntity()).getName().equals("Paladin")){
					Progress p = getQuestProgression(cl.getDamager().getUniqueId(), getName());

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
