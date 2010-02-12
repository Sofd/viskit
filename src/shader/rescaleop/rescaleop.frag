//#version 130

uniform float preScale;
uniform float preOffset;
uniform float scale;
uniform float offset;
uniform sampler2D tex;

void main() {
    vec3 texColor_normalized = preScale * texture2D(tex, gl_TexCoord[0].st).rgb + preOffset;
    gl_FragColor.rgb = scale * texColor_normalized + offset;
    gl_FragColor.a = 1.0;
}
