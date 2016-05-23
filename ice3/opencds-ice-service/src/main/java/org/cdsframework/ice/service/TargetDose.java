/**
 * Copyright (C) 2016 New York City Department of Health and Mental Hygiene, Bureau of Immunization
 * Contributions by HLN Consulting, LLC
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version. You should have received a copy of the GNU Lesser
 * General Public License along with this program. If not, see <http://www.gnu.org/licenses/> for more
 * details.
 *
 * The above-named contributors (HLN Consulting, LLC) are also licensed by the New York City
 * Department of Health and Mental Hygiene, Bureau of Immunization to have (without restriction,
 * limitation, and warranty) complete irrevocable access and rights to this project.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; THE
 *
 * SOFTWARE IS PROVIDED "AS IS" WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING,
 * BUT NOT LIMITED TO, WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE COPYRIGHT HOLDERS, IF ANY, OR DEVELOPERS BE LIABLE FOR
 * ANY CLAIM, DAMAGES, OR OTHER LIABILITY OF ANY KIND, ARISING FROM, OUT OF, OR IN CONNECTION WITH
 * THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * For more information about this software, see http://www.hln.com/ice or send
 * correspondence to ice@hln.com.
 */

package org.cdsframework.ice.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class TargetDose {
	
	private String uniqueId;
	private String doseId;
	private TargetSeries associatedTargetSeries;
	private Vaccine administeredVaccine;
	private VaccineComponent vaccineComponent;
	private int administeredShotNumberInSeries;
	private Date administrationDate;
	private int doseNumberInSeries;
	private boolean isPrimarySeriesShot;
	private boolean isValid;
	private boolean countsTowardsCompletionOfSeries;
	private boolean hasBeenEvaluated;
	private boolean postEvaluationCheckCompleted;
	private DoseStatus status;
	private HashSet<String> validReasons;
	private HashSet<String> acceptedReasons;
	private HashSet<String> invalidReasons;
	private HashSet<String> doseRulesProcessed;
	
	private static Log logger = LogFactory.getLog(TargetDose.class);

	
	/**
	 * Initialize a TargetDose.
	 * @param pDoseId
	 * @param pVaccineComponentToBeEvaluated
	 * @param pAdministrationDate
	 * @throws IllegalArgumentException if the Dose ID, vaccine or administration date is not populated
	 */
	public TargetDose(String pDoseId, Vaccine pAdministeredVaccine, VaccineComponent pVaccineComponentToBeEvaluated, Date pAdministrationDate, TargetSeries pEncompassingTargetSeries) {	
		
		if (pDoseId == null || pAdministeredVaccine == null || pVaccineComponentToBeEvaluated == null || pAdministrationDate == null || pEncompassingTargetSeries == null) {
			logger.error("TargetDose(): Dose ID, Vaccine, Vaccine Component to be Evaluated, Administration Date not supplied and/or Encompassing TargetSeries not supplied");
			throw new IllegalArgumentException("TargetDose(): Dose ID, Vaccine Component to be Evaluated, Vaccine, Administration Date not supplied and/or Encompassing TargetSeries not supplied");
		}
		uniqueId = ICELogicHelper.generateUniqueString();
		doseId = pDoseId;
		associatedTargetSeries = pEncompassingTargetSeries;
		administeredVaccine = pAdministeredVaccine;
		vaccineComponent = pVaccineComponentToBeEvaluated;
		administeredShotNumberInSeries = 0;
		administrationDate = pAdministrationDate;
		doseNumberInSeries = 1;
		status = DoseStatus.NOT_EVALUATED;
		validReasons = new HashSet<String>();
		acceptedReasons = new HashSet<String>();
		invalidReasons = new HashSet<String>();
		doseRulesProcessed = new HashSet<String>();
		isPrimarySeriesShot = false;
		isValid = false;
		countsTowardsCompletionOfSeries = true;
		hasBeenEvaluated = false;
		postEvaluationCheckCompleted = false;
	}

	public void addDoseRuleProcessed(String ruleName) {
		if (ruleName != null && ! doseRulesProcessed.contains(ruleName)) {
			doseRulesProcessed.add(ruleName);
		}
	}
	
	public boolean containsRuleProcessed(String ruleName) {
		
		return doseRulesProcessed.contains(ruleName);
	}
	
	public boolean containsInvalidReason(String openCdsConceptCode) {
		
		return invalidReasons.contains(openCdsConceptCode);
	}
	
	public boolean containsAcceptedReason(String openCdsConceptCode) {

		return acceptedReasons.contains(openCdsConceptCode);
	}

	public boolean containsValidReason(String openCdsConceptCode) {

		return validReasons.contains(openCdsConceptCode);
	}
	
	public Collection<String> getAllEvaluationReasonsFromAllReasonSets() {
	
		List<String> allReasons = new ArrayList<String>();
		allReasons.addAll(invalidReasons);
		allReasons.addAll(acceptedReasons);
		allReasons.addAll(validReasons);
		
		return allReasons;
	}
	
	public void removeAllEvaluationReasonsFromAllReasonSets() {
		validReasons = new HashSet<String>();
		acceptedReasons = new HashSet<String>();
		invalidReasons = new HashSet<String>();
	}
	
	public void removeEvaluationReasonFromAllReasonSets(String openCdsConceptCode) {
		
		if (openCdsConceptCode != null) {
			invalidReasons.remove(openCdsConceptCode);
			acceptedReasons.remove(openCdsConceptCode);
			validReasons.remove(openCdsConceptCode);
		}
	}
	
	public void removeValidReason(String openCdsConceptCode) {
		
		if (openCdsConceptCode != null) {
			validReasons.remove(openCdsConceptCode);
		}
	}
	
	public void removeAcceptedReason(String openCdsConceptCode) {
		
		if (openCdsConceptCode != null) {
			acceptedReasons.remove(openCdsConceptCode);
		}
	}
	
	public void removeInvalidReason(String openCdsConceptCode) {
		
		if (openCdsConceptCode != null) {
			invalidReasons.remove(openCdsConceptCode);
		}
	}

	public String getUniqueId() {
		return uniqueId;
	}

	public TargetSeries getAssociatedTargetSeries() {
		
		return associatedTargetSeries;
	}
	
	public TargetSeries getTargetSeries() {
		
		return associatedTargetSeries;
	}
	
	public String getDoseId() {
		return doseId;
	}

	public void setDoseId(String doseId) {
		this.doseId = doseId;
	}

	/**
	 * Return administered shot number in series, which is ordered by increasing administration date relative to all other shots in the series, 
	 * not whether it is valid or not
	 * @return administered shot number in series, regardless of whether it is a valid shot or not.
	 */
	public int getAdministeredShotNumberInSeries() {
		return administeredShotNumberInSeries;
	}

	/**
	 * Likely to only be called by the TargetSeries that contains this administered dose
	 * @param administedShotNumber
	 */
	public void setAdministeredShotNumberInSeries(int administeredShotNumber) {
		this.administeredShotNumberInSeries = administeredShotNumber;
	}
	
	/**
	 * Returns the valid shot number in the series (relative to all other valid shots). 
	 * @return valid dose number in series
	 */
	public int getDoseNumberInSeries() {
		return doseNumberInSeries;
	}
	
	/**
	 * Likely to only be called by the TargetSeries that contains this administered dose
	 * @param pDoseNumber
	 */
	public void setDoseNumberInSeries(int pDoseNumber) {
		this.doseNumberInSeries = pDoseNumber;
	}

	/**
	 * Returns whether or not this shot is a part of the primary series
	 */
	public boolean isPrimarySeriesShot() {
		return isPrimarySeriesShot;
	}
	
	public void setIsPrimarySeriesShot(boolean yesno) {
		isPrimarySeriesShot = yesno;
	}
	
	/**
	 * Returns whether this is a valid dose or not. 
	 * @return true of the DoseStatus is either DoseStatus.VALID, false if not
	 */
	public boolean getIsValid() {
		return isValid;
	}

	/**
	 * This method is private; shot validity should be set via setStatus()
	 * @param isValid
	 */
	private void setIsValid(boolean isValid) {
		this.isValid = isValid;
	}

	public boolean countsTowardsCompletionOfSeries() {
		return countsTowardsCompletionOfSeries;
	}

	public void setCountsTowardsCompletionOfSeries(boolean countsTowardsCompletionOfSeries) {
		this.countsTowardsCompletionOfSeries = countsTowardsCompletionOfSeries;
	}

	public boolean hasBeenEvaluated() {
		return hasBeenEvaluated;
	}

	public void setHasBeenEvaluated(boolean hasBeenEvaluated) {
		this.hasBeenEvaluated = hasBeenEvaluated;
	}
	
	public boolean isPostEvaluationCheckCompleted() {
		return postEvaluationCheckCompleted;
	}
	
	public void setPostEvaluationCheckCompleted(boolean truefalse) {
		this.postEvaluationCheckCompleted = truefalse;
	}

	public Vaccine getAdministeredVaccine() {
		return administeredVaccine;
	}
	
	public VaccineComponent getVaccineComponent() {
		return vaccineComponent;
	}

	public void setVaccineComponent(VaccineComponent vaccine) {
		this.vaccineComponent = vaccine;
	}

	public DoseStatus getStatus() {
		return status;
	}

	public void setStatus(DoseStatus status) {

		this.status = status;
		setHasBeenEvaluated(false);
		
		if (status != null) {
			if (status == DoseStatus.ACCEPTED || status == DoseStatus.INVALID || status == DoseStatus.VALID)
				setHasBeenEvaluated(true);
			if (status == DoseStatus.ACCEPTED || status == DoseStatus.VALID) {
				setIsValid(true);
			}
			else {
				setIsValid(false);
			}
		}
	}

	public Date getAdministrationDate() {
		return administrationDate;
	}

	/**
	 * Set the administration date of the shot. If the administration date is null, throw an IllegalArgumentException
	 * @param administrationDate The administration date of the shot
	 */
	public void setAdministrationDate(Date administrationDate) {
		
		String _METHODNAME = "setAdministrationDate(): ";
		if (administrationDate == null) {
			String errStr = "Administration Date not supplied";
			logger.warn(_METHODNAME + errStr);
			throw new IllegalArgumentException(errStr);
		}
		this.administrationDate = administrationDate;
	}

	public Collection<String> getValidReasons() {
		return validReasons;
	}
	
	/**
	 * Adds the valid reason, if not already present
	 * @param reason null reasons are permitted
	 */
	public void addValidReason(String reason) {
		if (reason != null && ! validReasons.contains(reason))
			validReasons.add(reason);
	}

	public Collection<String> getAcceptedReasons() {
		return acceptedReasons;
	}
	
	/**
	 * Adds the accepted reason, if not already present
	 * @param reason
	 */
	public void addAcceptedReason(String reason) {
		
		if (reason != null && ! acceptedReasons.contains(reason))
			acceptedReasons.add(reason);
	}

	public Collection<String> getInvalidReasons() {
		return invalidReasons;
	}
	
	/**
	 * Adds the invalid reason, if not already present
	 * @param reason
	 */
	public void addInvalidReason(String reason) {
		if (reason != null && ! invalidReasons.contains(reason))
			invalidReasons.add(reason);
	}

	@Override
	public String toString() {
		
		String s = "TargetDose [uniqueId=" + uniqueId + ", doseId=" + doseId + ", administeredShotNumber=" + administeredShotNumberInSeries + 
				", doseNumber=" + doseNumberInSeries + ", vaccine=" + administeredVaccine.getCdsConceptName() + 
				", vaccineComponent=" + vaccineComponent.getCdsConceptName() + ", administrationDate=" + administrationDate + ", status=" + status + 
				", isValid=" + isValid;
		int i=0;
		for (String reason : validReasons) {
			if (i == 0)
				s += ", validReasons={\"" + reason + "\"";
			else
				s += "\"" + reason + "\"";
			i++;
		}
		if (i > 0) {
			s += "}";
		}
		i = 0;
		for (String reason : acceptedReasons) {
			if (i == 0)
				s += ", acceptedReasons={\"" + reason + "\"";
			else
				s += "\"" + reason + "\"";
			i++;
		}
		if (i > 0) {
			s += "}";
		}
		i = 0;
		for (String reason : invalidReasons) {
			if (i == 0)
				s += ", invalidReasons={\"" + reason + "\"";
			else
				s += "\"" + reason + "\"";
			i++;
		}
		if (i > 0) {
			s += "}";
		}
		
		s+= "]";
		
		return s;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((uniqueId == null) ? 0 : uniqueId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TargetDose other = (TargetDose) obj;
		if (uniqueId == null) {
			if (other.uniqueId != null)
				return false;
		} else if (!uniqueId.equals(other.uniqueId))
			return false;
		return true;
	}

}