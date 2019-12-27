package com.flemmli97.tenshilib.common;

import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

public class TextHelper {

    public static TextComponentTranslation setColor(TextComponentTranslation text, TextFormatting color) {
        text.getStyle().setColor(color);
        return text;
    }
}
