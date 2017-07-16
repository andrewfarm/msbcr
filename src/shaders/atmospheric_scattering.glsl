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

struct SphereIntersection {
    bool intersects;
    vec3 near;
    vec3 far;
};

SphereIntersection intersectRaySphere(vec3 point, vec3 direction, vec3 sphereCenter, float sphereRadius) {
    const vec3 sphereCenterToPoint = point - sphereCenter;
    const float a = lengthSquared(direction);
    const float b = dot(2 * direction, sphereCenterToPoint);
    const float c = lengthSquared(sphereCenterToPoint) - (sphereRadius * sphereRadius);
    SphereIntersection intersection;
    const float discriminant = (b * b) - (4.0 * a * c);
    if (discriminant < 0) {
        intersection.intersects = false;
    } else {
        intersection.intersects = true;
        const float root = sqrt(discriminant);
        const float denom = 2.0 * a;
        intersection.near = point + ((-b - root) / denom * direction);
        intersection.far = point + ((-b + root) / denom * direction);
    }
    return intersection;
}

float phase_rayleigh(float theta) {
    const float cosTheta = cos(theta);
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
    const vec3 differential = (pointB - pointA) / float(INNER_INTEGRAL_DIVS);
    vec3 samplePoint = pointA + (differential * 0.5);
    for (int i = 0; i < INNER_INTEGRAL_DIVS; i++) {
        opticalDepth += density(samplePoint);
        samplePoint += differential;
    }
    return FOUR_PI * scatterCoef_rayleigh(wavelength) * opticalDepth;
}

float inScatter_rayleigh(float wavelength) {
    float outerIntegral = 0.0;
    //pointA is the NEAR intersection between the atmosphere ceiling and
    //  the ray from the camera to the vertex.
    //pointB is the FAR intersection between the atmosphere ceiling and
    //  the ray from the camera to the vertex.
    //pointC is the intersection between the atmosphere ceiling and the
    //  ray from the sample point to the sun.
    vec3 pointA, pointB, pointC; //TODO
    const vec3 differential = (pointB - pointA) / float(INNER_INTEGRAL_DIVS);
    vec3 samplePoint = pointA + (differential * 0.5);
    for (int i = 0; i < INNER_INTEGRAL_DIVS; i++) {
        outerIntegral += (density(samplePoint) *
            exp(-outScatter_rayleigh(samplePoint, pointC, wavelength) -
                outScatter_rayleigh(samplePoint, u_CamPos, wavelength)));
        samplePoint += differential;
    }
//    float theta; TODO
    return SUN_BRIGHTNESS * scatterCoef_rayleigh(wavelength)/* * phase_rayleigh(theta)*/ * outerIntegral;
}

float surfaceScatter_rayleigh(float wavelength, float reflectedLight, vec3 surfaceVertex) {
    vec3 atmosphereEntryPoint; //TODO
    return inScatter_rayleigh(wavelength) +
        (reflectedLight * exp(-outScatter_rayleigh(atmosphereEntryPoint, surfaceVertex, wavelength)));
}
