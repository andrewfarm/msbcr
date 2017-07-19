package com.andrewofarm.msbcr.objects.geom;

/**
 * Created by Andrew on 7/4/17.
 */
class QuadTree<E> {

    private E value;
    private QuadTree<E> topLeft, topRight, bottomLeft, bottomRight;

    QuadTree(E value) {
        setValue(value);
    }

    E getValue() {
        return value;
    }

    void setValue(E value) {
        this.value = value;
    }

    QuadTree<E> getTopLeft() {
        return topLeft;
    }

    QuadTree<E> getTopRight() {
        return topRight;
    }

    QuadTree<E> getBottomLeft() {
        return bottomLeft;
    }

    QuadTree<E> getBottomRight() {
        return bottomRight;
    }

    void addChildren(E topLeftValue, E topRightValue, E bottomLeftValue, E bottomRightValue) {
        topLeft = new QuadTree<>(topLeftValue);
        topRight = new QuadTree<>(topRightValue);
        bottomLeft = new QuadTree<>(bottomLeftValue);
        bottomRight = new QuadTree<>(bottomRightValue);
    }

    void removeChildren() {
        topLeft = null;
        topRight = null;
        bottomLeft = null;
        bottomRight = null;
    }

    boolean isLeaf() {
        return topLeft == null;
    }
}
