#version 130

uniform sampler1D transferTex;
uniform isampler1D intTable;
uniform int intTableSize;

in vec3 tc;

out vec4 gl_FragColor;

void main() {
	vec4 color = vec4(0.0f);
	
	float tcMin = tc.x;
	float tcMax = tc.y;
	
	if ( tcMax < tcMin )
	{
		tcMax = tc.x;
		tcMin = tc.y;
	}
	
	if ( tcMax != tcMin )
	{
		float delta = intTableSize * ( tcMax - tcMin );
		ivec4 minCol = ivec4(texture( intTable, tcMin ));
		ivec4 maxCol = ivec4(texture( intTable, tcMax ));
		
		color.rgb = ( maxCol.rgb - minCol.rgb ) / ( 255.0f * delta );
		color.a = 1 - exp( - ( maxCol.a - minCol.a ) / ( 255.0f * delta ) );
		//color.a = exp( ( ( maxCol.a - minCol.a ) / ( 255.0f * delta ) - 1 ) * 10 );
	}
	else
	{
		vec4 tfCol = texture( transferTex, tcMin );
		color.rgb = tfCol.rgb * tfCol.a;
		color.a = 1 - exp( - tfCol.a );
		//color.a = exp( ( tfCol.a - 1 ) * 10 );
	}
	
	
	gl_FragColor = color;
	
} 