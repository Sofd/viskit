//#version 130

uniform float preScale;
uniform float preOffset;
uniform float scale;
uniform float offset;
uniform sampler2D tex;
uniform sampler1D lutTex;
uniform bool useLut;

void main() {
    vec3 texColor_normalized = preScale * texture2D(tex, gl_TexCoord[0].st).rgb + preOffset;
    if (useLut) {
	    float intensity = scale * texColor_normalized.r + offset;
	    gl_FragColor.rgba = texture1D(lutTex, intensity).rgba;
		// TODO: composite/combine with the existing pixel in a configurable way
    } else {
	    gl_FragColor.rgb = scale * texColor_normalized + offset;
        gl_FragColor.a = 1.0;
    }
}
