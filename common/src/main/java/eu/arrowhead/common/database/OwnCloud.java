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

/**
 * JPA entity class for storing <tt>OwnCloud</tt> information in the database. The <i>cloud_id</i> column must be unique.
 * <p>
 * The database table belonging to this class stores the information about the Local Cloud, meaning this table should only have 1 entry at all times.
 * The information in this table is used during Global Service Discovery, Inter-Cloud Negotiations (by the Gatekeeper) and token generation (by the
 * Authorization).
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
  @JoinColumn(name = "cloud_id")
  @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)

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

  public boolean isValid() {
    return cloud != null && cloud.isValid();
  }

}
