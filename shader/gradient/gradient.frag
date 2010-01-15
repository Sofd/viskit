#version 130

uniform sampler3D volTex;
uniform sampler2D winTex;

uniform float xStep;
uniform float yStep;
uniform float zStep;

uniform float xMin;
uniform float xMax;
uniform float yMin;
uniform float yMax;
uniform float zMin;
uniform float zMax;

in vec3 tc;

out vec4 gl_FragColor;

float getValue( in float x, in float y, in float z )
{
	float windowCenter = texture2D(winTex, vec2(0, z) ).a * 2.0f;
	float windowWidth = texture2D(winTex, vec2(1, z) ).a * 2.0f;
	
	float value = 0.0f;
	
	if ( x >= xMin && x <= xMax && 1 - y >= yMin && 1 - y <= yMax && z >= zMin && z <= zMax )
		value = 0.5f + ( texture(volTex, vec3(x, y, z)).r - windowCenter ) / windowWidth;
	
	if ( value < 0.0f ) value = 0.0f;
	if ( value > 1.0f ) value = 1.0f;	
		
	return value;
}

void main() {
	vec4 gradient = vec4(0.0f);
	
	if ( tc.x >= xMin && tc.x <= xMax && 1 - tc.y >= yMin && 1 - tc.y <= yMax && tc.z >= zMin && tc.z <= zMax )
	{
		float value;
		//float fak2 = sqrt(2.0f)/2.0f;
		//float fak3 = sqrt(3.0f)/3.0f;
		float fak2 = 0.5f;
		float fak3 = 0.25f;
		
		//central differences
		/*gradient.x =  ( getValue(tc.x - xStep, tc.y, tc.z) - getValue(tc.x + xStep, tc.y, tc.z) );
		gradient.y =  ( getValue(tc.x, tc.y + yStep, tc.z) - getValue(tc.x, tc.y - yStep, tc.z) );
		gradient.z =  ( getValue(tc.x, tc.y, tc.z - zStep) - getValue(tc.x, tc.y, tc.z + zStep) );*/
		
		//sobel
		value = fak3 * getValue(tc.x - xStep, tc.y - yStep, tc.z - zStep);
		gradient.x += value; gradient.y -= value; gradient.z += value;
		
		value = fak2 * getValue(tc.x - xStep, tc.y - yStep, tc.z);
		gradient.x += value; gradient.y -= value;
		
		value = fak3 * getValue(tc.x - xStep, tc.y - yStep, tc.z + zStep);
		gradient.x += value; gradient.y -= value; gradient.z -= value;
		
		value = fak2 * getValue(tc.x - xStep, tc.y, tc.z - zStep);
		gradient.x += value; gradient.z += value;
		
		value = getValue(tc.x - xStep, tc.y, tc.z);
		gradient.x += value;
		
		value = fak2 * getValue(tc.x - xStep, tc.y, tc.z + zStep);
		gradient.x += value; gradient.z -= value;
		
		value = fak3 * getValue(tc.x - xStep, tc.y + yStep, tc.z - zStep);
		gradient.x += value; gradient.y += value; gradient.z += value;
		
		value = fak2 * getValue(tc.x - xStep, tc.y + yStep, tc.z);
		gradient.x += value; gradient.y += value; 
		
		value = fak3 * getValue(tc.x - xStep, tc.y + yStep, tc.z + zStep);
		gradient.x += value; gradient.y += value; gradient.z -= value;
		
		
		value = fak2 * getValue(tc.x, tc.y - yStep, tc.z - zStep);
		gradient.y -= value; gradient.z += value;
		
		value = getValue(tc.x, tc.y - yStep, tc.z);
		gradient.y -= value;
		
		value = fak2 * getValue(tc.x, tc.y - yStep, tc.z + zStep);
		gradient.y -= value; gradient.z -= value;
		
		value = getValue(tc.x, tc.y, tc.z - zStep);
		gradient.z += value;
		
		value = getValue(tc.x, tc.y, tc.z + zStep);
		gradient.z -= value;
		
		value = fak2 * getValue(tc.x, tc.y + yStep, tc.z - zStep);
		gradient.y += value; gradient.z += value;
		
		value = getValue(tc.x, tc.y + yStep, tc.z);
		gradient.y += value; 
		
		value = fak2 * getValue(tc.x, tc.y + yStep, tc.z + zStep);
		gradient.y += value; gradient.z -= value;
		
		
		value = fak3 * getValue(tc.x + xStep, tc.y - yStep, tc.z - zStep);
		gradient.x -= value; gradient.y -= value; gradient.z += value;
		
		value = fak2 * getValue(tc.x + xStep, tc.y - yStep, tc.z);
		gradient.x -= value; gradient.y -= value;
		
		value = fak3 * getValue(tc.x + xStep, tc.y - yStep, tc.z + zStep);
		gradient.x -= value; gradient.y -= value; gradient.z -= value;
		
		value = fak2 * getValue(tc.x + xStep, tc.y, tc.z - zStep);
		gradient.x -= value; gradient.z += value;
		
		value = getValue(tc.x + xStep, tc.y, tc.z);
		gradient.x -= value;
		
		value = fak2 * getValue(tc.x + xStep, tc.y, tc.z + zStep);
		gradient.x -= value; gradient.z -= value;
		
		value = fak3 * getValue(tc.x + xStep, tc.y + yStep, tc.z - zStep);
		gradient.x -= value; gradient.y += value; gradient.z += value;
		
		value = fak2 * getValue(tc.x + xStep, tc.y + yStep, tc.z);
		gradient.x -= value; gradient.y += value; 
		
		value = fak3 * getValue(tc.x + xStep, tc.y + yStep, tc.z + zStep);
		gradient.x -= value; gradient.y += value; gradient.z -= value;
		
		gradient.a = length( gradient.xyz ) / sqrt(3*pow(1+4*fak2+4*fak3, 2));
		
		if ( gradient.a > 0.01f )
			gradient.xyz = normalize( gradient.xyz ) * 0.5 + vec3(0.5f);
		else 
		{
			gradient.xyz = vec3(0.5f);
			gradient.a = 0.0f;
		}
	}
	
	gl_FragColor = gradient;
	
} 