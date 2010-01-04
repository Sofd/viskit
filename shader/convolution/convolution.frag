#version 130

uniform sampler3D volTex;

uniform float xStep;
uniform float yStep;
uniform float zStep;

in vec3 tc;

out vec4 gl_FragColor;

void main() {
	float value = 0.0f;
	
	value += 0.5 * texture(volTex, tc).r;
	
	value += 0.25 * texture(volTex, vec3(tc.x, tc.y, tc.z-zStep)).r;	
	value += 0.25 * texture(volTex, vec3(tc.x, tc.y, tc.z+zStep)).r;
	value += 0.25 * texture(volTex, vec3(tc.x, tc.y-yStep, tc.z)).r;
	value += 0.25 * texture(volTex, vec3(tc.x, tc.y+yStep, tc.z)).r;
	value += 0.25 * texture(volTex, vec3(tc.x-xStep, tc.y, tc.z)).r;
	value += 0.25 * texture(volTex, vec3(tc.x+xStep, tc.y, tc.z)).r;
	
	value += 0.125 * texture(volTex, vec3(tc.x, tc.y-yStep, tc.z-zStep)).r;
	value += 0.125 * texture(volTex, vec3(tc.x, tc.y+yStep, tc.z-zStep)).r;
	value += 0.125 * texture(volTex, vec3(tc.x, tc.y-yStep, tc.z+zStep)).r;
	value += 0.125 * texture(volTex, vec3(tc.x, tc.y+yStep, tc.z+zStep)).r;
	value += 0.125 * texture(volTex, vec3(tc.x-xStep, tc.y, tc.z-zStep)).r;
	value += 0.125 * texture(volTex, vec3(tc.x+xStep, tc.y, tc.z-zStep)).r;
	value += 0.125 * texture(volTex, vec3(tc.x-xStep, tc.y, tc.z+zStep)).r;
	value += 0.125 * texture(volTex, vec3(tc.x+xStep, tc.y, tc.z+zStep)).r;
	value += 0.125 * texture(volTex, vec3(tc.x-xStep, tc.y-yStep, tc.z)).r;
	value += 0.125 * texture(volTex, vec3(tc.x+xStep, tc.y-yStep, tc.z)).r;
	value += 0.125 * texture(volTex, vec3(tc.x-xStep, tc.y+yStep, tc.z)).r;
	value += 0.125 * texture(volTex, vec3(tc.x+xStep, tc.y+yStep, tc.z)).r;
	
	value += 0.0625 * texture(volTex, vec3(tc.x-xStep, tc.y-yStep, tc.z-zStep)).r;
	value += 0.0625 * texture(volTex, vec3(tc.x+xStep, tc.y-yStep, tc.z-zStep)).r;
	value += 0.0625 * texture(volTex, vec3(tc.x-xStep, tc.y+yStep, tc.z-zStep)).r;
	value += 0.0625 * texture(volTex, vec3(tc.x+xStep, tc.y+yStep, tc.z-zStep)).r;
	value += 0.0625 * texture(volTex, vec3(tc.x-xStep, tc.y-yStep, tc.z+zStep)).r;
	value += 0.0625 * texture(volTex, vec3(tc.x+xStep, tc.y-yStep, tc.z+zStep)).r;
	value += 0.0625 * texture(volTex, vec3(tc.x-xStep, tc.y+yStep, tc.z+zStep)).r;
	value += 0.0625 * texture(volTex, vec3(tc.x+xStep, tc.y+yStep, tc.z+zStep)).r;
	
	value /= 4;
	
	gl_FragColor.r = value;
	
} 