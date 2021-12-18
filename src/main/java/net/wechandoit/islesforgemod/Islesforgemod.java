package net.wechandoit.islesforgemod;

import com.jagrosh.discordipc.IPCClient;
import com.jagrosh.discordipc.exceptions.NoDiscordClientException;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.monster.MagmaCubeEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.wechandoit.islesforgemod.config.IslesAddonConfig;
import net.wechandoit.islesforgemod.util.DiscordUtils;
import net.wechandoit.islesforgemod.util.MiscUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.OffsetDateTime;
import java.util.Iterator;
import java.util.List;

@Mod("islesforgemod")
public class Islesforgemod {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final String MOD_ID = "islesforgeaddons";
    public static Minecraft client = Minecraft.getInstance();
    public static IPCClient ipcClient = new IPCClient(904055870483222528L);

    private static boolean firstLoad;

    public static boolean isFishing = false;
    public static Entity fishingEntity = null;
    public static Entity fishingHoloEntity = null;
    public static boolean justStartedFishing = false;
    public static String islesLocation = "";

    private static int discordAppCount = 0;
    private static String previousIP = "";
    private static int clientTick = 1;
    private final String skullSignature = "ngBEheIaXuWnZaiWkxNB8XPN8Nbuo08mDHPZWNEVs82GnKfsC6lLU/nED3VGeHUT/8pxWxwS1Zjfuh/ty0Yzd7jovVrI8qYNIrHidHoct4twJ1Nch8+NmeIY7aE9yy6EuI81x1MK90vhMmyNHYnalMYMMbZE7TizwvzKKKdpvvrK8xspzNednbyXpTbHsAUV90SjdNH5TQlaI61XT+TCPYjX7nBDBcqPLMWWzO/SVskQfPoufphgdw7uOugZPiULtoQy6TEYGIXOjvFmBcF0HlHUbhHKuxUSSr5wLhb5kMZQaUTkWAJIfH3V/1wU/vSG5T1IU4kcw3LOlFr3uUZHzzU6w+a3mAE+P7aBBsgtB0Qrw8sB/miqArNjEAz4p52Mqly1o+PTFhPvczTNzStWNHg6oDsYlzZ+xtqD/5XAr32YUHwUgFld22b4bOsYWLPd1dvT0GxMVEFDadXVYD5Omf2Qr+6dAbFbIcVN8qe+/Wo+AsYmr49VQxifCxZ3kg6RnomPSwNsIN+xGZzr42bPA4iHSMJ19uvhX1pvrw19tTJ6zvfCKgutQYx/hse5BDOADDc0ci4Og9U/aQGX33Q76SsW61Clg0a5g9rpqxTuTgcLUSMoaPvOp0goW8CetHR0DqqwzqHXIAZJNdD9bL1q3hEbzW7VwTduD5R98ELNb/Q=";


    public Islesforgemod() {

//        firstLoad = IslesAddonConfig.doesNotExist();
//        IslesAddonConfig.load();
        ModContainer mod = ModLoadingContext.get().getActiveContainer();

        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        // Register the doClientStuff method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            closeIPC();
        }));

    }

    private void setup(final FMLCommonSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
    }

    public static boolean isFirstLoad() {
        return firstLoad;
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        // do something that can only be done on the client
        LOGGER.info("Got game settings {}", event.getMinecraftSupplier().get().gameSettings);
    }

    public static Entity getFishingHoloEntity(Entity e) {
        // Change to store armorstands near magma cube and then check if new armorstands pop up
        List<Entity> nearbyArmorstands = client.world.getEntitiesInAABBexcluding(client.player, e.getBoundingBox().expand(0, 1, 0), entity -> entity.getType() == EntityType.ARMOR_STAND);
        if (nearbyArmorstands.isEmpty()) return null;
        for (Entity armorStand : nearbyArmorstands) {
            if (armorStand.getDisplayName().toString().contains("Fishing")) {
                return armorStand;
            }
        }
        return null;
    }


    private boolean isFishingEntityAlive() {
        return fishingEntity != null && fishingEntity.isAlive();
    }

    private boolean isFishingArmorstandNearby() {
        return getFishingHoloEntity(fishingEntity) != null && fishingHoloEntity.getEntityId() != getFishingHoloEntity(fishingEntity).getEntityId();
    }

    public static boolean enableGlowingGiantSkulls = true;
    public static boolean enableFishingNotif = true;

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (enableFishingNotif) {
            Entity entity = event.getTarget();
            if (!isFishing && entity.getType() == EntityType.MAGMA_CUBE && !justStartedFishing && client.player.inventory.getFirstEmptyStack() > -1 && ((MagmaCubeEntity) entity).getSlimeSize() > 1 && Islesforgemod.getFishingHoloEntity(entity) != null) {
                isFishing = true;
                justStartedFishing = true;
                fishingEntity = entity;
                fishingHoloEntity = Islesforgemod.getFishingHoloEntity(entity);
            }
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            clientTick++;
            if (enableFishingNotif) {
                if (isFishing && (!isFishingEntityAlive() || isFishingArmorstandNearby() || client.player.isSneaking() || client.player.inventory.getFirstEmptyStack() == -1) && !justStartedFishing) {
                    isFishing = false;
                    fishingEntity = null;
                    fishingHoloEntity = null;
                    client.player.playSound(SoundEvents.ENTITY_ELDER_GUARDIAN_CURSE, 1F, 0.8F);
                    client.player.sendStatusMessage(MiscUtils.getMessage("You have stopped fishing...", 'e'), false);
                } else if (justStartedFishing) {
                    justStartedFishing = false;
                }
            }
            if (enableGlowingGiantSkulls) {
                // get closest armorstand to particleLoc
                try {
                    List<Entity> nearbyArmorStands = client.world.getEntitiesInAABBexcluding(client.player, client.player.getBoundingBox().expand(client.gameRenderer.getFarPlaneDistance(), client.gameRenderer.getFarPlaneDistance(), client.gameRenderer.getFarPlaneDistance()), (entity -> entity.getType() == EntityType.ARMOR_STAND));
                    for (Entity en : nearbyArmorStands) {
                        if (!en.isGlowing()) {
                            Iterator<ItemStack> armorItems = en.getArmorInventoryList().iterator();
                            while (armorItems.hasNext()) {
                                ItemStack stack = armorItems.next();
                                if (stack != null && !stack.toString().toUpperCase().contains("AIR") && stack.getTag() != null && stack.getTag().get("SkullOwner") != null && stack.getTag().get("SkullOwner").toString().contains(skullSignature)) {
                                    en.setGlowing(true);
                                }
                            }
                        }
                    }
                } catch (NullPointerException exception) {

                }
            }
            if (clientTick > 20) clientTick = 1;
            else if (clientTick == 20) {
                discordAppCount++;
                if (discordAppCount > 5) discordAppCount = 0;
                else if (discordAppCount == 5) {
                    if (MiscUtils.onIsles()) {
                        List<String> scoreboard = MiscUtils.getScoreboard();
                        if (scoreboard != null && scoreboard.size() > 1) {
                            if (scoreboard.get(1).startsWith("Rank: "))
                                DiscordUtils.updateRPC(scoreboard.get(2), "In Hub");
                            else {
                                DiscordUtils.updateRPC(scoreboard.get(2), "In Game");
                                islesLocation = scoreboard.get(2);
                            }
                        }
                    } else {
                        if (!Minecraft.getInstance().isSingleplayer())
                            DiscordUtils.updateRPC("In Game Menu", "");
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void playerJoinServer(ClientPlayerNetworkEvent.LoggedInEvent event) {
        setupIPC(client);
    }

    @SubscribeEvent
    public void playerLeaveServer(ClientPlayerNetworkEvent.LoggedOutEvent event) {
        closeIPC();
    }

    public void closeIPC() {
        if (MiscUtils.onIsles()) {
            previousIP = "";
            ipcClient.close();
        }
    }

    public void setupIPC(Minecraft client) {
        clientTick = 1;
        if (MiscUtils.onIsles()) {
            if (previousIP.equals("")) {
                previousIP = client.getCurrentServerData().serverIP;
                try {
                    ipcClient.connect();
                } catch (NoDiscordClientException e) {
                    e.printStackTrace();
                }
                DiscordUtils.lastTimestamp = OffsetDateTime.now().toEpochSecond();
            }
            DiscordUtils.updateRPC("", "");
        }
    }


}
