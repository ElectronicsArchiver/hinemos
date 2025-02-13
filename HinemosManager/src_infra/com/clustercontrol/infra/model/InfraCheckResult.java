/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.infra.model;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;

@XmlType(namespace = "http://infra.ws.clustercontrol.com")
@Entity
@Table(name="cc_infra_check_result_info", schema="setting")
@Cacheable(true)
public class InfraCheckResult {
	private InfraCheckResultPK id;
	private Integer result;

	public InfraCheckResult() {
	}

	public InfraCheckResult(String managementId, String moduleId, String facilityId) {
		this.setId(new InfraCheckResultPK(managementId, moduleId, facilityId));
	}
	
	@XmlTransient
	@EmbeddedId
	public InfraCheckResultPK getId() {
		if (id == null)
			id = new InfraCheckResultPK();
		return id;
	}
	public void setId(InfraCheckResultPK id) {
		this.id = id;
	}

	@Transient
	public String getManagementId() {
		return getId().getManagementId();
	}
	public void setManagementId(String managementId) {
		getId().setManagementId(managementId);
	}
	
	@Transient
	public String getModuleId() {
		return getId().getModuleId();
	}
	public void setModuleId(String moduleId) {
		getId().setModuleId(moduleId);
	}
	
	@Transient
	public String getNodeId() {
		return getId().getNodeId();
	}
	public void setNodeId(String nodeId) {
		getId().setNodeId(nodeId);
	}

	@Column(name="result")
	public Integer getResult() {
		return result;
	}
	public void setResult(Integer result) {
		this.result = result;
	}
	
	public void removeSelf() {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			em.remove(this);
		}
	}
}
