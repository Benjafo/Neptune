package neptune.neptune.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import neptune.neptune.challenge.ChallengeCategory;
import neptune.neptune.challenge.ChallengeData;
import neptune.neptune.challenge.ChallengeType;
import neptune.neptune.data.NeptuneAttachments;
import neptune.neptune.data.VoidEssenceData;
import neptune.neptune.entity.BrokerEntity;
import neptune.neptune.entity.NeptuneEntities;
import neptune.neptune.unlock.UnlockData;
import neptune.neptune.unlock.UnlockManager;
import neptune.neptune.unlock.UnlockType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.entity.EntitySpawnReason;

import java.util.Arrays;
import java.util.List;

public class NeptuneCommands {

    private static final SuggestionProvider<CommandSourceStack> UNLOCK_SUGGESTIONS = (context, builder) ->
            SharedSuggestionProvider.suggest(
                    Arrays.stream(UnlockType.values()).map(Enum::name),
                    builder
            );

    private static final SuggestionProvider<CommandSourceStack> CHALLENGE_SUGGESTIONS = (context, builder) ->
            SharedSuggestionProvider.suggest(
                    Arrays.stream(ChallengeType.values()).map(Enum::name),
                    builder
            );

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {

            // Broker commands
            dispatcher.register(Commands.literal("broker")
                    .then(Commands.literal("spawn")
                            .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                            .executes(context -> {
                                ServerPlayer player = context.getSource().getPlayerOrException();
                                ServerLevel level = player.level();
                                BrokerEntity broker = NeptuneEntities.BROKER.spawn(
                                        level, player.blockPosition(), EntitySpawnReason.COMMAND);
                                if (broker != null) {
                                    context.getSource().sendSuccess(
                                            () -> Component.literal("§aBroker spawned!"), true);
                                    return 1;
                                }
                                context.getSource().sendFailure(Component.literal("§cFailed to spawn broker."));
                                return 0;
                            })
                    )
            );

            // Essence commands
            dispatcher.register(Commands.literal("essence")
                    .then(Commands.literal("balance")
                            .executes(context -> showBalance(context.getSource())))
                    .then(Commands.literal("add")
                            .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                            .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                    .executes(context -> {
                                        ServerPlayer player = context.getSource().getPlayerOrException();
                                        int amount = IntegerArgumentType.getInteger(context, "amount");
                                        VoidEssenceData current = player.getAttachedOrCreate(NeptuneAttachments.VOID_ESSENCE);
                                        player.setAttached(NeptuneAttachments.VOID_ESSENCE, current.add(amount));
                                        context.getSource().sendSuccess(
                                                () -> Component.literal("§aAdded " + amount + " void essence. Balance: "
                                                        + player.getAttachedOrCreate(NeptuneAttachments.VOID_ESSENCE).current()), true);
                                        return 1;
                                    })))
                    .then(Commands.literal("remove")
                            .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                            .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                    .executes(context -> {
                                        ServerPlayer player = context.getSource().getPlayerOrException();
                                        int amount = IntegerArgumentType.getInteger(context, "amount");
                                        VoidEssenceData current = player.getAttachedOrCreate(NeptuneAttachments.VOID_ESSENCE);
                                        VoidEssenceData result = current.spend(amount);
                                        if (result == null) {
                                            context.getSource().sendFailure(
                                                    Component.literal("§cInsufficient essence. Balance: " + current.current()));
                                            return 0;
                                        }
                                        player.setAttached(NeptuneAttachments.VOID_ESSENCE, result);
                                        context.getSource().sendSuccess(
                                                () -> Component.literal("§eRemoved " + amount + " void essence. Balance: "
                                                        + player.getAttachedOrCreate(NeptuneAttachments.VOID_ESSENCE).current()), true);
                                        return 1;
                                    })))
                    .then(Commands.literal("set")
                            .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                            .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                    .executes(context -> {
                                        ServerPlayer player = context.getSource().getPlayerOrException();
                                        int amount = IntegerArgumentType.getInteger(context, "amount");
                                        VoidEssenceData current = player.getAttachedOrCreate(NeptuneAttachments.VOID_ESSENCE);
                                        player.setAttached(NeptuneAttachments.VOID_ESSENCE, current.withCurrent(amount));
                                        context.getSource().sendSuccess(
                                                () -> Component.literal("§aSet void essence to " + amount), true);
                                        return 1;
                                    })))
                    .executes(context -> showBalance(context.getSource()))
            );

            // Unlock commands
            dispatcher.register(Commands.literal("unlock")
                    .then(Commands.literal("list")
                            .executes(context -> {
                                ServerPlayer player = context.getSource().getPlayerOrException();
                                UnlockData unlocks = player.getAttachedOrCreate(NeptuneAttachments.UNLOCKS);
                                player.sendSystemMessage(Component.literal("§6=== Unlock Tree ==="));
                                for (UnlockType type : UnlockType.values()) {
                                    boolean has = unlocks.hasUnlock(type);
                                    String status = has ? "§a✓" : "§c✗";
                                    String branch = type.getBranch() != null ? type.getBranch().name() : "ENDGAME";
                                    player.sendSystemMessage(Component.literal(
                                            status + " §7[" + branch + " T" + type.getTier() + "] §f"
                                                    + type.getDisplayName() + " §8(" + type.getEssenceCost() + " essence)"));
                                }
                                return 1;
                            }))
                    .then(Commands.literal("buy")
                            .then(Commands.argument("unlock", StringArgumentType.word())
                                    .suggests(UNLOCK_SUGGESTIONS)
                                    .executes(context -> {
                                        ServerPlayer player = context.getSource().getPlayerOrException();
                                        String name = StringArgumentType.getString(context, "unlock");
                                        try {
                                            UnlockType type = UnlockType.valueOf(name.toUpperCase());
                                            UnlockManager.tryPurchase(player, type);
                                        } catch (IllegalArgumentException e) {
                                            context.getSource().sendFailure(Component.literal("§cUnknown unlock: " + name));
                                        }
                                        return 1;
                                    })))
                    .then(Commands.literal("grant")
                            .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                            .then(Commands.argument("unlock", StringArgumentType.word())
                                    .suggests(UNLOCK_SUGGESTIONS)
                                    .executes(context -> {
                                        ServerPlayer player = context.getSource().getPlayerOrException();
                                        String name = StringArgumentType.getString(context, "unlock");
                                        try {
                                            UnlockType type = UnlockType.valueOf(name.toUpperCase());
                                            UnlockData unlocks = player.getAttachedOrCreate(NeptuneAttachments.UNLOCKS);
                                            player.setAttached(NeptuneAttachments.UNLOCKS, unlocks.withUnlock(type));
                                            context.getSource().sendSuccess(
                                                    () -> Component.literal("§aGranted unlock: " + type.getDisplayName()), true);
                                        } catch (IllegalArgumentException e) {
                                            context.getSource().sendFailure(Component.literal("§cUnknown unlock: " + name));
                                        }
                                        return 1;
                                    })))
            );

            // Challenge commands
            dispatcher.register(Commands.literal("challenges")
                    .then(Commands.literal("list")
                            .executes(context -> {
                                ServerPlayer player = context.getSource().getPlayerOrException();
                                ChallengeData challenges = player.getAttachedOrCreate(NeptuneAttachments.CHALLENGES);
                                player.sendSystemMessage(Component.literal(
                                        "§6=== Challenges (" + challenges.totalCompleted() + "/45) ==="));
                                for (ChallengeCategory cat : ChallengeCategory.values()) {
                                    player.sendSystemMessage(Component.literal("§e" + cat.getDisplayName() + ":"));
                                    for (ChallengeType type : ChallengeType.values()) {
                                        if (type.getCategory() != cat) continue;
                                        boolean done = challenges.isCompleted(type);
                                        int progress = challenges.getProgress(type);
                                        String status = done ? "§a✓" : "§7○";
                                        String progressText = done ? "" : " §8[" + progress + "/" + type.getTargetValue() + "]";
                                        player.sendSystemMessage(Component.literal(
                                                "  " + status + " §f" + type.getDisplayName() + progressText));
                                    }
                                }
                                return 1;
                            }))
                    .then(Commands.literal("complete")
                            .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                            .then(Commands.argument("challenge", StringArgumentType.word())
                                    .suggests(CHALLENGE_SUGGESTIONS)
                                    .executes(context -> {
                                        ServerPlayer player = context.getSource().getPlayerOrException();
                                        String name = StringArgumentType.getString(context, "challenge");
                                        try {
                                            ChallengeType type = ChallengeType.valueOf(name.toUpperCase());
                                            ChallengeData data = player.getAttachedOrCreate(NeptuneAttachments.CHALLENGES);
                                            player.setAttached(NeptuneAttachments.CHALLENGES, data.withCompleted(type));
                                            context.getSource().sendSuccess(
                                                    () -> Component.literal("§aCompleted challenge: " + type.getDisplayName()), true);
                                        } catch (IllegalArgumentException e) {
                                            context.getSource().sendFailure(Component.literal("§cUnknown challenge: " + name));
                                        }
                                        return 1;
                                    })))
                    .executes(context -> {
                        ServerPlayer player = context.getSource().getPlayerOrException();
                        ChallengeData challenges = player.getAttachedOrCreate(NeptuneAttachments.CHALLENGES);
                        player.sendSystemMessage(Component.literal(
                                "§6Challenges: " + challenges.totalCompleted() + "/45 complete. Use /challenges list for details."));
                        return 1;
                    })
            );
        });
    }

    private static int showBalance(CommandSourceStack source) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        VoidEssenceData data = player.getAttachedOrCreate(NeptuneAttachments.VOID_ESSENCE);
        source.sendSuccess(
                () -> Component.literal("§5Void Essence: §d" + data.current() + " §8(Lifetime: " + data.lifetime() + ")"),
                false);
        return 1;
    }
}
