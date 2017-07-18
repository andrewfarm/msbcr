#version 120

#define FOUR_PI 12.566370614359173

// wavelengths in micrometers
#define WAVELENGTH_RED   0.650
#define WAVELENGTH_GREEN 0.532
#define WAVELENGTH_BLUE  0.473

#define INNER_INTEGRAL_DIVS 5
#define OUTER_INTEGRAL_DIVS 5

#define SCATTER_CONST_RAYLEIGH 0.005

#define SCALE_HEIGHT 0.2

#define SUN_BRIGHTNESS 15.0

uniform vec3 u_CamPos;
uniform vec3 u_LightDirection; //must be normalized!
uniform float u_GlobeRadius;
uniform float u_AtmosphereWidth;
#define ATMOSPHERE_CEILING u_GlobeRadius + u_AtmosphereWidth

float lengthSquared(vec3 v) {
    return dot(v, v);
}

struct SphereIntersection {
    bool intersects;
    vec3 near;
    vec3 far;
};

SphereIntersection intersectRaySphere(vec3 point, vec3 direction, vec3 sphereCenter, float sphereRadius) {
    vec3 sphereCenterToPoint = point - sphereCenter;
    float a = lengthSquared(direction);
    float b = dot(2 * direction, sphereCenterToPoint);
    float c = lengthSquared(sphereCenterToPoint) - (sphereRadius * sphereRadius);
    SphereIntersection intersection;
    float discriminant = (b * b) - (4.0 * a * c);
    if (discriminant < 0) {
        intersection.intersects = false;
    } else {
        intersection.intersects = true;
        float root = sqrt(discriminant);
        float denom = 2.0 * a;
        intersection.near = point + ((-b - root) / denom * direction);
        intersection.far = point + ((-b + root) / denom * direction);
    }
    return intersection;
}

float phase_rayleigh(float theta) {
    float cosTheta = cos(theta);
    return 0.75 * (1.0 + cosTheta * cosTheta);
}

float scatterCoef_rayleigh(float wavelength) {
    return SCATTER_CONST_RAYLEIGH / pow(wavelength, 4);
}

float density(vec3 point) {
    // optimized version of
    // exp(-(length(point) - u_GlobeRadius) / ATMOSPHERE_THCKNESS / SCALE_HEIGHT);
    return exp((u_GlobeRadius - length(point)) / (u_AtmosphereWidth * SCALE_HEIGHT));
}

float outScatter_rayleigh(vec3 pointA, vec3 pointB, float wavelength) {
    float opticalDepth = 0.0;
    vec3 dist = pointB - pointA;
    vec3 differential = dist / float(INNER_INTEGRAL_DIVS);
    vec3 samplePoint = pointA + (differential * 0.5);
    for (int i = 0; i < INNER_INTEGRAL_DIVS; i++) {
        opticalDepth += density(samplePoint);
        samplePoint += differential;
    }
    opticalDepth *= length(dist);
    return FOUR_PI * scatterCoef_rayleigh(wavelength) * opticalDepth;
}

/*
 * pointA is the NEAR intersection between the atmosphere ceiling and
 *     the ray from the camera to the fragment, or the camera position if
 *     it is inside the atmosphere.
 * pointB is the FAR intersection between the atmosphere ceiling or the ground and
 *     the ray from the camera to the fragment.
 */
float inScatter_rayleigh(float wavelength, vec3 pointA, vec3 pointB) {
    float outerIntegral = 0.0;
    vec3 dist = pointB - pointA;
    vec3 differential = dist / float(INNER_INTEGRAL_DIVS);
    vec3 samplePoint = pointA + (differential * 0.5);
    // pointC is the intersection between the atmosphere ceiling and the
    //     ray from the sample point to the sun.
    vec3 pointC;
    for (int i = 0; i < INNER_INTEGRAL_DIVS; i++) {
        pointC = intersectRaySphere(samplePoint, u_LightDirection, vec3(0.0) /*TODO*/, ATMOSPHERE_CEILING).far;
        outerIntegral += (density(samplePoint) *
            exp(-outScatter_rayleigh(samplePoint, pointC, wavelength) -
                outScatter_rayleigh(samplePoint, u_CamPos, wavelength)));
        samplePoint += differential;
    }
    outerIntegral *= length(dist);
//    float theta; TODO
    return SUN_BRIGHTNESS * scatterCoef_rayleigh(wavelength)/* * phase_rayleigh(theta)*/ * outerIntegral;
}

float inScatter_rayleigh_doIntersection(float wavelength, vec3 pointOnCeiling) {
    SphereIntersection intersection = intersectRaySphere(u_CamPos, pointOnCeiling - u_CamPos,
        vec3(0.0) /*TODO*/, ATMOSPHERE_CEILING);
    vec3 pointA;
    if (lengthSquared(u_CamPos) /*TODO*/ > ATMOSPHERE_CEILING * ATMOSPHERE_CEILING) {
//        if (!intersection.intersects) {
//            //this should never happen except in the case of geometry and/or rounding errors
//            //at the edge of the atmosphere
//            //if it does, just don't scatter any light
//            return 0.0;
//        }
        pointA = intersection.near;
    } else {
        pointA = u_CamPos;
    }
    return inScatter_rayleigh(wavelength, pointA, intersection.far);
}

float surfaceScatter_rayleigh(float wavelength, float reflectedLight, vec3 surfacePoint) {
    vec3 pointA;
    if (lengthSquared(u_CamPos) /*TODO*/ > ATMOSPHERE_CEILING * ATMOSPHERE_CEILING) {
        SphereIntersection camRayIntersection = intersectRaySphere(u_CamPos, surfacePoint - u_CamPos,
            vec3(0.0) /*TODO*/, ATMOSPHERE_CEILING);
        if (!camRayIntersection.intersects) {
            return reflectedLight;
        }
        pointA = camRayIntersection.near;
    } else {
        pointA = u_CamPos;
    }
//    SphereIntersection lightEntryPoint = intersectRaySphere(surfacePoint, u_LightDirection,
//        vec3(0.0) /*TODO*/, ATMOSPHERE_CEILING);
//    if (lightEntryPoint.intersects) {
//        reflectedLight -= SUN_BRIGHTNESS * 0.03 * exp(-outScatter_rayleigh(lightEntryPoint.far, surfacePoint, wavelength));
//    }
    return inScatter_rayleigh(wavelength, pointA, surfacePoint) +
        (reflectedLight * exp(-outScatter_rayleigh(pointA, surfacePoint, wavelength)));
}
