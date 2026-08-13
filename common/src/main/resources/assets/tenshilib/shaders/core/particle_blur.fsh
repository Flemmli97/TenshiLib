#version 150

#moj_import <fog.glsl>

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;

in vec4 uvMinMax;
in float vertexDistance;
in vec2 texCoord0;
in vec4 vertexColor;

out vec4 fragColor;

const int RADIUS = 2;
const float KERNEL[RADIUS * 2 + 1] = float[](0.15338835280702454, 0.22146110682534667, 0.2503010807352574, 0.22146110682534667, 0.15338835280702454);
const float BLUR_SCALE = .25;

vec4 gaussianBlur(vec2 uv, vec2 texel) {
    vec4 color = vec4(0.0);
    bool clipDefined = uvMinMax[0] != uvMinMax[2];

    for (int x = -RADIUS; x <= RADIUS; x++) {
        for (int y = -RADIUS; y <= RADIUS; y++) {
            float weight = KERNEL[x + RADIUS] * KERNEL[y + RADIUS];
            vec2 uvTarget = uv + vec2(x, y) * texel;
            // Outside of texture is transparent which we ignore
            if (clipDefined) {
                if (uvTarget.x < uvMinMax[0] || uvTarget.y < uvMinMax[1] || uvTarget.x > uvMinMax[2] || uvTarget.y > uvMinMax[3]) {
                    continue;
                }
            }
            vec4 text = texture(Sampler0, uvTarget);
            color.rgb += text.rgb * text.a * weight;
            color.a += text.a * weight;
        }
    }
    if (color.a > 0.0) {
        color.rgb /= color.a;
    }
    return color;
}

void main() {
    vec2 texel = 1./textureSize(Sampler0, 0);
    texel *= BLUR_SCALE;
    vec4 color = gaussianBlur(texCoord0, texel) * vertexColor * ColorModulator;
    fragColor = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);
}
