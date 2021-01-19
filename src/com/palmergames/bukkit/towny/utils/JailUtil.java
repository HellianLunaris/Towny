package com.palmergames.bukkit.towny.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.palmergames.bukkit.towny.TownyEconomyHandler;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.event.resident.ResidentUnjailEvent;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.Translation;
import com.palmergames.bukkit.towny.object.jail.Jail;
import com.palmergames.bukkit.towny.object.jail.JailReason;
import com.palmergames.bukkit.towny.object.jail.UnJailReason;
import com.palmergames.bukkit.util.BookFactory;

public class JailUtil {

	
	
	/**
	 * Jails a resident.
	 * 
	 * @param resident Resident being jailed.
	 * @param town Town resident is being jailed by.
	 * @param jail Jail resident is being jailed into.
	 * @param hours Hours resident is jailed for.
	 * @param reason JailReason resident is jailed for.
	 * @param jailer Resident who did the jailing or null. (For Ticket #4096.)
	 */
	public static void jailResident(Resident resident, Town town, Jail jail, int hours, JailReason reason, Resident jailer) {
		sendJailedBookToResident(resident.getPlayer(), reason);		
	}

	/**
	 * Unjails a resident.
	 * 
	 * @param resident Resident being unjailed.
	 * @param reason UnJailReason the resident is unjailed for.
	 */
	public static void unJailResident(Resident resident, UnJailReason reason) {
		
		Jail jail = resident.getJail();
		String jailNumber = "Placeholder";
		switch (reason) {
		case ESCAPE:
			Town town = null;
			try {
				town = resident.getTown();
			} catch (NotRegisteredException ignored) {}
			
			// First show a message to the resident, either by broadcasting to the resident's town or just the resident (if they have no town.)
			if (town != null)
				TownyMessaging.sendPrefixedTownMessage(town, Translation.of("msg_player_escaped_jail_into_wilderness", resident.getName(), jail.getTownBlock().getWorld().getUnclaimedZoneName()));
			else 
				TownyMessaging.sendMsg(resident, Translation.of("msg_you_have_been_freed_from_jail"));
			
			// Second, show a message to the town which has just had a prisoner escape.
			if (!resident.hasTown() || (town != null && !town.equals(jail.getTown())))
				TownyMessaging.sendPrefixedTownMessage(jail.getTown(), Translation.of("msg_player_escaped_jail_into_wilderness", resident.getName(), jail.getTownBlock().getWorld().getUnclaimedZoneName()));
			break;

		case BAIL:
			break;
		case PARDONED:
			TownyMessaging.sendMsg(resident, Translation.of("msg_you_have_been_freed_from_jail"));
			TownyMessaging.sendPrefixedTownMessage(jail.getTown(), Translation.of("msg_player_has_been_freed_from_jail_number", resident.getName(), jailNumber));
			break;
		case SENTENCE_SERVED:
			break;
		case LEFT_TOWN:
			break;
		case JAIL_DELETED:
			break;
		case JAILBREAK:
			break;
		}

		resident.setJailed(false);
		resident.save();
		
		Bukkit.getPluginManager().callEvent(new ResidentUnjailEvent(resident));
	}

	
	/**
	 * A wonderful little handbook to help out the sorry jailed person.
	 * 
	 * @param player Player who will receive a book.
	 * @param reason JailReason the player is in jail for.
	 */
	private static void sendJailedBookToResident(Player player, JailReason reason) {
		
		/*
		 * A nice little book for the not so nice person in jail.
		 */
		String pages = "Looks like you've been jailed, for the reason " + reason.getCause();
		pages += "That's too bad huh. Well what can you do about it? Here's some helpful tips for you while you serve your sentence.\n\n";
		pages += "You have been jailed for " + reason.getHours() + " hours. By serving your sentence you will become free.\n\n";
		if (TownySettings.JailDeniesTownLeave())
			pages += "While you're jailed you won't be able to leave your town to escape jail.\n";
		else
			pages += "You can escape from jail by leaving your town using /town leave.\n";
		if (TownySettings.isAllowingBail()) {
			pages += "You can also pay your bail using '/res jail paybail' to be freed from jail.";
			double cost = TownySettings.getBailAmount();
			Resident resident = TownyUniverse.getInstance().getResident(player.getUniqueId());
			if (resident.isMayor())
				cost = TownySettings.getBailAmountMayor();
			if (resident.isKing())
				cost = TownySettings.getBailAmountKing();
			pages += "Bail will cost: " + TownyEconomyHandler.getFormattedBalance(cost) + "\n\n";
		}
		pages += "If you must persist and make efforts to escape, if you make it to the wilderness you will also earn your freedom.";
		pages += "But don't die before you reach the wilderness or you'll end up right back in jail.";
		if (TownySettings.JailAllowsEnderPearls())
			pages += "Luckily, enderpearls are allowed to be used to help you escape, now you've just got to get a hold of some.";
		pages +="\n\n";
		if (reason.equals(JailReason.PRISONER_OF_WAR))
			pages += "As a prisoner of war you will be freed if your countrymen can reduce the jail plot's HP to 0, or if the town you are jailed in is removed from the war.";
		
		/*
		 * Send the book off to the BookFactory to be made.
		 */
		player.getInventory().addItem(new ItemStack(BookFactory.makeBook("So you've been jailed :(", "Towny Jailco.", pages)));
	}
}
