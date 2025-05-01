#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D HeightmapSampler;
uniform float isEnabled;
uniform vec2 ScreenSize;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec2 resolution = ScreenSize;

    // Base color
    vec4 baseColor = texture(DiffuseSampler, texCoord);

    if (isEnabled == 1.0) {
        // Read heightmap and compute brightness (0-1)
        vec3 heightColor = texture(HeightmapSampler, texCoord).rgb;
        float brightness = dot(heightColor, vec3(0.299, 0.587, 0.114));

        // Use heightmap as a distortion map (centered around 0)
        vec2 distortion = (heightColor.rg - 0.5) * 0.02 * 5 * brightness;

        // Apply distortion
        vec2 distortedCoord = texCoord + distortion;

        // Sample the distorted texture
        vec4 distortedColor = texture(DiffuseSampler, distortedCoord);

        fragColor = vec4(distortedColor.rgb, baseColor.a);
    } else {
        fragColor = vec4(baseColor.rgb,1.0);
    }
}
