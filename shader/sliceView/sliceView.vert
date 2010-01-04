#version 130

out vec3 texCoord;

void main() {
	texCoord = ( gl_TextureMatrix[0] * gl_MultiTexCoord0 ).xyz;

	gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
}