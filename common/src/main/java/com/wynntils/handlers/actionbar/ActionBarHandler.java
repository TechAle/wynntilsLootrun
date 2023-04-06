/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.actionbar;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Handler;
import com.wynntils.core.components.Models;
import com.wynntils.handlers.actionbar.type.ActionBarPosition;
import com.wynntils.mc.event.ChatPacketReceivedEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.network.chat.Component;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class ActionBarHandler extends Handler {
    // example: "§c❤ 218/218§0    §7502§f S§7 -1580    §b✺ 1/119"
    private static final Pattern ACTIONBAR_PATTERN = Pattern.compile("(?<LEFT>§[^§]+)(?<CENTER>.*)(?<RIGHT>§[^§]+)");
    private static final String CENTER_PADDING = "§0               ";

    private final Map<ActionBarPosition, List<ActionBarSegment>> allSegments = Map.of(
            ActionBarPosition.LEFT,
            new ArrayList<>(),
            ActionBarPosition.CENTER,
            new ArrayList<>(),
            ActionBarPosition.RIGHT,
            new ArrayList<>());
    private final Map<ActionBarPosition, ActionBarSegment> lastSegments = new HashMap<>();
    private String previousRawContent = null;
    private String previousProcessedContent;

    public void registerSegment(ActionBarSegment segment) {
        allSegments.get(segment.getPosition()).add(segment);
    }




    private void processPosition(ActionBarPosition pos, Map<ActionBarPosition, String> positionMatches) {
        List<ActionBarSegment> potentialSegments = allSegments.get(pos);
        for (ActionBarSegment segment : potentialSegments) {
            Matcher m = segment.getPattern().matcher(positionMatches.get(pos));
            if (m.matches()) {
                ActionBarSegment lastSegment = lastSegments.get(pos);
                if (segment != lastSegment) {
                    // This is a new kind of segment, tell the old one it disappeared
                    if (lastSegment != null) {
                        lastSegment.removed();
                    }
                    lastSegments.put(pos, segment);
                    segment.appeared(m);
                } else {
                    segment.update(m);
                }
            }
        }
    }
}
