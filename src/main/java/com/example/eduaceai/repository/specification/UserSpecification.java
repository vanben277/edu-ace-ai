package com.example.eduaceai.repository.specification;

import com.example.eduaceai.dto.req.UserFilterForm;
import com.example.eduaceai.entity.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class UserSpecification {

    public static Specification<User> filterUsers(UserFilterForm form) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Lọc theo từ khóa (Tên hoặc Mã sinh viên)
            if (form.search() != null && !form.search().isEmpty()) {
                String pattern = "%" + form.search().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("fullName")), pattern),
                        cb.like(cb.lower(root.get("studentCode")), pattern)
                ));
            }

            // Lọc theo Vai trò (ADMIN/STUDENT)
            if (form.role() != null && !form.role().isEmpty()) {
                predicates.add(cb.equal(root.get("role"), User.Role.valueOf(form.role().toUpperCase())));
            }

            // Lọc theo Trạng thái (Bị khóa hoặc Hoạt động)
            // Nếu form.enabled() là null thì sẽ lấy tất cả (không lọc)
            if (form.enabled() != null) {
                predicates.add(cb.equal(root.get("enabled"), form.enabled()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}