#version 150

uniform sampler2D DiffuseSampler;
uniform float isEnabled;
uniform vec3 Data;

in vec2 texCoord;
out vec4 fragColor;

vec4 grayscale(vec4 rgb) {
    return vec4(vec3(dot(rgb.rgb, vec3(0.299, 0.587, 0.114))), rgb.a);
}

vec3 rgb2hsv(vec3 c)
{
    vec4 K = vec4(0.0, -1.0 / 3.0, 2.0 / 3.0, -1.0);
    vec4 p = mix(vec4(c.bg, K.wz), vec4(c.gb, K.xy), step(c.b, c.g));
    vec4 q = mix(vec4(p.xyw, c.r), vec4(c.r, p.yzx), step(p.x, c.r));

    float d = q.x - min(q.w, q.y);
    float e = 1.0e-10;
    return vec3(abs(q.z + (q.w - q.y) / (6.0 * d + e)), d / (q.x + e), q.x);
}


void main() {
    vec4 texColor = texture(DiffuseSampler, texCoord);
    if (isEnabled == 1.0) {
        vec3 hsvColor = rgb2hsv(vec3(texColor.r,texColor.g,texColor.b));
        bool inRange = (Data.z <= 0.5 && hsvColor.r >= Data.x && hsvColor.r <= Data.y) || (Data.z > 0.5 && hsvColor.r <= Data.x && hsvColor.r >= Data.y);

        if (inRange) {
            fragColor = vec4(grayscale(texColor).rgb,1.0);
        } else {
            fragColor = vec4(texColor.rgb,1.0);
        }
    } else {
        fragColor = vec4(texColor.rgb,1.0);
    }
}