package com.lvt.apps.common.db.myvote_rdbms.entities.base;

import jakarta.persistence.Column;

import java.sql.Timestamp;

/**
 * Base class for audit fields in database entities.
 */
abstract class AuditEntity {

    @Column(name = "l_created_by", nullable = false, updatable = false)
    private Long createdBy;

    @Column(name = "t_created_at", nullable = false, updatable = false)
    private Timestamp createdAt;

    @Column(name = "l_last_updated_by", nullable = false, updatable = false)
    private Long lastUpdatedBy;

    @Column(name = "t_last_updated_at", nullable = false, updatable = false)
    private Timestamp lastUpdatedAt;
}
