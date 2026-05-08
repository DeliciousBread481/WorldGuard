package deliciousbread481.worldguard;  
  
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;  
import net.minecraftforge.common.MinecraftForge;  
import net.minecraftforge.fml.ModList;  
import net.minecraftforge.fml.common.Mod;  
import net.minecraftforge.registries.MissingMappingsEvent;  
import org.apache.logging.log4j.LogManager;  
import org.apache.logging.log4j.Logger;  
  
import java.util.Set;  
import java.util.stream.Collectors;  
  
@Mod("worldguard")  
public class WorldGuardMod {  
  
    public static final Logger LOGGER = LogManager.getLogger("WorldGuard");  
    private static Set<String> loadedModNamespaces;  
  
    public WorldGuardMod() {  
        MinecraftForge.EVENT_BUS.register(this);  
        loadedModNamespaces = ModList.get().getMods().stream()  
                .map(info -> info.getModId())  
                .collect(Collectors.toSet());  
        loadedModNamespaces.add("minecraft");  
        loadedModNamespaces.add("forge");  
        loadedModNamespaces.add("c");  
        LOGGER.info("[WorldGuard] Initialized. Tracking {} mod namespaces.", loadedModNamespaces.size());  
    }  
  
    public static boolean isPackAvailable(String packId) {  
        if (packId == null) return false;  
        if (packId.equals("vanilla") || packId.equals("mod_resources")) return true;  
        if (packId.startsWith("file/")) return true;  
        if (packId.startsWith("mod:")) {  
            String modId = packId.substring(4);  
            return loadedModNamespaces.contains(modId);  
        }  
        return true;  
    }  
  
    public static boolean isNamespaceLoaded(String namespace) {  
        if (namespace == null) return false;  
        return loadedModNamespaces != null && loadedModNamespaces.contains(namespace);  
    }  
  
    public static Set<String> getLoadedModNamespaces() {  
        return loadedModNamespaces;  
    }  
  
    @SuppressWarnings("unchecked")  
    @net.minecraftforge.eventbus.api.SubscribeEvent  
    public void onMissingMappings(MissingMappingsEvent event) {  
        ResourceKey<? extends Registry<Object>> registryKey =  
                (ResourceKey<? extends Registry<Object>>) (ResourceKey<?>) event.getKey();  
        event.getAllMappings(registryKey).forEach(mapping -> {  
            ResourceLocation key = mapping.getKey();  
            if (!loadedModNamespaces.contains(key.getNamespace())) {  
                LOGGER.warn("[WorldGuard] Ignoring missing registry entry from removed mod: {}", key);  
                mapping.ignore();  
            }  
        });  
    }  
}