package eu.arrowhead.common.database;

import eu.arrowhead.common.model.ArrowheadCloud;
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
 * JPA entity class for storing <tt>NeighborCloud</tt> information in the database. The <i>cloud_id</i> column must be unique.
 * <p>
 * The database table belonging to this class is called <i>neighborhood</i>, which is a subset of the {@link eu.arrowhead.common.model.ArrowheadCloud}
 * table. If an <tt>ArrowheadCloud</tt> can also be found in the <tt>neighborhood</tt> table, that means it is a trusted <tt>ArrowheadCloud</tt>,
 * which can be queried during a Global Service Discovery, Inter-Cloud Negotiations (by the Gatekeeper) and token generation (by the Authorization).
 *
 * @author Umlauf Zoltán
 * @see eu.arrowhead.common.model.ArrowheadCloud
 * @see eu.arrowhead.common.model.messages.GSDPoll
 * @see eu.arrowhead.common.model.messages.ICNProposal
 */
@Entity
@Table(name = "neighborhood", uniqueConstraints = {@UniqueConstraint(columnNames = {"cloud_id"})})
public class NeighborCloud implements Serializable {

  @Id
  @JoinColumn(name = "cloud_id")
  @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
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

  public boolean isValid() {
    return cloud != null && cloud.isValid();
  }

}
