package com.example.r005_student_assignment_tracker;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class AssignmentTest {

    @Test
    public void testAssignmentCreation() {
        Assignment assignment = new Assignment(
                "Algorithms Homework",
                "Computer Science",
                "Solve dynamic programming problems",
                "Oct 30, 2026",
                1700000000000L,
                "High"
        );

        assertNotNull(assignment.getId());
        assertEquals("Algorithms Homework", assignment.getTitle());
        assertEquals("Computer Science", assignment.getSubject());
        assertEquals("Solve dynamic programming problems", assignment.getDescription());
        assertEquals("Oct 30, 2026", assignment.getDueDate());
        assertEquals(1700000000000L, assignment.getDueDateMillis());
        assertEquals("High", assignment.getPriority());
        assertFalse(assignment.isCompleted());
    }

    @Test
    public void testAssignmentSetters() {
        Assignment assignment = new Assignment();
        assignment.setId("test-id-123");
        assignment.setTitle("Calculus Quiz");
        assignment.setSubject("Mathematics");
        assignment.setDescription("Study limits and derivatives");
        assignment.setDueDate("Nov 01, 2026");
        assignment.setDueDateMillis(1700100000000L);
        assignment.setPriority("Medium");
        assignment.setCompleted(true);

        assertEquals("test-id-123", assignment.getId());
        assertEquals("Calculus Quiz", assignment.getTitle());
        assertEquals("Mathematics", assignment.getSubject());
        assertEquals("Study limits and derivatives", assignment.getDescription());
        assertEquals("Nov 01, 2026", assignment.getDueDate());
        assertEquals(1700100000000L, assignment.getDueDateMillis());
        assertEquals("Medium", assignment.getPriority());
        assertTrue(assignment.isCompleted());
    }

    @Test
    public void testToggleCompletionState() {
        Assignment assignment = new Assignment();
        assertFalse(assignment.isCompleted());

        assignment.setCompleted(true);
        assertTrue(assignment.isCompleted());

        assignment.setCompleted(false);
        assertFalse(assignment.isCompleted());
    }
}
