package neptune.neptune;

import neptune.neptune.broker.NeptuneMenus;
import neptune.neptune.broker.TomeBuffManager;
import neptune.neptune.command.NeptuneCommands;
import neptune.neptune.data.NeptuneAttachments;
import neptune.neptune.entity.NeptuneEntities;
import neptune.neptune.map.RegionCompletionTracker;
import neptune.neptune.network.NeptuneNetworking;
import neptune.neptune.processing.NeptuneBlocks;
import neptune.neptune.relic.NeptuneItems;
import neptune.neptune.relic.RelicLootHandler;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Neptune implements ModInitializer {
    public static final String MOD_ID = "neptune";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Neptune mod initializing...");

        NeptuneAttachments.register();
        NeptuneItems.register();
        NeptuneBlocks.register();
        NeptuneEntities.register();
        NeptuneMenus.register();
        NeptuneNetworking.register();
        NeptuneCommands.register();
        RelicLootHandler.register();
        RegionCompletionTracker.register();
        TomeBuffManager.register();

        LOGGER.info("Neptune mod initialized.");
    }
}
