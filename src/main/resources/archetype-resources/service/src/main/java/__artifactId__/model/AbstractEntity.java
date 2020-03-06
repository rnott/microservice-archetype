#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
/*
 * Copyright 2016 Randy Nott
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ${package}.${artifactId}.model;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;

/**
 * Entity base class providing create and update timestamp functionality.
 */
@MappedSuperclass
@Access(AccessType.FIELD)
public class AbstractEntity {

    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATED")
    private Date createDate;

    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "UPDATED")
    private Date updateDate;

    @Column(name = "CREATED_BY", length = 64)
    private String createdBy;

    @Column(name = "LAST_UPDATED_BY", length = 64)
    private String updatedBy;

    /*
     * JPA uses a version field in your entities to detect concurrent modifications to the same 
     * datastore record. When the JPA runtime detects an attempt to concurrently modify the same 
     * record, it throws an exception to the transaction attempting to commit last.
     * 
     * This <em>DOES NOT</em> prevent user from supplying an outdated instance.
     */
    @Version
    @Column(name = "LOCK", columnDefinition = "integer DEFAULT 0")
    private long lock;

    /*
     * Entity business version. Business logic should compare this value to the submitted
     * value prior to modifying state so that version conflicts can be identified.
     */
    @NotNull
    @Column(name = "version")
    private long version;

    @PrePersist
    void insert() {
        createDate = Date.from( LocalDateTime.now().atZone( ZoneId.systemDefault() ).toInstant() );
        updateDate = createDate;
        updatedBy = createdBy;
        version = 1L;
    }

    @PreUpdate
    void update() {
        updateDate = Date.from( LocalDateTime.now().atZone( ZoneId.systemDefault() ).toInstant() );
        // increment the version
        version += 1;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy( String createdBy ) {
    	this.createdBy = createdBy;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy( String updatedBy ) {
    	this.updatedBy = updatedBy;
    }

    public long getVersion() {
    	return version;
    }

    public void setVersion( long version ) {
    	this.version = version;
    }
}
