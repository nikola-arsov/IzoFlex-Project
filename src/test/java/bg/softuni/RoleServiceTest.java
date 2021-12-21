package bg.softuni;

import bg.softuni.model.entity.Role;
import bg.softuni.repository.RoleRepository;
import bg.softuni.service.impl.RoleServiceImpl;
import bg.softuni.service.interf.RoleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RoleServiceTest {
    @Mock
    private RoleRepository roleRepository;
    private RoleService roleService;

    @BeforeEach
    public void init() {
        this.roleService = new RoleServiceImpl(roleRepository);
    }

    @Test
    public void testGetAllRoles() {
        when(roleRepository.findAll()).thenReturn(List.of(new Role("ONE"), new Role("TWO")));
        List<String> result = this.roleService.getAllRoles();

        assertEquals("ONE", result.get(0));
        assertEquals("TWO", result.get(1));
    }

    @Test
    void testGetRolesByNames() {
        when(roleRepository.findByName(anyString())).thenReturn(Optional.of(new Role("TEST")));
        List<Role> result = this.roleService.getRolesByNames(List.of("TEST"));

        assertEquals("TEST", result.get(0).getName());
    }
    @Test
    void testGetRolesByNamesError() {
        when(roleRepository.findByName(anyString())).thenReturn(Optional.empty());
        assertThrows(IllegalStateException.class,()->this.roleService.getRolesByNames(List.of("TEST")));
    }
}