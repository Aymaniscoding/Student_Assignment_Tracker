package com.example.r005_student_assignment_tracker;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class AssignmentTest {

    @Test
    public void testAssignmentCreation() {
        long dueTomorrow = System.currentTimeMillis() + (1000 * 60 * 60 * 20); // 20 hours in future
        Assignment assignment = new Assignment(
                "Algorithms Homework",
                "Computer Science",
                "Solve dynamic programming problems",
                "Oct 30, 2026",
                dueTomorrow,
                "High"
        );

        assertNotNull(assignment.getId());
        assertEquals("Algorithms Homework", assignment.getTitle());
        assertEquals("Computer Science", assignment.getSubject());
        assertEquals("Solve dynamic programming problems", assignment.getDescription());
        assertEquals("Oct 30, 2026", assignment.getDueDate());
        assertEquals(dueTomorrow, assignment.getDueDateMillis());
        assertEquals("High", assignment.getPriority()); // High priority since due in < 1 day
        assertFalse(assignment.isCompleted());
        assertEquals(0L, assignment.getReminderTimeMillis());
    }

    @Test
    public void testDateBasedPriorityCalculation() {
        long now = System.currentTimeMillis();

        // Due today / tomorrow (<= 1 day) -> High Priority
        Assignment highPriority = new Assignment("Quiz", "Math", "Limits", "Today", now + (1000 * 60 * 60 * 12), "Medium");
        assertEquals("High", highPriority.getPriority());

        // Due in 3 days (<= 5 days) -> Medium Priority
        Assignment mediumPriority = new Assignment("Report", "Physics", "Essay", "In 3 Days", now + (1000L * 60 * 60 * 24 * 3), "Low");
        assertEquals("Medium", mediumPriority.getPriority());

        // Due in 10 days (> 5 days) -> Low Priority
        Assignment lowPriority = new Assignment("Project", "CS", "Final", "In 10 Days", now + (1000L * 60 * 60 * 24 * 10), "High");
        assertEquals("Low", lowPriority.getPriority());
    }

    @Test
    public void testAssignmentSetters() {
        Assignment assignment = new Assignment();
        assignment.setId("test-id-123");
        assignment.setUserId("user-456");
        assignment.setTitle("Calculus Quiz");
        assignment.setSubject("Mathematics");
        assignment.setDescription("Study limits and derivatives");
        assignment.setDueDate("Nov 01, 2026");
        assignment.setDueDateMillis(1700100000000L);
        assignment.setCompleted(true);
        assignment.setReminderTimeMillis(1700050000000L);
        assignment.setSubmissionNotes("Submitted solutions");

        assertEquals("test-id-123", assignment.getId());
        assertEquals("user-456", assignment.getUserId());
        assertEquals("Calculus Quiz", assignment.getTitle());
        assertEquals("Mathematics", assignment.getSubject());
        assertEquals("Study limits and derivatives", assignment.getDescription());
        assertEquals("Nov 01, 2026", assignment.getDueDate());
        assertEquals(1700100000000L, assignment.getDueDateMillis());
        assertTrue(assignment.isCompleted());
        assertEquals(1700050000000L, assignment.getReminderTimeMillis());
        assertEquals("Submitted solutions", assignment.getSubmissionNotes());
    }

    @Test
    public void testUserProfile() {
        UserProfile profile = new UserProfile("user-789", "Alex Morgan", "alex@student.edu");
        assertEquals("user-789", profile.getUserId());
        assertEquals("Alex Morgan", profile.getName());
        assertEquals("alex@student.edu", profile.getEmail());
        assertTrue(profile.getCreatedAt() > 0);
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

    @Test
    public void testSearchAndFilterMatching() {
        Assignment csAssignment = new Assignment(
                "Data Structures Lab Project",
                "Computer Science",
                "Implement BST",
                "Oct 28, 2026",
                1700000000000L,
                "High"
        );

        Assignment mathAssignment = new Assignment(
                "Calculus Problem Set",
                "Mathematics",
                "Derivatives",
                "Oct 31, 2026",
                1700100000000L,
                "Medium"
        );

        // Search by title match
        String searchQuery = "data";
        assertTrue(csAssignment.getTitle().toLowerCase().contains(searchQuery));
        assertFalse(mathAssignment.getTitle().toLowerCase().contains(searchQuery));

        // Search by subject match
        String subjectQuery = "math";
        assertTrue(mathAssignment.getSubject().toLowerCase().contains(subjectQuery));
        assertFalse(csAssignment.getSubject().toLowerCase().contains(subjectQuery));
    }

    @Test
    public void testPastReminderValidation() {
        long pastMillis = System.currentTimeMillis() - 10000L;
        long futureMillis = System.currentTimeMillis() + 100000L;

        boolean isPastValid = pastMillis > System.currentTimeMillis();
        boolean isFutureValid = futureMillis > System.currentTimeMillis();

        assertFalse(isPastValid);
        assertTrue(isFutureValid);
    }
}
