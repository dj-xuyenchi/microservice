package com.erp.vo;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BaseFilter {
    private Integer pageNumber;
    private Integer pageSize;
    private String keyword;
}
