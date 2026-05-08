package deliciousbread481.worldguard.mixin;  
  
import com.google.gson.JsonElement;  
import com.google.gson.JsonParser;  
import com.mojang.serialization.DataResult;  
import com.mojang.serialization.Decoder;  
import net.minecraft.core.Registry;  
import net.minecraft.core.WritableRegistry;  
import net.minecraft.resources.*;  
import net.minecraft.server.packs.resources.Resource;  
import net.minecraft.server.packs.resources.ResourceManager;  
import deliciousbread481.worldguard.WorldGuardMod;  
import org.spongepowered.asm.mixin.Mixin;  
import org.spongepowered.asm.mixin.injection.At;  
import org.spongepowered.asm.mixin.injection.Inject;  
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;  
  
import java.io.Reader;  
import java.util.Map;  
  
@Mixin(RegistryDataLoader.class)  
public class MixinRegistryDataLoader {  
  
    @Inject(  
        method = "loadRegistryContents",  
        at = @At("HEAD"),  
        cancellable = true  
    )  
    private static <E> void worldguard_interceptLoadContents(  
            RegistryOps.RegistryInfoLookup p_256369_,  
            ResourceManager p_256349_,  
            ResourceKey<? extends Registry<E>> p_255792_,  
            WritableRegistry<E> p_256211_,  
            Decoder<E> p_256232_,  
            Map<ResourceKey<?>, Exception> p_255884_,  
            CallbackInfo ci  
    ) {  
        ci.cancel();  
  
        String registryDir = net.minecraftforge.common.ForgeHooks.prefixNamespace(p_255792_.location());  
        RegistryOps<JsonElement> registryops = RegistryOps.create(com.mojang.serialization.JsonOps.INSTANCE, p_256369_);  
  
        Map<ResourceLocation, Resource> resources = p_256349_.listResources(registryDir, (loc) -> loc.getPath().endsWith(".json"));  
  
        for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {  
            ResourceLocation fileLocation = entry.getKey();  
            String path = fileLocation.getPath();  
            String entryPath = path.substring(registryDir.length() + 1, path.length() - 5);  
            ResourceLocation entryId = new ResourceLocation(fileLocation.getNamespace(), entryPath);  
            ResourceKey<E> resourceKey = ResourceKey.create(p_255792_, entryId);  
  
            try (Reader reader = entry.getValue().openAsReader()) {  
                JsonElement jsonelement = JsonParser.parseReader(reader);  
  
                if (!net.minecraftforge.common.crafting.conditions.ICondition.shouldRegisterEntry(jsonelement)) {  
                    continue;  
                }  
  
                DataResult<E> dataresult = p_256232_.parse(registryops, jsonelement);  
  
                E element = dataresult.getOrThrow(false, (errorMsg) -> {});  
  
                p_256211_.register(resourceKey, element, entry.getValue().isBuiltin()  
                        ? com.mojang.serialization.Lifecycle.stable()  
                        : com.mojang.serialization.Lifecycle.experimental());
  
            } catch (Exception e) {  
                String errorMessage = e.getMessage() != null ? e.getMessage() : "";  
                if (isRemovedModError(errorMessage) || isRemovedModEntry(entryId)) {  
                    WorldGuardMod.LOGGER.warn("[WorldGuard] Skipping worldgen entry '{}' in registry '{}' - references removed mod data: {}",  
                            entryId, p_255792_.location(), summarizeError(errorMessage));  
                } else {  
                    p_255884_.put(resourceKey, e);  
                }  
            }  
        }  
    }  
  
    private static boolean isRemovedModError(String errorMessage) {  
        if (errorMessage == null) return false;  
        if (errorMessage.contains("Unknown registry key in") || errorMessage.contains("Unknown registry element")) {  
            String[] parts = errorMessage.split(":\\s*");  
            for (String part : parts) {  
                String trimmed = part.trim().replaceAll("[;\\s].*", "");  
                if (trimmed.contains(":")) {  
                    continue;  
                }  
                if (!trimmed.isEmpty() && !WorldGuardMod.isNamespaceLoaded(trimmed)) {  
                    return true;  
                }  
            }  
            java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("([a-z0-9_.-]+):([a-z0-9_/.-]+)").matcher(errorMessage);  
            while (matcher.find()) {  
                String ns = matcher.group(1);  
                if (!ns.equals("minecraft") && !WorldGuardMod.isNamespaceLoaded(ns)) {  
                    return true;  
                }  
            }  
            return true; // Be permissive - skip rather than crash  
        }  
        return false;  
    }  
  
    private static boolean isRemovedModEntry(ResourceLocation entryId) {  
        return !WorldGuardMod.isNamespaceLoaded(entryId.getNamespace());  
    }  
  
    private static String summarizeError(String error) {  
        if (error == null) return "unknown";  
        if (error.length() > 200) return error.substring(0, 200) + "...";  
        return error;  
    }  
}