package com.example.eduaceai.repository.specification;

import com.example.eduaceai.dto.req.UserFilterForm;
import com.example.eduaceai.entity.User;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class UserSpecification {

    public static Specification<User> filterUsers(UserFilterForm form) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (form.search() != null && !form.search().isEmpty()) {
                String pattern = "%" + form.search().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("fullName")), pattern),
                        cb.like(cb.lower(root.get("studentCode")), pattern)
                ));
            }

            if (form.role() != null && !form.role().isEmpty()) {
                predicates.add(cb.equal(root.get("role"), User.Role.valueOf(form.role().toUpperCase())));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}