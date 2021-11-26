package bg.softuni.service.impl;

import bg.softuni.model.entity.Role;
import bg.softuni.service.interf.RoleService;
import bg.softuni.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoleServiceImpl implements RoleService {
    private final RoleRepository repository;

    @Autowired
    public RoleServiceImpl(RoleRepository repository) {
        this.repository = repository;
    }

    @PostConstruct
    private void init() {
        if (this.repository.count() == 0) {
            this.repository.save(new Role("ROLE_USER"));
            this.repository.save(new Role("ROLE_ADMIN"));
        }
    }


    @Override
    public List<String> getAllRoles() {
        return this.repository.findAll().stream().map(Role::getName).collect(Collectors.toList());
    }

    @Override
    public List<Role> getRolesByNames(List<String> roles) {
        return roles.stream().map(r -> this.repository.findByName(r)
                .orElseThrow(() -> new IllegalStateException("Role with that name doesn't exist!"))).collect(Collectors.toList());
    }
}