package com.volunteer.management.event;

import com.volunteer.management.shift.Shift;
import com.volunteer.management.skill.Skill;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds a dynamic WHERE clause for event browsing based on whichever
 * filters were actually provided. Each static method returns a small,
 * independent condition; EventController combines only the ones that apply.
 */
public class EventSpecification {

    public static Specification<Event> hasStatus(EventStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    /**
     * Matches events that have at least one shift requiring the given skill name.
     */
    public static Specification<Event> requiresSkill(String skillName) {
        return (root, query, cb) -> {
            if (skillName == null || skillName.isBlank()) {
                return null; // null predicate = "no filter applied here"
            }
            query.distinct(true); // avoid duplicate Event rows from the join
            Join<Event, Shift> shifts = root.join("shifts");
            Join<Shift, Skill> skills = shifts.join("requiredSkills");
            return cb.equal(cb.lower(skills.get("name")), skillName.toLowerCase());
        };
    }

    /** Matches events that have at least one shift starting on/after `from`. */
    public static Specification<Event> shiftFrom(Instant from) {
        return (root, query, cb) -> {
            if (from == null)
                return null;
            query.distinct(true);
            Join<Event, Shift> shifts = root.join("shifts");
            return cb.greaterThanOrEqualTo(shifts.get("startTime"), from);
        };
    }

    /** Matches events that have at least one shift starting on/before `to`. */
    public static Specification<Event> shiftTo(Instant to) {
        return (root, query, cb) -> {
            if (to == null)
                return null;
            query.distinct(true);
            Join<Event, Shift> shifts = root.join("shifts");
            return cb.lessThanOrEqualTo(shifts.get("startTime"), to);
        };
    }

    public static Specification<Event> buildFilter(EventStatus status, String skill, Instant from, Instant to) {
        List<Specification<Event>> specs = new ArrayList<>();
        specs.add(hasStatus(status));
        specs.add(requiresSkill(skill));
        specs.add(shiftFrom(from));
        specs.add(shiftTo(to));

        return specs.stream()
                .reduce(Specification.where(null), Specification::and);
    }
}