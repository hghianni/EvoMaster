package org.evomaster.client.java.instrumentation;

import com.foo.somedifferentpackage.examples.entity.EntityX;
import com.foo.somedifferentpackage.examples.entity.EntityY;
import org.evomaster.client.java.instrumentation.staticstate.UnitsInfoRecorder;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class ClassAnalyzerTest {

    @Test
    void testDoAnalyze() {

        UnitsInfoRecorder.reset();

        ClassAnalyzer.doAnalyze(Arrays.asList(
                EntityX.class.getName(),
                EntityY.class.getName()
        ));

        List<JpaConstraint> jpa = UnitsInfoRecorder.getInstance().getJpaConstraints();
        assertTrue(jpa.size() > 0);

        List<JpaConstraint> x = jpa.stream().filter(j -> j.getTableName().equals("EntityX")).collect(Collectors.toList());
        List<JpaConstraint> y = jpa.stream().filter(j -> j.getTableName().equals("BAR")).collect(Collectors.toList());

        assertEquals(2, x.size());
        assertEquals(4, y.size());

        assertTrue(x.stream().anyMatch(j -> j.getColumnName().equals("y") && !j.getNullable()));
        assertTrue(x.stream().anyMatch(j -> j.getColumnName().equals("k") && !j.getNullable()));

        assertTrue(y.stream().anyMatch(j -> j.getColumnName().equals("x") && !j.getNullable()));
        assertTrue(y.stream().anyMatch(j -> j.getColumnName().equals("foo") && !j.getNullable()));
        assertTrue(y.stream().anyMatch(j -> j.getColumnName().equals("hello") && !j.getNullable()));
        assertTrue(y.stream().anyMatch(j -> j.getColumnName().equals("k") && !j.getNullable()));
    }
}