package com.uees.studentservices.security;

import com.uees.studentservices.model.Student;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class StudentPrincipal implements UserDetails {

    private final Student student;

    public StudentPrincipal(Student student) {
        this.student = student;
    }

    public Student getStudent() { return student; }

    public UUID getId() { return student.getId(); }

    public String getEmail() { return student.getEmail(); }

    public String getFullName() { return student.getFullName(); }

    public String getProfilePictureUrl() { return student.getProfilePictureUrl(); }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + student.getRole().name()));
    }

    @Override
    public String getPassword() { return student.getPasswordHash(); }

    @Override
    public String getUsername() { return student.getEmail(); }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return student.isActive(); }
}
