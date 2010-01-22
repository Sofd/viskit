//#version 130

uniform float scale;
uniform float offset;
uniform sampler2D tex;

void main() {
    vec3 texColor_normalized = (texture2D(tex, gl_TexCoord[0].st).rgb + 1.0) / 2.0;  // TODO: make this externally parameterizable
    gl_FragColor.rgb = scale * texColor_normalized + offset;
    gl_FragColor.a = 1.0;
}
