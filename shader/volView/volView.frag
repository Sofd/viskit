#version 130

uniform sampler3D volTex;
//uniform sampler3D gradientTex;
uniform sampler2D backTex;
uniform sampler2D winTex;
uniform sampler2D transferTex;

in vec3 texCoord;
in vec3 position;
in vec3 eyePosition;
in vec3 lightPosition;

in vec3 normal;

uniform int screenWidth;
uniform int screenHeight;
uniform float sliceStep;
uniform float alpha;

out vec4 gl_FragColor;

vec3 shading( in vec3 N, in vec3 V, in vec3 L, in vec3 color )
{
	vec3 spec = vec3(0.7f);
		
	vec3 ambient = color * vec3(0.3f);
	
	float dotNL = max(dot(L, N), 0);
	vec3 diffuse = color * dotNL;
	
	vec3 H = normalize( L + V );
	float dotNH = max(dot(N, H), 0);
	float sf = pow( dotNH, 100 );
	vec3 specular = spec * sf;
	
	return min(max(ambient + diffuse + specular, vec3(0.0f)), 1.0f);
}

void main() {
	if ( gl_FrontFacing )
		discard;
		
	vec2 tc;
	tc.x = gl_FragCoord.x / screenWidth;
	tc.y = gl_FragCoord.y / screenHeight;
	vec3 rayStart = texCoord.xyz;
	vec3 rayPos = rayStart;
	vec3 dir = texture(backTex, tc).rgb - rayStart.xyz;
	float dirLength = length(dir);
	vec3 moveDir = normalize(dir) * sliceStep;
	
	float steps = floor(dirLength/sliceStep); 
	
	vec4 sumColor = vec4(0.0f);
	
	float texValue;
	float decay = alpha * sliceStep * 1000;
		
	float windowCenter = texture2D(winTex, vec2(0, 0.5f) ).a * 2.0f;
	float windowWidth = texture2D(winTex, vec2(1, 0.5f) ).a * 2.0f;
	
	vec4 tfColor = vec4(0.0f);
	vec2 tfCoord = vec2(0.0f);
	//vec4 gradColor = vec4(0.0f);
	
	for ( int i=0; i<steps; ++i )
	{
		//windowCenter = texture2D(winTex, vec2(0, rayPos.z) ).a * 2.0f;
		//windowWidth = texture2D(winTex, vec2(1, rayPos.z) ).a * 2.0f;
		
		tfCoord.y = 0.5f + ( texture(volTex, rayPos).r - windowCenter ) / windowWidth;
		//tfCoord.y = texture(volTex, rayPos).r;
		
		tfColor = texture2D( transferTex, tfCoord );
		
		/* if ( tfColor.a > 0.0f )
		{
			
			vec4 G = ( texture( gradientTex, rayPos.xyz ) - vec4( 0.5f, 0.5f, 0.5f, 0.0f ) ) * vec4( 2.0f, 2.0f, 2.0f, 1.0f );
			if ( G.a > 0.07 )
			{
			
				vec3 N = normalize( gl_NormalMatrix * (vec4( G.x, G.y, G.z, 1.0f )).xyz );
				vec3 V = normalize( eyePosition - position );
				vec3 L = normalize( lightPosition - position );
							
				tfColor.rgb = shading(N, V, L, tfColor.rgb);
				
				//tfColor.r = max(0.0f, N.r) * tfColor.a;	
				//tfColor.b = max(0.0f, N.b) * tfColor.a;	
				//tfColor.g = max(0.0f, N.g) * tfColor.a;
			}
			
			//tfColor.rgb = vec3(G.a) * tfColor.a;
		}*/
				
		
		
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
	gl_FragColor.a = 1.0f;
} 