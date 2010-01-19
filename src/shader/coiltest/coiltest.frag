//#version 130

uniform sampler2D tex;

void main() {
    gl_FragColor.rg = 0.5 * texture2D(tex, gl_TexCoord[0].st).rg;
    gl_FragColor.b = gl_FragCoord.y / 2000;
    gl_FragColor.a = 1.0;

    //gl_FragColor.rgb = texture2D(tex, gl_TexCoord[0].st).rgb;
}
