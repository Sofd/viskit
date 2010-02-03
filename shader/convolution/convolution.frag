#version 130

uniform sampler3D volTex;
//uniform sampler2D winTex;

uniform float xStep;
uniform float yStep;
uniform float zStep;

in vec3 tc;

out vec4 gl_FragColor;

/* float getWindowed( in vec3 texPos ) {
	float windowCenter = texture2D(winTex, vec2(0, texPos.z) ).a * 2.0f;
	float windowWidth = texture2D(winTex, vec2(1, texPos.z) ).a * 2.0f;
	
	float value = 0.0f;
	
	value = ( 0.5f + ( texture(volTex, texPos).r - windowCenter ) / windowWidth );
		
	if ( value < 0.0f ) value = 0.0f;
	if ( value > 1.0f ) value = 1.0f;
	
	return value;	
}

float getWindowed( in vec3 texPos, in float orgValue ) {
	float windowCenter = texture2D(winTex, vec2(0, texPos.z) ).a * 2.0f;
	float windowWidth = texture2D(winTex, vec2(1, texPos.z) ).a * 2.0f;
	
	float value = 0.0f;
	
	value = ( 0.5f + ( orgValue - windowCenter ) / windowWidth );
		
	if ( value < 0.0f ) value = 0.0f;
	if ( value > 1.0f ) value = 1.0f;
	
	return value;	
}*/

float getValue( in vec3 texPos ) {
	return texture(volTex, texPos).r;
}

void main() {
	float value = 0.0f;
	
	value += 0.5 * getValue(tc);
	
	value += 0.25 * getValue(vec3(tc.x, tc.y, tc.z-zStep));	
	value += 0.25 * getValue(vec3(tc.x, tc.y, tc.z+zStep));
	value += 0.25 * getValue(vec3(tc.x, tc.y-yStep, tc.z));
	value += 0.25 * getValue(vec3(tc.x, tc.y+yStep, tc.z));
	value += 0.25 * getValue(vec3(tc.x-xStep, tc.y, tc.z));
	value += 0.25 * getValue(vec3(tc.x+xStep, tc.y, tc.z));
	
	value += 0.125 * getValue(vec3(tc.x, tc.y-yStep, tc.z-zStep));
	value += 0.125 * getValue(vec3(tc.x, tc.y+yStep, tc.z-zStep));
	value += 0.125 * getValue(vec3(tc.x, tc.y-yStep, tc.z+zStep));
	value += 0.125 * getValue(vec3(tc.x, tc.y+yStep, tc.z+zStep));
	value += 0.125 * getValue(vec3(tc.x-xStep, tc.y, tc.z-zStep));
	value += 0.125 * getValue(vec3(tc.x+xStep, tc.y, tc.z-zStep));
	value += 0.125 * getValue(vec3(tc.x-xStep, tc.y, tc.z+zStep));
	value += 0.125 * getValue(vec3(tc.x+xStep, tc.y, tc.z+zStep));
	value += 0.125 * getValue(vec3(tc.x-xStep, tc.y-yStep, tc.z));
	value += 0.125 * getValue(vec3(tc.x+xStep, tc.y-yStep, tc.z));
	value += 0.125 * getValue(vec3(tc.x-xStep, tc.y+yStep, tc.z));
	value += 0.125 * getValue(vec3(tc.x+xStep, tc.y+yStep, tc.z));
	
	value += 0.0625 * getValue(vec3(tc.x-xStep, tc.y-yStep, tc.z-zStep));
	value += 0.0625 * getValue(vec3(tc.x+xStep, tc.y-yStep, tc.z-zStep));
	value += 0.0625 * getValue(vec3(tc.x-xStep, tc.y+yStep, tc.z-zStep));
	value += 0.0625 * getValue(vec3(tc.x+xStep, tc.y+yStep, tc.z-zStep));
	value += 0.0625 * getValue(vec3(tc.x-xStep, tc.y-yStep, tc.z+zStep));
	value += 0.0625 * getValue(vec3(tc.x+xStep, tc.y-yStep, tc.z+zStep));
	value += 0.0625 * getValue(vec3(tc.x-xStep, tc.y+yStep, tc.z+zStep));
	value += 0.0625 * getValue(vec3(tc.x+xStep, tc.y+yStep, tc.z+zStep));
	
	value /= 4;
	
	//gl_FragColor.r = getWindowed(tc, value);
	gl_FragColor.r = value;
	
} 