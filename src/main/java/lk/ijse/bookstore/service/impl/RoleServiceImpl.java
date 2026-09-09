package lk.ijse.bookstore.service.impl;

import lk.ijse.bookstore.entity.Role;
import lk.ijse.bookstore.enumiration.RoleType;
import lk.ijse.bookstore.repository.RoleRepository;
import lk.ijse.bookstore.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    @Override
    public Role getOrCreate(RoleType roleType) {
        return roleRepository.findByRoleName(roleType)
                .orElseGet(() -> {
                    log.info("Creating missing role {}", roleType);
                    return roleRepository.save(Role.builder().roleName(roleType).build());
                });
    }

}


