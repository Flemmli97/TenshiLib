package io.github.flemmli97.tenshilib.client;

public class Color {

    private int hexVariant;

    protected int red, green, blue, alpha;

    public Color(int hexColor, boolean noAlpha) {
        this(hexColor >> 16 & 0xFF, hexColor >> 8 & 0xFF, hexColor & 0xFF, noAlpha ? 255 : hexColor >> 24 & 0xff);
    }

    public Color(int red, int green, int blue) {
        this(red, green, blue, 255);
    }

    public Color(int red, int green, int blue, int alpha) {
        this.red = Math.min(255, red);
        this.green = Math.min(255, green);
        this.blue = Math.min(255, blue);
        this.alpha = Math.min(255, alpha);
        this.updateHexValue();
    }

    public Color(Color other, int alpha) {
        this(other.red, other.green, other.blue, alpha);
    }

    public Color(float red, float green, float blue, float alpha) {
        this.red = (int) Math.min(255, 255 * red);
        this.green = (int) Math.min(255, 255 * green);
        this.blue = (int) Math.min(255, 255 * blue);
        this.alpha = (int) Math.min(255, 255 * alpha);
        this.updateHexValue();
    }

    public static int fromRGBA(int red, int green, int blue, int alpha) {
        return ((alpha & 0xFF) << 24) |
                ((red & 0xFF) << 16) |
                ((green & 0xFF) << 8) |
                ((blue & 0xFF));
    }

    private void updateHexValue() {
        this.hexVariant = fromRGBA(this.red, this.green, this.blue, this.alpha);
    }

    public int getRed() {
        return this.red;
    }

    public int getGreen() {
        return this.green;
    }

    public int getBlue() {
        return this.blue;
    }

    public int getAlpha() {
        return this.alpha;
    }

    protected void setRGB(int red, int green, int blue, int alpha) {
        this.red = Math.min(255, red);
        this.green = Math.min(255, green);
        this.blue = Math.min(255, blue);
        this.alpha = Math.min(255, alpha);
        this.updateHexValue();
    }

    public Color add(Color other) {
        return new Color(this.red + other.red, this.green + other.green, this.blue + other.blue, this.alpha + other.alpha);
    }

    public int hex() {
        return this.hexVariant;
    }

    @Override
    public int hashCode() {
        return this.hexVariant;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj instanceof Color other)
            return this.hexVariant == other.hexVariant;
        return false;
    }

    public static class MutableColor extends Color {

        public MutableColor(int hexColor, boolean ignoreAlpha) {
            super(hexColor, ignoreAlpha);
        }

        public MutableColor(int red, int green, int blue, int alpha) {
            super(red, green, blue, alpha);
        }

        public MutableColor(float red, float green, float blue, float alpha) {
            super(red, green, blue, alpha);
        }

        @Override
        public void setRGB(int red, int green, int blue, int alpha) {
            super.setRGB(red, green, blue, alpha);
        }
    }
}
