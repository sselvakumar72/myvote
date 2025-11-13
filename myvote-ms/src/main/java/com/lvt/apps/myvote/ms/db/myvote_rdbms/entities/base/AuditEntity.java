package com.lvt.apps.myvote.ms.db.myvote_rdbms.entities.base;

import jakarta.persistence.Column;

import java.sql.Timestamp;

/**
 * Base class for audit fields in database entities.
 */
abstract class AuditEntity {

    @Column(name = "created_by", nullable = false, updatable = false)
    private Long l_created_by;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Timestamp t_created_at;

    @Column(name = "last_updated_by, nullable = false, updatable = false)")
    private Long l_last_updated_by;

    @Column(name = "last_updated_at, nullable = false, updatable = false)")
    private Timestamp t_last_updated_by;
}
