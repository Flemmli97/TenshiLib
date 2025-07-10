#version 150

#moj_import <fog.glsl>

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;

in float vertexDistance;
in vec2 texCoord0;
in vec4 vertexColor;

out vec4 fragColor;

const float PI2 = 6.28318530718;

vec4 getColor(vec2 uv) {
    return texture(Sampler0, uv) * vertexColor * ColorModulator;
}

// Based on https://www.shadertoy.com/view/Xltfzj
vec4 blur(vec4 color, float steps, float radSteps, vec2 radius) {
    for (float d = 0.0; d < PI2; d += PI2/steps) {
        for (float i = 1.0/radSteps; i <= 1.0; i += 1.0/radSteps) {
            color += getColor(texCoord0 + vec2(cos(d), sin(d)) * radius * i);
        }
    }
    return color /= radSteps * steps;
}

void main() {
    vec4 color = getColor(texCoord0);
    ivec2 textureSize2d = textureSize(Sampler0, 0);
    color = blur(color, 16., 2., 2./textureSize2d.xy);
    if (color.a < 0.1) {
        discard;
    }
    fragColor = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);
}