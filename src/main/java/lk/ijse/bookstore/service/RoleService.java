package lk.ijse.bookstore.service;

import lk.ijse.bookstore.entity.Role;
import lk.ijse.bookstore.enumiration.RoleType;

public interface RoleService {
    Role getOrCreate(RoleType roleType);
}
