/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.core.components.Models;
import com.wynntils.core.functions.Function;
import com.wynntils.core.functions.arguments.FunctionArguments;
import com.wynntils.models.worlds.profile.ServerProfile;
import com.wynntils.utils.type.CappedValue;
import java.util.List;
import java.util.Locale;

public class WorldFunctions {
    public static class CurrentWorldFunction extends Function<String> {
        private static final String NO_DATA = "<unknown>";
        private static final String NO_WORLD = "<not on world>";

        @Override
        public String getValue(FunctionArguments arguments) {

            return NO_DATA;
        }

        @Override
        public List<String> getAliases() {
            return List.of("world");
        }
    }

    public static class CurrentWorldUptimeFunction extends Function<String> {
        private static final String NO_DATA = "<unknown>";
        private static final String NO_WORLD = "<not on world>";

        @Override
        public String getValue(FunctionArguments arguments) {
            return NO_DATA;
        }

        @Override
        public List<String> getAliases() {
            return List.of("world_uptime", "uptime");
        }
    }

    public static class WorldStateFunction extends Function<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            return null;
        }
    }

}
