package com.palmergames.bukkit.towny.db;

import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.exceptions.AlreadyRegisteredException;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.TownyUniverse;
import com.palmergames.bukkit.towny.object.TownyWorld;
import com.palmergames.bukkit.towny.regen.PlotBlockData;
import com.palmergames.bukkit.towny.war.siegewar.Siege;
import com.palmergames.bukkit.towny.war.siegewar.SiegeType;
import com.palmergames.util.FileMgmt;

import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

//import java.util.Hashtable;
//import com.palmergames.bukkit.towny.TownySettings;

/*
 * --- : Loading process : ---
 *
 * Load all the names/keys for each world, nation, town, and resident.
 * Load each world, which loads it's town blocks.
 * Load nations, towns, and residents.
 */

/*
 * Loading Towns:
 * Make sure to load TownBlocks, then HomeBlock, then Spawn.
 */

public abstract class TownyDataSource {

	protected final Lock lock = new ReentrantLock();

	protected TownyUniverse universe;
	protected Towny plugin;
	protected boolean firstRun = true;

	public void initialize(Towny plugin, TownyUniverse universe) {

		this.universe = universe;
		this.plugin = plugin;
	}

	public synchronized void backup() throws IOException {
		//place holder to be overridden
	}

	public synchronized void cleanupBackups() {

		long deleteAfter = TownySettings.getBackupLifeLength();
		if (deleteAfter >= 0)
			FileMgmt.deleteOldBackups(new File(universe.getRootFolder() + FileMgmt.fileSeparator() + "backup"), deleteAfter);

	}

	public synchronized void deleteUnusedResidentFiles() {
		//place holder to be overridden
	}

	public boolean confirmContinuation(String msg) {

		Boolean choice = null;
		String input = null;
		while (choice == null) {
			System.out.println(msg);
			System.out.print("    Continue (y/n): ");
			Scanner in = new Scanner(System.in);
			input = in.next();
			input = input.toLowerCase();
			if (input.equals("y") || input.equals("yes")) {
				in.close();
				return true;
			} else if (input.equals("n") || input.equals("no")) {
				in.close();
				return false;
			}
			in.close();

		}
		System.out.println("[Towny] Error recieving input, exiting.");
		return false;
	}

	public boolean loadAll() {

		return loadWorldList() && loadNationList() && loadTownList() && loadSiegeList() && loadResidentList() && loadTownBlockList() && loadWorlds() && loadNations() && loadTowns() && loadSieges() && loadResidents() && loadTownBlocks() && loadRegenList() && loadSnapshotList();
	}

	public boolean saveAll() {

		return saveWorldList() && saveNationList() && saveTownList() && saveSiegeList() && saveResidentList() && saveTownBlockList() && saveWorlds() && saveNations() && saveTowns() && saveSieges() && saveResidents() && saveAllTownBlocks() && saveRegenList() && saveSnapshotList();
	}

	public boolean saveAllWorlds() {

		return saveWorldList() && saveWorlds();
	}

	public boolean saveQueues() {

		return saveRegenList() && saveSnapshotList();
	}

	abstract public void cancelTask();

	abstract public boolean loadTownBlockList();

	abstract public boolean loadResidentList();

	abstract public boolean loadTownList();

	abstract public boolean loadNationList();

	abstract public boolean loadSiegeList();

	abstract public boolean loadWorldList();

	abstract public boolean loadRegenList();

	abstract public boolean loadSnapshotList();

	abstract public boolean loadTownBlocks();

	abstract public boolean loadResident(Resident resident);

	abstract public boolean loadTown(Town town);

	abstract public boolean loadNation(Nation nation);

	abstract public boolean loadSiege(Siege siege);

	abstract public boolean loadWorld(TownyWorld world);

	abstract public boolean saveTownBlockList();

	abstract public boolean saveResidentList();

	abstract public boolean saveTownList();

	abstract public boolean saveNationList();

	abstract public boolean saveSiegeList();

	abstract public boolean saveWorldList();

	abstract public boolean saveRegenList();

	abstract public boolean saveSnapshotList();

	abstract public boolean saveResident(Resident resident);

	abstract public boolean saveTown(Town town);

	abstract public boolean saveNation(Nation nation);

	abstract public boolean saveSiege(Siege siege);

	abstract public boolean saveWorld(TownyWorld world);

	abstract public boolean saveAllTownBlocks();

	abstract public boolean saveTownBlock(TownBlock townBlock);

	abstract public boolean savePlotData(PlotBlockData plotChunk);

	abstract public PlotBlockData loadPlotData(String worldName, int x, int z);

	abstract public PlotBlockData loadPlotData(TownBlock townBlock);

	abstract public void deletePlotData(PlotBlockData plotChunk);

	abstract public void deleteResident(Resident resident);

	abstract public void deleteTown(Town town);

	abstract public void deleteNation(Nation nation);

	abstract public void deleteSiege(Siege siege);

	abstract public void deleteWorld(TownyWorld world);

	abstract public void deleteTownBlock(TownBlock townBlock);

	abstract public void deleteFile(String file);

	/*
	 * public boolean loadWorldList() {
	 * return loadServerWorldsList();
	 * }
	 *
	 * public boolean loadServerWorldsList() {
	 * sendDebugMsg("Loading Server World List");
	 * for (World world : plugin.getServer().getWorlds())
	 * try {
	 * //String[] split = world.getName().split("/");
	 * //String worldName = split[split.length-1];
	 * //universe.newWorld(worldName);
	 * universe.newWorld(world.getName());
	 * } catch (AlreadyRegisteredException e) {
	 * e.printStackTrace();
	 * } catch (NotRegisteredException e) {
	 * e.printStackTrace();
	 * }
	 * return true;
	 * }
	 */

	/*
	 * Load all of category
	 */

	public boolean cleanup() {

		return true;

	}

	public boolean loadResidents() {

		TownyMessaging.sendDebugMsg("Loading Residents");

		List<Resident> toRemove = new ArrayList<>();

		for (Resident resident : new ArrayList<>(getResidents()))
			if (!loadResident(resident)) {
				System.out.println("[Towny] Loading Error: Could not read resident data '" + resident.getName() + "'.");
				toRemove.add(resident);
				//return false;
			}

		// Remove any resident which failed to load.
		for (Resident resident : toRemove) {
			System.out.println("[Towny] Loading Error: Removing resident data for '" + resident.getName() + "'.");
			removeResidentList(resident);
		}

		return true;
	}

	public boolean loadTowns() {

		TownyMessaging.sendDebugMsg("Loading Towns");
		for (Town town : getTowns())
			if (!loadTown(town)) {
				System.out.println("[Towny] Loading Error: Could not read town data " + town.getName() + "'.");
				return false;
			}
		return true;
	}

	public boolean loadNations() {

		TownyMessaging.sendDebugMsg("Loading Nations");
		for (Nation nation : getNations())
			if (!loadNation(nation)) {
				System.out.println("[Towny] Loading Error: Could not read nation data '" + nation.getName() + "'.");
				return false;
			}
		return true;
	}

	public boolean loadSieges() {

		TownyMessaging.sendDebugMsg("Loading Sieges");
		for (Siege siege : getSieges())
			if (!loadSiege(siege)) {
				System.out.println("[Towny] Loading Error: Could not read siege data '" + siege.getDefendingTown().getName() + "'.");
				return false;
			}
		return true;
	}


	public boolean loadWorlds() {

		TownyMessaging.sendDebugMsg("Loading Worlds");
		for (TownyWorld world : getWorlds())
			if (!loadWorld(world)) {
				System.out.println("[Towny] Loading Error: Could not read world data '" + world.getName() + "'.");
				return false;
			} else {
				// Push all Towns belonging to this world
			}
		return true;
	}

	/*
	 * Save all of category
	 */

	public boolean saveResidents() {

		TownyMessaging.sendDebugMsg("Saving Residents");
		for (Resident resident : getResidents())
			saveResident(resident);
		return true;
	}

	public boolean saveTowns() {

		TownyMessaging.sendDebugMsg("Saving Towns");
		for (Town town : getTowns())
			saveTown(town);
		return true;
	}

	public boolean saveNations() {

		TownyMessaging.sendDebugMsg("Saving Nations");
		for (Nation nation : getNations())
			saveNation(nation);
		return true;
	}

	public boolean saveSieges() {

		TownyMessaging.sendDebugMsg("Saving Sieges");
		for (Siege siege : getSieges())
			saveSiege(siege);
		return true;
	}

	public boolean saveWorlds() {

		TownyMessaging.sendDebugMsg("Saving Worlds");
		for (TownyWorld world : getWorlds())
			saveWorld(world);
		return true;
	}

	// Database functions
	abstract public List<Resident> getResidents(Player player, String[] names);

	abstract public List<Resident> getResidents();

	abstract public List<Resident> getResidents(String[] names);

	abstract public Resident getResident(String name) throws NotRegisteredException;

	abstract public void removeResidentList(Resident resident);

	abstract public void removeNation(Nation nation);

	abstract public boolean hasResident(String name);

	abstract public boolean hasTown(String name);

	abstract public boolean hasNation(String name);

	abstract public List<Town> getTowns(String[] names);

	abstract public List<Town> getTowns();

	abstract public Town getTown(String name) throws NotRegisteredException;

	abstract public Town getTown(UUID uuid) throws NotRegisteredException;

	abstract public List<Nation> getNations(String[] names);

	abstract public List<Nation> getNations();

	abstract public List<Siege> getSieges();

	abstract public Nation getNation(String name) throws NotRegisteredException;

	abstract public Nation getNation(UUID uiid) throws NotRegisteredException;

	abstract public TownyWorld getWorld(String name) throws NotRegisteredException;

	abstract public List<TownyWorld> getWorlds();

	abstract public TownyWorld getTownWorld(String townName);

	abstract public void removeResident(Resident resident);

	abstract public void removeTownBlock(TownBlock townBlock);

	abstract public void removeTownBlocks(Town town);

	abstract public List<TownBlock> getAllTownBlocks();

	abstract public void newResident(String name) throws AlreadyRegisteredException, NotRegisteredException;

	abstract public void newTown(String name) throws AlreadyRegisteredException, NotRegisteredException;

	abstract public void newNation(String name) throws AlreadyRegisteredException, NotRegisteredException;

	abstract public void newWorld(String name) throws AlreadyRegisteredException, NotRegisteredException;

	abstract public void removeTown(Town town);

	public abstract void removeSiege(Siege siege);

	abstract public void removeWorld(TownyWorld world) throws UnsupportedOperationException;

	abstract public Set<String> getResidentKeys();

	abstract public Set<String> getTownsKeys();

	abstract public Set<String> getNationsKeys();

	abstract public Set<String> getSiegesKeys();

	abstract public List<Town> getTownsWithoutNation();

	abstract public List<Resident> getResidentsWithoutTown();

	abstract public void renameTown(Town town, String newName) throws AlreadyRegisteredException, NotRegisteredException;

	abstract public void renameNation(Nation nation, String newName) throws AlreadyRegisteredException, NotRegisteredException;

	abstract public void renamePlayer(Resident resident, String newName) throws AlreadyRegisteredException, NotRegisteredException;

	abstract public void newSiege(String townName) throws AlreadyRegisteredException;
    abstract public Siege getSiege(String townName) throws TownyException;
}
