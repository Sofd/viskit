!!ARBfp1.0

TEMP texValue;
TEMP color;
TEMP rangeMin;
TEMP rangeMax;
TEMP windowCen;
TEMP windowWidth;
TEMP compare;
 
#lookup texture
TEX texValue, fragment.texcoord[0], texture[0], 2D;


MOV windowCen, program.local[0];
MOV windowWidth, program.local[1];

MUL windowWidth.y, windowWidth.x, 0.5;
RCP windowWidth.z, windowWidth.x;

SUB rangeMin.x, windowCen.x, windowWidth.y;
ADD rangeMax.x, windowCen.x, windowWidth.y;

## RED
# 1: rangeMin.x <= texValue.w, 0: rangeMin.x > texValue.w  
SGE compare.x, texValue.r, rangeMin.x;

# 1: texValue.w <= rangeMax.x, 0: texValue.w > rangeMax.x
SGE compare.y, rangeMax.x, texValue.r; 
SUB compare.y, 1, compare.y;

#y = (x-rangeMin)/windowWidth
SUB color.r, texValue.r, rangeMin.x;
MUL color.r, color.r, windowWidth.z;
MIN color.r, color.r, 1.0;
MAX color.r, color.r, 0.0;

MUL color.r, color.r, compare.x;
MAX color.r, color.r, compare.y; 

## GREEN
# 1: rangeMin.x <= texValue.w, 0: rangeMin.x > texValue.w  
SGE compare.x, texValue.g, rangeMin.x;

# 1: texValue.w <= rangeMax.x, 0: texValue.w > rangeMax.x
SGE compare.y, rangeMax.x, texValue.g; 
SUB compare.y, 1, compare.y;

#y = (x-rangeMin)/windowWidth
SUB color.g, texValue.g, rangeMin.x;
MUL color.g, color.g, windowWidth.z;
MIN color.g, color.g, 1.0;
MAX color.g, color.g, 0.0;

MUL color.g, color.g, compare.x;
MAX color.g, color.g, compare.y;

## BLUE
# 1: rangeMin.x <= texValue.w, 0: rangeMin.x > texValue.w  
SGE compare.x, texValue.b, rangeMin.x;

# 1: texValue.w <= rangeMax.x, 0: texValue.w > rangeMax.x
SGE compare.y, rangeMax.x, texValue.b; 
SUB compare.y, 1, compare.y;

#y = (x-rangeMin)/windowWidth
SUB color.b, texValue.b, rangeMin.x;
MUL color.b, color.b, windowWidth.z;
MIN color.b, color.b, 1.0;
MAX color.b, color.b, 0.0;

MUL color.b, color.b, compare.x;
MAX color.b, color.b, compare.y;

MOV result.color.r, color.r;
MOV result.color.g, color.g;
MOV result.color.b, color.b;
MOV result.color.w, 1.0;
 
END