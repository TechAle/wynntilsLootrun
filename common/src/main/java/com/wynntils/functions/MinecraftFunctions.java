/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.core.functions.Function;
import com.wynntils.core.functions.arguments.FunctionArguments;
import com.wynntils.mc.mixin.accessors.MinecraftAccessor;
import com.wynntils.utils.mc.McUtils;


public class MinecraftFunctions {
    public static class DirFunction extends Function<Double> {
        @Override
        public Double getValue(FunctionArguments arguments) {
            return (double) McUtils.player().getYRot();
        }
    }

    public static class FpsFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return MinecraftAccessor.getFps();
        }
    }
}
