package com.agricolacertini.application.rest;

/**
 * Base REST controller providing common pagination defaults and utilities.
 */
public abstract class BaseRestController {
    private static final int DEFAULT_PAGE_NUMBER = 0;
    private static final int DEFAULT_PAGE_SIZE = 10;

    public Integer getPageNumber(Integer pageNumberRequested) {
        return pageNumberRequested == null ? DEFAULT_PAGE_NUMBER : pageNumberRequested;
    }

    public Integer getPageSize(Integer pageSizeRequested) {
        return pageSizeRequested == null ? DEFAULT_PAGE_SIZE : pageSizeRequested;
    }
}
