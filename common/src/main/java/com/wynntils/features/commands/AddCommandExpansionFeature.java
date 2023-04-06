/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.commands;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.config.Category;
import com.wynntils.core.config.Config;
import com.wynntils.core.config.ConfigCategory;
import com.wynntils.core.config.RegisterConfig;
import com.wynntils.core.features.Feature;
import com.wynntils.mc.event.CommandsAddedEvent;
import com.wynntils.utils.mc.McUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Set up Brigadier command structure of known Wynncraft commands.
 *
 * The commands in this file were extracted from https://wynncraft.fandom.com/wiki/Commands,
 * https://wynncraft.com/help?guide=commands, from running the commands in-game, and from a
 * list of server commands provided by HeyZeer0.
 */
@ConfigCategory(Category.COMMANDS)
public class AddCommandExpansionFeature extends Feature {



    @RegisterConfig
    public final Config<Boolean> includeDeprecatedCommands = new Config<>(false);

    @RegisterConfig
    public final Config<AliasCommandLevel> includeAliases = new Config<>(AliasCommandLevel.SHORT_FORMS);

    @SubscribeEvent
    public void onCommandPacket(CommandsAddedEvent event) {
        RootCommandNode<SharedSuggestionProvider> root = event.getRoot();

        addArgumentlessCommandNodes(root);
        addChangetagCommandNode(root);

        if (includeDeprecatedCommands.get()) {
            addDeprecatedCommandNodes(root);
        }
    }

    private void addNode(
            RootCommandNode<SharedSuggestionProvider> root, CommandNode<? extends SharedSuggestionProvider> node) {
        Managers.Command.addNode(root, node);
    }

    private void addAlias(
            RootCommandNode<SharedSuggestionProvider> root,
            CommandNode<CommandSourceStack> originalNode,
            String aliasName,
            AliasCommandLevel level) {
        if (includeAliases.get().ordinal() >= level.ordinal()) {
            addNode(root, literal(aliasName).redirect(originalNode).build());
        }
    }

    private void addArgumentlessCommandNodes(RootCommandNode<SharedSuggestionProvider> root) {
        addNode(root, literal("claimingredientbomb").build());
        addNode(root, literal("claimitembomb").build());
        addNode(root, literal("daily").build());
        addNode(root, literal("fixquests").build());
        addNode(root, literal("fixstart").build());
        addNode(root, literal("forum").build());
        addNode(root, literal("help").build());
        addNode(root, literal("rules").build());
        addNode(root, literal("scrap").build());
        addNode(root, literal("sign").build());
        addNode(root, literal("skiptutorial").build());
        addNode(root, literal("tracking").build());
        addNode(root, literal("use").build());

        // There is also a command "server" but it is reserved for those with admin permissions
        // only, so don't include it here.
        // The command "checknickname" is also available but is probably a defunct legacy command.

        // "hub" aliases
        CommandNode<CommandSourceStack> hubNode = literal("hub").build();
        addNode(root, hubNode);

        addAlias(root, hubNode, "change", AliasCommandLevel.ALL);
        addAlias(root, hubNode, "lobby", AliasCommandLevel.ALL);
        addAlias(root, hubNode, "leave", AliasCommandLevel.ALL);
        addAlias(root, hubNode, "port", AliasCommandLevel.ALL);

        // There is also an alias "servers" for "hub", but it conflicts with our command
        // so don't include it here

        // "class" aliases
        CommandNode<CommandSourceStack> classNode = literal("class").build();
        addNode(root, classNode);

        addAlias(root, classNode, "classes", AliasCommandLevel.ALL);

        // "crate" aliases
        CommandNode<CommandSourceStack> crateNode = literal("crate").build();
        addNode(root, crateNode);

        addAlias(root, crateNode, "crates", AliasCommandLevel.ALL);

        // "kill" aliases
        CommandNode<CommandSourceStack> killNode = literal("kill").build();
        addNode(root, killNode);

        addAlias(root, killNode, "die", AliasCommandLevel.ALL);
        addAlias(root, killNode, "suicide", AliasCommandLevel.ALL);

        // "itemlock" aliases
        CommandNode<CommandSourceStack> itemlockNode = literal("itemlock").build();
        addNode(root, itemlockNode);

        addAlias(root, itemlockNode, "ilock", AliasCommandLevel.ALL);
        addAlias(root, itemlockNode, "lock", AliasCommandLevel.ALL);
        addAlias(root, itemlockNode, "locki", AliasCommandLevel.ALL);
        addAlias(root, itemlockNode, "lockitem", AliasCommandLevel.ALL);

        // "pet" aliases
        CommandNode<CommandSourceStack> petNode = literal("pet").build();
        addNode(root, petNode);

        addAlias(root, petNode, "pets", AliasCommandLevel.ALL);

        // "partyfinder" aliases
        CommandNode<CommandSourceStack> partyfinderNode = literal("partyfinder").build();
        addNode(root, partyfinderNode);

        addAlias(root, partyfinderNode, "pfinder", AliasCommandLevel.SHORT_FORMS);

        // "silverbull" aliases
        CommandNode<CommandSourceStack> silverbullNode = literal("silverbull").build();
        addNode(root, silverbullNode);

        addAlias(root, silverbullNode, "shop", AliasCommandLevel.ALL);
        addAlias(root, silverbullNode, "store", AliasCommandLevel.ALL);
        addAlias(root, silverbullNode, "share", AliasCommandLevel.ALL);

        // "stream" aliases
        CommandNode<CommandSourceStack> streamNode = literal("stream").build();
        addNode(root, streamNode);

        addAlias(root, streamNode, "streamer", AliasCommandLevel.ALL);

        // "totem" aliases
        CommandNode<CommandSourceStack> totemNode = literal("totem").build();
        addNode(root, totemNode);

        addAlias(root, totemNode, "totems", AliasCommandLevel.ALL);

        // "hunted" aliases
        CommandNode<CommandSourceStack> huntedNode = literal("hunted").build();
        addNode(root, huntedNode);

        addAlias(root, huntedNode, "pvp", AliasCommandLevel.SHORT_FORMS);

        // "recruit" aliases
        CommandNode<CommandSourceStack> recruitNode = literal("recruit").build();
        addNode(root, recruitNode);

        addAlias(root, recruitNode, "rf", AliasCommandLevel.ALL);

        // "discord" aliases
        CommandNode<CommandSourceStack> discordNode = literal("discord").build();
        addNode(root, discordNode);

        addAlias(root, discordNode, "link", AliasCommandLevel.ALL);
    }

    private void addChangetagCommandNode(RootCommandNode<SharedSuggestionProvider> root) {
        addNode(
                root,
                literal("changetag")
                        .then(literal("VIP"))
                        .then(literal("VIP+"))
                        .then(literal("HERO"))
                        .then(literal("CHAMPION"))
                        .then(literal("RESET"))
                        .build());
    }




    private void addDeprecatedCommandNodes(RootCommandNode<SharedSuggestionProvider> root) {
        // "legacystore" aliases
        CommandNode<CommandSourceStack> legacystoreNode = literal("legacystore").build();
        addNode(root, legacystoreNode);

        addAlias(root, legacystoreNode, "buy", AliasCommandLevel.ALL);
        addAlias(root, legacystoreNode, "cash", AliasCommandLevel.ALL);
        addAlias(root, legacystoreNode, "cashshop", AliasCommandLevel.ALL);
        addAlias(root, legacystoreNode, "gc", AliasCommandLevel.ALL);
        addAlias(root, legacystoreNode, "gold", AliasCommandLevel.ALL);
        addAlias(root, legacystoreNode, "goldcoins", AliasCommandLevel.ALL);
        addAlias(root, legacystoreNode, "goldshop", AliasCommandLevel.ALL);

        // "rename" aliases
        CommandNode<CommandSourceStack> renameNode = literal("rename").build();
        addNode(root, renameNode);

        addAlias(root, renameNode, "name", AliasCommandLevel.ALL);
    }

    public enum AliasCommandLevel {
        NONE,
        SHORT_FORMS,
        ALL
    }
}
