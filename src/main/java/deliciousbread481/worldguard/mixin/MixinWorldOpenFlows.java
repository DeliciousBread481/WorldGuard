package deliciousbread481.worldguard.mixin;  
  
import deliciousbread481.worldguard.WorldGuardMod;  
import net.minecraft.client.gui.screens.Screen;  
import net.minecraft.client.gui.screens.worldselection.WorldOpenFlows;  
import org.spongepowered.asm.mixin.Mixin;  
import org.spongepowered.asm.mixin.injection.At;  
import org.spongepowered.asm.mixin.injection.Inject;  
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;  
  
@Mixin(WorldOpenFlows.class)  
public class MixinWorldOpenFlows {  
  
    @Inject(  
        method = "doLoadLevel",  
        at = @At(  
            value = "INVOKE",  
            target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Throwable;)V",  
            remap = false  
        ),  
        remap = false  
    )  
    private void worldguard_onLoadFailed(  
            Screen p_233146_, String p_233147_, boolean p_233148_, boolean p_233149_,  
            boolean confirmExperimentalWarning, CallbackInfo ci  
    ) {  
        WorldGuardMod.LOGGER.warn("[WorldGuard] World loading failed. This may be due to removed mods. " +  
                "WorldGuard will attempt to filter unavailable datapacks on retry.");  
    }  
}