#version 150

uniform sampler2D DiffuseSampler;
uniform vec2 ScreenSize;
uniform float Time;
uniform vec3 Data; // x,y = center (UV), z = radius

in vec2 texCoord;
out vec4 fragColor;

void main() {
    // Compute vector from effect center to current pixel
    vec2 dir = texCoord - Data.xy;

    // Distance from effect center
    float dist = length(dir);

    // Smooth falloff based on distance
    float falloff = smoothstep(Data.z, 0.0, dist);

    // Distortion animation
    float wave = sin((texCoord.y + Time * 1.5) * 40.0) * 0.005;

    // Apply falloff to distortion
    vec2 distortion = vec2(wave * falloff, 0.0);

    // Warp the coordinates
    vec2 warpedUV = texCoord + distortion;

    // Output the warped texture sample
    fragColor = texture(DiffuseSampler, warpedUV);
}
