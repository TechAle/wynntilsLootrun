/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.features;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.commands.CommandManager;
import com.wynntils.core.components.Manager;
import com.wynntils.core.components.Managers;
import com.wynntils.core.config.Category;
import com.wynntils.core.config.ConfigCategory;
import com.wynntils.core.features.overlays.OverlayManager;
import com.wynntils.core.features.properties.StartDisabled;
import com.wynntils.core.keybinds.KeyBindManager;
import com.wynntils.core.mod.CrashReportManager;
import com.wynntils.core.mod.type.CrashType;
import com.wynntils.features.GammabrightFeature;
import com.wynntils.features.LootrunFeature;
import com.wynntils.features.chat.ChatTabsFeature;
import com.wynntils.features.commands.AddCommandExpansionFeature;
import com.wynntils.features.commands.CommandAliasesFeature;
import com.wynntils.features.commands.CustomCommandKeybindsFeature;
import com.wynntils.features.commands.FilterAdminCommandsFeature;
import com.wynntils.features.debug.ConnectionProgressFeature;
import com.wynntils.features.debug.PacketDebuggerFeature;
import com.wynntils.features.overlays.GameNotificationOverlayFeature;
import com.wynntils.features.overlays.InfoBoxFeature;
import com.wynntils.features.ui.WynncraftButtonFeature;
import com.wynntils.features.wynntils.CommandsFeature;
import com.wynntils.mc.event.ClientsideMessageEvent;
import com.wynntils.utils.mc.McUtils;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.eventbus.api.Event;

/** Loads {@link Feature}s */
public final class FeatureManager extends Manager {
    private static final Map<Feature, FeatureState> FEATURES = new LinkedHashMap<>();
    private static final Map<Class<? extends Feature>, Feature> FEATURE_INSTANCES = new LinkedHashMap<>();

    private final FeatureCommands commands = new FeatureCommands();

    public FeatureManager(
            CommandManager command, CrashReportManager crashReport, KeyBindManager keyBind, OverlayManager overlay) {
        super(List.of(command, crashReport, keyBind, overlay));
    }

    public void init() {
        // debug
        registerFeature(new ConnectionProgressFeature());
        registerFeature(new PacketDebuggerFeature());

        // always on
        registerFeature(new LootrunFeature());

        // user
        registerFeature(new AddCommandExpansionFeature());
        registerFeature(new CommandAliasesFeature());
        registerFeature(new CommandsFeature());
        registerFeature(new CustomCommandKeybindsFeature());
        registerFeature(new FilterAdminCommandsFeature());;
        registerFeature(new GameNotificationOverlayFeature());
        registerFeature(new GammabrightFeature());
        registerFeature(new InfoBoxFeature());
        registerFeature(new WynncraftButtonFeature());

        // Reload Minecraft's config files so our own keybinds get loaded
        // This is needed because we are late to register the keybinds,
        // but we cannot move it earlier to the init process because of I18n
        synchronized (McUtils.options()) {
            McUtils.mc().options.load();
        }

        addCrashCallbacks();
    }

    private void registerFeature(Feature feature) {
        FEATURES.put(feature, FeatureState.DISABLED);
        FEATURE_INSTANCES.put(feature.getClass(), feature);

        try {
            initializeFeature(feature);
        } catch (AssertionError ae) {
            WynntilsMod.error("Fix i18n for " + feature.getClass().getSimpleName(), ae);
            if (WynntilsMod.isDevelopmentEnvironment()) {
                System.exit(1);
            }
        } catch (Throwable exception) {
            // Log and handle gracefully, just disable this feature
            crashFeature(feature);
            WynntilsMod.reportCrash(
                    feature.getClass().getName() + ":initialize",
                    feature.getClass().getSimpleName() + " during init",
                    CrashType.FEATURE,
                    exception,
                    false,
                    true);
        }
    }

    private void initializeFeature(Feature feature) {
        Class<? extends Feature> featureClass = feature.getClass();

        // Set feature category
        ConfigCategory configCategory = feature.getClass().getAnnotation(ConfigCategory.class);
        Category category = configCategory != null ? configCategory.value() : Category.UNCATEGORIZED;
        feature.setCategory(category);

        // Register commands and key binds
        commands.discoverCommands(feature);
        Managers.KeyBind.discoverKeyBinds(feature);

        // Determine if feature should be enabled & set default enabled value for user features
        boolean startDisabled = featureClass.isAnnotationPresent(StartDisabled.class);
        feature.userEnabled.updateConfig(!startDisabled);

        Managers.Overlay.discoverOverlays(feature);
        Managers.Overlay.discoverOverlayGroups(feature);

        // Assert that the feature name is properly translated
        assert !feature.getTranslatedName().startsWith("feature.wynntils.")
                : "Fix i18n for " + feature.getTranslatedName();

        if (!feature.userEnabled.get()) return; // not enabled by user

        enableFeature(feature);
    }

    public void enableFeature(Feature feature) {
        if (!FEATURES.containsKey(feature)) {
            throw new IllegalArgumentException("Tried to enable an unregistered feature: " + feature);
        }

        FeatureState state = FEATURES.get(feature);

        if (state != FeatureState.DISABLED && state != FeatureState.CRASHED) return;

        feature.onEnable();

        FEATURES.put(feature, FeatureState.ENABLED);

        WynntilsMod.registerEventListener(feature);

        Managers.Overlay.enableOverlays(feature);

        Managers.KeyBind.enableFeatureKeyBinds(feature);
    }

    public void disableFeature(Feature feature) {
        if (!FEATURES.containsKey(feature)) {
            throw new IllegalArgumentException("Tried to disable an unregistered feature: " + feature);
        }

        FeatureState state = FEATURES.get(feature);

        if (state != FeatureState.ENABLED) return;

        feature.onDisable();

        FEATURES.put(feature, FeatureState.DISABLED);

        WynntilsMod.unregisterEventListener(feature);

        Managers.Overlay.disableOverlays(feature);

        Managers.KeyBind.disableFeatureKeyBinds(feature);
    }

    public void crashFeature(Feature feature) {
        if (!FEATURES.containsKey(feature)) {
            throw new IllegalArgumentException("Tried to crash an unregistered feature: " + feature);
        }

        disableFeature(feature);

        FEATURES.put(feature, FeatureState.CRASHED);
    }

    public FeatureState getFeatureState(Feature feature) {
        if (!FEATURES.containsKey(feature)) {
            throw new IllegalArgumentException(
                    "Feature " + feature + " is not registered, but was was queried for its state");
        }

        return FEATURES.get(feature);
    }

    public boolean isEnabled(Feature feature) {
        return getFeatureState(feature) == FeatureState.ENABLED;
    }

    public List<Feature> getFeatures() {
        return FEATURES.keySet().stream().toList();
    }

    @SuppressWarnings("unchecked")
    public <T extends Feature> T getFeatureInstance(Class<T> featureClass) {
        return (T) FEATURE_INSTANCES.get(featureClass);
    }

    public Optional<Feature> getFeatureFromString(String featureName) {
        return getFeatures().stream()
                .filter(feature -> feature.getShortName().equals(featureName))
                .findFirst();
    }

    public void handleExceptionInEventListener(Event event, String featureClassName, Throwable t) {
        String featureName = featureClassName.substring(featureClassName.lastIndexOf('.') + 1);

        Optional<Feature> featureOptional = getFeatureFromString(featureName);
        if (featureOptional.isEmpty()) {
            WynntilsMod.error("Exception in event listener in feature that cannot be located: " + featureClassName, t);
            return;
        }

        Feature feature = featureOptional.get();

        crashFeature(feature);

        // If a crash happens in a client-side message event, and we send a new message about disabling X feature,
        // we will cause a new exception and an endless recursion.
        boolean shouldSendChat = !(event instanceof ClientsideMessageEvent);

        WynntilsMod.reportCrash(
                feature.getClass().getName(), feature.getTranslatedName(), CrashType.FEATURE, t, shouldSendChat, true);

        if (shouldSendChat) {
            MutableComponent enableMessage = Component.literal("Click here to enable it again.")
                    .withStyle(ChatFormatting.UNDERLINE)
                    .withStyle(ChatFormatting.RED)
                    .withStyle(style -> style.withClickEvent(new ClickEvent(
                            ClickEvent.Action.RUN_COMMAND, "/feature enable " + feature.getShortName())));

            McUtils.sendMessageToClient(enableMessage);
        }
    }

    private void addCrashCallbacks() {
        Managers.CrashReport.registerCrashContext("Loaded Features", () -> {
            StringBuilder result = new StringBuilder();

            for (Feature feature : FEATURES.keySet()) {
                if (feature.isEnabled()) {
                    result.append("\n\t\t").append(feature.getTranslatedName());
                }
            }

            return result.toString();
        });
    }
}
