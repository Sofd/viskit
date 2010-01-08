#version 130

uniform sampler3D volTex;
uniform sampler2D winTex;
uniform sampler1D transferTex;

in vec3 texCoord;

out vec4 gl_FragColor;

void main() {
		
	float windowCenter = texture2D(winTex, vec2(0, texCoord.z) ).a * 2;
	float windowWidth = texture2D(winTex, vec2(1, texCoord.z) ).a * 2;
	
	float tfCoord =  0.5f + ( texture(volTex, texCoord).r - windowCenter ) / windowWidth;
		
	if ( tfCoord > 1.0f )
		tfCoord = 1.0f;
			
	if ( tfCoord < 0.0f )
		tfCoord = 0.0f;
		
	vec3 color = texture( transferTex, tfCoord ).rgb;	
	
	//gl_FragColor = vec4( color.r, color.g, color.b, 1.0f );
	gl_FragColor = vec4( tfCoord, tfCoord, tfCoord, 1.0f );
} 