package io.github.kisaragieffective.recomposedlogic;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CableMeshTest {
    @Test
    void removesOpposingFacesAtTheSamePlane() {
        List<CableMesh.Vertex> mesh = pair(0, 1, 0, 1, 0);

        List<CableMesh.Vertex> visible = CableMesh.removeCoveredFaces(mesh);

        assertEquals(0, visible.size());
    }

    @Test
    void retainsFacesSeparatedByAGap() {
        List<CableMesh.Vertex> mesh = pair(0, 1, 0, 1, 0.1F);

        List<CableMesh.Vertex> visible = CableMesh.removeCoveredFaces(mesh);

        assertEquals(mesh, visible);
    }

    @Test
    void retainsPartiallyOverlappingFaces() {
        List<CableMesh.Vertex> mesh = pair(0, 1, 0.5F, 1.5F, 0);

        List<CableMesh.Vertex> visible = CableMesh.removeCoveredFaces(mesh);

        assertEquals(mesh, visible);
    }

    @Test
    void onlyRemovesTheFullyCoveredFace() {
        List<CableMesh.Vertex> mesh = pair(0, 1, 0.25F, 0.75F, 0);

        List<CableMesh.Vertex> visible = CableMesh.removeCoveredFaces(mesh);

        assertEquals(mesh.subList(0, 4), visible);
    }

    @Test
    void retainsFacesWithTheSameNormal() {
        List<CableMesh.Vertex> mesh = new ArrayList<>(face(0, 1, 0, 1));
        mesh.addAll(face(0, 1, 0, 1));

        List<CableMesh.Vertex> visible = CableMesh.removeCoveredFaces(mesh);

        assertEquals(mesh, visible);
    }

    private static List<CableMesh.Vertex> pair(float firstMin, float firstMax, float secondMin, float secondMax, float gap) {
        List<CableMesh.Vertex> mesh = new ArrayList<>(face(firstMin, firstMax, 0, 1));
        mesh.addAll(face(secondMin, secondMax, gap, -1));
        return mesh;
    }

    private static List<CableMesh.Vertex> face(float min, float max, float z, float normal) {
        return List.of(
                new CableMesh.Vertex(min, min, z, 0, 0, 0, 0, normal),
                new CableMesh.Vertex(max, min, z, 1, 0, 0, 0, normal),
                new CableMesh.Vertex(max, max, z, 1, 1, 0, 0, normal),
                new CableMesh.Vertex(min, max, z, 0, 1, 0, 0, normal));
    }
}
