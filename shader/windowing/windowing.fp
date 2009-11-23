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

# 1: rangeMin.x <= texValue.w, 0: rangeMin.x > texValue.w  
SGE compare.x, texValue.w, rangeMin.x;

# 1: texValue.w <= rangeMax.x, 0: texValue.w > rangeMax.x
SGE compare.y, rangeMax.x, texValue.w; 
SUB compare.y, 1, compare.y;

#y = (x-rangeMin)/windowWidth
SUB color.w, texValue.w, rangeMin.x;
MUL color.w, color.w, windowWidth.z;
MIN color.w, color.w, 1.0;
MAX color.w, color.w, 0.0;

MUL color.w, color.w, compare.x;
MAX color.w, color.w, compare.y; 

MOV result.color.x, color.w;
MOV result.color.y, color.w;
MOV result.color.z, color.w;
MOV result.color.w, 1.0;
 
END