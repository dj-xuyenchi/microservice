package com.erp.authenservice.dto.request.roleapply;

import com.erp.vo.BaseFilter;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class RoleApplyFilter extends BaseFilter {
    private List<Long> roleId;
}
