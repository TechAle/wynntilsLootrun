/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.components;

import com.wynntils.handlers.actionbar.ActionBarHandler;
import com.wynntils.handlers.chat.ChatHandler;
import com.wynntils.handlers.container.ContainerQueryHandler;

public final class Handlers {
    public static final ActionBarHandler ActionBar = new ActionBarHandler();
    public static final ChatHandler Chat = new ChatHandler();
    public static final ContainerQueryHandler ContainerQuery = new ContainerQueryHandler();
}
