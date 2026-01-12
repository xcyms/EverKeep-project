package org.xcyms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.xcyms.entity.Role;
import org.xcyms.mapper.RoleMapper;
import org.xcyms.service.IRoleService;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author liu-xu
 * @since 2026-01-12
 */
@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements IRoleService {

}
