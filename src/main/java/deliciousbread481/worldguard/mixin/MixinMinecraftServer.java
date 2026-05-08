package deliciousbread481.worldguard.mixin;  
  
import deliciousbread481.worldguard.WorldGuardMod;  
import net.minecraft.server.MinecraftServer;  
import net.minecraft.server.packs.repository.PackRepository;  
import net.minecraft.world.flag.FeatureFlagSet;  
import net.minecraft.world.level.DataPackConfig;  
import net.minecraft.world.level.WorldDataConfiguration;  
import org.spongepowered.asm.mixin.Mixin;  
import org.spongepowered.asm.mixin.injection.At;  
import org.spongepowered.asm.mixin.injection.Inject;  
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;  
  
import java.util.ArrayList;  
import java.util.List;  
  
@Mixin(MinecraftServer.class)  
public class MixinMinecraftServer {  
  
    @Inject(  
        method = "configurePackRepository",  
        at = @At("HEAD")  
    )  
    private static void worldguard_filterRemovedPacks(  
            PackRepository p_248681_,  
            DataPackConfig p_248920_,  
            boolean p_249869_,  
            FeatureFlagSet p_251243_,  
            CallbackInfoReturnable<WorldDataConfiguration> cir  
    ) {  
        List<String> enabled = p_248920_.getEnabled();  
        List<String> disabled = p_248920_.getDisabled();  
  
        List<String> removedPacks = new ArrayList<>();  
        for (String packId : enabled) {  
            if (!WorldGuardMod.isPackAvailable(packId)) {  
                removedPacks.add(packId);  
            }  
        }  
  
        if (!removedPacks.isEmpty()) {  
            WorldGuardMod.LOGGER.warn("[WorldGuard] Filtering out {} unavailable datapack(s) from removed mods: {}",  
                    removedPacks.size(), removedPacks);  
            enabled.removeAll(removedPacks);  
            for (String pack : removedPacks) {  
                if (!disabled.contains(pack)) {  
                    disabled.add(pack);  
                }  
            }  
        }  
    }  
}