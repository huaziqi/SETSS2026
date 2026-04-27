package com.LHZ.SETSS2026.service;

import com.LHZ.SETSS2026.dto.UserAdminDTO;
import com.LHZ.SETSS2026.entity.Manuscript;
import com.LHZ.SETSS2026.entity.Role;
import com.LHZ.SETSS2026.entity.User;
import com.LHZ.SETSS2026.repository.ManuRepository;
import com.LHZ.SETSS2026.repository.RoleRepository;
import com.LHZ.SETSS2026.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChairService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ManuRepository manuRepository;

    // 查询所有用户（主席可以查看所有用户）
    public List<UserAdminDTO> listAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserAdminDTO::fromEntityForChair)
                .toList();
    }

    // 禁用用户
    @Transactional
    public void disableUser(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        user.setEnable(false);
        userRepository.save(user);
    }

    // 启用用户
    @Transactional
    public void enableUser(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        user.setEnable(true);
        userRepository.save(user);
    }

    // 分配角色（主席可以分配所有角色，包括ADMIN、REVIEWER等）
    @Transactional
    public void assignRoleToUser(Integer userId, Integer roleId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        Role targetRole = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("角色不存在"));

        user.setRole(targetRole);
        userRepository.save(user);
    }

    // 分配稿件给审稿员
    @Transactional
    public Manuscript assignManuscript(Integer manuscriptId, Integer reviewerId) {
        Manuscript manuscript = manuRepository.findById(manuscriptId)
                .orElseThrow(() -> new RuntimeException("稿件不存在"));

        if (!"待审核".equals(manuscript.getStatus())) {
            throw new RuntimeException("只能分配状态为'待审核'的稿件");
        }

        // 验证审稿员是否存在且角色正确
        User reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() -> new RuntimeException("审稿员不存在"));

        if (reviewer.getRole() == null || !"ROLE_REVIEWER".equals(reviewer.getRole().getName())) {
            throw new RuntimeException("指定的用户不是审稿员");
        }

        manuscript.setStatus("已分配");
        manuscript.setReviewerId(reviewerId);
        manuscript.setReviewer(reviewer.getName());
        manuscript.setUpdateTime(LocalDateTime.now());

        return manuRepository.save(manuscript);
    }

    // 查询所有待审核的稿件
    public List<Manuscript> getPendingManuscripts() {
        return manuRepository.findByStatus("待审核");
    }

    // 查询所有已分配的稿件
    public List<Manuscript> getAssignedManuscripts() {
        return manuRepository.findByStatus("已分配");
    }

    // 查询某审稿员的所有稿件
    public List<Manuscript> getManuscriptsByReviewerId(Integer reviewerId) {
        return manuRepository.findByReviewerId(reviewerId);
    }
}