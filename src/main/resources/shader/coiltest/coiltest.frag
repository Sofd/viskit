//#version 130

uniform sampler2D tex;

void main() {
    gl_FragColor.rgb = (texture2D(tex, gl_TexCoord[0].st).rgb + 1.0) / 2.0;
    //gl_FragColor.rg = 0.5 * texture2D(tex, gl_TexCoord[0].st).rg;
    //gl_FragColor.b = abs(dot(vec2(1,0.7), (gl_FragCoord.xy - vec2(500,300)))) / 2000;
    gl_FragColor.a = 1.0;

    //gl_FragColor.rgb = texture2D(tex, gl_TexCoord[0].st).rgb;
}
