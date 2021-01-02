package pl.bartlomiejstepien.chess.localization;

import java.util.Locale;
import java.util.ResourceBundle;

public class Localization
{
    private static Locale LOCALE = Locale.getDefault();
    private static ResourceBundle resourceBundle = ResourceBundle.getBundle("lang/chess", LOCALE);

    public static String translate(String key)
    {
        return resourceBundle.getString(key);
    }
}
