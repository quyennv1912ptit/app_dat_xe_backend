package  com.nhom15.app_dat_xe.common.config

import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

/** Bật JPA Auditing để @CreatedDate / @LastModifiedDate trong BaseEntity tự điền. */
@Configuration
@EnableJpaAuditing
class JpaAuditingConfig
