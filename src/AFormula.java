import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import net.minecraft.server.v1_4_6.EntityPlayer;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_4_6.entity.CraftCreeper;
import org.bukkit.craftbukkit.v1_4_6.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_4_6.entity.CraftSkeleton;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Snowman;
import org.bukkit.entity.Squid;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Witch;
import org.bukkit.entity.Wither;
import org.bukkit.entity.WitherSkull;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.entity.EntityBreakDoorEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PigZapEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.hanging.HangingBreakEvent.RemoveCause;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class AFormula extends JavaPlugin implements Listener
{
	File ConfigFile;
	FileConfiguration Config;
	
	File DataFile;
	FileConfiguration Data;
	
	File ValueFile;
	FileConfiguration Value;
	
	int TipNumber = 0;
	
	boolean Correct = false;

	public void onEnable()
	{	
		if (getResource("config.yml") != null && (!new File(getDataFolder(), "config.yml").exists())) { saveDefaultConfig(); }
		ConfigFile = new File(getDataFolder(), "config.yml");
		Config = YamlConfiguration.loadConfiguration(ConfigFile);
		
		DataFile = new File(getDataFolder(), "data.yml");
		Data = YamlConfiguration.loadConfiguration(DataFile);
		
		ValueFile = new File(getDataFolder(), "value.yml");
		InputStream ValueStream = getResource("value.yml");
		if (ValueStream != null && ValueFile.exists() == false) { Value = YamlConfiguration.loadConfiguration(ValueStream); try { Value.save(ValueFile); } catch (IOException e) { e.printStackTrace(); } }
		Value = YamlConfiguration.loadConfiguration(ValueFile);
		
		Set<String> DataList = Data.getKeys(true);
		String PlayerData = DataList.toString().replaceAll("\\[", "").replaceAll("\\]", "").replaceAll(" ", "");
		String[] PlayerDataArray = PlayerData.split(",");
		String PlayerList = null;
		int NewDataMax = -1;
		if (PlayerDataArray.length > 1)
		{
			for (int Number = 1; (Number + 1) != PlayerDataArray.length; Number++)
			{
				String[] NewData = PlayerDataArray[Number].toString().split("[.]");
				if (NewDataMax < 0) { PlayerList = NewData[1]; NewDataMax++; }
				else
				{
					String[] NowList = PlayerList.split(",");
					int Check = 0;
					for (int CheckNumber = 0; NewDataMax >= CheckNumber; CheckNumber++)
					{
						if (NowList[CheckNumber].equals(NewData[1])) { Check = 1; }
					}
					if (Check == 0)
					{
						PlayerList = PlayerList + "," + NewData[1];
						NewDataMax = NewDataMax + 1;
					}
				}
			}
		}
		
		if (Config.getBoolean("MoneySystem.Enable") && PlayerDataArray.length > 1)
		{
			String[] FinalList = PlayerList.split(",");
			for (int NeedCheckNumber = FinalList.length; NeedCheckNumber != 0; NeedCheckNumber--)
			{
				if (Data.get("Player." + (FinalList[NeedCheckNumber - 1]) + ".Trade") != null) { Data.set("Player." + (FinalList[NeedCheckNumber - 1]) + ".Trade", null); DataSave(); }
				if (Data.get("Player." + (FinalList[NeedCheckNumber - 1]) + ".TradeItem") != null) { Data.set("Player." + (FinalList[NeedCheckNumber - 1]) + ".TradeItem", null); DataSave(); }
			}
		}
		
		if (Config.getBoolean("CommandManager.Enable") && PlayerDataArray.length > 1)
		{
			String[] FinalList = PlayerList.split(",");
			for (int NeedCheckNumber = FinalList.length; NeedCheckNumber != 0; NeedCheckNumber--)
			{
				if (Data.get("Player." + (FinalList[NeedCheckNumber - 1]) + ".CommandCooldown") != null) { Data.set("Player." + (FinalList[NeedCheckNumber - 1]) + ".CommandCooldown", null); DataSave(); }
			}
		}
		
		getServer().getPluginManager().registerEvents(this, this);
	}
	
	public boolean onCommand (CommandSender CommandSender, Command Command, String CommandString, String[] StringArray)
	{
		if (CommandSender instanceof Player)
		{
			Player Player = (Player) CommandSender;
			String PlayerName = Player.getName().toUpperCase();
			
			boolean CommandBack = false;
			boolean CommandHat = false;
			boolean CommandHelp = false;
			boolean CommandHome = false;
			boolean CommandHomeset = false;
			boolean CommandMoney = false;
			boolean CommandMoneyset = false;
			boolean CommandPing = false;
			boolean CommandTip = false;
			boolean CommandTrade = false;
			boolean CommandSell = false;
			boolean CommandSpawn = false;
			boolean CommandSpawnset = false;
			
			if (Config.getBoolean("CommandManager.Enable"))
			{
				List<String> CommandList = new ArrayList<String>(Config.getStringList("CommandManager.Setting.CommandRestrict.List"));
				for (int ListNumber = 0; ListNumber <= CommandList.size() - 1; ListNumber++)
				{
					String[] CheckCommand = CommandList.get(ListNumber).split(",");
					if (CheckCommand[0].equalsIgnoreCase("back")) { CommandBack = true; }
					if (CheckCommand[0].equalsIgnoreCase("hat")) { CommandHat = true; }
					if (CheckCommand[0].equalsIgnoreCase("help")) { CommandHelp = true; }
					if (CheckCommand[0].equalsIgnoreCase("home")) { CommandHome = true; }
					if (CheckCommand[0].equalsIgnoreCase("homeset")) { CommandHomeset = true; }
					if (CheckCommand[0].equalsIgnoreCase("money")) { CommandMoney = true; }
					if (CheckCommand[0].equalsIgnoreCase("moneyset")) { CommandMoneyset = true; }
					if (CheckCommand[0].equalsIgnoreCase("ping")) { CommandPing = true; }
					if (CheckCommand[0].equalsIgnoreCase("tip")) { CommandTip = true; }
					if (CheckCommand[0].equalsIgnoreCase("trade")) { CommandTrade = true; }
					if (CheckCommand[0].equalsIgnoreCase("sell")) { CommandSell = true; }
					if (CheckCommand[0].equalsIgnoreCase("spawn")) { CommandSpawn = true; }
					if (CheckCommand[0].equalsIgnoreCase("spawnset")) { CommandSpawnset = true; }
				}
				
				if (CommandString.equalsIgnoreCase("back") && CommandBack)
				{
					if (Data.getString("Player." + PlayerName + ".DeathPoint") != null)
					{
						String[] DeathPointData = Data.getString("Player." + PlayerName + ".DeathPoint").split(",");
						Location DeathPoint = new Location(getServer().getWorld(DeathPointData[0]), Float.valueOf(DeathPointData[1]), Float.valueOf(DeathPointData[2] + 1), Float.valueOf(DeathPointData[3]));
						Player.teleport(DeathPoint);
						if (Config.getString("CommandManager.Message.BackSuccess").equals("") == false) { Player.sendMessage(Config.getString("CommandManager.Message.BackSuccess")); }
						Data.set("Player." + PlayerName + ".DeathPoint", null);
						DataSave();
					}
					else { if (Config.getString("CommandManager.Message.BackError").equals("") == false) { Player.sendMessage(Config.getString("CommandManager.Message.BackError")); } Correct = true; }
				}
				
				if (CommandString.equalsIgnoreCase("hat") && CommandHat)
				{
					if (Player.getItemInHand().getType() == Material.AIR)
					{
						if (Config.getString("CommandManager.Message.HatSuccess").equals("") == false) { Player.sendMessage(Config.getString("CommandManager.Message.HatErrorAir")); }
						Correct = true;
						return true;
					}
					ItemStack Hand = Player.getItemInHand();
					ItemStack Head = Player.getInventory().getHelmet();
					if (Player.getItemInHand().getAmount() == 1)
					{
						Player.getInventory().setHelmet(Hand);
						Player.getInventory().setItemInHand(Head);
					}
					else
					{
						int Amount = Player.getItemInHand().getAmount();
						Hand.setAmount(1);
						Player.getInventory().setHelmet(Hand);
						Hand.setAmount(Amount - 1);
						Player.getInventory().setItemInHand(Hand);
						if (Head != null)
						{
							if (Player.getInventory().firstEmpty() < 0 || (Player.getInventory().firstEmpty() < 0 && Player.getGameMode().toString().equals("CREATIVE"))) { Player.getWorld().dropItemNaturally(Player.getLocation(), Head); }
							else { Player.getInventory().addItem(Head); }
						}
					}
					if (Config.getString("CommandManager.Message.HatSuccess").equals("") == false) { Player.sendMessage(Config.getString("CommandManager.Message.HatSuccess")); }
				}
				
				if (CommandString.equalsIgnoreCase("home") && CommandHome)
				{
					if (Data.getString("Player." + PlayerName + ".Home") != null)
					{
						String[] HomeData = Data.getString("Player." + PlayerName + ".Home").split(",");
						Location Home = new Location(getServer().getWorld(HomeData[0]), Float.valueOf(HomeData[1]), Float.valueOf(HomeData[2]), Float.valueOf(HomeData[3]));
						Player.teleport(Home);
						if (Config.getString("CommandManager.Message.HomeSucces").equals("") == false) { Player.sendMessage(Config.getString("CommandManager.Message.HomeSucces")); }
					}
					else { if (Config.getString("CommandManager.Message.HomeError").equals("") == false) { Player.sendMessage(Config.getString("CommandManager.Message.HomeError")); } Correct = true; }
				}
					
				if (CommandString.equalsIgnoreCase("homeset") && CommandHomeset)
				{
					Data.set("Player." + PlayerName + ".Home", Player.getWorld().getName() + "," + ((float) Player.getLocation().getX()) + "," + ((float) Player.getLocation().getY()) + "," + ((float) Player.getLocation().getZ()));
					DataSave();
					if (Config.getString("CommandManager.Message.HomesetSucces").equals("") == false) { Player.sendMessage(Config.getString("CommandManager.Message.HomesetSucces")); }
				}
				
				if (CommandString.equalsIgnoreCase("ping") && CommandPing)
				{
					EntityPlayer EntityPlayer = (EntityPlayer) ((CraftPlayer) CommandSender).getHandle();
					int Ping = EntityPlayer.ping;
					String FinalPing = null;
					String[] ValueArray = Config.getString("CommandManager.Setting.Ping.Value").split(",");
					String[] ColorArray = Config.getString("CommandManager.Setting.Ping.Color").split(",");
					if (Ping < 0) { Ping = 0; }
					if (Ping > Integer.valueOf(ValueArray[1])) { FinalPing = ColorArray[2] + Integer.toString(Ping); }
					if (Ping <= Integer.valueOf(ValueArray[1])) { FinalPing = ColorArray[1] + Integer.toString(Ping); }
					if (Ping <= Integer.valueOf(ValueArray[0])) { FinalPing = ColorArray[0] + Integer.toString(Ping); }
					if (Config.getString("CommandManager.Message.Ping").equals("") == false) { Player.sendMessage(Config.getString("CommandManager.Message.Ping").replaceAll("%Ping", FinalPing)); }
				}
				
				if (CommandString.equalsIgnoreCase("spawn") && CommandSpawn)
				{
					Location SpawnLocation = Player.getWorld().getSpawnLocation();
					SpawnLocation.setX(SpawnLocation.getX() + 0.5);
					SpawnLocation.setY(SpawnLocation.getY() + 1);
					SpawnLocation.setZ(SpawnLocation.getZ() + 0.5); 
					Player.teleport(SpawnLocation);
					if (Config.getString("CommandManager.Message.SpawnSucces").equals("") == false) { Player.sendMessage(Config.getString("CommandManager.Message.SpawnSucces")); }
				}
				
				if (CommandString.equalsIgnoreCase("spawnset") && Player.isOp() && CommandSpawnset)
				{
					Location PlayerLocation = Player.getLocation();
					Player.getWorld().setSpawnLocation(PlayerLocation.getBlockX(), PlayerLocation.getBlockY(), PlayerLocation.getBlockZ());
					if (Config.getString("CommandManager.Message.SpawnsetSucces").equals("") == false) { Player.sendMessage(Config.getString("CommandManager.Message.SpawnsetSucces")); }
				}
			}
			
			if (Config.getBoolean("MessageManager.Enable") && Config.getBoolean("MessageManager.Setting.Help") && CommandString.equalsIgnoreCase("help") && CommandHelp)
			{
				List<String> Help = new ArrayList<String>(Config.getStringList("MessageManager.Message.Help.Player"));
				for (int HelpNumber = 0; HelpNumber <= Help.size() - 1; HelpNumber++)
				{
					if (Help.get(HelpNumber).equals("") == false)
					{
						Player.sendMessage(Help.get(HelpNumber).toString());
					}
				}
				if (Player.isOp())
				{
					List<String> HelpToOP = new ArrayList<String>(Config.getStringList("MessageManager.Message.Help.Operator"));
					for (int HelpNumber = 0; HelpNumber <= HelpToOP.size() - 1; HelpNumber++)
					{
						if (HelpToOP.get(HelpNumber).equals("") == false)
						{
							Player.sendMessage(HelpToOP.get(HelpNumber).toString());
						}
					}
				}
			}
			
			if (Config.getBoolean("MoneySystem.Enable"))
			{
				if (CommandString.equalsIgnoreCase("money") && Config.getString("MoneySystem.Message.CheckMoney").equals("") == false && CommandMoney)
				{
					Player.sendMessage(Config.getString("MoneySystem.Message.CheckMoney").replaceAll("%Money", NumberFormat.getInstance().format(Data.getInt("Player." + PlayerName + ".Money"))));
				}
				
				if ((CommandString.equalsIgnoreCase("moneyset")) && Player.isOp() && CommandMoneyset)
				{
					if (StringArray.length == 2)
					{
						if (Data.get("Player." + StringArray[0].toUpperCase() + ".Money") != null)
						{
							byte Code = 0;
							if (StringArray[1].substring(0, 1).equals("+")) { Code = 1; } else if (StringArray[1].substring(0, 1).equals("-")) { Code = 2; }
							String Amount = 0 + StringArray[1].replaceAll("\\D+", "");
							int NewAmount = Integer.parseInt(Amount);
							int PrimeAmount = (int) Data.get("Player." + StringArray[0].toUpperCase() + ".Money");
							if (Code == 0)
							{
								Data.set("Player." + StringArray[0].toUpperCase() + ".Money", NewAmount);
								DataSave();
								if (Config.getString("MoneySystem.Message.SuccessSet").equals("") == false) { Player.sendMessage(Config.getString("MoneySystem.Message.SuccessSet").replaceAll("%Player", getServer().getPlayer(StringArray[0]).getName()).replaceAll("%Money", NumberFormat.getInstance().format(NewAmount))); }
							}
							else if (Code == 1)
							{
								Data.set("Player." + StringArray[0].toUpperCase() + ".Money", PrimeAmount + NewAmount);
								DataSave();
								if (Config.getString("MoneySystem.Message.SuccessSetAdd").equals("") == false) { Player.sendMessage(Config.getString("MoneySystem.Message.SuccessSetAdd").replaceAll("%Player", getServer().getPlayer(StringArray[0]).getName()).replaceAll("%Amount", NumberFormat.getInstance().format(NewAmount)).replaceAll("%Money", NumberFormat.getInstance().format(Data.getInt("Player." + StringArray[0].toUpperCase() + ".Money")))); }
							}
							else if (Code == 2)
							{
								Data.set("Player." + StringArray[0].toUpperCase() + ".Money", PrimeAmount - NewAmount);
								DataSave();
								if (Config.getString("MoneySystem.Message.SuccessSetSubtract").equals("") == false) { Player.sendMessage(Config.getString("MoneySystem.Message.SuccessSetSubtract").replaceAll("%Player", getServer().getPlayer(StringArray[0]).getName()).replaceAll("%Amount", NumberFormat.getInstance().format(NewAmount)).replaceAll("%Money", NumberFormat.getInstance().format(Data.getInt("Player." + StringArray[0].toUpperCase() + ".Money")))); }
							}
						}
						else { if (Config.getString("MoneySystem.Message.ErrorData").equals("") == false) { Player.sendMessage(Config.getString("MoneySystem.Message.ErrorData")); } Correct = true; }
					}
					else { if (Config.getString("MoneySystem.Message.ErrorFormat").equals("") == false) { Player.sendMessage(Config.getString("MoneySystem.Message.ErrorFormat")); } Correct = true; }
				}
				
				if (CommandString.equalsIgnoreCase("sell") && CommandSell)
				{
					if (StringArray.length != 2) { if (Config.getString("MoneySystem.Message.ErrorFormatSell").equals("") == false) { Player.sendMessage(Config.getString("MoneySystem.Message.ErrorFormatSell")); } Correct = true; return true; }
					if (getServer().getPlayer(StringArray[0]) == null) { if (Config.getString("MoneySystem.Message.ErrorOfflineSell").equals("") == false) { Player.sendMessage(Config.getString("MoneySystem.Message.ErrorOfflineSell")); } Correct = true; return true; }
					String Amount = 0 + StringArray[1].replaceAll("\\D+", "");
					int Price = Integer.parseInt(Amount);
					if (Price <= 0) { if (Config.getString("MoneySystem.Message.ErrorPriceSell").equals("") == false) { Player.sendMessage(Config.getString("MoneySystem.Message.ErrorPriceSell")); } Correct = true; return true; }
					Player Buyer = (Player) getServer().getPlayer(StringArray[0]);
					if (Player.getName().toUpperCase().equals(StringArray[0].toUpperCase())) { if (Config.getString("MoneySystem.Message.ErrorSelfSell").equals("") == false) { Player.sendMessage(Config.getString("MoneySystem.Message.ErrorSelfSell")); } Correct = true; return true; }
					if (Data.get("Player." + Player.getName().toUpperCase() + ".Trade") != null) { if (Config.getString("MoneySystem.Message.ErrorIngSell").equals("") == false) { Player.sendMessage(Config.getString("MoneySystem.Message.ErrorIngSell")); } Correct = true; return true; }
					if (Data.get("Player." + StringArray[0].toUpperCase() + ".Trade") != null)
					{
						String[] TradeData = Data.getString("Player." + StringArray[0].toUpperCase() + ".Trade").split(",");
						if (TradeData[0].equals(Player.getName().toUpperCase()) && Config.getString("MoneySystem.Message.SellRequesting").equals("") == false) { Player.sendMessage(Config.getString("MoneySystem.Message.SellRequesting")); }
						else if (Config.getString("MoneySystem.Message.SellRequestError").equals("") == false) { Player.sendMessage(Config.getString("MoneySystem.Message.SellRequestError")); }
						Correct = true;
						return true;
					}
					if (Player.getItemInHand().getType().name().equals("AIR") || Player.getItemInHand().getAmount() == 0) { if (Config.getString("MoneySystem.Message.ErrorItemSell").equals("") == false) { Player.sendMessage(Config.getString("MoneySystem.Message.ErrorItemSell")); } Correct = true; return true; }
					Data.set("Player." + Player.getName().toUpperCase() + ".Trade", "Requesting");
					Data.set("Player." + StringArray[0].toUpperCase() + ".Trade", Player.getName().toUpperCase() + "," + Price + "," + Player.getItemInHand().getData().toString() + "," + Player.getItemInHand().getTypeId() + "," + Player.getItemInHand().getAmount() + "," + Player.getItemInHand().getDurability() + "," + Player.getItemInHand().getEnchantments().size() + "," + System.currentTimeMillis());
					Data.set("Player." + StringArray[0].toUpperCase() + ".TradeItem", Player.getItemInHand());
					
					String ItemName = Player.getItemInHand().getData().toString();
					if (Value.getString("DataValue." + Player.getItemInHand().getData().toString()) != null)
					{
						ItemName = Value.getString("DataValue." + Player.getItemInHand().getData().toString());
					}
					else if (Value.getString("DataValue." + Player.getItemInHand().getTypeId() + ":" + String.valueOf(Player.getItemInHand().getDurability())) != null)
					{
						ItemName = Value.getString("DataValue." + Player.getItemInHand().getTypeId() + ":" + String.valueOf(Player.getItemInHand().getDurability()));
					}
					else if (Value.getString("DataValue." + Player.getItemInHand().getTypeId()) != null)
					{
						ItemName = Value.getString("DataValue." + Player.getItemInHand().getTypeId());
					}
					
					String InfoDurability = "";
					String InfoEnchantments = "";
					String[] CheckList = Value.get("DataValue.CheckDurability").toString().split(",");
					int CheckListLenght = CheckList.length - 1;
					boolean CheckEnable = false;
					for (int CheckNumber = 0; CheckNumber <= CheckListLenght; CheckNumber++)
					{
						if (Player.getItemInHand().getTypeId() == Integer.valueOf(CheckList[CheckNumber])) { CheckEnable = true; }
					}
					if (CheckEnable == true) { InfoDurability = Config.getString("MoneySystem.Message.TradeInfoDurability").replaceAll("%Durability", String.valueOf(Player.getItemInHand().getDurability())); }
					if (Player.getItemInHand().getEnchantments().size() > 0) { InfoEnchantments = Config.getString("MoneySystem.Message.TradeInfoEnchantments").replaceAll("%Enchantments", String.valueOf(Player.getItemInHand().getEnchantments().size())); }
					
					if (Config.getString("MoneySystem.Message.TradeRequest").equals("") == false)
					{
						Buyer.sendMessage(Config.getString("MoneySystem.Message.TradeRequest")
							.replaceAll("%Player", Player.getName())
							.replaceAll("%Price", NumberFormat.getInstance().format(Price))
							.replaceAll("%Item", ItemName)
							.replaceAll("%Amount", String.valueOf(Player.getItemInHand().getAmount()))
							.replaceAll("%InfoDurability", InfoDurability)
							.replaceAll("%InfoEnchantments", InfoEnchantments));
					}
					
					if (Config.getBoolean("MessageManager.Enable") && Config.getBoolean("MessageManager.Setting.Tip.Enable") && Data.getBoolean("Player." + Buyer.getName().toUpperCase() + ".Tip") && Config.getString("MoneySystem.Message.TradeRequestTip").equals("") == false)
					{
						Buyer.sendMessage(Config.getString("MoneySystem.Message.TradeRequestTip"));
					}
					
					if (Config.getString("MoneySystem.Message.SellRequesting").equals("") == false) { Player.sendMessage(Config.getString("MoneySystem.Message.SellRequesting")); }
					
					final Player FinalBuyer = getServer().getPlayer(StringArray[0]);
					final Player FinalSeller = (Player) CommandSender;
					final ItemStack FinalItem = Player.getItemInHand();
					final int TradeTime = Config.getInt("MoneySystem.Setting.TradeTime");
					getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable()
					{
						public void run()
						{
							if ((Data.get("Player." + FinalBuyer.getName().toUpperCase() + ".TradeItem") != null) && (Data.get("Player." + FinalBuyer.getName().toUpperCase() + ".Trade") != null))
							{
								String[] DataArray = Data.get("Player." + FinalBuyer.getName().toUpperCase() + ".Trade").toString().split(",");
								if ((Data.get("Player." + FinalBuyer.getName().toUpperCase() + ".TradeItem").equals(FinalItem)) && ((((System.currentTimeMillis() - Long.valueOf(DataArray[7])) / 1000) + 2) >= TradeTime))
								{
									Data.set("Player." + FinalSeller.getName().toUpperCase() + ".Trade", null);
									Data.set("Player." + FinalBuyer.getName().toUpperCase() + ".Trade", null);
									Data.set("Player." + FinalBuyer.getName().toUpperCase() + ".TradeItem", null);
									if (FinalSeller.isOnline())
									{
										if ((FinalSeller.getInventory().firstEmpty() < 0) || (FinalSeller.getInventory().firstEmpty() < 0 && FinalSeller.getGameMode().toString().equals("CREATIVE")))
										{
											FinalSeller.getWorld().dropItemNaturally(FinalSeller.getLocation(), FinalItem);
											Data.set("Player." + FinalSeller.getName().toUpperCase() + ".TradeTemp", null);
										}
										else
										{
											FinalSeller.getInventory().addItem(FinalItem);
											Data.set("Player." + FinalSeller.getName().toUpperCase() + ".TradeTemp", null);
										}
									}
									DataSave();
									if (Config.getString("MoneySystem.Message.TradeFailTimeout").equals("") == false) { FinalBuyer.sendMessage(Config.getString("MoneySystem.Message.TradeFailTimeout")); }
									if (Config.getString("MoneySystem.Message.TradeFailTimeout").equals("") == false) { FinalSeller.sendMessage(Config.getString("MoneySystem.Message.TradeFailTimeout")); }
								}
							}
						}
					} , 20 * TradeTime);
					
					Player.getInventory().remove(FinalItem);
					Data.set("Player." + Player.getName().toUpperCase() + ".TradeTemp", FinalItem);
					DataSave();
				}
				
				if (CommandString.equalsIgnoreCase("trade") && CommandTrade)
				{
					if (StringArray.length != 1) { if (Config.getString("MoneySystem.Message.ErrorFormatTrade").equals("") == false) { Player.sendMessage(Config.getString("MoneySystem.Message.ErrorFormatTrade")); } Correct = true; return true; }
					if (StringArray[0] == null) { if (Config.getString("MoneySystem.Message.ErrorFormatTrade").equals("") == false) { Player.sendMessage(Config.getString("MoneySystem.Message.ErrorFormatTrade")); } Correct = true; return true; }
					if (Data.get("Player." + Player.getName().toUpperCase() + ".Trade") == null) { if (Config.getString("MoneySystem.Message.TradeErrorNoRequest").equals("") == false) { Player.sendMessage(Config.getString("MoneySystem.Message.TradeErrorNoRequest")); } Correct = true; return true; }
					if (Data.get("Player." + Player.getName().toUpperCase() + ".Trade").equals("Requesting")) { if (Config.getString("MoneySystem.Message.TradeErrorRequesting").equals("") == false) { Player.sendMessage(Config.getString("MoneySystem.Message.TradeErrorRequesting")); } Correct = true; return true; }
					
					if (StringArray[0].equalsIgnoreCase("yes"))
					{
						String[] TradeData = Data.getString("Player." + Player.getName().toUpperCase() + ".Trade").split(",");
						ItemStack TradeItem = Data.getItemStack("Player." + Player.getName().toUpperCase() + ".TradeItem");
						int BuyerMoney = Data.getInt("Player." + Player.getName().toUpperCase() + ".Money");
						int TradePrice = Integer.valueOf(TradeData[1]);
						Player Seller = getServer().getPlayer(TradeData[0]);
						
						if (Seller == null || Seller.isOnline() == false)
						{
							Data.set("Player." + TradeData[0].toUpperCase() + ".Trade", null);
							Data.set("Player." + Player.getName().toUpperCase() + ".Trade", null);
							Data.set("Player." + Player.getName().toUpperCase() + ".TradeItem", null);
							DataSave();
							if (Config.getString("MoneySystem.Message.TradeFailOffline").equals("") == false) { Player.sendMessage(Config.getString("MoneySystem.Message.TradeFailOffline")); }
							Correct = true;
							return true;
						}
						
						if (BuyerMoney < TradePrice)
						{
							Data.set("Player." + Seller.getName().toUpperCase() + ".Trade", null);
							Data.set("Player." + Player.getName().toUpperCase() + ".Trade", null);
							Data.set("Player." + Player.getName().toUpperCase() + ".TradeItem", null);
							if ((Seller.getInventory().firstEmpty() < 0) || (Seller.getInventory().firstEmpty() < 0 && Seller.getGameMode().toString().equals("CREATIVE")))
							{
								Seller.getWorld().dropItemNaturally(Seller.getLocation(), TradeItem);
								Data.set("Player." + Seller.getName().toUpperCase() + ".TradeTemp", null);
							}
							else
							{
								Seller.getInventory().addItem(TradeItem);
								Data.set("Player." + Seller.getName().toUpperCase() + ".TradeTemp", null);
							}
							DataSave();
							if (Config.getString("MoneySystem.Message.TradeFailNoMoneySeller").equals("") == false) { Seller.sendMessage(Config.getString("MoneySystem.Message.TradeFailNoMoneySeller")); }
							if (Config.getString("MoneySystem.Message.TradeFailNoMoneyBuyer").equals("") == false) { Player.sendMessage(Config.getString("MoneySystem.Message.TradeFailNoMoneyBuyer")); }
							Correct = true;
							return true;
						}
						
						if ((Player.getInventory().firstEmpty() < 0) || (Player.getInventory().firstEmpty() < 0 && Player.getGameMode().toString().equals("CREATIVE"))) { Player.getWorld().dropItemNaturally(Player.getLocation(), TradeItem); }
						else { Player.getInventory().addItem(TradeItem); }
						int SellerMoney = Data.getInt("Player." + Seller.getName().toUpperCase() + ".Money");
						Data.set("Player." + Player.getName().toUpperCase() + ".Money", BuyerMoney - TradePrice);
						Data.set("Player." + Seller.getName().toUpperCase() + ".Money", SellerMoney + TradePrice);
						Data.set("Player." + Seller.getName().toUpperCase() + ".Trade", null);
						Data.set("Player." + Seller.getName().toUpperCase() + ".TradeTemp", null);
						Data.set("Player." + Player.getName().toUpperCase() + ".Trade", null);
						Data.set("Player." + Player.getName().toUpperCase() + ".TradeItem", null);
						DataSave();
						if (Config.getString("MoneySystem.Message.TradeSuccessBuyer").equals("") == false) { Player.sendMessage(Config.getString("MoneySystem.Message.TradeSuccessBuyer").replaceAll("%Player", Seller.getName()).replaceAll("%Price", NumberFormat.getInstance().format(TradePrice))); }
						if (Config.getString("MoneySystem.Message.TradeSuccessSeller").equals("") == false) { Seller.sendMessage(Config.getString("MoneySystem.Message.TradeSuccessSeller").replaceAll("%Player", Player.getName()).replaceAll("%Price", NumberFormat.getInstance().format(TradePrice))); }
					}
					
					else if (StringArray[0].equalsIgnoreCase("no"))
					{						
						String[] TradeData = Data.getString("Player." + Player.getName().toUpperCase() + ".Trade").split(",");
						ItemStack TradeItem = Data.getItemStack("Player." + Player.getName().toUpperCase() + ".TradeItem");
						Player Seller = getServer().getPlayer(TradeData[0]);
						Data.set("Player." + TradeData[0].toUpperCase() + ".Trade", null);
						Data.set("Player." + Player.getName().toUpperCase() + ".Trade", null);
						Data.set("Player." + Player.getName().toUpperCase() + ".TradeItem", null);
						if (Seller != null)
						{
							if (Seller.isOnline())
							{
								if (Config.getString("MoneySystem.Message.TradeFailSeller").equals("") == false) { Seller.sendMessage(Config.getString("MoneySystem.Message.TradeFailSeller")); }
								if ((Seller.getInventory().firstEmpty() < 0) || (Seller.getInventory().firstEmpty() < 0 && Seller.getGameMode().toString().equals("CREATIVE")))
								{
									Seller.getWorld().dropItemNaturally(Seller.getLocation(), TradeItem);
									Data.set("Player." + Seller.getName().toUpperCase() + ".TradeTemp", null);
								}
								else
								{
									Seller.getInventory().addItem(TradeItem);
									Data.set("Player." + Seller.getName().toUpperCase() + ".TradeTemp", null);
								}
							}
						}
						DataSave();
						if (Config.getString("MoneySystem.Message.TradeFailBuyer").equals("") == false) { Player.sendMessage(Config.getString("MoneySystem.Message.TradeFailBuyer")); }
					}
					else { if (Config.getString("MoneySystem.Message.ErrorFormatTrade").equals("") == false) { Player.sendMessage(Config.getString("MoneySystem.Message.ErrorFormatTrade")); } Correct = true; return true; }
				}
				
			}
			
			if (Config.getBoolean("MessageManager.Enable") && Config.getBoolean("MessageManager.Setting.Tip.Enable") && CommandString.equalsIgnoreCase("tip") && CommandTip)
			{
				if ((Data.getBoolean("Player." + PlayerName + ".Tip")) || (Data.get("Player." + PlayerName + ".Tip")) == null)
				{
					Data.set("Player." + PlayerName + ".Tip", false);
					if (Config.getString("MessageManager.Message.Tip.Close").equals("") == false)
					{
						Player.sendMessage(Config.getString("MessageManager.Message.Tip.Close"));
					}
				}
				else
				{
					Data.set("Player." + PlayerName + ".Tip", true);
					if (Config.getString("MessageManager.Message.Tip.Open").equals("") == false)
					{
						Player.sendMessage(Config.getString("MessageManager.Message.Tip.Open"));
					}
				}
				DataSave();
			}
		}
		return true;
	}
	
	@EventHandler
	public void PlayerLogin (PlayerLoginEvent event)
	{
		if (Config.getBoolean("ExpansionManager.Enable") && Config.getBoolean("ExpansionManager.Setting.IPAuth.Enable"))
		{
			Set<String> DataList = Data.getKeys(true);
			String PlayerData = DataList.toString().replaceAll("\\[", "").replaceAll("\\]", "").replaceAll(" ", "");
			String[] PlayerDataArray = PlayerData.split(",");
			String PlayerList = null;
			int NewDataMax = -1;
			if (PlayerDataArray.length > 1)
			{
				for (int Number = 1; (Number + 1) != PlayerDataArray.length; Number++)
				{
					String[] NewData = PlayerDataArray[Number].toString().split("[.]");
					if (NewDataMax < 0) { PlayerList = NewData[1]; NewDataMax++; }
					else
					{
						String[] NowList = PlayerList.split(",");
						int Check = 0;
						for (int CheckNumber = 0; NewDataMax >= CheckNumber; CheckNumber++)
						{
							if (NowList[CheckNumber].equals(NewData[1])) { Check = 1; }
						}
						if (Check == 0)
						{
							PlayerList = PlayerList + "," + NewData[1];
							NewDataMax = NewDataMax + 1;
						}
					}
				}
				
				String[] FinalList = PlayerList.split(",");
				String IPList = "";
				for (int NeedCheckNumber = FinalList.length; NeedCheckNumber != 0; NeedCheckNumber--)
				{
					if (IPList.equals("")) { IPList = (String) Data.get("Player." + (FinalList[NeedCheckNumber - 1]) + ".IP"); }
					else { IPList = IPList + ";" + (String) Data.get("Player." + (FinalList[NeedCheckNumber - 1]) + ".IP"); }
				}
				
				String[] FinalIPList = IPList.split(";");
				int IPAmount = 0;
				
				for (int NeedCheckIPNumber = FinalIPList.length; NeedCheckIPNumber != 0; NeedCheckIPNumber--)
				{
					if (event.getAddress().getHostAddress().toString().equals(FinalIPList[NeedCheckIPNumber - 1])) { IPAmount++; }
				}
				
				String PlayerName = event.getPlayer().getName().toUpperCase();
				
				if (Data.get("Player." + PlayerName + ".IP") != null)
				{
					if (event.getAddress().getHostAddress().equals(Data.get("Player." + PlayerName + ".IP")) == false)
					{
						event.disallow(PlayerLoginEvent.Result.KICK_OTHER, Config.getString("ExpansionManager.Message.AnotherIPIsUsing"));
					}
					else if (IPAmount > ((byte) Config.getInt("ExpansionManager.Setting.IPAuth.MaxAccounts")))
					{
						event.disallow(PlayerLoginEvent.Result.KICK_OTHER, Config.getString("ExpansionManager.Message.AccountsTooMuch"));
					}
				}
				else if ((Data.get("Player." + PlayerName + ".IP") == null) && ((IPAmount + 1) > ((byte) Config.getInt("ExpansionManager.Setting.IPAuth.MaxAccounts"))))
				{
					event.disallow(PlayerLoginEvent.Result.KICK_OTHER, Config.getString("ExpansionManager.Message.AccountsTooMuch"));
				}
			}
		}
	}
	
	@EventHandler
	public void PlayerJoin (PlayerJoinEvent event)
	{
		String PlayerName = event.getPlayer().getName().toUpperCase();
		
		if (getServer().getOnlinePlayers().length == 1) {
			if (Config.getBoolean("BonusSystem.Enable") && Config.getInt("BonusSystem.Setting.TimerInterval") > 0 && Config.getInt("BonusSystem.Setting.BonusInterval") > 0)
			{
				Timer BonusTimer = new Timer();
				BonusTimer.schedule(new TimerTaskBonus(), Config.getInt("BonusSystem.Setting.TimerInterval") * 1000, Config.getInt("BonusSystem.Setting.TimerInterval") * 1000);
			}
			if (Config.getBoolean("MessageManager.Enable") && Config.getBoolean("MessageManager.Setting.Tip.Enable") && Config.getInt("MessageManager.Setting.Tip.Interval") > 0)
			{
				Timer TipTimer = new Timer();
				TipTimer.schedule(new TimerTaskTip(), (Config.getInt("MessageManager.Setting.Tip.Interval")) * 1000, (Config.getInt("MessageManager.Setting.Tip.Interval")) * 1000);
			}
		}
		
		if (Config.getBoolean("BonusSystem.Enable") && Data.get("Player." + PlayerName + ".Bonus") == null)
		{
			Data.set("Player." + PlayerName + ".Bonus", 0);
		}
		
		if (Config.getBoolean("MessageManager.Enable"))
		{
			if (Config.getString("MessageManager.Message.First").equals("") == false && event.getPlayer().getLastPlayed() == 0)
			{
				getServer().broadcastMessage(Config.getString("MessageManager.Message.First").replaceAll("%Player", event.getPlayer().getName()));
				event.setJoinMessage(null);
			}
			else if (Config.getString("MessageManager.Message.Join").equals("") == false)
			{
				getServer().broadcastMessage(Config.getString("MessageManager.Message.Join").replaceAll("%Player", event.getPlayer().getName()));
				event.setJoinMessage(null);
			}
			
			if (Config.getBoolean("MessageManager.Setting.Tip.Enable") && Data.get("Player." + PlayerName + ".Tip") == null)
			{
				Data.set("Player." + PlayerName + ".Tip", true);
			}
		}
		
		if (Config.getBoolean("ExpansionManager.Enable") && Config.getBoolean("ExpansionManager.Setting.IPAuth.Enable") && Data.get("Player." + PlayerName + ".IP") == null)
		{
			Data.set("Player." + PlayerName + ".IP", event.getPlayer().getAddress().getAddress().getHostAddress());
		}

		if (Config.getBoolean("MoneySystem.Enable"))
		{
			if (Data.get("Player." + PlayerName + ".Money") == null)
			{
				Data.set("Player." + PlayerName + ".Money", 0);
			}
			
			if (Data.get("Player." + PlayerName + ".TradeItem") != null)
			{
				String[] TradeList = Data.get("Player." + PlayerName + ".Trade").toString().split(",");
				String ItemName = TradeList[2];
				if (Value.getString("DataValue." + ItemName) != null) { ItemName = Value.getString("DataValue." + ItemName); }
				else if (Value.getString("DataValue." + TradeList[3] + ":" + TradeList[5]) != null) { ItemName = Value.getString("DataValue." + TradeList[3] + ":" + TradeList[5]); }
				else if (Value.getString("DataValue." + TradeList[3]) != null) { ItemName = Value.getString("DataValue." + TradeList[3]); }
				
				String InfoDurability = "";
				String InfoEnchantments = "";
				String[] CheckList = Value.get("DataValue.CheckDurability").toString().split(",");
				int CheckListLenght = CheckList.length - 1;
				boolean CheckEnable = false;
				for (int CheckNumber = 0; CheckNumber <= CheckListLenght; CheckNumber++)
				{
					if (Integer.valueOf(TradeList[3]) == Integer.valueOf(CheckList[CheckNumber])) { CheckEnable = true; }
				}
				if (CheckEnable == true) { InfoDurability = Config.getString("MoneySystem.Message.TradeInfoDurability").replaceAll("%Durability", String.valueOf(TradeList[5])); }
				if (Integer.valueOf(TradeList[6]) > 0) { InfoEnchantments = Config.getString("MoneySystem.Message.TradeInfoEnchantments").replaceAll("%Enchantments", String.valueOf(TradeList[6])); }
				
				if (Config.getString("MoneySystem.Message.TradeRequest").equals("") == false)
				{
					event.getPlayer().sendMessage(Config.getString("MoneySystem.Message.TradeRequest")
						.replaceAll("%Player", TradeList[0])
						.replaceAll("%Price", NumberFormat.getInstance().format(Integer.valueOf(TradeList[1])))
						.replaceAll("%Item", ItemName)
						.replaceAll("%Amount", String.valueOf(TradeList[4]))
						.replaceAll("%InfoDurability", InfoDurability)
						.replaceAll("%InfoEnchantments", InfoEnchantments));
				}
				
				if (Config.getBoolean("MessageManager.Enable") && Config.getBoolean("MessageManager.Setting.Tip.Enable") && Data.getBoolean("Player." + event.getPlayer().getName().toUpperCase() + ".Tip") && Config.getString("MoneySystem.Message.TradeRequestTip").equals("") == false)
				{
					event.getPlayer().sendMessage(Config.getString("MoneySystem.Message.TradeRequestTip"));
				}
			}
		}
		
		if (Config.getBoolean("CommandManager.Setting.Spawn.Enable") && event.getPlayer().getLastPlayed() == 0)
		{
			if (Config.getBoolean("CommandManager.Setting.Spawn.FirstJoinInSpawn"))
			{
				Location SpawnLocation = event.getPlayer().getWorld().getSpawnLocation();
				SpawnLocation.setX(SpawnLocation.getX() + 0.5);
				SpawnLocation.setY(SpawnLocation.getY() + 1);
				SpawnLocation.setZ(SpawnLocation.getZ() + 0.5); 
				event.getPlayer().teleport(SpawnLocation);
			}
			
			if (Config.getBoolean("CommandManager.Setting.Spawn.FirstJoinStrikeLighting"))
			{
				event.getPlayer().getWorld().strikeLightning(event.getPlayer().getLocation());
			}
		}
		
		if (Data.get("Player." + PlayerName + ".TradeTemp") != null && Data.get("Player." + PlayerName + ".Trade") == null)
		{
			ItemStack TradeItem = (ItemStack) Data.get("Player." + PlayerName + ".TradeTemp");
			if ((event.getPlayer().getInventory().firstEmpty() < 0) || (event.getPlayer().getInventory().firstEmpty() < 0 && event.getPlayer().getGameMode().toString().equals("CREATIVE"))) { event.getPlayer().getWorld().dropItemNaturally(event.getPlayer().getLocation(), TradeItem); }
			else { event.getPlayer().getInventory().addItem(TradeItem); }
			if (Config.getString("MoneySystem.Message.TradeTemp").equals("") == false) { event.getPlayer().sendMessage(Config.getString("MoneySystem.Message.TradeTemp")); }
			Data.set("Player." + PlayerName + ".TradeTemp",  null);
		}
		
		DataSave();
	}
	
	@EventHandler
	public void PlayerQuit (PlayerQuitEvent event)
	{
		if (Config.getBoolean("MessageManager.Enable") && Config.getString("MessageManager.Message.Quit").equals("") == false)
		{
			getServer().broadcastMessage(Config.getString("MessageManager.Message.Quit").replaceAll("%Player", event.getPlayer().getName()));
			event.setQuitMessage(null);
		}
	}
	
	@EventHandler
	public void PlayerDeath (PlayerDeathEvent event)
	{
		if (Config.getBoolean("ExpansionManager.Enable") && Config.getBoolean("ExpansionManager.Setting.KeepInventory.Enable"))
		{
			byte DropPercent = (byte) Config.getInt("ExpansionManager.Setting.KeepInventory.DropPercent");
			byte Random = (byte) ((Math.random() * 100) + 1);
			if (Random >= DropPercent)
			{
				final Player Player = event.getEntity();
				final ItemStack[] Armor = Player.getInventory().getArmorContents();
				final ItemStack[] Inventory = Player.getInventory().getContents();
				getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() { public void run() { Player.getInventory().setArmorContents(Armor); Player.getInventory().setContents(Inventory); } } );
				for (ItemStack ItemStack : Armor) { event.getDrops().remove(ItemStack); }
				for (ItemStack ItemStack : Inventory) { event.getDrops().remove(ItemStack); }
			}
			else
			{
				if (Config.getString("ExpansionManager.Setting.KeepInventory.DropBroadcast").equals("") == false && event.getDrops().size() > 0)
				{
					getServer().broadcastMessage(Config.getString("ExpansionManager.Message.DropBroadcast").replaceAll("%Player", event.getEntity().getName()));
				}
			}
		}
		
		if (Config.getBoolean("MessageManager.Enable") && Config.getBoolean("MessageManager.Setting.Death"))
		{
			String Cause = event.getEntity().getLastDamageCause().getCause().toString();
			String Attacker = null;
			if (Cause.equalsIgnoreCase("SUICIDE")) { Cause = "Suicide"; }
			else if (Cause.equalsIgnoreCase("CONTACT")) { Cause = "Contact"; }
			else if (Cause.equalsIgnoreCase("SUFFOCATION")) { Cause = "Suffocation"; }
			else if (Cause.equalsIgnoreCase("FALL")) { Cause = "Fall"; }
			else if (Cause.equalsIgnoreCase("FIRE") || Cause.equalsIgnoreCase("FIRE_TICK")) { Cause = "Fire"; }
			else if (Cause.equalsIgnoreCase("LAVA")) { Cause = "Lava"; }
			else if (Cause.equalsIgnoreCase("DROWING")) { Cause = "Drowing"; }
			else if (Cause.equalsIgnoreCase("BLOCK_EXPLOSION")) { Cause = "Explosion"; }
			else if (Cause.equalsIgnoreCase("VOID")) { Cause = "Void"; }
			else if (Cause.equalsIgnoreCase("STRVATION")) { Cause = "Strvation"; }
			else if (Cause.equalsIgnoreCase("MAGIC"))
			{
				Cause = "Magic";
				if (event.getEntity().getLastDamageCause().getEventName().equals("EntityDamageByEntityEvent"))
				{
					ThrownPotion ThrownPotion = (ThrownPotion) ((EntityDamageByEntityEvent) event.getEntity().getLastDamageCause()).getDamager();
					LivingEntity Shooter = ThrownPotion.getShooter();
					if (event.getEntity() == Shooter) { Cause = "Suicide"; }
					else if (Shooter instanceof Player) { Attacker = event.getEntity().getKiller().getName(); Cause = "PVP"; }
					else if (Shooter instanceof Witch) { Cause = "CraftWitch"; }
				}
			}
			else if (Cause.equalsIgnoreCase("WITHER")) { Cause = "Wither"; }
			else if (Cause.equalsIgnoreCase("FALLING_BLOCK")) { Cause = "FallingBlock"; }
			else if ((Cause.equalsIgnoreCase("ENTITY_ATTACK") || Cause.equalsIgnoreCase("PROJECTILE")) && (event.getEntity().getKiller() instanceof Player))
			{
				Attacker = event.getEntity().getKiller().getName();
				if (Cause == "ENTITY_ATTACK") { Cause = "PVP"; }
				else if (Cause == "PROJECTILE") { String Projectile = ((EntityDamageByEntityEvent) event.getEntity().getLastDamageCause()).getDamager().toString(); if (Projectile == "CraftArrow") { Cause = "PVPArrow"; } else { Cause = "PVP"; } }
			}
			else if ((Cause.equalsIgnoreCase("ENTITY_ATTACK") || Cause.equalsIgnoreCase("PROJECTILE")) && (event.getEntity().getKiller() instanceof Player == false || Cause.equalsIgnoreCase("ENTITY_EXPLOSION")))
			{
				Cause = ((EntityDamageByEntityEvent) event.getEntity().getLastDamageCause()).getDamager().toString();
				if (((EntityDamageByEntityEvent) event.getEntity().getLastDamageCause()).getDamager() instanceof EnderDragon) { Cause = "CraftEnderDragon"; }
				else if (Cause == "CraftArrow") { Arrow Arrow = (Arrow) ((EntityDamageByEntityEvent) event.getEntity().getLastDamageCause()).getDamager(); LivingEntity Shooter = Arrow.getShooter(); if (Shooter instanceof Skeleton) { Cause = "CraftSkeletonArrow"; } else { Cause = "Arrow"; } }
				else if (Cause == "CraftCreeper") { CraftCreeper Creeper = (CraftCreeper) ((EntityDamageByEntityEvent) event.getEntity().getLastDamageCause()).getDamager(); if (Creeper.isPowered()) { Cause = "CraftChargedCreeper";  } else { Cause = "CraftCreeper";  } }
				else if ((Cause == "CraftSmallFireball") || (Cause == "CraftLargeFireball") || (Cause == "CraftFireball")) { Fireball Fireball = (Fireball) ((EntityDamageByEntityEvent) event.getEntity().getLastDamageCause()).getDamager(); LivingEntity Shooter =  Fireball.getShooter(); if (Shooter instanceof Blaze) { Cause = "CraftBlaze"; } else if (Shooter instanceof Ghast) { Cause = "CraftGhast"; } else { Cause = "Fireball"; } }
				else if (Cause == "CraftSkeleton") { CraftSkeleton Damager = (CraftSkeleton) ((EntityDamageByEntityEvent) event.getEntity().getLastDamageCause()).getDamager(); int SkeletonType = Damager.getHandle().getSkeletonType(); if (SkeletonType == 1) { Cause = "CraftWitherSkeleton";  } else { Cause = "CraftSkeleton";  } }
				else if ((Cause == "CraftWither") || (Cause == "CraftWitherSkull")) { Cause = "CraftWither"; }
				else if (Cause.substring(0, 9).equals("CraftWolf")) { Wolf Wolf = (Wolf) ((EntityDamageByEntityEvent)event.getEntity().getLastDamageCause()).getDamager(); if (Wolf.isTamed()) { Cause = "CraftWolfIsTamed"; String Owner = ((Player) Wolf.getOwner()).getName(); Attacker = Owner; } else { Cause = "CraftWolf"; } }
				else if (Config.getStringList("MessageManager.Message.Death." + Cause) == null) { Cause = "Default"; }
			}
			else
			{ Cause = "Default"; }
			List<String> DeathMessages = new ArrayList<String>(Config.getStringList("MessageManager.Message.Death." + Cause));
			int MessageNumber = (int) (Math.random() * DeathMessages.size());
			getServer().broadcastMessage(DeathMessages.get(MessageNumber).replaceAll("%Player", event.getEntity().getName()).replaceAll("%Attacker", Attacker));
			event.setDeathMessage(null);
		}
		
		if (Config.getBoolean("CommandManager.Enable") && Config.getBoolean("CommandManager.Setting.Back.Enable"))
		{
			Data.set("Player." + event.getEntity().getName().toUpperCase() + ".DeathPoint", event.getEntity().getLocation().getWorld().getName() + "," + (float) event.getEntity().getLocation().getX() + "," + (float) event.getEntity().getLocation().getY() + "," + (float) event.getEntity().getLocation().getZ());
			DataSave();
		}
		
	}
	
	@EventHandler
	public void PlayerRespawn (PlayerRespawnEvent event)
	{
		if (Config.getBoolean("MessageManager.Enable") && Config.getString("MessageManager.Message.Respawn").equals("") == false)
		{
			getServer().broadcastMessage(Config.getString("MessageManager.Message.Respawn").replaceAll("%Player", event.getPlayer().getName()));
		}
		
		if (Config.getBoolean("CommandManager.Enable") && Config.getBoolean("CommandManager.Setting.Back.Enable") && Config.getString("CommandManager.Setting.Back.TipMessage").equals("") == false)
		{
			final Player Player = event.getPlayer();
			getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() { public void run() { if (Data.getBoolean("Player." + Player.getPlayer().getName().toUpperCase() + ".Tip") && Config.getString("CommandManager.Setting.Back.TipMessage").equals("") == false) { Player.getPlayer().sendMessage(Config.getString("CommandManager.Setting.Back.TipMessage")); } } } ,20);
		}
	}
	
	@EventHandler
	public void PlayerInteract (PlayerInteractEvent event)
	{		
		if (Config.getBoolean("ExpansionManager.Enable") && Config.getBoolean("ExpansionManager.Setting.Protection.Enable"))
		{
			List<String> ProtectionWorldList = new ArrayList<String>(Config.getStringList("ExpansionManager.Setting.Protection.WorldName"));
			boolean ProtectionEnable = false;
			String ProtectionWorldName = null;
			int Number = 0;
			for (Number = 0; Number != ProtectionWorldList.size(); Number++)
			{
				ProtectionWorldName = ProtectionWorldList.get(Number);
				if (event.getPlayer().getLocation().getWorld().getName().equalsIgnoreCase(ProtectionWorldName))
				{
					ProtectionEnable = true;
					break;
				}
			}
			if (ProtectionEnable != false)
			{
				int ProtectionSpawnX = getServer().getWorld(ProtectionWorldName).getSpawnLocation().getBlockX();
				int ProtectionSpawnZ = getServer().getWorld(ProtectionWorldName).getSpawnLocation().getBlockZ();
				int ProtectionSpawnR = getServer().getSpawnRadius();
				int ProtectionEventX = event.getPlayer().getLocation().getBlockX();
				int ProtectionEventZ = event.getPlayer().getLocation().getBlockZ();
				boolean ProtectionSpawnLocation = ((ProtectionEventX >= (ProtectionSpawnX-ProtectionSpawnR) && ProtectionEventX <= (ProtectionSpawnX+ProtectionSpawnR)) && (ProtectionEventZ >= (ProtectionSpawnZ-ProtectionSpawnR) && ProtectionEventZ <= (ProtectionSpawnZ+ProtectionSpawnR)));
				
				if (event.getAction() == Action.PHYSICAL && event.getClickedBlock().getType() == Material.SOIL)
				{
					String[] ProtectionType = Config.getString("ExpansionManager.Setting.Protection.PlayerInteractTrampleSoil").split(",");
					byte Type = Byte.valueOf(ProtectionType[Number]);
					if (Type == 1) { event.setCancelled(true); }
					else if (Type == 2 && ProtectionSpawnLocation) { event.setCancelled(true); }
				}
				else if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_BLOCK)
				{
					int EventBlockID = event.getClickedBlock().getTypeId();
					List<String> ProtectionBlockIDListArray = new ArrayList<String>(Config.getStringList("ExpansionManager.Setting.Protection.PlayerInteractBreakBlockIDList"));
					String[] ProtectionBlockIDList = ProtectionBlockIDListArray.get(Number).split(",");
					if (ProtectionBlockIDListArray.get(Number) != null && ProtectionBlockIDListArray.get(Number).length() != 0)
					{
						for (String Each : ProtectionBlockIDList)
						{
							int AnyOneID = Integer.valueOf(Each).intValue();
							if (EventBlockID == AnyOneID)
							{
								byte Type = (byte) Config.getInt("ExpansionManager.Setting.Protection.PlayerInteractBreakBlock");
								if (event.getPlayer().isOp() == true) { event.setCancelled(false); }
								else if (Type == 1) { event.setCancelled(true); }
								else if ((Type == 2) && ProtectionSpawnLocation) { event.setCancelled(true); }
							}
						}
					}
				}
			}
		}
	}
	
	@EventHandler
	public void PlayerChat (AsyncPlayerChatEvent event)
	{
		if (Config.getBoolean("MessageManager.Enable") && Config.getString("MessageManager.Message.Chat").equals("") == false)
		{
			event.setFormat(Config.getString("MessageManager.Message.Chat").replaceAll("%Player", event.getPlayer().getName()).replaceAll("%Message", event.getMessage().replaceAll("\\\\", "").replaceAll("\\$", "").replaceAll("\\%", "")));
		}
	}
	
	@EventHandler
	public void PlayerCommandPreprocess (PlayerCommandPreprocessEvent event)
	{
		if (Config.getBoolean("CommandManager.Enable") && Config.getBoolean("CommandManager.Setting.CommandRestrict.DisableOtherCommand"))
		{
			final Player Player = event.getPlayer();
			
			boolean Allow = false;
			boolean CoolDown = false;
			boolean SpendFood = false;
			boolean SpendMoney = false;
			boolean SpendYes = false;
			
			if (Data.get("Player." + Player.getName().toUpperCase() + ".Command") != null)
			{
				String[] CommandData = Data.getString("Player." + Player.getName().toUpperCase() + ".Command").split("[;]");
				if (event.getMessage().equalsIgnoreCase("/spend yes")) { event.setMessage(CommandData[0]); SpendYes = true; }
				else { Data.set("Player." + Player.getName().toUpperCase() + ".Command", null); DataSave(); }
			}
			
			if (Data.get("Player." + Player.getName().toUpperCase() + ".Command") == null || SpendYes == true)
			{
				List<String> CommandList = new ArrayList<String>(Config.getStringList("CommandManager.Setting.CommandRestrict.List"));
				final String[] CommandString = event.getMessage().replaceFirst("/", "").split(" ");
				int CommandNumber = 0;
				for (int AllowListNumber = 0; AllowListNumber <= CommandList.size() - 1; AllowListNumber++)
				{
					String[] Command = CommandList.get(AllowListNumber).split(",");
					if (CommandString[0].equalsIgnoreCase(Command[0]))
					{
						Allow = true;
						CommandNumber = AllowListNumber;
						if (Command.length > 1 && Command[1].equals("0") == false) { CoolDown = true; }
						if (Command.length > 2 && Command[2].equals("0") == false) { SpendFood = true; }
						if (Command.length > 3 && Command[3].equals("0") == false && Config.getBoolean("MoneySystem.Enable")) { SpendMoney = true; }
					}
				}
				
				final String[] Command = CommandList.get(CommandNumber).split(",");
				
				if (Allow == false && event.getPlayer().isOp() == false)
				{
					if (Config.getString("CommandManager.Message.ErrorCommand").equals("") == false) { event.getPlayer().sendMessage(Config.getString("CommandManager.Message.ErrorCommand")); }
					event.setCancelled(true);
				}
				
				if (SpendMoney == true)
				{
					if (Integer.valueOf(Command[3]) > Data.getInt("Player." + Player.getName().toUpperCase() + ".Money"))
					{
						if (Config.getString("CommandManager.Message.ErrorMoneyNotEnough").equals("") == false)
						{
							event.getPlayer().sendMessage(Config.getString("CommandManager.Message.ErrorMoneyNotEnough").replaceAll("%Money", NumberFormat.getInstance().format(Integer.valueOf(Command[3]))));
						}
						SpendMoney = false;
						SpendFood = false;
						CoolDown = false;
						if (Data.get("Player." + Player.getName().toUpperCase() + ".Command") != null)
						{
							Data.set("Player." + Player.getName().toUpperCase() + ".Command", null);
							DataSave();
						}
						event.setCancelled(true);
					}
					else if (SpendYes == false && Data.get("Player." + Player.getName().toUpperCase() + ".Command") == null)
					{
						if (Config.getString("CommandManager.Message.SpendTip").equals("") == false)
						{
							String SpendTip = Config.getString("CommandManager.Message.SpendTip").replaceAll("%Command", Command[0]);
							String SpendTipFood = "";
							String SpendTipMoney = "";
							if (SpendFood == true) { SpendTipFood = Config.getString("CommandManager.Message.SpendTipFood").replaceAll("%Food", Command[2]); }
							if (SpendMoney == true) { SpendTipMoney = Config.getString("CommandManager.Message.SpendTipMoney").replaceAll("%Money", Command[3]); }
							if (SpendFood == true && SpendMoney == true) { SpendTip = SpendTip.replaceAll("%Spend", SpendTipFood + SpendTipMoney); }
							else if (SpendFood == true) { SpendTip = SpendTip.replaceAll("%Spend", SpendTipFood); }
							else if (SpendMoney == true) { SpendTip = SpendTip.replaceAll("%Spend", SpendTipMoney); }
							event.getPlayer().sendMessage(SpendTip);
						}
						Data.set("Player." + Player.getName().toUpperCase() + ".Command", event.getMessage() + ";" + Command[2] + ";" + Command[3]);
						SpendMoney = false;
						SpendFood = false;
						CoolDown = false;
						event.setCancelled(true);
					}
				}
				
				if (SpendFood == true)
				{
					if (Integer.valueOf(Command[2]) > event.getPlayer().getFoodLevel())
					{
						SpendMoney = false;
						SpendFood = false;
						CoolDown = false;
						if (Data.get("Player." + Player.getName().toUpperCase() + ".Command") != null)
						{
							Data.set("Player." + Player.getName().toUpperCase() + ".Command", null);
							DataSave();
						}
						if (Config.getString("CommandManager.Message.ErrorFoodNotEnough").equals("") == false)
						{
							event.getPlayer().sendMessage(Config.getString("CommandManager.Message.ErrorFoodNotEnough").replaceAll("%Food", Command[2]));
						}
						event.setCancelled(true);
					}
					else if (SpendYes == false && Data.get("Player." + Player.getName().toUpperCase() + ".Command") == null)
					{
						if (Config.getString("CommandManager.Message.SpendTip").equals("") == false)
						{
							String SpendTip = Config.getString("CommandManager.Message.SpendTip").replaceAll("%Command", Command[0]);
							String SpendTipFood = "";
							String SpendTipMoney = "";
							if (SpendFood == true) { SpendTipFood = Config.getString("CommandManager.Message.SpendTipFood").replaceAll("%Food", Command[2]); }
							if (SpendMoney == true) { SpendTipMoney = Config.getString("CommandManager.Message.SpendTipMoney").replaceAll("%Money", Command[3]); }
							if (SpendFood == true && SpendMoney == true) { SpendTip = SpendTip.replaceAll("%Spend", SpendTipFood + SpendTipMoney); }
							else if (SpendFood == true) { SpendTip = SpendTip.replaceAll("%Spend", SpendTipFood); }
							else if (SpendMoney == true) { SpendTip = SpendTip.replaceAll("%Spend", SpendTipMoney); }
							event.getPlayer().sendMessage(SpendTip);
						}
						Data.set("Player." + Player.getName().toUpperCase() + ".Command", event.getMessage() + ";" + Command[2] + ";" + Command[3]);
						DataSave();
						SpendMoney = false;
						SpendFood = false;
						CoolDown = false;
						event.setCancelled(true);
					}
				}
				
				if (CoolDown == true)
				{
					if (Data.get("Player." + event.getPlayer().getName().toUpperCase() + ".CommandCooldown." + CommandString[0].toLowerCase()) != null)
					{
						int CommandCooldown = Integer.valueOf(Command[1]);
						int IntervalTime = (int) ((System.currentTimeMillis() - (long) Data.get("Player." + event.getPlayer().getName().toUpperCase() + ".CommandCooldown." + CommandString[0].toLowerCase())) / 1000);
						if (IntervalTime < CommandCooldown)
						{
							String[] TimeFormatList = Config.getString("CommandManager.Setting.CommandRestrict.TimeFormat").split(",");
							String TimeFormat = null;
							int WaitTime = (CommandCooldown - IntervalTime);
							if (((CommandCooldown - IntervalTime) / 3600) >= 1) { TimeFormat = TimeFormatList[2]; WaitTime = WaitTime / 3600;}
							else if (((CommandCooldown - IntervalTime) / 60) >= 1) { TimeFormat = TimeFormatList[1]; WaitTime = WaitTime/ 60;}
							else { TimeFormat = TimeFormatList[0]; }
							if (Config.getString("CommandManager.Message.ErrorCommandCooldown").equals("") == false) { event.getPlayer().sendMessage(Config.getString("CommandManager.Message.ErrorCommandCooldown").replaceAll("%Time", String.valueOf(WaitTime)).replaceAll("%Format", TimeFormat)); }
							SpendMoney = false;
							SpendFood = false;
							CoolDown = false;
							event.setCancelled(true);
						}
					}
				}
				
				final boolean FinalCoolDown = CoolDown;
				final boolean FinalSpendFood = SpendFood;
				final boolean FinalSpendMoney = SpendMoney;
				final boolean FinalSpendYes = SpendYes;
				
				if (SpendMoney == true && SpendYes == true && Data.get("Player." + Player.getName().toUpperCase() + ".Command") != null)
				{
					int SpendMoneyAmount = Integer.valueOf(Command[3]);
					int PlayerMoneyAmount = Data.getInt("Player." + Player.getName().toUpperCase() + ".Money");
					Data.set("Player." + Player.getName().toUpperCase() + ".Money", PlayerMoneyAmount - SpendMoneyAmount);
					if (Config.getString("CommandManager.Message.SpendMoney").equals("") == false) { Player.sendMessage(Config.getString("CommandManager.Message.SpendMoney").replaceAll("%Money", NumberFormat.getInstance().format(SpendMoneyAmount))); }
				}
				
				if (SpendFood == true && SpendYes == true && Data.get("Player." + Player.getName().toUpperCase() + ".Command") != null)
				{
					event.getPlayer().setFoodLevel(event.getPlayer().getFoodLevel() - Integer.valueOf(Command[2]));
					if (Config.getString("CommandManager.Message.SpendFood").equals("") == false) { Player.sendMessage(Config.getString("CommandManager.Message.SpendFood").replaceAll("%Food", Command[2])); }
				}
				
				if (CoolDown == true)
				{
					if (Data.get("Player." + event.getPlayer().getName().toUpperCase() + ".CommandCooldown." + CommandString[0].toLowerCase()) == null)
					{
						Data.set("Player." + event.getPlayer().getName().toUpperCase() + ".CommandCooldown." + CommandString[0].toLowerCase(), System.currentTimeMillis());
						DataSave();
						
					}
					else
					{
						int CommandCooldown = Integer.valueOf(Command[1]);
						int IntervalTime = (int) ((System.currentTimeMillis() - (long) Data.get("Player." + event.getPlayer().getName().toUpperCase() + ".CommandCooldown." + CommandString[0].toLowerCase())) / 1000);
						if (IntervalTime >= CommandCooldown)
						{
							Data.set("Player." + event.getPlayer().getName().toUpperCase() + ".CommandCooldown." + CommandString[0].toLowerCase(), System.currentTimeMillis());
							DataSave();
						}
					}
				}
				
				if (SpendFood == true && SpendMoney == true && SpendYes == true && Data.get("Player." + Player.getName().toUpperCase() + ".Command") != null)
				{
					Data.set("Player." + Player.getName().toUpperCase() + ".Command", null);
					DataSave();
				}
				else if (SpendFood == true && SpendYes == true && Data.get("Player." + Player.getName().toUpperCase() + ".Command") != null)
				{
					Data.set("Player." + Player.getName().toUpperCase() + ".Command", null);
					DataSave();
				}
				else if (SpendMoney == true && SpendYes == true && Data.get("Player." + Player.getName().toUpperCase() + ".Command") != null)
				{
					Data.set("Player." + Player.getName().toUpperCase() + ".Command", null);
					DataSave();
				}

				getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() { public void run()
				{
					if (Correct == true && FinalCoolDown == true)
					{
						Data.set("Player." + Player.getName().toUpperCase() + ".CommandCooldown." + CommandString[0].toLowerCase(), null);
						DataSave();
					}
					if (Correct == true && FinalSpendFood == true && FinalSpendYes == true)
					{
						Player.setFoodLevel(Player.getFoodLevel() + Integer.valueOf(Command[2]));
						if (Config.getString("CommandManager.Message.SpendFoodCorrect").equals("") == false) { Player.sendMessage(Config.getString("CommandManager.Message.SpendFoodCorrect").replaceAll("%Food", Command[2])); }
					}
					if (Correct == true && FinalSpendMoney == true && FinalSpendYes == true)
					{
						Data.set("Player." + Player.getName().toUpperCase() + ".Money", Data.getInt("Player." + Player.getName().toUpperCase() + ".Money") + Integer.valueOf(Command[3]));
						DataSave();
						if (Config.getString("CommandManager.Message.SpendMoneyCorrect").equals("") == false) { Player.sendMessage(Config.getString("CommandManager.Message.SpendMoneyCorrect").replaceAll("%Money", NumberFormat.getInstance().format(Integer.valueOf(Command[3])))); }
					}
					Correct = false;
				} } );
			}
		}
	}
	
	@EventHandler
	public void EntityBreakDoor (EntityBreakDoorEvent event)
	{
		if (Config.getBoolean("ExpansionManager.Enable") && Config.getBoolean("ExpansionManager.Setting.Protection.Enable"))
		{
			List<String> ProtectionWorldList = new ArrayList<String>(Config.getStringList("ExpansionManager.Setting.Protection.WorldName"));
			boolean ProtectionEnable = false;
			String ProtectionWorldName = null;
			int Number = 0;
			for (Number = 0; Number != ProtectionWorldList.size(); Number++)
			{
				ProtectionWorldName = ProtectionWorldList.get(Number);
				if (event.getBlock().getLocation().getWorld().getName().equalsIgnoreCase(ProtectionWorldName))
				{
					ProtectionEnable = true;
					break;
				}
			}
			if (ProtectionEnable != false)
			{
				int ProtectionSpawnX = getServer().getWorld(ProtectionWorldName).getSpawnLocation().getBlockX();
				int ProtectionSpawnZ = getServer().getWorld(ProtectionWorldName).getSpawnLocation().getBlockZ();
				int ProtectionSpawnR = getServer().getSpawnRadius();
				int ProtectionEventX = event.getBlock().getLocation().getBlockX();
				int ProtectionEventZ = event.getBlock().getLocation().getBlockZ();
				boolean ProtectionSpawnLocation = ((ProtectionEventX >= (ProtectionSpawnX-ProtectionSpawnR) && ProtectionEventX <= (ProtectionSpawnX+ProtectionSpawnR)) && (ProtectionEventZ >= (ProtectionSpawnZ-ProtectionSpawnR) && ProtectionEventZ <= (ProtectionSpawnZ+ProtectionSpawnR)));
            	String[] ProtectionType = Config.getString("ExpansionManager.Setting.Protection.EntityBreakDoor").split(",");
				byte Type = Byte.valueOf(ProtectionType[Number]);
				if (Type == 1) { event.setCancelled(true); }
				else if ((Type == 2) && ProtectionSpawnLocation) { event.setCancelled(true); }
            }
		}
	}
	
	@EventHandler
	public void EntityChangeBlock (EntityChangeBlockEvent event)
	{
		if (Config.getBoolean("ExpansionManager.Enable") && Config.getBoolean("ExpansionManager.Setting.Protection.Enable"))
		{
			List<String> ProtectionWorldList = new ArrayList<String>(Config.getStringList("ExpansionManager.Setting.Protection.WorldName"));
			boolean ProtectionEnable = false;
			String ProtectionWorldName = null;
			int Number = 0;
			for (Number = 0; Number != ProtectionWorldList.size(); Number++)
			{
				ProtectionWorldName = ProtectionWorldList.get(Number);
				if (event.getBlock().getLocation().getWorld().getName().equalsIgnoreCase(ProtectionWorldName))
				{
					ProtectionEnable = true;
					break;
				}
			}
			if (ProtectionEnable != false)
			{
				int ProtectionSpawnX = getServer().getWorld(ProtectionWorldName).getSpawnLocation().getBlockX();
				int ProtectionSpawnZ = getServer().getWorld(ProtectionWorldName).getSpawnLocation().getBlockZ();
				int ProtectionSpawnR = getServer().getSpawnRadius();
				int ProtectionEventX = event.getBlock().getLocation().getBlockX();
				int ProtectionEventZ = event.getBlock().getLocation().getBlockZ();
				boolean ProtectionSpawnLocation = ((ProtectionEventX >= (ProtectionSpawnX-ProtectionSpawnR) && ProtectionEventX <= (ProtectionSpawnX+ProtectionSpawnR)) && (ProtectionEventZ >= (ProtectionSpawnZ-ProtectionSpawnR) && ProtectionEventZ <= (ProtectionSpawnZ+ProtectionSpawnR)));
				byte Type = 0;
				String[] ProtectionType = null;
				if (event.getEntity() instanceof Enderman) { ProtectionType = Config.getString("ExpansionManager.Setting.Protection.EntityChangeBlockByEnderman").split(","); }
				else if (event.getEntity() instanceof Wither) { ProtectionType = Config.getString("ExpansionManager.Setting.Protection.EntityChangeBlockByWither").split(","); }
				if (ProtectionType != null)
				{
					Type = Byte.valueOf(ProtectionType[Number]);
					if (Type == 1) { event.setCancelled(true); }
					else if ((Type == 2) && ProtectionSpawnLocation) { event.setCancelled(true); }
				}
			}
		}
	}
	
	@EventHandler
	public void EntityDamageByEntity (EntityDamageByEntityEvent event)
	{
		if (Config.getBoolean("ExpansionManager.Enable") && Config.getBoolean("ExpansionManager.Setting.Protection.Enable"))
		{
			List<String> ProtectionWorldList = new ArrayList<String>(Config.getStringList("ExpansionManager.Setting.Protection.WorldName"));
			boolean ProtectionEnable = false;
			String ProtectionWorldName = null;
			int Number = 0;
			for (Number = 0; Number != ProtectionWorldList.size(); Number++)
			{
				ProtectionWorldName = ProtectionWorldList.get(Number);
				if (event.getEntity().getLocation().getWorld().getName().equalsIgnoreCase(ProtectionWorldName))
				{
					ProtectionEnable = true;
					break;
				}
			}
			if (ProtectionEnable != false)
			{
				int ProtectionSpawnX = getServer().getWorld(ProtectionWorldName).getSpawnLocation().getBlockX();
				int ProtectionSpawnZ = getServer().getWorld(ProtectionWorldName).getSpawnLocation().getBlockZ();
				int ProtectionSpawnR = getServer().getSpawnRadius();
				int ProtectionEventX = event.getEntity().getLocation().getBlockX();
				int ProtectionEventZ = event.getEntity().getLocation().getBlockZ();
				boolean ProtectionSpawnLocation = ((ProtectionEventX >= (ProtectionSpawnX-ProtectionSpawnR) && ProtectionEventX <= (ProtectionSpawnX+ProtectionSpawnR)) && (ProtectionEventZ >= (ProtectionSpawnZ-ProtectionSpawnR) && ProtectionEventZ <= (ProtectionSpawnZ+ProtectionSpawnR)));
            	String[] ProtectionType = Config.getString("ExpansionManager.Setting.Protection.EntityDamage").split(",");
				byte Type = Byte.valueOf(ProtectionType[Number]);
				if (event.getEntity() instanceof Animals || event.getEntity() instanceof IronGolem || event.getEntity() instanceof Player || event.getEntity() instanceof Snowman || event.getEntity() instanceof Squid || event.getEntity() instanceof Villager)
				{
					if (event.getDamager() instanceof Player && ((Player) event.getDamager()).isOp()) { event.setCancelled(false); }
					else if (Type == 1) { event.setCancelled(true); }
					else if (Type == 2 && ProtectionSpawnLocation) { event.setCancelled(true); }
				}
            }
		}
	}
	
	@EventHandler
	public void EntityDamage (EntityDamageEvent event)
	{
		if (event.getCause().toString().equalsIgnoreCase("LIGHTNING") && event.getEntity() instanceof Player && ((Player) event.getEntity()).getLastPlayed() == 0 && Config.getBoolean("CommandManager.Enable") && Config.getBoolean("CommandManager.Setting.Spawn.Enable") && Config.getBoolean("CommandManager.Setting.Spawn.FirstJoinStrikeLighting")) { event.setCancelled(true); }
		
		if (Config.getBoolean("ExpansionManager.Enable") && Config.getBoolean("ExpansionManager.Setting.Protection.Enable"))
		{
			List<String> ProtectionWorldList = new ArrayList<String>(Config.getStringList("ExpansionManager.Setting.Protection.WorldName"));
			boolean ProtectionEnable = false;
			String ProtectionWorldName = null;
			int Number = 0;
			for (Number = 0; Number != ProtectionWorldList.size(); Number++)
			{
				ProtectionWorldName = ProtectionWorldList.get(Number);
				if (event.getEntity().getLocation().getWorld().getName().equalsIgnoreCase(ProtectionWorldName))
				{
					ProtectionEnable = true;
					break;
				}
			}
			if (ProtectionEnable != false)
			{
				int ProtectionSpawnX = getServer().getWorld(ProtectionWorldName).getSpawnLocation().getBlockX();
				int ProtectionSpawnZ = getServer().getWorld(ProtectionWorldName).getSpawnLocation().getBlockZ();
				int ProtectionSpawnR = getServer().getSpawnRadius();
				int ProtectionEventX = event.getEntity().getLocation().getBlockX();
				int ProtectionEventZ = event.getEntity().getLocation().getBlockZ();
				boolean ProtectionSpawnLocation = ((ProtectionEventX >= (ProtectionSpawnX-ProtectionSpawnR) && ProtectionEventX <= (ProtectionSpawnX+ProtectionSpawnR)) && (ProtectionEventZ >= (ProtectionSpawnZ-ProtectionSpawnR) && ProtectionEventZ <= (ProtectionSpawnZ+ProtectionSpawnR)));
				if (event.getEntity() instanceof Animals || event.getEntity() instanceof IronGolem || event.getEntity() instanceof Player || event.getEntity() instanceof Snowman || event.getEntity() instanceof Squid || event.getEntity() instanceof Villager)
				{
					if (event.getCause().toString().equalsIgnoreCase("ENTITY_ATTACK") == false)
					{
						String[] ProtectionType = Config.getString("ExpansionManager.Setting.Protection.EntityDamage").split(",");
						byte Type = Byte.valueOf(ProtectionType[Number]);
						if (Type == 1) { event.setCancelled(true); }
						else if (Type == 2 && ProtectionSpawnLocation) { event.setCancelled(true); }
					}
				}
				
				if (event.getEntity() instanceof Wolf || event.getEntity() instanceof Ocelot)
				{
					AnimalTamer Owner;
					Owner = ((Tameable) event.getEntity()).getOwner();
					if (Owner != null && ((OfflinePlayer) Owner).isOnline() == false)
					{
						String[] ProtectionType = Config.getString("ExpansionManager.Setting.Protection.EntityDamageByPetWhenOwnerOffline").split(",");
						byte Type = Byte.valueOf(ProtectionType[Number]);
						if (Type == 1) { event.setCancelled(true); }
						else if (Type == 2 && ProtectionSpawnLocation) { event.setCancelled(true); }
					}
				}
            }
		}
	}
	
	@EventHandler
	public void EntityDeath (EntityDeathEvent event)
	{
		if (event.getEntity() instanceof Player == false && event.getEntity().getKiller() instanceof Player && Config.getBoolean("MoneySystem.Enable"))
		{
			String MobName = event.getEntity().toString().substring(5);
			if (MobName.equalsIgnoreCase("Skeleton")) { Skeleton Skeleton = (Skeleton) event.getEntity(); if (Skeleton.getSkeletonType().toString().equalsIgnoreCase("WITHER")) { MobName = "WitherSkeleton"; } else { MobName = "Skeleton"; } }
			else if (MobName.equalsIgnoreCase("Creeper")) { Creeper Creeper = (Creeper) event.getEntity(); if (Creeper.isPowered()) { MobName = "ChargedCreeper"; } else { MobName = "Creeper"; } }
			if (Config.getString("MoneySystem.Setting." + MobName) != null)
			{
				String[] MobArray = Config.getString("MoneySystem.Setting." + MobName).split(",");
				if (MobArray[0] != null && MobArray[1] != null && MobArray[2] != null)
				{
					String MobFinalName = MobName;
					if (Value.get("DataValue." + MobName) != null) { MobFinalName = Value.getString("DataValue." + MobName); }
					int DropMoney_Max = Integer.parseInt(MobArray[0]);
					int DropMoney_Min = Integer.parseInt(MobArray[1]);
					byte DropMoney_Percent = (byte) Integer.parseInt(MobArray[2]);
					if (DropMoney_Percent <= 0 || DropMoney_Max < DropMoney_Min) { return; }
					byte DropMoney_DropPercent = (byte) ((Math.random() * 100) + 1);
					if (DropMoney_Percent >= DropMoney_DropPercent)
					{
						String PlayerName = null;
						int PlayerMoney = 0;
						int DropMoney = (int) (Math.random() * (DropMoney_Max - DropMoney_Min)) + DropMoney_Min;
						if (DropMoney <= 0) { return; }
						if (MobName.equalsIgnoreCase("EnderDragon"))
						{
							for (Player Player : getServer().getOnlinePlayers()) { PlayerName = Player.getName().toUpperCase(); PlayerMoney = Data.getInt("Player." + PlayerName + ".Money"); Data.set("Player." + PlayerName + ".Money", PlayerMoney + DropMoney); DataSave(); }
							if (event.getEntity().getKiller() instanceof Player) { String HeroName = event.getEntity().getKiller().getPlayer().getName(); getServer().broadcastMessage(Config.getString("MoneySystem.Message.EnderDragonDeathByHero").replaceAll("%Monster", MobFinalName).replaceAll("%Drop", Integer.toString(DropMoney)).replaceAll("%Hero", HeroName)); }
							else { getServer().broadcastMessage(Config.getString("MoneySystem.Message.EnderDragonDeath").replaceAll("%Monster", MobFinalName).replaceAll("%Drop", Integer.toString(DropMoney))); }
						}
						else
						{
							Player Player = null;
							if (event.getEntity().getKiller() instanceof Player) { Player = event.getEntity().getKiller().getPlayer(); PlayerName = event.getEntity().getKiller().getPlayer().getName().toUpperCase(); }
							else if ((event.getEntity().getLastDamageCause().toString().contains("EntityDamageByEntityEvent")) && (event.getEntity().getLastDamageCause().getCause().toString() == "ENTITY_ATTACK") && (((EntityDamageByEntityEvent) event.getEntity().getLastDamageCause()).getDamager().toString().substring(0, 9).equalsIgnoreCase("CraftWolf")))
							{
								Wolf Wolf = (Wolf) ((EntityDamageByEntityEvent)event.getEntity().getLastDamageCause()).getDamager();
								if (Wolf.isTamed()) { Player = (Player) Wolf.getOwner(); PlayerName = ((Player) Wolf.getOwner()).getName().toString().toUpperCase(); }
							}
							if (Player != null && PlayerName != null)
							{
								PlayerMoney = Data.getInt("Player." + PlayerName + ".Money");
								Data.set("Player." + PlayerName + ".Money", PlayerMoney + DropMoney);
								if (Config.getString("MoneySystem.Message.GetMoney").equals("") == false) 
								{
									Player.sendMessage(Config.getString("MoneySystem.Message.GetMoney").replaceAll("%Monster", MobFinalName).replaceAll("%Drop", Integer.toString(DropMoney)));
								}
								DataSave();
							}
						}
					}
				}
			}
		}
	}
	
	@EventHandler
	public void EntityExplode (EntityExplodeEvent event)
	{
		if (Config.getBoolean("ExpansionManager.Enable") && Config.getBoolean("ExpansionManager.Setting.Protection.Enable"))
		{
			List<String> ProtectionWorldList = new ArrayList<String>(Config.getStringList("ExpansionManager.Setting.Protection.WorldName"));
			boolean ProtectionEnable = false;
			String ProtectionWorldName = null;
			int Number = 0;
			for (Number = 0; Number != ProtectionWorldList.size(); Number++)
			{
				ProtectionWorldName = ProtectionWorldList.get(Number);
				if (event.getLocation().getWorld().getName().equalsIgnoreCase(ProtectionWorldName))
				{
					ProtectionEnable = true;
					break;
				}
			}
			if (ProtectionEnable != false)
			{
				int ProtectionSpawnX = getServer().getWorld(ProtectionWorldName).getSpawnLocation().getBlockX();
				int ProtectionSpawnZ = getServer().getWorld(ProtectionWorldName).getSpawnLocation().getBlockZ();
				int ProtectionSpawnR = getServer().getSpawnRadius();
				int ProtectionEventX = event.getLocation().getBlockX();
				int ProtectionEventZ = event.getLocation().getBlockZ();
				boolean ProtectionSpawnLocation = ((ProtectionEventX >= (ProtectionSpawnX-ProtectionSpawnR) && ProtectionEventX <= (ProtectionSpawnX+ProtectionSpawnR)) && (ProtectionEventZ >= (ProtectionSpawnZ-ProtectionSpawnR) && ProtectionEventZ <= (ProtectionSpawnZ+ProtectionSpawnR)));
				byte Type = 0;
				String[] ProtectionType = null;
				if (event.getEntity() instanceof Creeper)
				{
	            	ProtectionType = Config.getString("ExpansionManager.Setting.Protection.EntityExplodeByCreeper").split(",");
					Type = Byte.valueOf(ProtectionType[Number]);
				}
				else if (event.getEntity() instanceof Fireball)
				{
	            	ProtectionType = Config.getString("ExpansionManager.Setting.Protection.EntityExplodeByFireball").split(",");
					Type = Byte.valueOf(ProtectionType[Number]);
				}
				else if (event.getEntity() instanceof TNTPrimed)
				{
	            	ProtectionType = Config.getString("ExpansionManager.Setting.Protection.EntityExplodeByTNT").split(",");
					Type = Byte.valueOf(ProtectionType[Number]);
				}
				else if (event.getEntity() instanceof Wither)
				{
	            	ProtectionType = Config.getString("ExpansionManager.Setting.Protection.EntityExplodeByWitherSpawn").split(",");
					Type = Byte.valueOf(ProtectionType[Number]);
				}
				else if (event.getEntity() instanceof WitherSkull)
				{
	            	ProtectionType = Config.getString("ExpansionManager.Setting.Protection.EntityExplodeByWitherSkull").split(",");
					Type = Byte.valueOf(ProtectionType[Number]);
				}
				if (Type == 1) { event.blockList().clear(); }
				else if (Type == 2 && ProtectionSpawnLocation) { event.blockList().clear(); }
            }
		}
	}
	
	@EventHandler
	public void HangingBreakByEntity (HangingBreakByEntityEvent event)
	{
		if (Config.getBoolean("ExpansionManager.Enable") && Config.getBoolean("ExpansionManager.Setting.Protection.Enable"))
		{
			List<String> ProtectionWorldList = new ArrayList<String>(Config.getStringList("ExpansionManager.Setting.Protection.WorldName"));
			boolean ProtectionEnable = false;
			String ProtectionWorldName = null;
			int Number = 0;
			for (Number = 0; Number != ProtectionWorldList.size(); Number++)
			{
				ProtectionWorldName = ProtectionWorldList.get(Number);
				if (event.getEntity().getLocation().getWorld().getName().equalsIgnoreCase(ProtectionWorldName))
				{
					ProtectionEnable = true;
					break;
				}
			}
			if (ProtectionEnable != false)
			{
				int ProtectionSpawnX = getServer().getWorld(ProtectionWorldName).getSpawnLocation().getBlockX();
				int ProtectionSpawnZ = getServer().getWorld(ProtectionWorldName).getSpawnLocation().getBlockZ();
				int ProtectionSpawnR = getServer().getSpawnRadius();
				int ProtectionEventX = event.getEntity().getLocation().getBlockX();
				int ProtectionEventZ = event.getEntity().getLocation().getBlockZ();
				boolean ProtectionSpawnLocation = ((ProtectionEventX >= (ProtectionSpawnX-ProtectionSpawnR) && ProtectionEventX <= (ProtectionSpawnX+ProtectionSpawnR)) && (ProtectionEventZ >= (ProtectionSpawnZ-ProtectionSpawnR) && ProtectionEventZ <= (ProtectionSpawnZ+ProtectionSpawnR)));
            	String[] ProtectionType = Config.getString("ExpansionManager.Setting.Protection.HangingBreak").split(",");
				byte Type = Byte.valueOf(ProtectionType[Number]);
				if (event.getRemover().getType() == EntityType.PLAYER && ((Player) event.getRemover()).isOp()) { event.setCancelled(false); }
				else if (Type == 1) { event.setCancelled(true); }
				else if (Type == 2 && ProtectionSpawnLocation) { event.setCancelled(true); }
            }
		}
	}
	
	@EventHandler
	public void HangingBreak (HangingBreakEvent event)
	{
		if (Config.getBoolean("ExpansionManager.Enable") && Config.getBoolean("ExpansionManager.Setting.Protection.Enable"))
		{
			List<String> ProtectionWorldList = new ArrayList<String>(Config.getStringList("ExpansionManager.Setting.Protection.WorldName"));
			boolean ProtectionEnable = false;
			String ProtectionWorldName = null;
			int Number = 0;
			for (Number = 0; Number != ProtectionWorldList.size(); Number++)
			{
				ProtectionWorldName = ProtectionWorldList.get(Number);
				if (event.getEntity().getLocation().getWorld().getName().equalsIgnoreCase(ProtectionWorldName))
				{
					ProtectionEnable = true;
					break;
				}
			}
			if (ProtectionEnable != false)
			{
				int ProtectionSpawnX = getServer().getWorld(ProtectionWorldName).getSpawnLocation().getBlockX();
				int ProtectionSpawnZ = getServer().getWorld(ProtectionWorldName).getSpawnLocation().getBlockZ();
				int ProtectionSpawnR = getServer().getSpawnRadius();
				int ProtectionEventX = event.getEntity().getLocation().getBlockX();
				int ProtectionEventZ = event.getEntity().getLocation().getBlockZ();
				boolean ProtectionSpawnLocation = ((ProtectionEventX >= (ProtectionSpawnX-ProtectionSpawnR) && ProtectionEventX <= (ProtectionSpawnX+ProtectionSpawnR)) && (ProtectionEventZ >= (ProtectionSpawnZ-ProtectionSpawnR) && ProtectionEventZ <= (ProtectionSpawnZ+ProtectionSpawnR)));
            	String[] ProtectionType = Config.getString("ExpansionManager.Setting.Protection.HangingBreak").split(",");
				byte Type = Byte.valueOf(ProtectionType[Number]);
				if (event.getCause() == RemoveCause.EXPLOSION || event.getCause() == RemoveCause.DEFAULT)
				{
					if (Type == 1) { event.setCancelled(true); }
					else if (Type == 2 && ProtectionSpawnLocation) { event.setCancelled(true); }
				}
            }
		}
	}
	
	@EventHandler
	public void HangingPlace (HangingPlaceEvent event)
	{
		if (Config.getBoolean("ExpansionManager.Enable") && Config.getBoolean("ExpansionManager.Setting.Protection.Enable"))
		{
			List<String> ProtectionWorldList = new ArrayList<String>(Config.getStringList("ExpansionManager.Setting.Protection.WorldName"));
			boolean ProtectionEnable = false;
			String ProtectionWorldName = null;
			int Number = 0;
			for (Number = 0; Number != ProtectionWorldList.size(); Number++)
			{
				ProtectionWorldName = ProtectionWorldList.get(Number);
				if (event.getEntity().getLocation().getWorld().getName().equalsIgnoreCase(ProtectionWorldName))
				{
					ProtectionEnable = true;
					break;
				}
			}
			if (ProtectionEnable != false)
			{
				int ProtectionSpawnX = getServer().getWorld(ProtectionWorldName).getSpawnLocation().getBlockX();
				int ProtectionSpawnZ = getServer().getWorld(ProtectionWorldName).getSpawnLocation().getBlockZ();
				int ProtectionSpawnR = getServer().getSpawnRadius();
				int ProtectionEventX = event.getEntity().getLocation().getBlockX();
				int ProtectionEventZ = event.getEntity().getLocation().getBlockZ();
				boolean ProtectionSpawnLocation = ((ProtectionEventX >= (ProtectionSpawnX-ProtectionSpawnR) && ProtectionEventX <= (ProtectionSpawnX+ProtectionSpawnR)) && (ProtectionEventZ >= (ProtectionSpawnZ-ProtectionSpawnR) && ProtectionEventZ <= (ProtectionSpawnZ+ProtectionSpawnR)));
            	String[] ProtectionType = Config.getString("ExpansionManager.Setting.Protection.HangingPlace").split(",");
				byte Type = Byte.valueOf(ProtectionType[Number]);
				if (event.getPlayer().isOp()) { event.setCancelled(false); }
				else if (Type == 1) { event.setCancelled(true); }
				else if (Type == 2 && ProtectionSpawnLocation) { event.setCancelled(true); }
                
            }
		}
	}
	
	@EventHandler
	public void BlockIgnite (BlockIgniteEvent event)
	{
		if (Config.getBoolean("ExpansionManager.Enable") && Config.getBoolean("ExpansionManager.Setting.Protection.Enable"))
		{
			List<String> ProtectionWorldList = new ArrayList<String>(Config.getStringList("ExpansionManager.Setting.Protection.WorldName"));
			boolean ProtectionEnable = false;
			String ProtectionWorldName = null;
			int Number = 0;
			for (Number = 0; Number != ProtectionWorldList.size(); Number++)
			{
				ProtectionWorldName = ProtectionWorldList.get(Number);
				if (event.getBlock().getLocation().getWorld().getName().equalsIgnoreCase(ProtectionWorldName))
				{
					ProtectionEnable = true;
					break;
				}
			}
			if (ProtectionEnable != false)
			{
				int ProtectionSpawnX = getServer().getWorld(ProtectionWorldName).getSpawnLocation().getBlockX();
				int ProtectionSpawnZ = getServer().getWorld(ProtectionWorldName).getSpawnLocation().getBlockZ();
				int ProtectionSpawnR = getServer().getSpawnRadius();
				int ProtectionEventX = event.getBlock().getLocation().getBlockX();
				int ProtectionEventZ = event.getBlock().getLocation().getBlockZ();
				boolean ProtectionSpawnLocation = ((ProtectionEventX >= (ProtectionSpawnX-ProtectionSpawnR) && ProtectionEventX <= (ProtectionSpawnX+ProtectionSpawnR)) && (ProtectionEventZ >= (ProtectionSpawnZ-ProtectionSpawnR) && ProtectionEventZ <= (ProtectionSpawnZ+ProtectionSpawnR)));
            	String[] ProtectionType = Config.getString("ExpansionManager.Setting.Protection.BlockIgniteByLightning").split(",");
				byte Type = Byte.valueOf(ProtectionType[Number]);
				if (event.getCause() == IgniteCause.LIGHTNING)
				{
					if (Type == 1) { event.setCancelled(true); }
					else if (Type == 2 && ProtectionSpawnLocation) { event.setCancelled(true); }
				}
            }
		}
	}
	
	@EventHandler
	public void PigZap (PigZapEvent event)
	{
		if (Config.getBoolean("ExpansionManager.Enable") && Config.getBoolean("ExpansionManager.Setting.Protection.Enable"))
		{
			List<String> ProtectionWorldList = new ArrayList<String>(Config.getStringList("ExpansionManager.Setting.Protection.WorldName"));
			boolean ProtectionEnable = false;
			String ProtectionWorldName = null;
			int Number = 0;
			for (Number = 0; Number != ProtectionWorldList.size(); Number++)
			{
				ProtectionWorldName = ProtectionWorldList.get(Number);
				if (event.getEntity().getLocation().getWorld().getName().equalsIgnoreCase(ProtectionWorldName))
				{
					ProtectionEnable = true;
					break;
				}
			}
			if (ProtectionEnable != false)
			{
				int ProtectionSpawnX = getServer().getWorld(ProtectionWorldName).getSpawnLocation().getBlockX();
				int ProtectionSpawnZ = getServer().getWorld(ProtectionWorldName).getSpawnLocation().getBlockZ();
				int ProtectionSpawnR = getServer().getSpawnRadius();
				int ProtectionEventX = event.getEntity().getLocation().getBlockX();
				int ProtectionEventZ = event.getEntity().getLocation().getBlockZ();
				boolean ProtectionSpawnLocation = ((ProtectionEventX >= (ProtectionSpawnX-ProtectionSpawnR) && ProtectionEventX <= (ProtectionSpawnX+ProtectionSpawnR)) && (ProtectionEventZ >= (ProtectionSpawnZ-ProtectionSpawnR) && ProtectionEventZ <= (ProtectionSpawnZ+ProtectionSpawnR)));
            	String[] ProtectionType = Config.getString("ExpansionManager.Setting.Protection.PigZap").split(",");
				byte Type = Byte.valueOf(ProtectionType[Number]);
				if (Type == 1) { event.setCancelled(true); }
				else if (Type == 2 && ProtectionSpawnLocation) { event.setCancelled(true); }
            }
		}
	}
	
	class TimerTaskBonus extends TimerTask
	{
		public void run()
		{
			if (getServer().getOnlinePlayers().length > 0)
			{
				for (Player Player : getServer().getOnlinePlayers())
				{
					String PlayerName = Player.getName().toUpperCase();
					int PlayerTime = (int) Data.get("Player." + PlayerName + ".Bonus");
					int TimerInterval = Config.getInt("BonusSystem.Setting.TimerInterval");
					int BonusInterval = Config.getInt("BonusSystem.Setting.BonusInterval");
					Data.set("Player." + PlayerName + ".Bonus", PlayerTime + TimerInterval);
					PlayerTime = (int) Data.get("Player." + PlayerName + ".Bonus");
					int LackQuantity = PlayerTime / BonusInterval;
					for (int GetTime = PlayerTime / BonusInterval; GetTime >= 1; GetTime = GetTime - 1 )
					{
						int BonusGroupMax = ((MemorySection) Config.get("BonusSystem.Group")).getValues(true).size();
						if (BonusGroupMax < 0) { BonusGroupMax = 0; }
						if (!(BonusGroupMax >= 0) ) { return; }
						List<String> BonusItem = null;
						String BonusMessage = null;
						String BonusBroadcastMessage = null;
						int BonusPercent = 0;
						int Percent = 0;
						boolean BonusBroadcast = false;
						do
						{
							int BonusGroup = (int) (Math.random() * BonusGroupMax);
							Percent = (int) (Math.random() * 100) + 1;
							BonusItem = Config.getStringList("BonusSystem.Group." + BonusGroup + ".Item");
							BonusPercent = Config.getInt("BonusSystem.Group." + BonusGroup + ".Percent");
							BonusMessage = Config.getString("BonusSystem.Group." + BonusGroup + ".Message");
							BonusBroadcast = Config.getBoolean("BonusSystem.Group." + BonusGroup + ".Broadcast");
							if (BonusBroadcast == true) { BonusBroadcastMessage = Config.getString("BonusSystem.Group." + BonusGroup + ".BroadcastMessage"); }
							if (BonusPercent > 100) { BonusPercent = 100; }
							if (BonusPercent < 1) { BonusPercent = 1; }
				        }
						while ((BonusItem == null) || (BonusMessage == null) || ((BonusBroadcast == true) && (BonusBroadcastMessage == null)) || (Percent > BonusPercent));
						List<?> ItemList = BonusItem;
						String[] ItemArray = (String[]) ItemList.toArray(new String[0]);
						for (int Number = 0; Number < ItemArray.length; Number++)
						{
							String[] ItemData = ItemArray[Number].split("x");
							if (!ItemData[0].equals("0"))
							{
								ItemStack BonusItemStack = new ItemStack(Material.getMaterial(Integer.parseInt(ItemData[0])), Integer.parseInt(ItemData[1]));
								if (Player.getInventory().firstEmpty() < 0) { Player.getWorld().dropItemNaturally(Player.getLocation(), BonusItemStack); } else { Player.getInventory().addItem(BonusItemStack); }
							}
							
							if (Value.get("DataValue." + ItemData[0]) != null)
							{
								BonusMessage = BonusMessage.replaceAll("%Item" + (Number + 1), Value.getString("DataValue." + ItemData[0]));
								if (BonusBroadcastMessage != null)
								{
									BonusBroadcastMessage = BonusBroadcastMessage.replaceAll("%Item" + (Number + 1), Value.getString("DataValue." + ItemData[0]));
								}
							}
						}
						if ((BonusBroadcast == true) && (BonusBroadcastMessage != null))
						{
							getServer().broadcastMessage(BonusBroadcastMessage.replaceAll("%Player", Player.getName()));
							if (BonusMessage.equals("") == false) { Player.sendMessage(BonusMessage); }
						}
						else if (BonusMessage.equals("") == false) { Player.sendMessage(BonusMessage); }
					}
					Data.set("Player." + PlayerName + ".Bonus", PlayerTime - (BonusInterval * LackQuantity));
					DataSave();
				}
			}
			else { this.cancel(); }
		}
	}
	
	class TimerTaskTip extends TimerTask
	{
		public void run()
		{
			if (getServer().getOnlinePlayers().length > 0)
			{
				List<String> Tip = new ArrayList<String>(Config.getStringList("MessageManager.Message.Tip.Tips"));
				if (TipNumber > Tip.size() - 1) { TipNumber = 0; }
				if (Config.getBoolean("MessageManager.Setting.Tip.Random")) { TipNumber = (int) (Math.random() * Tip.size()); }
				for (Player Player : getServer().getOnlinePlayers())
				{
					if ((Data.getBoolean("Player." + Player.getName().toUpperCase() + ".Tip") || Data.get("Player." + Player.getName().toUpperCase() + ".Tip") == null) && Tip.get(TipNumber).equals("") == false)
					{
						Player.sendMessage(Tip.get(TipNumber).toString());
					}
				}
				TipNumber = TipNumber + 1;
			}
			else { this.cancel(); }
		}
	}
	
	public void DataSave()
	{
		try { Data.save(DataFile); } catch (Exception event) { event.printStackTrace(); }
	}
	
}