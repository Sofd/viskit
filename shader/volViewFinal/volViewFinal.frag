#version 130

uniform sampler3D volTex;
//uniform sampler3D gradientTex;
uniform sampler2D backTex;
uniform sampler2D winTex;
uniform sampler2D transferTex;

in vec3 texCoord;
in vec3 position;

in vec3 normal;

uniform int screenWidth;
uniform int screenHeight;
uniform float sliceStep;
uniform float alpha;

uniform float ambient;
uniform float diffuse;
uniform float specExp;
uniform int useLighting;
uniform float gradientLimit;
uniform float nDiff;

uniform vec4 eyePos;
uniform vec4 lightPos;

uniform float xMin;
uniform float xMax;
uniform float yMin;
uniform float yMax;
uniform float zMin;
uniform float zMax;

uniform float xStep;
uniform float yStep;
uniform float zStep;


out vec4 gl_FragColor;

float getValue( in float x, in float y, in float z ) {
	//if ( x >= xMin && x <= xMax && 1 - y >= yMin && 1 - y <= yMax && z >= zMin && z <= zMax )
		return texture(volTex, vec3(x, y, z)).r;
		
	//return 0;
}

float getWindowed( in vec3 texPos ) {
	float windowCenter = texture2D(winTex, vec2(0, texPos.z) ).a * 2.0f;
	float windowWidth = texture2D(winTex, vec2(1, texPos.z) ).a * 2.0f;
	
	float value = 0.0f;
	
	value = ( 0.5f + ( texture(volTex, texPos).r - windowCenter ) / windowWidth );
		
	if ( value < 0.0f ) value = 0.0f;
	if ( value > 1.0f ) value = 1.0f;
	
	return value;	
}

/*vec4 getNormal( in vec3 texPos ) {
	vec4 result;

	vec3 sample1;
	vec3 sample2;
	sample1.x = getValue( texPos.x + xStep, texPos.y, texPos.z );
	sample1.y = getValue( texPos.x, texPos.y - yStep, texPos.z );
	sample1.z = getValue( texPos.x, texPos.y, texPos.z + zStep );
	
	sample2.x = getValue( texPos.x - xStep, texPos.y, texPos.z );
	sample2.y = getValue( texPos.x, texPos.y + yStep, texPos.z );
	sample2.z = getValue( texPos.x, texPos.y, texPos.z - zStep );
	
	result.xyz = sample2 - sample1;
	
	result.a = length( result.xyz );
		
	if ( result.a >= gradientLimit * 0.2 )
		result.xyz = normalize( result.xyz );
	else 
		result = vec4(0.0f);
	
	return result;
}*/

vec4 getNormal( in vec3 tc ) {
	vec4 gradient = vec4(0.0f);
	
	//if ( tc.x >= xMin && tc.x <= xMax && 1 - tc.y >= yMin && 1 - tc.y <= yMax && tc.z >= zMin && tc.z <= zMax )
	//{
		float value;
		float fak2 = 0.5f;
		float fak3 = 0.25f;
		
		//sobel
		value = fak3 * getWindowed(vec3(tc.x - xStep, tc.y - yStep, tc.z - zStep));
		gradient.x += value; gradient.y -= value; gradient.z += value;
		
		value = fak2 * getWindowed(vec3(tc.x - xStep, tc.y - yStep, tc.z));
		gradient.x += value; gradient.y -= value;
		
		value = fak3 * getWindowed(vec3(tc.x - xStep, tc.y - yStep, tc.z + zStep));
		gradient.x += value; gradient.y -= value; gradient.z -= value;
		
		value = fak2 * getWindowed(vec3(tc.x - xStep, tc.y, tc.z - zStep));
		gradient.x += value; gradient.z += value;
		
		value = getWindowed(vec3(tc.x - xStep, tc.y, tc.z));
		gradient.x += value;
		
		value = fak2 * getWindowed(vec3(tc.x - xStep, tc.y, tc.z + zStep));
		gradient.x += value; gradient.z -= value;
		
		value = fak3 * getWindowed(vec3(tc.x - xStep, tc.y + yStep, tc.z - zStep));
		gradient.x += value; gradient.y += value; gradient.z += value;
		
		value = fak2 * getWindowed(vec3(tc.x - xStep, tc.y + yStep, tc.z));
		gradient.x += value; gradient.y += value; 
		
		value = fak3 * getWindowed(vec3(tc.x - xStep, tc.y + yStep, tc.z + zStep));
		gradient.x += value; gradient.y += value; gradient.z -= value;
		
		
		value = fak2 * getWindowed(vec3(tc.x, tc.y - yStep, tc.z - zStep));
		gradient.y -= value; gradient.z += value;
		
		value = getWindowed(vec3(tc.x, tc.y - yStep, tc.z));
		gradient.y -= value;
		
		value = fak2 * getWindowed(vec3(tc.x, tc.y - yStep, tc.z + zStep));
		gradient.y -= value; gradient.z -= value;
		
		value = getWindowed(vec3(tc.x, tc.y, tc.z - zStep));
		gradient.z += value;
		
		value = getWindowed(vec3(tc.x, tc.y, tc.z + zStep));
		gradient.z -= value;
		
		value = fak2 * getWindowed(vec3(tc.x, tc.y + yStep, tc.z - zStep));
		gradient.y += value; gradient.z += value;
		
		value = getWindowed(vec3(tc.x, tc.y + yStep, tc.z));
		gradient.y += value; 
		
		value = fak2 * getWindowed(vec3(tc.x, tc.y + yStep, tc.z + zStep));
		gradient.y += value; gradient.z -= value;
		
		
		value = fak3 * getWindowed(vec3(tc.x + xStep, tc.y - yStep, tc.z - zStep));
		gradient.x -= value; gradient.y -= value; gradient.z += value;
		
		value = fak2 * getWindowed(vec3(tc.x + xStep, tc.y - yStep, tc.z));
		gradient.x -= value; gradient.y -= value;
		
		value = fak3 * getWindowed(vec3(tc.x + xStep, tc.y - yStep, tc.z + zStep));
		gradient.x -= value; gradient.y -= value; gradient.z -= value;
		
		value = fak2 * getWindowed(vec3(tc.x + xStep, tc.y, tc.z - zStep));
		gradient.x -= value; gradient.z += value;
		
		value = getWindowed(vec3(tc.x + xStep, tc.y, tc.z));
		gradient.x -= value;
		
		value = fak2 * getWindowed(vec3(tc.x + xStep, tc.y, tc.z + zStep));
		gradient.x -= value; gradient.z -= value;
		
		value = fak3 * getWindowed(vec3(tc.x + xStep, tc.y + yStep, tc.z - zStep));
		gradient.x -= value; gradient.y += value; gradient.z += value;
		
		value = fak2 * getWindowed(vec3(tc.x + xStep, tc.y + yStep, tc.z));
		gradient.x -= value; gradient.y += value; 
		
		value = fak3 * getWindowed(vec3(tc.x + xStep, tc.y + yStep, tc.z + zStep));
		gradient.x -= value; gradient.y += value; gradient.z -= value;
		
		//gradient.a = length( gradient.xyz ) / sqrt(3*pow(1+4*fak2+4*fak3, 2));
		gradient.a = length( gradient.xyz ) / 6.93;
		
		if ( gradient.a >= gradientLimit * 0.2 )
			gradient.xyz = normalize( gradient.xyz );
		else 
			gradient = vec4(0.0f);
		
	//}
	
	return gradient;
}

vec3 shading( in vec3 N, in vec3 E, in vec3 L, in vec4 color )
{
	vec3 spec = vec3(1.0f);
		
	vec3 ambient = color.rgb * vec3(ambient);
	
	float dotNL = max(dot(L, N), 0);
	
	vec3 diffuse = color.rgb * vec3(diffuse) * dotNL;
	
	vec3 H = normalize( L + E );
	float dotNH = max(dot(N, H), 0);
	float sf = pow( dotNH, specExp );
	
	//vec3 R = normalize( -reflect(L, N) );
	//float dotER = max(dot(R, E), 0);
	//float sf = pow( dotER, specExp );
	
	vec3 specular = spec * sf * color.a;
	
	return min(max(ambient + diffuse + specular, vec3(0.0f)), 1.0f);
}

vec3 shading2( in vec3 E, in vec3 L, in vec3 color, in float value, in vec3 texPos )
{
	vec3 spec = vec3(1.0f);
		
	vec3 ambient = color * vec3(ambient);
	
	//float dotNL = max(dot(L, N), 0);
	L.y = -L.y;
	float dotNL = max( nDiff * ( value - texture(volTex, texPos + 0.1f * alpha * L).r ), 0 );
	L.y = -L.y;
	
	vec3 diffuse = color * vec3(diffuse) * dotNL;
	
	vec3 H = normalize( L + E );
	H.y = -H.y;
	//float dotNH = max(dot(N, H), 0);
	
	float dotNH = max( nDiff * ( value - texture(volTex,  texPos + 0.1f * alpha * H ).r ), 0 );
	float sf = pow( dotNH, specExp );
	
	//vec3 R = normalize( -reflect(L, N) );
	//float dotER = max(dot(R, E), 0);
	//float sf = pow( dotER, specExp );
	
	vec3 specular = spec * sf;
	
	//return min(max(ambient + diffuse + specular, vec3(0.0f)), 1.0f);
	
	return vec3(sf);
}

float random(in vec3 seed) {
	return fract(sin(seed.x * 12.9898 + seed.y * 78.233 + 23 * seed.z) * 43758.5453 );
}

void main() {
	if ( gl_FrontFacing )
		discard;
	
	vec3 rayStart = texCoord.xyz;
		
	vec2 tc = vec2( gl_FragCoord.x / screenWidth, gl_FragCoord.y / screenHeight );
	
	vec3 dir = texture(backTex, tc).rgb - rayStart.xyz;
	vec3 moveDir = normalize(dir) * sliceStep;
	
	vec3 rayPos = rayStart + random(texCoord.xyz) * moveDir;
	//vec3 rayPos = rayStart;
	
	float dirLength = length(dir);
	float steps = floor(dirLength/sliceStep); 
	
	vec4 sumColor = vec4(0.0f);
	
	float texValue;
	float decay = sliceStep * 600;
	
	vec4 tfColor = vec4(0.0f);
	vec2 tfCoord = vec2(0.0f);
	
	for ( int i=0; i<steps; ++i )
	{
		tfCoord.y = getWindowed( rayPos );
		
		tfColor = texture2D( transferTex, tfCoord );
				
		if ( useLighting == 1 && tfColor.a > 0.1 )
		{
			
			//vec4 G = ( texture( gradientTex, rayPos.xyz ) - vec4( 0.5f, 0.5f, 0.5f, 0.0f ) ) * vec4( 2.0f, 2.0f, 2.0f, 1.0f );
			
			vec4 N = getNormal( rayPos );
			
			//if ( G.a >= gradientLimit )
			if ( N.a >= gradientLimit * 0.2 )
			{			
				//vec3 N = normalize(G.xyz);
				//vec3 N = normalize( gl_NormalMatrix * G.xyz );
				
				//vec3 V = (gl_ModelViewMatrix * vec4(rayPos.x, rayPos.y, rayPos.z, 1.0)).xyz;
				
				vec3 E = normalize( eyePos.xyz - rayPos );
				vec3 L = normalize( lightPos.xyz - rayPos );
											
				tfColor.rgb = shading(N.xyz, E, L, tfColor );
			}
			
			//tfColor.rgb = N.rgb;
			
		}
		
		sumColor.rgb = sumColor.rgb + (1 - sumColor.a) * tfColor.rgb * decay;
		sumColor.a = sumColor.a + (1 - sumColor.a) * tfColor.a * decay;
		
		if ( sumColor.a >= 0.95 ) break;
		
		rayPos += moveDir;
		
		tfCoord.x = tfCoord.y;
	}
	
	gl_FragColor = sumColor;
	//gl_FragColor.rgb = normal;
	//gl_FragColor.rgb = vec3(texture(gradientTex, texCoord.xyz).a);
	//gl_FragColor.rgb = vec3(texture(transferTex, texCoord.xy).a);
	//gl_FragColor.rgb = texture(backTex, tc).rgb;
	
	gl_FragColor.a = 1.0f;
} 