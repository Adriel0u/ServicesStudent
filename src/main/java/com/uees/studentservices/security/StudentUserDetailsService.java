package com.uees.studentservices.security;

import com.uees.studentservices.repository.StudentRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class StudentUserDetailsService implements UserDetailsService {

    private final StudentRepository students;

    public StudentUserDetailsService(StudentRepository students) {
        this.students = students;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return students.findByEmailIgnoreCase(email)
                .map(StudentPrincipal::new)
                .orElseThrow(() -> new UsernameNotFoundException("Estudiante no encontrado: " + email));
    }
}
