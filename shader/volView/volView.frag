#version 130

uniform sampler3D volTex;
uniform sampler2D backTex;

varying vec3 texCoord;

uniform int screenWidth;
uniform int screenHeight;
uniform float sliceStep;
uniform float alpha;
uniform float bias;

void main() {
	if ( gl_FrontFacing )
		discard;
		
	vec2 tc;
	tc.x = gl_FragCoord.x / screenWidth;
	tc.y = gl_FragCoord.y / screenHeight;
	vec3 rayStart = texture2D(backTex, tc).rgb;
	//vec3 rayStart = texCoord.xyz;
	vec3 rayPos = rayStart;
	vec3 dir = texCoord.xyz - rayStart.xyz;
	//vec3 dir = texture2D(backTex, tc).rgb - rayStart.xyz;
	float dirLength = length(dir);
	vec3 moveDir = normalize(dir) * sliceStep;
	
	float steps = floor(dirLength/sliceStep); 
	
	float value = 0.0f;
	float volValue = 0.0f;
	
	for ( int i=0; i<steps; ++i )
	{
		volValue = ( 1.0f - abs(bias - texture3D(volTex, rayPos).a)/bias ) * alpha;
		//volValue = texture3D(volTex, rayPos).a;
		value = volValue * volValue + (1 - volValue) * value;
		
		/*if ( volValue >= alpha - 0.05f && volValue <= alpha + 0.05f )
		{
			
			value = 1.0f;
			break; 	
		}*/
		//value = max(value, volValue);
		rayPos += moveDir;
	}
	
	gl_FragColor = vec4( value, value, value, 1.0f );
	//gl_FragColor = vec4( rayStart.x, value, value, 1.0f );
} 