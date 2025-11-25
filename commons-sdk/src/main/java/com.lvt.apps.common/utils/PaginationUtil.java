package com.lvt.apps.common.utils;

import com.lvt.apps.common.exceptions.InvalidPageSizeException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public class PaginationUtil {

    private static final Integer DEFAULT_PAGE_NUMBER = 0;
    private static final Integer MIN_PAGE_NUMBER = 0;

    private static final Integer DEFAULT_PAGE_SIZE = 10;
    private static final Integer MIN_PAGE_SIZE = 1;
    private static final Integer MAX_PAGE_SIZE = 100;

    public static Pageable getPageable(Integer pageNumber, Integer pageSize) {
        if (pageNumber == null) {
            pageNumber = DEFAULT_PAGE_NUMBER;
        } else if (pageNumber < MIN_PAGE_NUMBER) {
            throw new InvalidPageSizeException("Invalid page number: " + pageNumber);
        }

        if (pageSize == null) {
            pageSize = DEFAULT_PAGE_SIZE;
        } else if (pageSize < MIN_PAGE_SIZE || pageSize > MAX_PAGE_SIZE) {
            throw new InvalidPageSizeException("Invalid page size: " + pageSize);
        }

        return PageRequest.of(pageNumber, pageSize);
    }

}
