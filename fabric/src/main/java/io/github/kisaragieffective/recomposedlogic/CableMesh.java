package io.github.kisaragieffective.recomposedlogic;

import java.util.ArrayList;
import java.util.List;

/** Geometry in block-local coordinates, independent of lighting and camera position. */
final class CableMesh {
    private static final float EPSILON = 0.000001F;

    private CableMesh() {
    }

    static List<Vertex> removeCoveredFaces(List<Vertex> vertices) {
        List<Face> faces = new ArrayList<>();
        for (int start = 0; start < vertices.size(); start += 4) {
            faces.add(Face.of(vertices, start));
        }
        List<Vertex> visible = new ArrayList<>();
        for (int index = 0; index < faces.size(); index++) {
            Face face = faces.get(index);
            boolean covered = false;
            for (int other = 0; other < faces.size() && !covered; other++) {
                covered = index != other && faces.get(other).covers(face);
            }
            if (!covered) {
                visible.addAll(vertices.subList(index * 4, index * 4 + 4));
            }
        }
        return List.copyOf(visible);
    }

    record Vertex(float x, float y, float z, float u, float v, float nx, float ny, float nz) {
    }

    private record Face(int axis, float normal, float plane, float minA, float maxA, float minB, float maxB) {
        static Face of(List<Vertex> vertices, int start) {
            Vertex first = vertices.get(start);
            int axis = Math.abs(first.nx()) > 1 - EPSILON ? 0
                    : Math.abs(first.ny()) > 1 - EPSILON ? 1
                    : Math.abs(first.nz()) > 1 - EPSILON ? 2 : -1;
            if (axis < 0) {
                return new Face(-1, 0, 0, 0, 0, 0, 0);
            }
            float plane = coordinate(first, axis);
            float minA = Float.POSITIVE_INFINITY;
            float maxA = Float.NEGATIVE_INFINITY;
            float minB = Float.POSITIVE_INFINITY;
            float maxB = Float.NEGATIVE_INFINITY;
            for (int offset = 0; offset < 4; offset++) {
                Vertex vertex = vertices.get(start + offset);
                if (Math.abs(coordinate(vertex, axis) - plane) > EPSILON) {
                    return new Face(-1, 0, 0, 0, 0, 0, 0);
                }
                float a = coordinate(vertex, (axis + 1) % 3);
                float b = coordinate(vertex, (axis + 2) % 3);
                minA = Math.min(minA, a);
                maxA = Math.max(maxA, a);
                minB = Math.min(minB, b);
                maxB = Math.max(maxB, b);
            }
            // Bounding rectangles only prove occlusion for rectangular, axis-aligned faces.
            int corners = 0;
            for (int offset = 0; offset < 4; offset++) {
                Vertex vertex = vertices.get(start + offset);
                float a = coordinate(vertex, (axis + 1) % 3);
                float b = coordinate(vertex, (axis + 2) % 3);
                int cornerA = Math.abs(a - minA) < EPSILON ? 0 : Math.abs(a - maxA) < EPSILON ? 1 : -1;
                int cornerB = Math.abs(b - minB) < EPSILON ? 0 : Math.abs(b - maxB) < EPSILON ? 1 : -1;
                if (cornerA < 0 || cornerB < 0) {
                    return new Face(-1, 0, 0, 0, 0, 0, 0);
                }
                corners |= 1 << (cornerA * 2 + cornerB);
            }
            if (corners != 15) {
                return new Face(-1, 0, 0, 0, 0, 0, 0);
            }
            float normal = axis == 0 ? first.nx() : axis == 1 ? first.ny() : first.nz();
            return new Face(axis, normal, plane, minA, maxA, minB, maxB);
        }

        boolean covers(Face other) {
            return axis >= 0 && axis == other.axis && normal * other.normal < -1 + EPSILON
                    && Math.abs(plane - other.plane) < EPSILON
                    && minA <= other.minA + EPSILON && maxA >= other.maxA - EPSILON
                    && minB <= other.minB + EPSILON && maxB >= other.maxB - EPSILON;
        }

        private static float coordinate(Vertex vertex, int axis) {
            return axis == 0 ? vertex.x() : axis == 1 ? vertex.y() : vertex.z();
        }
    }
}
