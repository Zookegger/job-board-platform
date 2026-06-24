package com.yoedu.job_board_platform.mappers;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.yoedu.job_board_platform.dtos.admin.AdminSkillResponse;
import com.yoedu.job_board_platform.dtos.skill.SkillRequest;
import com.yoedu.job_board_platform.dtos.skill.SkillResponse;
import com.yoedu.job_board_platform.models.Skill;

@Mapper(componentModel = "spring")
public interface SkillMapper {

    /**
     * Chuyển đổi {@link SkillRequest} thành entity {@link Skill}.
     * Trường {@code id} được bỏ qua (sẽ do DB tự sinh).
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Skill toEntity(SkillRequest request);

    /**
     * Chuyển đổi entity {@link Skill} thành {@link SkillResponse}.
     * Ánh xạ trường {@code active} (boolean Java) sang {@code isActive} trong response.
     */
    @Mapping(target = "isActive", source = "active")
    SkillResponse toResponse(Skill skill);

    @Mapping(target = "isActive", source = "active")
    AdminSkillResponse toAdminResponse(Skill skill);

    /**
     * Cập nhật entity {@link Skill} từ {@link SkillRequest}.
     * Chỉ cập nhật các trường không null; bỏ qua {@code id} và {@code active}.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(SkillRequest request, @MappingTarget Skill skill);
}
