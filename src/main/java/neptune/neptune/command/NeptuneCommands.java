package neptune.neptune.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import neptune.neptune.data.NeptuneAttachments;
import neptune.neptune.data.VoidEssenceData;
import neptune.neptune.entity.BrokerEntity;
import neptune.neptune.entity.NeptuneEntities;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.entity.EntitySpawnReason;

public class NeptuneCommands {

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
                                        level,
                                        player.blockPosition(),
                                        EntitySpawnReason.COMMAND
                                );
                                if (broker != null) {
                                    context.getSource().sendSuccess(
                                            () -> Component.literal("§aBroker spawned!"),
                                            true
                                    );
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
                            .executes(context -> {
                                ServerPlayer player = context.getSource().getPlayerOrException();
                                VoidEssenceData data = player.getAttachedOrCreate(NeptuneAttachments.VOID_ESSENCE);
                                context.getSource().sendSuccess(
                                        () -> Component.literal("§5Void Essence: §d" + data.current()
                                                + " §8(Lifetime: " + data.lifetime() + ")"),
                                        false
                                );
                                return 1;
                            })
                    )
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
                                                        + player.getAttachedOrCreate(NeptuneAttachments.VOID_ESSENCE).current()),
                                                true
                                        );
                                        return 1;
                                    })
                            )
                    )
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
                                                    Component.literal("§cInsufficient essence. Current balance: " + current.current())
                                            );
                                            return 0;
                                        }
                                        player.setAttached(NeptuneAttachments.VOID_ESSENCE, result);
                                        context.getSource().sendSuccess(
                                                () -> Component.literal("§eRemoved " + amount + " void essence. Balance: "
                                                        + player.getAttachedOrCreate(NeptuneAttachments.VOID_ESSENCE).current()),
                                                true
                                        );
                                        return 1;
                                    })
                            )
                    )
                    .then(Commands.literal("set")
                            .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                            .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                    .executes(context -> {
                                        ServerPlayer player = context.getSource().getPlayerOrException();
                                        int amount = IntegerArgumentType.getInteger(context, "amount");
                                        VoidEssenceData current = player.getAttachedOrCreate(NeptuneAttachments.VOID_ESSENCE);
                                        player.setAttached(NeptuneAttachments.VOID_ESSENCE, current.withCurrent(amount));
                                        context.getSource().sendSuccess(
                                                () -> Component.literal("§aSet void essence to " + amount),
                                                true
                                        );
                                        return 1;
                                    })
                            )
                    )
                    // Default: show balance when just typing /essence
                    .executes(context -> {
                        ServerPlayer player = context.getSource().getPlayerOrException();
                        VoidEssenceData data = player.getAttachedOrCreate(NeptuneAttachments.VOID_ESSENCE);
                        context.getSource().sendSuccess(
                                () -> Component.literal("§5Void Essence: §d" + data.current()
                                        + " §8(Lifetime: " + data.lifetime() + ")"),
                                false
                        );
                        return 1;
                    })
            );
        });
    }
}
