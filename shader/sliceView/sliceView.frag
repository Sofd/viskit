#version 130

uniform sampler3D volTex;

varying vec3 texCoord;

void main() {
		
	vec4 texValue = texture3D(volTex, texCoord);
	
	gl_FragColor = vec4( texValue.a, texValue.a, texValue.a, 1.0f );
} 