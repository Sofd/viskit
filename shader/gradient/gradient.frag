#version 130

uniform sampler3D volTex;

uniform float xStep;
uniform float yStep;
uniform float zStep;

in vec3 tc;

out vec4 gl_FragColor;

void main() {
	vec4 gradient = vec4(0.0f);
	
	//central differences
	gradient.x = 64 * ( texture(volTex, vec3(tc.x - xStep, tc.y, tc.z)).r - texture(volTex, vec3(tc.x + xStep, tc.y, tc.z)).r );
	gradient.y = 64 * ( texture(volTex, vec3(tc.x, tc.y + yStep, tc.z)).r - texture(volTex, vec3(tc.x, tc.y - yStep, tc.z)).r );
	gradient.z = 64 * ( texture(volTex, vec3(tc.x, tc.y, tc.z - zStep)).r - texture(volTex, vec3(tc.x, tc.y, tc.z + zStep)).r );
	
	gradient.a = length( gradient.xyz );
	gradient.xyz = normalize( gradient.xyz ) * 0.5 + vec3(0.5f);
	
	gl_FragColor = gradient;
	
} 