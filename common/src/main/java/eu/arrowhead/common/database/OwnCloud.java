/*
 *  Copyright (c) 2018 AITIA International Inc.
 *
 *  This work is part of the Productive 4.0 innovation project, which receives grants from the
 *  European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 *  (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 *  national funding authorities from involved countries.
 */

package eu.arrowhead.common.database;

import eu.arrowhead.common.messages.GSDPoll;
import eu.arrowhead.common.messages.ICNProposal;
import java.io.Serializable;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.Valid;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

/**
 * JPA entity class for storing <tt>OwnCloud</tt> information in the database. The <i>cloud_id</i> column must be unique. <p> The database table
 * belonging to this class stores the information about the Local Cloud, meaning this table should only have 1 entry at all times. The information in
 * this table is used during Global Service Discovery, Inter-Cloud Negotiations (by the Gatekeeper) and token generation (by the Authorization).
 *
 * @author Umlauf Zoltán
 * @see ArrowheadCloud
 * @see GSDPoll
 * @see ICNProposal
 */
@Entity
@Table(name = "own_cloud", uniqueConstraints = {@UniqueConstraint(columnNames = {"cloud_id"})})
public class OwnCloud implements Serializable {

  @Id
  @Valid
  @JoinColumn(name = "cloud_id")
  @OneToOne(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @OnDelete(action = OnDeleteAction.CASCADE)
  private ArrowheadCloud cloud;

  public OwnCloud() {
  }

  public OwnCloud(ArrowheadCloud cloud) {
    this.cloud = cloud;
  }

  public ArrowheadCloud getCloud() {
    return cloud;
  }

  public void setCloud(ArrowheadCloud cloud) {
    this.cloud = cloud;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof OwnCloud)) {
      return false;
    }

    OwnCloud ownCloud = (OwnCloud) o;

    return cloud != null ? cloud.equals(ownCloud.cloud) : ownCloud.cloud == null;
  }

  @Override
  public int hashCode() {
    return cloud != null ? cloud.hashCode() : 0;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("OwnCloud{");
    sb.append("cloud=").append(cloud);
    sb.append('}');
    return sb.toString();
  }
}
