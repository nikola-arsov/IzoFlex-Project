package bg.softuni.service.interf;

import bg.softuni.model.entity.Role;

import java.util.List;

public interface RoleService {
    List<String> getAllRoles();

    List<Role> getRolesByNames(List<String> roles);
}
