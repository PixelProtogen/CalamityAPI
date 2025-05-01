#version 150

uniform sampler2D DiffuseSampler;
uniform vec3 Data;
uniform float isEnabled;
uniform vec2 ScreenSize;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    if (isEnabled == 1.0) {
        vec2 center = vec2(Data.x, Data.y);
        vec2 delta = texCoord - center;

        // Apply aspect correction
        float aspect = ScreenSize.x / ScreenSize.y;
        delta.x *= aspect;

        float distance = length(delta);
        float angle = atan(delta.y, delta.x);

        float horizonRadius = Data.z / 2.6;
        float lensRadius = Data.z;
        float transitionWidth = Data.z / 8.0;

        float lensEffect = 0.0;
        if (distance < lensRadius) {
            lensEffect = (lensRadius - distance) / (lensRadius - horizonRadius);
            lensEffect = pow(lensEffect, 2.5);
        }

        float maxSwirl = 3.0;

        float swirlFactor = smoothstep(lensRadius, horizonRadius, distance) * 
                            (1.0 - smoothstep(horizonRadius, 0.0, distance));
        float swirl = maxSwirl * swirlFactor;

        float newAngle = angle + swirl;

        // Rebuild undistorted vector, undo aspect ratio for x
        vec2 offset = vec2(cos(newAngle), sin(newAngle)) * (distance + lensEffect * 0.05);
        offset.x /= aspect;
        vec2 distortedCoord = center + offset;

        vec2 finalCoord;

        if (distance < horizonRadius) {
            float t = clamp((horizonRadius - distance) / transitionWidth, 0.0, 1.0);
            finalCoord = mix(distortedCoord, texCoord, t);
        } else {
            finalCoord = distortedCoord;
        }

        finalCoord = clamp(finalCoord, vec2(0.0), vec2(1.0));
        vec4 finalTex = texture(DiffuseSampler, finalCoord);
        fragColor = vec4(finalTex.rgb, 1.0);
    } else {
        fragColor = vec4(texture(DiffuseSampler, texCoord).rgb, 1.0);
    }
}