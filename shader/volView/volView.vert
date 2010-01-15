#version 130

out vec3 texCoord;
out vec3 position;
out vec3 eyePosition;
out vec3 lightPosition;
out vec3 normal;

void main() {
	texCoord = gl_MultiTexCoord0.xyz;

	normal = gl_Normal;
	position = (gl_ModelViewMatrix * gl_Vertex).xyz;
	//position = gl_Vertex.xyz;
	//eyePosition = (gl_ModelViewMatrix * vec4(0, 0, -4, 0)).xyz;
	//lightPosition = (gl_ModelViewMatrix * vec4(-4, -4, -4, 0)).xyz;
	
	eyePosition = vec3(0, 0, 0);
	lightPosition = vec3(0, 0, 0);

	gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
}