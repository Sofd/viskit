#version 130

uniform float scale;
uniform float offset;
uniform sampler2D tex;

void main() {
    //gl_FragColor.rgb = scale * texture2D(tex, gl_TexCoord[0].st).rgb + offset;
    gl_FragColor.rgb = scale * vec3(1,0,0);
    //gl_FragColor.rgb = vec3(1,0,0);
    //gl_FragColor.rgb = texture2D(tex, gl_TexCoord[0].st).rgb;
    gl_FragColor.a = 1.0;
}
