#version 130

uniform sampler3D volTex;
uniform sampler3D gradientTex;
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

uniform float ambient;
uniform float diffuse;
uniform float specExp;
uniform int useLighting;

out vec4 gl_FragColor;

vec3 shading( in vec3 N, in vec3 V, in vec3 L, in vec3 color )
{
	vec3 spec = vec3(1.0f);
		
	vec3 ambient = color * vec3(ambient);
	
	float dotNL = max(dot(L, N), 0);
	vec3 diffuse = color * vec3(diffuse) * dotNL;
	
	vec3 H = normalize( L + V );
	float dotNH = max(dot(N, H), 0);
	float sf = pow( dotNH, specExp );
	vec3 specular = spec * sf * color;
	
	return min(max(ambient + diffuse + specular, vec3(0.0f)), 1.0f);
}

void main() {
	if ( gl_FrontFacing )
		discard;
	
	vec3 rayStart = texCoord.xyz;
	vec3 rayPos = rayStart;
	
	vec2 tc = vec2( gl_FragCoord.x / screenWidth, gl_FragCoord.y / screenHeight );
	
	vec3 dir = texture(backTex, tc).rgb - rayStart.xyz;
	vec3 moveDir = normalize(dir) * sliceStep;
	
	float dirLength = length(dir);
	float steps = floor(dirLength/sliceStep); 
	
	vec4 sumColor = vec4(0.0f);
	
	float texValue;
	float decay = alpha * sliceStep * 1000;
	
	vec4 tfColor = vec4(0.0f);
	vec2 tfCoord = vec2(0.0f);
	
	vec3 V = normalize( eyePosition - position );
	vec3 L = normalize( lightPosition - position );
	
	for ( int i=0; i<steps; ++i )
	{
		float windowCenter = texture2D(winTex, vec2(0, rayPos.z) ).a * 2.0f;
		float windowWidth = texture2D(winTex, vec2(1, rayPos.z) ).a * 2.0f;
		tfCoord.y = 0.5f + ( texture(volTex, rayPos).r - windowCenter ) / windowWidth;
		
		tfColor = texture2D( transferTex, tfCoord );
		
		if ( useLighting == 1 && tfColor.a > 0 )
		{
			
			vec4 G = ( texture( gradientTex, rayPos.xyz ) - vec4( 0.5f, 0.5f, 0.5f, 0.0f ) ) * vec4( 2.0f, 2.0f, 2.0f, 1.0f );
			
			if ( G.a > 0.01 )
			{			
				G.xyz = normalize(G.xyz);
				vec3 N = normalize( gl_NormalMatrix * G.xyz );
											
				tfColor.rgb = shading(N, V, L, tfColor.rgb);
			}
			
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
	gl_FragColor.a = 1.0f;
} 