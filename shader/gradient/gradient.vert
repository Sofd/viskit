#version 130

out vec3 tc;

void main() {
	tc = gl_MultiTexCoord0.xyz;

	gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
}