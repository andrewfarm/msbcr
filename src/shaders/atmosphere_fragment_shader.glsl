#version 120

#define FOUR_PI 12.566370614359173

#define INNER_INTEGRAL_DIVS 5
#define OUTER_INTEGRAL_DIVS 5

#define SCATTER_CONST_RAYLEIGH 1.0

#define ATMOSPHERE_THICKNESS 0.1
#define SCALE_HEIGHT 0.25

#define SUN_BRIGHTNESS 1.0

uniform vec3 u_CamPos;
uniform float u_GlobeRadius;

float phase_rayleigh(float theta) {
    float cosTheta = cos(theta);
    return 0.75 * (1.0 + cosTheta * cosTheta);
}

float scatterCoef_rayleigh(float wavelength) {
    return SCATTER_CONST_RAYLEIGH / pow(wavelength, 4);
}

float density(vec3 point) {
    return exp((u_GlobeRadius - length(point)) / (ATMOSPHERE_THICKNESS * SCALE_HEIGHT));
}

float outScatter_rayleigh(vec3 pointA, vec3 pointB, float wavelength) {
    float opticalDepth = 0.0;
    vec3 differential = (pointB - pointA) / float(INNER_INTEGRAL_DIVS);
    vec3 samplePoint = pointA + (differential * 0.5);
    for (int i = 0; i < INNER_INTEGRAL_DIVS; i++) {
        opticalDepth += density(samplePoint);
        samplePoint += differential;
    }
    return FOUR_PI * scatterCoef_rayleigh(wavelength) * opticalDepth;
}

float inScatter_rayleigh(float wavelength) {
    float outerIntegral = 0.0;
//    vec3 pointA, pointB, pointC; TODO
    vec3 differential = (pointB - pointA) / float(INNER_INTEGRAL_DIVS);
    vec3 samplePoint = pointA + (differential * 0.5);
    for (int i = 0; i < INNER_INTEGRAL_DIVS; i++) {
        outerIntegral += (density(samplePoint) *
            exp(-outScatter_rayleigh(samplePoint, pointC, wavelength) -
                outScatter_rayleigh(samplePoint, u_CamPos, waveLength)));
        samplePoint += differential;
    }
//    float theta; TODO
    return SUN_BRIGHTNESS * scatterCoef_rayleigh(wavelength) * phase_rayleigh(theta) * outerIntegral;
}

float surfaceScatter_rayleigh(float wavelength, float reflectedLight) {
//    vec3 pointA, pointB; TODO
    return inScatter_rayleigh(wavelength) +
        (reflectedLight * exp(-outScatter_rayleigh(pointA, pointB, wavelength)));
}

void main() {

}
