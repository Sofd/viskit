//#version 130

uniform float preScale;
uniform float preOffset;
uniform float scale;
uniform float offset;
uniform sampler2D tex;
uniform sampler1D lutTex;
uniform bool useLut;
uniform bool useLutAlphaBlending;
uniform bool useGrayscaleRGBOutput;
uniform sampler1D grayscaleRgbTex;

void main() {
    vec3 texColor_normalized = preScale * texture2D(tex, gl_TexCoord[0].st).rgb + preOffset;
    if (useGrayscaleRGBOutput) {
        float intensity = scale * texColor_normalized.r + offset;
        vec3 grayscaleRGB = texture1D(grayscaleRgbTex, intensity).rgb;
        gl_FragColor.rgb = grayscaleRGB;
        gl_FragColor.a = 1.0;
    } else if (useLut) {
	    float intensity = scale * texColor_normalized.r + offset;
        vec4 lutRGBA = texture1D(lutTex, intensity).rgba;
        if (useLutAlphaBlending) {
            //TODO: impl previousColor (draw everything to FBO)
            //gl_FragColor.rgb = lutRGBA.a * lutRGBA.rgb + (1-lutRGBA.a) * previousColor;
            gl_FragColor.rgba = lutRGBA;
        } else {
            gl_FragColor.rgba = lutRGBA;
        }
    } else {
	    gl_FragColor.rgb = scale * texColor_normalized + offset;
        gl_FragColor.a = 1.0;
    }
}
