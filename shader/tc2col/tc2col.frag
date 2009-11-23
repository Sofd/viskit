#version 130

varying vec3 texCoord;

void main() {
	gl_FragColor = vec4( texCoord, 1.0f);
} 
