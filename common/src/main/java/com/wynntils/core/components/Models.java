/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.components;

import com.wynntils.models.containers.ContainerModel;
import com.wynntils.models.containers.LootChestModel;

import com.wynntils.models.elements.ElementModel;
import com.wynntils.models.lootruns.LootrunModel;


public final class Models {

    public static final ContainerModel Container = new ContainerModel();
    public static final ElementModel Element = new ElementModel();

    // Models with dependencies, ordered alphabetically as far as possible

    public static final LootChestModel LootChest = new LootChestModel(Container);
    public static final LootrunModel Lootrun = new LootrunModel(Container);


}
