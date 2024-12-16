package one.tranic.oxygen;

public class Config {
    private static double chanceGlassBottleBreaksWithFireTypeInOffhand;
    private static int amountOfAirInBottles;

    public static double getChanceGlassBottleBreaksWithFireTypeInOffhand() {
        return chanceGlassBottleBreaksWithFireTypeInOffhand;
    }

    public static int getAmountOfAirInBottles() {
        return amountOfAirInBottles;
    }

    public static void reload() {
        OxygenCylinder.getInstance().saveDefaultConfig();
        OxygenCylinder.getInstance().reloadConfig();
        chanceGlassBottleBreaksWithFireTypeInOffhand = OxygenCylinder.getInstance().getConfig().getDouble("chanceGlassBottleBreaksWithFireTypeInOffhand");
        amountOfAirInBottles = OxygenCylinder.getInstance().getConfig().getInt("amountOfAirInBottles");

        if (chanceGlassBottleBreaksWithFireTypeInOffhand > 1.0 || chanceGlassBottleBreaksWithFireTypeInOffhand < 0) {
            chanceGlassBottleBreaksWithFireTypeInOffhand = 0.65;
        }
        if (amountOfAirInBottles > 300 || amountOfAirInBottles < 0) {
            amountOfAirInBottles = 100;
        }
    }
}
