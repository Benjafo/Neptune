package neptune.neptune.processing;

import neptune.neptune.relic.NeptuneItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public record VoidSynthesisRecipe(
        String id,
        String displayName,
        ItemStack result,
        List<Ingredient> ingredients,
        int shardCost,
        int requiredTier
) {
    public record Ingredient(net.minecraft.world.item.Item item, int count) {}

    public static final List<VoidSynthesisRecipe> ALL = List.of(
            new VoidSynthesisRecipe(
                    "reinforced_elytra", "Reinforced Elytra",
                    new ItemStack(NeptuneItems.REINFORCED_ELYTRA),
                    List.of(new Ingredient(Items.ELYTRA, 1)),
                    30, 3
            ),
            new VoidSynthesisRecipe(
                    "void_pouch", "Void Pouch",
                    new ItemStack(NeptuneItems.VOID_POUCH),
                    List.of(new Ingredient(Items.SHULKER_SHELL, 1)),
                    15, 3
            ),
            new VoidSynthesisRecipe(
                    "ender_magnet", "Ender Magnet",
                    new ItemStack(NeptuneItems.ENDER_MAGNET),
                    List.of(new Ingredient(Items.ENDER_PEARL, 2)),
                    10, 3
            ),
            new VoidSynthesisRecipe(
                    "elytra_booster", "Elytra Booster x4",
                    new ItemStack(NeptuneItems.ELYTRA_BOOSTER, 4),
                    List.of(new Ingredient(Items.FIREWORK_ROCKET, 1)),
                    5, 4
            ),
            new VoidSynthesisRecipe(
                    "portable_ender_chest", "Portable Ender Chest",
                    new ItemStack(NeptuneItems.PORTABLE_ENDER_CHEST),
                    List.of(new Ingredient(Items.ENDER_CHEST, 1)),
                    20, 4
            )
    );

    public static VoidSynthesisRecipe getById(String id) {
        for (VoidSynthesisRecipe recipe : ALL) {
            if (recipe.id().equals(id)) return recipe;
        }
        return null;
    }

    public static VoidSynthesisRecipe getByIndex(int index) {
        if (index < 0 || index >= ALL.size()) return null;
        return ALL.get(index);
    }

    public String ingredientSummary() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ingredients.size(); i++) {
            if (i > 0) sb.append(" + ");
            Ingredient ing = ingredients.get(i);
            if (ing.count() > 1) sb.append(ing.count()).append("x ");
            sb.append(new ItemStack(ing.item()).getHoverName().getString());
        }
        return sb.toString();
    }
}
