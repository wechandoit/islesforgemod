package net.wechandoit.islesforgemod.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleType;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.wechandoit.islesforgemod.config.IslesAddonConfig;
import net.wechandoit.islesforgemod.util.MiscUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static net.wechandoit.islesforgemod.Islesforgemod.client;

@Mixin(ParticleManager.class)
public abstract class MixinParticleManager {

    @Inject(method = "addParticle(Lnet/minecraft/particles/IParticleData;DDDDDD)Lnet/minecraft/client/particle/Particle;", at = @At("HEAD"), cancellable = true)
    private void addParticle(IParticleData particleData, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, CallbackInfoReturnable<Boolean> callback) {

        List<EntityType> qtetypes = Arrays.asList(EntityType.MAGMA_CUBE, EntityType.ITEM);
        List<ParticleType> legendaryQTEParticleTypes = Arrays.asList(ParticleTypes.DRIPPING_HONEY, ParticleTypes.FALLING_HONEY, ParticleTypes.LANDING_HONEY);

        if ((particleData.getType() == ParticleTypes.HAPPY_VILLAGER && IslesAddonConfig.CONFIG.get("green-qte-notifier", Boolean.class))
                || (particleData.getType() == ParticleTypes.DRAGON_BREATH && IslesAddonConfig.CONFIG.get("purple-qte-notifier", Boolean.class))
                || (legendaryQTEParticleTypes.contains(particleData.getType()) && IslesAddonConfig.CONFIG.get("gold-qte-notifier", Boolean.class))) {
            AxisAlignedBB particleBox = new AxisAlignedBB(x - 0.5, y - 0.5, z - 0.5, x + 0.5, y + 0.5, z + 0.5);
            // get closest entity of type ItemEntity to particleLoc
            List<Entity> nearbyClickBoxes = client.world.getEntitiesInAABBexcluding(client.player, particleBox, (entity -> qtetypes.contains(entity.getType())));
            List<EntityType> nearbyEntityTypes = new ArrayList<>();
            nearbyClickBoxes.forEach(entity -> {
                if (!nearbyEntityTypes.contains(entity.getType()))
                    nearbyEntityTypes.add(entity.getType());
            });
            if (nearbyEntityTypes.containsAll(qtetypes)) {
                for (Entity e : nearbyClickBoxes) {
                    if (!isBlockTypeNearby(e, 1)) {
                        client.player.sendStatusMessage(MiscUtils.getMessage("There is a QTE nearby...", getColorFromType(particleData.getType())), false);
                    }
                }
            }
        }
    }

    private boolean isBlockTypeNearby(Entity e, int range) {
        for (int x = -range; x <= range; x++) {
            for (int y = -range; y <= range; y++) {
                for (int z = -range; z <= range; z++) {
                    Block checkBlock = client.world.getBlockState(e.getPosition().add(x, y, z)).getBlock();
                    if (checkBlock.equals(Blocks.CAMPFIRE) || checkBlock.equals(Blocks.IRON_TRAPDOOR))
                        return true;
                }
            }
        }
        return false;
    }

    private char getColorFromType(ParticleType<?> type) {
        if (type == ParticleTypes.HAPPY_VILLAGER) {
            return 'a';
        } else if (type == ParticleTypes.DRAGON_BREATH) {
            return '5';
        } else {
            return '6';
        }
    }

}
