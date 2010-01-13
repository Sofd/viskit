#version 130

void main() {
	gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
	gl_TexCoord[0] = gl_TextureMatrix[0] * gl_MultiTexCoord0;
    //gl_Position.x /= 2;
	//gl_Position = (gl_ModelViewProjectionMatrix * gl_Vertex) * 0.5;
    //gl_Position.w = 1.0;
	//gl_Position = vec4(1,2,3,4);
}