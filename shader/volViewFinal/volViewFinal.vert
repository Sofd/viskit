#version 130

out vec3 texCoord;
out vec3 position;
out vec3 normal;

void main() {
	texCoord = gl_MultiTexCoord0.xyz;

	normal = gl_Normal;
	position = (gl_ModelViewMatrix * gl_Vertex).xyz;
		
	gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
}