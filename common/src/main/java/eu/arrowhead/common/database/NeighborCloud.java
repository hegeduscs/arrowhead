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
 * JPA entity class for storing <tt>NeighborCloud</tt> information in the database. The <i>cloud_id</i> column must be unique. <p> The database table
 * belonging to this class is a subset of the {@link ArrowheadCloud} table. If an <tt>ArrowheadCloud</tt> can also be found in the
 * <tt>NeighborCloud</tt> table, that means it is a trusted <tt>ArrowheadCloud</tt>, which can be queried during a Global Service Discovery,
 * Inter-Cloud Negotiations (by the Gatekeeper) and token generation (by the Authorization).
 *
 * @author Umlauf Zoltán
 * @see ArrowheadCloud
 * @see GSDPoll
 * @see ICNProposal
 */
@Entity
@Table(name = "neighbor_cloud", uniqueConstraints = {@UniqueConstraint(columnNames = {"cloud_id"})})
public class NeighborCloud implements Serializable {

  @Id
  @Valid
  @JoinColumn(name = "cloud_id")
  @OneToOne(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @OnDelete(action = OnDeleteAction.CASCADE)
  private ArrowheadCloud cloud;

  public NeighborCloud() {
  }

  public NeighborCloud(ArrowheadCloud cloud) {
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
    if (!(o instanceof NeighborCloud)) {
      return false;
    }

    NeighborCloud that = (NeighborCloud) o;

    return cloud.equals(that.cloud);
  }

  @Override
  public int hashCode() {
    return cloud.hashCode();
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("NeighborCloud{");
    sb.append("cloud=").append(cloud);
    sb.append('}');
    return sb.toString();
  }
}
