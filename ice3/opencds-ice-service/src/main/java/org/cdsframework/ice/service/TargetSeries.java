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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cdsframework.cds.CdsConcept;
import org.cdsframework.ice.service.Recommendation.RecommendationType;
import org.cdsframework.ice.supportingdata.BaseDataEvaluationReason;
import org.cdsframework.ice.supportingdata.BaseDataRecommendationReason;
import org.cdsframework.ice.supportingdata.ICEConceptType;
import org.cdsframework.ice.util.TimePeriod;
import org.cdsframework.ice.util.TimePeriod.DurationType;
import org.cdsframework.ice.util.TimePeriodException;
import org.opencds.common.exceptions.ImproperUsageException;

public class TargetSeries {

	private String targetSeriesIdentifier;
	private Schedule scheduleBackingSeries;
	private SeriesRules seriesRules;
	private NavigableSet<TargetDose> targetDoses;
	private Season targetSeason;
	private boolean seriesComplete;
	private boolean seriesCompleteFlagManuallySet;
	private int seriesCompleteAtDoseNumber;
	private boolean selectedSeries;
	private boolean immunityToAllDiseasesRecorded;
	private boolean historyEvaluationInitiated;
	private boolean postForecastCheckCompleted;
	
	
	// private boolean performForecastCheck;
	private int manuallySetDoseNumberToRecommend;

	private List<Recommendation> interimRecommendationsScheduleEarliest;
	private List<Recommendation> interimRecommendationsScheduleEarliestRecommendedAge;
	private List<Recommendation> interimRecommendationsScheduleEarliestRecommendedInterval;
	private List<Recommendation> interimRecommendationsScheduleLatestRecommendedAge;
	private List<Recommendation> interimRecommendationsScheduleLatestRecommendedInterval;
	private List<Recommendation> interimRecommendationsCustom;
	// private List<Recommendation> interimRecommendationsScheduleLatest;
	private Map<String, Integer> interimEvaluationValidityCountByDisease;					// Disease -> evaluation validity count for disease
	private Map<String, Map<Integer, Integer>> interimDosesToSkipByDisease;					// Disease -> skip dose instructions for disease
	private Map<String, Date> diseaseImmunityDate;											// Disease -> disease immunity date
	private Boolean manuallySetAccountForLiveVirusIntervalsInRecommendation;
	private Vaccine recommendationVaccine;
	private RecommendationStatus recommendationStatus;
	private RecommendationStatus recommendationStatusPrior;
	private Date finalEarliestDate;
	private Date finalRecommendationDate;
	private Date finalLatestRecommendationDate;
	private List<Recommendation> finalRecommendations;
	private List<String> seriesRulesProcessed;

	private static Log logger = LogFactory.getLog(TargetSeries.class);

	
	/**
	 * TargetSeries constructor
	 * @param pSeriesRules SeriesRules parameter, must be provided
	 * @param pScheduleBackingSeries Schedule parameter, must be provided
	 * @throws IllegalArgumentException If SeriesRules or Schedule parameter not populated or improperly populated
	 */
	public TargetSeries(SeriesRules pSeriesRules, Schedule pScheduleBackingSeries) {

		String _METHODNAME = "TargetSeries(SeriesRules, Schedule): ";

		if (pSeriesRules == null || pScheduleBackingSeries == null) {
			String errStr = "SeriesRules and/or Schedule parameter was not supplied";
			logger.warn(_METHODNAME + errStr);
			throw new IllegalArgumentException(errStr);
		}
		
		targetSeriesIdentifier = ICELogicHelper.generateUniqueString();
		scheduleBackingSeries = pScheduleBackingSeries;
		seriesRules = pSeriesRules;
		targetDoses = new TreeSet<TargetDose>(new TargetSeriesComparator()); // Ordered by administration date
		targetSeason = null;
		seriesComplete = false;
		selectedSeries = false;
		seriesCompleteAtDoseNumber = 0;
		postForecastCheckCompleted = false;
		historyEvaluationInitiated = false;
		manuallySetDoseNumberToRecommend = 0;
		immunityToAllDiseasesRecorded = false;
		seriesRulesProcessed = new ArrayList<String>();
		seriesCompleteFlagManuallySet = false;
		interimRecommendationsScheduleEarliest = new ArrayList<Recommendation>();
		interimRecommendationsScheduleEarliestRecommendedAge = new ArrayList<Recommendation>();
		interimRecommendationsScheduleEarliestRecommendedInterval = new ArrayList<Recommendation>();
		interimRecommendationsScheduleLatestRecommendedAge = new ArrayList<Recommendation>();
		interimRecommendationsScheduleLatestRecommendedInterval = new ArrayList<Recommendation>();
		interimRecommendationsCustom = new ArrayList<Recommendation>();
		// interimRecommendationsScheduleLatest = new ArrayList<Recommendation>();

		manuallySetAccountForLiveVirusIntervalsInRecommendation = null;
		recommendationVaccine = null;
		recommendationStatus = RecommendationStatus.NOT_FORECASTED;
		recommendationStatusPrior = null;
		finalRecommendations = new ArrayList<Recommendation>();
		finalEarliestDate = null;
		finalRecommendationDate = null;
		finalLatestRecommendationDate = null;

		interimEvaluationValidityCountByDisease = new HashMap<String, Integer>();
		interimDosesToSkipByDisease = new HashMap<String, Map<Integer, Integer>>();
		Collection<String> targetedDiseases = pScheduleBackingSeries.getDiseasesTargetedByVaccineGroup(pSeriesRules.getVaccineGroup());
		if (targetedDiseases != null) {
			for (String disease : targetedDiseases) {
				interimEvaluationValidityCountByDisease.put(disease, new Integer(0));
				interimDosesToSkipByDisease.put(disease, new HashMap<Integer, Integer>());
			}
		}

		diseaseImmunityDate = new HashMap<String, Date>();
	}

	
	/**
	 * Constructs a TargetSeries for a single invocation. If the Season parameter is not valid (i.e. - is not supported by the underlying SeriesRules), 
	 * an IllegalArgumentException is thrown
	 * @param pSeriesRules
	 * @param pScheduleBackingSeries
	 * @param pTargetSeason
	 */
	public TargetSeries(SeriesRules pSeriesRules, Schedule pScheduleBackingSeries, Season pTargetSeason) {
		
		this(pSeriesRules, pScheduleBackingSeries);

		String _METHODNAME = "TargetSeries(SeriesRules, Schedule, Season): ";
		if (pTargetSeason == null || pTargetSeason.isDefaultSeason()) {
			throw new IllegalArgumentException(_METHODNAME + "Season parameter not specified or specified as a default season (default seasons not permitted)");
		}
		
		boolean foundApplicableSeason = false;
		if (pTargetSeason != null) {
			List<Season> seriesRulesSeasons = this.seriesRules.getSeasons();
			if (seriesRulesSeasons != null) {
				for (Season s : seriesRulesSeasons) {
					if (s.seasonsHaveEquivalentStartAndEndDates(pTargetSeason) || ! s.seasonOverlapsWith(pTargetSeason)) {
						foundApplicableSeason = true;
						break;
					}
				}
				if (foundApplicableSeason)
					this.targetSeason = pTargetSeason;
			}
		}

		if (foundApplicableSeason == false) {
			String errStr = "Season parameter specified is inconsistent with Series supporting data for this TargetSeries";
			logger.warn(_METHODNAME + errStr); 
			throw new IllegalArgumentException(errStr);
		}
	}

	
	public String getTargetSeriesIdentifier() {
		return targetSeriesIdentifier;
	}

	public Schedule getScheduleBackingSeries() {
		return this.scheduleBackingSeries;
	}
	
	public boolean isPostForecastCheckCompleted() {
		return postForecastCheckCompleted;
	}
	
	public void setPostForecastCheckCompleted(boolean postForecastCheckCompleted) {
		this.postForecastCheckCompleted = postForecastCheckCompleted;
	}

	public boolean isHistoryEvaluationInitiated() {
		return historyEvaluationInitiated;
	}
	
	public void setHistoryEvaluationInitiated(boolean truefalse) {
		this.historyEvaluationInitiated = truefalse;
	}
	
	public Vaccine getRecommendationVaccine() {
		return recommendationVaccine;
	}

	public void setRecommendationVaccine(Vaccine recommendationVaccine) {
		this.recommendationVaccine = recommendationVaccine;
	}
	

	/**
	 * Switch the dose rules to follow in this TargetSeries to that of the specified series, starting from the specified dose number. May only
	 * switch the doses to another series within the same vaccine group of this TargetSeries
	 * 
	 * @param seriesToConvertTo
	 * @param doseNumberFromWhichToBeginSwitch
	 * @throws IllegalArgumentException
	 *             If the series does not exist, or the dose number from which
	 *             to switch to does not exist in the specified series
	 */
	// public void convertToSpecifiedSeries(Schedule schedule, String seriesToConvertTo, int doseNumberFromWhichToBeginSwitch)
	public void convertToSpecifiedSeries(String seriesToConvertTo, int doseNumberFromWhichToBeginSwitch)
		throws InconsistentConfigurationException {

		String _METHODNAME = "switchSeries(): ";
		if (seriesToConvertTo == null) {
			String str = _METHODNAME + "series specified is null";
			logger.error(str);
			throw new IllegalArgumentException(str);
		}

		// Get the specified SeriesRules
		SeriesRules srOfSwitchSeries = scheduleBackingSeries.getScheduleSeriesByName(this.seriesRules.getVaccineGroup(), seriesToConvertTo);
		// SeriesRules srOfSwitchSeries = null; // TODO:  (SD) schedule.getScheduleSeriesByName(this.seriesRules.getVaccineGroup(), seriesToConvertTo);
		if (srOfSwitchSeries == null) {
			String str = _METHODNAME + "specified series not found";
			logger.error(str);
			throw new IllegalArgumentException(str);
		}
		if (srOfSwitchSeries.getNumberOfDosesInSeries() < doseNumberFromWhichToBeginSwitch) {
			String str = _METHODNAME + "number of doses in specified series is less than the specified dose number from which to begin the switch";
			logger.error(str);
			throw new IllegalArgumentException(str);
		}
		List<DoseRule> doseRulesOfSwitchSeries = srOfSwitchSeries.getSeriesDoseRules();
		if (doseRulesOfSwitchSeries == null) {
			String str = _METHODNAME + "List of DoseRules in switch series is null";
			logger.error(str);
			throw new InconsistentConfigurationException(str);
		}
		int sizeNewSeries = srOfSwitchSeries.getNumberOfDosesInSeries();
		if (doseRulesOfSwitchSeries.size() != sizeNewSeries) {
			String str = _METHODNAME + "Length of DoseRulesList in switch series does not equal reported size in switch series";
			logger.error(str);
			throw new InconsistentConfigurationException(str);
		}

		// SeriesRules srOfThisSeries = this.seriesRules;
		List<DoseRule> thisSeriesDoseRules = this.seriesRules.getSeriesDoseRules();
		if (thisSeriesDoseRules == null) {
			String str = _METHODNAME + "specified series does not have any dose rules";
			logger.error(str);
			throw new InconsistentConfigurationException(str);
		}
		
		// Remove existing DoseRules starting with the existing dose number
		int size = thisSeriesDoseRules.size();
		thisSeriesDoseRules.subList(doseNumberFromWhichToBeginSwitch - 1, size).clear();
		if (logger.isDebugEnabled()) {
			String debugStr = _METHODNAME + "After removing doseNumber-forward existing DoseRules from this Series, the following DoseRules remain: ";
			for (DoseRule dr : this.seriesRules.getSeriesDoseRules()) {
				debugStr += "(dose #: " + dr.getDoseNumber() + ") absoluteMinimumAge: " + dr.getAbsoluteMinimumAge() + " minAge: " + 
						dr.getMinimumAge() + " absoluteMinimumInterval: " + dr.getAbsoluteMinimumInterval() + " minInterval: " + 
						dr.getMinimumInterval() + " recommendedAge: " + dr.getEarliestRecommendedAge() + " recommendedInterval " + 
						dr.getEarliestRecommendedAge() + "; ";
				logger.debug(debugStr);
			}
		}
		// Add new DoseRules starting with the existing dose number
		List<DoseRule> ssDoseRulesToAdd = srOfSwitchSeries.getSeriesDoseRules().subList(doseNumberFromWhichToBeginSwitch - 1, sizeNewSeries);
		if (logger.isDebugEnabled()) {
			String debugStr = _METHODNAME + "Switch series doses to add: ";
			for (DoseRule dr : ssDoseRulesToAdd) {
				debugStr += "(dose #: " + dr.getDoseNumber() + ") absoluteMinimumAge: " + dr.getAbsoluteMinimumAge() + " minAge: " + 
						dr.getMinimumAge() + " absoluteMinimumInterval: " + dr.getAbsoluteMinimumInterval() + " minInterval: " + dr.getMinimumInterval() + 
						" recommendedAge: "	+ dr.getEarliestRecommendedAge() + " recommendedInterval " + dr.getEarliestRecommendedAge() + "; ";
				logger.debug(debugStr);
			}
		}
		thisSeriesDoseRules.addAll(ssDoseRulesToAdd);
		this.seriesRules.setSeriesDoseRules(thisSeriesDoseRules);
		if (logger.isDebugEnabled()) {
			String debugStr = _METHODNAME + "Final set of DoseRules with switch series doses added: ";
			for (DoseRule dr : this.seriesRules.getSeriesDoseRules()) {
				debugStr += "(dose #: " + dr.getDoseNumber() + ") absoluteMinimumAge: " + dr.getAbsoluteMinimumAge() + " minAge: " + 
						dr.getMinimumAge() + " absoluteMinimumInterval: " + dr.getAbsoluteMinimumInterval() + " minInterval: " + 
						dr.getMinimumInterval() + " recommendedAge: " + dr.getEarliestRecommendedAge() + " recommendedInterval " + 
						dr.getEarliestRecommendedAge() + "; ";
				logger.debug(debugStr);
			}
		}
		
		// Change the name of this series to the new series name
		this.seriesRules.setSeriesName(seriesToConvertTo);
	}

	
	public boolean containsRuleProcessed(String ruleName) {

		return seriesRulesProcessed.contains(ruleName);
	}

	
	/**
	 * Check to see if any of the shots administered was a live virus vaccine
	 * 
	 * @return true if any of the shots administered was a live virus vaccine,
	 *         false if not
	 */
	public boolean oneOrMoreShotsAdministeredIsALiveVirusVaccine() {

		if (targetDoses == null) {
			return false;
		}
		for (TargetDose d : targetDoses) {
			if (d.getVaccineComponent().isLiveVirusVaccine() == true) {
				return true;
			}
		}
		return false;
	}
	

	/**
	 * Check if next recommended shot is a live virus vaccine (and therefore recommendationStatus is either RECOMMENDED, RECOMMENDED_IN_FUTURE, or CONDITIONALLY_RECOMMENDED)
	 * @return true if recommended shot is a live virus vaccine or any vaccine that is in the vaccine group is a live virus vaccine, 
	 * false if recommended shot is not a live virus vaccine, or if not recommendation has been made yet
	 */
	public boolean isRecommendedVaccineOrVaccineGroupLevelRecommendationAnExpectedLiveVirusVaccine() {

		if (this.recommendationStatus != null && (this.recommendationStatus == RecommendationStatus.RECOMMENDED || 
				this.recommendationStatus == RecommendationStatus.RECOMMENDED_IN_FUTURE || this.recommendationStatus == RecommendationStatus.CONDITIONALLY_RECOMMENDED)) {

			int lTargetDoseNumber = determineEffectiveNumberOfDosesInSeries() + 1;
			DoseRule dr = getSeriesRules().getSeriesDoseRuleByDoseNumber(lTargetDoseNumber);
			if (dr == null) {
				return false;
			}
			
			if (manuallySetAccountForLiveVirusIntervalsInRecommendation != null) {
				return manuallySetAccountForLiveVirusIntervalsInRecommendation.booleanValue();
			}
			List<Vaccine> allPermittedVaccinesForThisDose = dr.getAllPermittedVaccines();
			for (Vaccine v : allPermittedVaccinesForThisDose) {
				if (v.isLiveVirusVaccine()) {
					return true;
				}
			}
			return false;
		}
		else {
			return false;
		}
	}

	
	public TargetDose getTargetDoseByAdministeredShotNumber(int shotNumber) {

		if (targetDoses == null)
			return null;

		int i = 1;
		for (TargetDose d : targetDoses) {
			if (i == shotNumber)
				return d;
			i++;
		}
		return null;
	}

	public Date getAdministrationDateOfTargetDoseByShotNumberNumber(int shotNumber) {
		
		TargetDose td = getTargetDoseByAdministeredShotNumber(shotNumber);
		if (td != null) {
			return td.getAdministrationDate();
		}
		else {
			return null;
		}
		
	}
	
	
	/**
	 * Skip the specified number of doses for all diseases associated with the target dose
	 * 
	 * @param doseNumberToSkipFrom
	 * @param doseNumberToSkipTo
	 * @throws IllegalArgumentException If doseNumberToSkipFrom and/or doseNumberToSkipTo is not possible for the series
	 */
	public void addSkipDoseEntryForDose(int doseNumberToSkipFrom, int doseNumberToSkipTo) {
	
		int lTargetDoseNumber = determineDoseNumberInSeries();
		Collection<String> lDiseasesSupportedByThisSeries = getDiseasesSupportedByThisSeries();
		for (String lSDC : lDiseasesSupportedByThisSeries) {
			addSkipDoseEntryForSpecifiedDisease(doseNumberToSkipFrom, doseNumberToSkipTo, lSDC, lTargetDoseNumber);
		}
	}
	
	
	/**
	 * Skip the specified number of doses
	 * 
	 * @param pDoseNumberToSkipFrom
	 * @param pDoseNumberToSkipTo
	 * @param pDisease
	 * @throws IllegalArgumentException If doseNumberToSkipFrom and/or doseNumberToSkipTo is not possible for the series. The doseNumberToSkipFrom must be less than the doseNumberToSkipTo,
	 * and the doseNumberToSkipFrom must equal the target dose number in this series. Also, if the supplied argument is not a known disease as available through the supporting data
	 */
	public void addSkipDoseEntryForSpecifiedDisease(int pDoseNumberToSkipFrom, int pDoseNumberToSkipTo, String pDisease) {
		
		int lTargetDoseNumber = determineDoseNumberInSeries();
		addSkipDoseEntryForSpecifiedDisease(pDoseNumberToSkipFrom, pDoseNumberToSkipTo, pDisease, lTargetDoseNumber);
	}
	
	
	/**
	 * Skip the specified number of doses
	 * 
	 * @param pDoseNumberToSkipFrom
	 * @param pDoseNumberToSkipTo
	 * @param pDisease
	 * @param pTargetDoseNumber If set to <= 0, this method will determine the target dose number of the series to check that the doseNumberToSkipFrom is equal to the target dose number; 
	 * 		otherwise it will use the targetDoseNumber value provided to check
	 * @throws IllegalArgumentException If doseNumberToSkipFrom and/or doseNumberToSkipTo is not possible for the series. The doseNumberToSkipFrom must be less than the doseNumberToSkipTo,
	 * and the doseNumberToSkipFrom must equal the target dose number in this series
	 */
	private void addSkipDoseEntryForSpecifiedDisease(int pDoseNumberToSkipFrom, int pDoseNumberToSkipTo, String pDisease, int pTargetDoseNumber) {

		String _METHODNAME = "skipToDosesNumberForSpecifiedDisease(): ";

		if (pDisease == null) {
			String lErrStr = "No disease was specified in an attempt to add a skip dose entry";
			logger.warn(_METHODNAME + lErrStr);
			throw new IllegalArgumentException(lErrStr);
		}
		
		// HERE - check that it is a valid disease
		if (this.scheduleBackingSeries.getICESupportingDataConfiguration().getSupportedCdsConcepts().
				getCdsListItemAssociatedWithICEConceptTypeAndICEConcept(ICEConceptType.DISEASE, new CdsConcept(pDisease)) == null) {
			String lErrStr = "An invalid disease was specified in an attempt to add a skip dose entry; disease specified: " + pDisease;
			logger.warn(_METHODNAME + lErrStr);
			throw new IllegalArgumentException(lErrStr);
		}
		
		int numberOfDosesInSeries = getSeriesRules().getNumberOfDosesInSeries();
		if (pDoseNumberToSkipTo < 1 || pDoseNumberToSkipTo > numberOfDosesInSeries || pDoseNumberToSkipFrom < 1 || 
				pDoseNumberToSkipFrom > numberOfDosesInSeries) {
			String errStr = "number of doses in series " + seriesRules.getSeriesName() + " is " + numberOfDosesInSeries + "; " + "dose number to skip from (" + 
				pDoseNumberToSkipFrom	+ ") or dose number to skip to (" + pDoseNumberToSkipTo + ") is not valid";
			logger.warn(_METHODNAME + errStr);
			throw new IllegalArgumentException(errStr);
		}
		if (pDoseNumberToSkipTo == pDoseNumberToSkipFrom) {
			String errStr = "dose number to skip from (" + pDoseNumberToSkipFrom + ") is equal to the dose number to skip to (" + pDoseNumberToSkipTo + "). Nothing to do.";
			logger.warn(errStr);
			return;
		}
		if (pDoseNumberToSkipTo < pDoseNumberToSkipFrom) {
			String errStr = "dose number to skip from (" + pDoseNumberToSkipFrom + ") is greater than dose number to skip to (" + pDoseNumberToSkipTo + ")";
			logger.warn(_METHODNAME + errStr);
			throw new IllegalArgumentException(errStr);
		}

		int lTargetDoseNumber = 0;
		if (pTargetDoseNumber <= 0) {
			lTargetDoseNumber = determineDoseNumberInSeries();
		}
		else {
			lTargetDoseNumber = pTargetDoseNumber;
		}
		if (pDoseNumberToSkipFrom != lTargetDoseNumber) {
			String errStr = "dose number to skip from (" + pDoseNumberToSkipFrom + ") does not equal the target dose number of the series (" + lTargetDoseNumber + ")";
			logger.warn(_METHODNAME + errStr);
			throw new IllegalArgumentException(errStr);
		}

		Map<Integer, Integer> skipDoseEntry = interimDosesToSkipByDisease.get(pDisease);
		if (skipDoseEntry != null) {
			skipDoseEntry.put(new Integer(pDoseNumberToSkipFrom), new Integer(pDoseNumberToSkipTo));
		}
		
		Integer lInterimValidityCountForDisease = interimEvaluationValidityCountByDisease.get(pDisease);
		if (lInterimValidityCountForDisease.intValue() == 0 && pDoseNumberToSkipFrom == 1) {
			interimEvaluationValidityCountByDisease.put(pDisease, pDoseNumberToSkipTo-1);
		}
	}

	public int determineDoseNumberInSeries() {
		
		return determineEffectiveNumberOfDosesInSeries() + 1;
	}

	
	public int determineDoseNumberInSeriesForDiseasesTargetedByThisDose(TargetDose pTD) {

		if (pTD == null || pTD.getVaccineComponent() == null) {
			return 0;
		}
		// Call below does not update the state of this object
		return doseNumberDeterminationUpdateUtility(pTD, true, false, true, pTD.getVaccineComponent().getAllDiseasesTargetedForImmunity());
	}

	
	/**
	 * Determines the series dose number of the specified dose.
	 * 
	 * @param pTD
	 * @return
	 */
	public int determineDoseNumberInSeries(TargetDose pTD) {
		// Call below _does not_ update the internal state of this object
		return doseNumberDeterminationUpdateUtility(pTD, true, false, true,	interimEvaluationValidityCountByDisease.keySet());
	}

	
	/**
	 * Update the dose validity count for the series and return the resulting series dose number (based on the supplied TargetDose). 
	 * Should be called if the dose validity was just set to VALID. Updates the internal state of this object.
	 * 
	 * @param pTD TargetDose in question
	 * @return the dose number for the specified TargetDose
	 */
	public int updateDoseValidityCountAndReturnActualDoseCountInSeries(TargetDose pTD) {

		String _METHODNAME = "updateDoseValidityCountAndReturnActualDoseCountInSeries(): ";
		
		// Call below _does update_ the internal state of this object
		int i = doseNumberDeterminationUpdateUtility(pTD, false, true, true, interimEvaluationValidityCountByDisease.keySet());
		//if (i == 0) {
		//	return 1;
		//}
		//else {
		logger.debug(_METHODNAME + "returning " + i);
			return i;
		//}
	}
	
	/**
	 * Update the dose validity count for the series and return the resulting series dose number (based on the supplied TargetDose). 
	 * Should be called if the dose validity was just set to VALID
	 * 
	 * @param pTD TargetDose in question
	 * @return the dose number for the specified TargetDose
	 */
	public int updateDoseValidityCountAndReturnTargetDoseNumberInSeries(TargetDose pTD) {

		return doseNumberDeterminationUpdateUtility(pTD, true, true, true,	interimEvaluationValidityCountByDisease.keySet());
	}
	
	
	/**
	 * Determines the dose number of the specified target dose. Updates the series dose number for the series
	 * 
	 * @param pTD TargetDose to determine dose number for
	 * @param updateInternalSeriesDoseNumberCount if set to true, increment the validity counters for all of the diseases
	 * @param antigensToIncludeInDetermination limit which diseases to look at in the determination of the dose number. Under usual circumstances, use
	 *            interimDiseaseEvaluationValidityCount.keySet(), to take into account all diseases for which this series induces immunity
	 * @param takeSkipDoseEntriesIntoAccount If set to true, take any skip dose entries into account when determining dose number
	 * @return
	 */
	private int doseNumberDeterminationUpdateUtility(TargetDose pTD, boolean returnTargetDoseNumber, boolean updateInternalSeriesDoseNumberCount,
			boolean takeSkipDoseEntriesIntoAccount,	Collection<String> antigensToIncludeInDetermination) {

		String _METHODNAME = "doseNumberDeterminationUpdateUtility(): ";
		if (pTD == null) {
			String str = "Supplied TargetDose parameter is null";
			logger.warn(_METHODNAME + str);
			throw new IllegalArgumentException(str);
		}

		int highestSkipDoseNumberToEntry = 0;
		int highestNonSkipDoseNumberToEntry = 0;

		// Tally up objects for each disease, that is-- how many valid doses for each disease
		/////// Map<SupportedDiseaseConcept, Integer> tallyOfDoseNumberByDisease = new HashMap<SupportedDiseaseConcept, Integer>();
		/////// Map<SupportedDiseaseConcept, Integer> tallyOfRelevantDiseaseImmunity = new HashMap<SupportedDiseaseConcept, Integer>();
		Map<String, Integer> tallyOfDoseNumberByDisease = new HashMap<String, Integer>();
		Map<String, Integer> tallyOfRelevantDiseaseImmunity = new HashMap<String, Integer>();
		
		// Initialize tally for supported diseases
		/////// Collection<SupportedDiseaseConcept> allSupportedDiseases = antigensToIncludeInDetermination;
		Collection<String> allSupportedDiseases = antigensToIncludeInDetermination;
		for (String disease : allSupportedDiseases) {
			tallyOfDoseNumberByDisease.put(disease, new Integer(0));
		}

		// Record tally for disease immunity
		Date targetDoseDate = pTD.getAdministrationDate();
		Integer numberOfDosesInSeriesInt = new Integer(getSeriesRules().getNumberOfDosesInSeries());
		for (String sdc : this.diseaseImmunityDate.keySet()) {
			Date lDiseaseImmunityDate = this.diseaseImmunityDate.get(sdc);
			if (targetDoseDate != null && lDiseaseImmunityDate != null && targetDoseDate.compareTo(lDiseaseImmunityDate) >= 0) {
				this.interimEvaluationValidityCountByDisease.put(sdc, numberOfDosesInSeriesInt);
				tallyOfRelevantDiseaseImmunity.put(sdc, numberOfDosesInSeriesInt);
			}
		}

		// Wherever disease immunity is recorded, remove the count from the tally so it does not skew the "least count" result
		Collection<String> tallyOfRelevantDiseaseImmunityKeys = tallyOfRelevantDiseaseImmunity.keySet();
		if (!tallyOfRelevantDiseaseImmunityKeys.containsAll(tallyOfDoseNumberByDisease.keySet())) {
			for (String sdc : tallyOfRelevantDiseaseImmunityKeys) {
				tallyOfDoseNumberByDisease.remove(sdc);
			}
		}

		String pTDUniqueIdentifier = pTD.getUniqueId();
		TargetDose lPreviouslyProcessedTD = null;
		Date lDuplicateShotSameDayValidDoseFoundDate = null;
		/////// HashSet<SupportedDiseaseConcept> lDuplicateShotDiseases = new HashSet<SupportedDiseaseConcept>();
		HashSet<String> lDuplicateShotDiseases = new HashSet<String>();
		for (TargetDose td : targetDoses) {
			if (updateInternalSeriesDoseNumberCount == false && lPreviouslyProcessedTD != null && lPreviouslyProcessedTD.getUniqueId().equals(pTDUniqueIdentifier)) {
				break;
			}
			if (lDuplicateShotSameDayValidDoseFoundDate != null && ! td.getAdministrationDate().equals(lDuplicateShotSameDayValidDoseFoundDate)) {
				lDuplicateShotSameDayValidDoseFoundDate = null;
				/////// lDuplicateShotDiseases = new HashSet<SupportedDiseaseConcept>();
				lDuplicateShotDiseases = new HashSet<String>();
			}
			if (lDuplicateShotSameDayValidDoseFoundDate != null && lPreviouslyProcessedTD != null && 
				this.seriesRules.isDoseNumberCalculationBasedOnDiseasesTargetedByVaccinesAdministered() == false && 
				lPreviouslyProcessedTD.getAdministeredShotNumberInSeries() < td.getAdministeredShotNumberInSeries() &&
				td.getAdministrationDate().equals(lPreviouslyProcessedTD.getAdministrationDate())) {
				// Don't count prior duplicate shots if the disease immunity of the vaccines are not looked at from shot to shot
				continue;
			}
			DoseStatus statusThisTD = td.getStatus();
			/////// Collection<SupportedDiseaseConcept> diseasesTargetedByThisDose = td.getVaccineComponent().getAllDiseasesTargetedForImmunity();
			Collection<String> diseasesTargetedByThisDose = td.getVaccineComponent().getAllDiseasesTargetedForImmunity();
			if (Collections.disjoint(diseasesTargetedByThisDose, antigensToIncludeInDetermination)) {
				// There is no overlap in diseases between this dose and the antigens we're checking.
				lPreviouslyProcessedTD = td;
				continue;
			}
			/////// for (SupportedDiseaseConcept diseaseTargeted : diseasesTargetedByThisDose) {
			for (String diseaseTargeted : diseasesTargetedByThisDose) {
				Integer numberOfValidDosesForDiseaseInt = tallyOfDoseNumberByDisease.get(diseaseTargeted);
				if (numberOfValidDosesForDiseaseInt != null) { 
					// Vaccine from this dose supports this disease in this series
					int numberOfValidDosesForDisease = numberOfValidDosesForDiseaseInt.intValue();
					int doseNumberForDisease = numberOfValidDosesForDisease + 1;
					// BEGIN: Determine if the disease tally should be incremented or not - based on whether (1) this shot is valid; (2) it counts towards completion of the series, 
					// and/or (3) it is a duplicate shot, taking into account targeted diseases if this series bases its dose count on the count of targeted diseases
					boolean lIncrementDoseNumber = false;
					if (statusThisTD == DoseStatus.VALID && td.countsTowardsCompletionOfSeries()) {
						boolean lDoseNumberCalculatedBasedOnDiseasesTargetedByEachVaccineAdministered = this.seriesRules.isDoseNumberCalculationBasedOnDiseasesTargetedByVaccinesAdministered();
						if (lDuplicateShotSameDayValidDoseFoundDate != null) {
							// If duplicate shot same day valid dose found date is not null, then it is equal to this shot date or it would have been null'd above
							// If the diseases should be taken into account for this series in determining dose number, and this disease has already been accounted for 
							// on this day, then we have a duplicate shot
							if (lDoseNumberCalculatedBasedOnDiseasesTargetedByEachVaccineAdministered == false) {
								lIncrementDoseNumber = false;
							}
							else if (lDoseNumberCalculatedBasedOnDiseasesTargetedByEachVaccineAdministered == true && lDuplicateShotDiseases.contains(diseaseTargeted)) {
								lIncrementDoseNumber = false;
							}
							else {
								if (lDoseNumberCalculatedBasedOnDiseasesTargetedByEachVaccineAdministered == true) {
									lDuplicateShotDiseases.add(diseaseTargeted);
								}
								lIncrementDoseNumber = true;
							}
						}
						else {
							if (lPreviouslyProcessedTD != null && td.getAdministrationDate().equals(lPreviouslyProcessedTD.getAdministrationDate())) {
								// The prior shot was the same date and this is the first shot of the same date that is valid. Find all shots of this date prior to this one 
								// and make note of the diseases targeted
								int lInnerCountOfPriorValidShotsSameDay = 0;
								for (TargetDose lInnerTd : targetDoses) {
									if (lInnerTd.getAdministeredShotNumberInSeries() >= td.getAdministeredShotNumberInSeries()) {
										break;
									}
									if (lInnerTd.getStatus() == DoseStatus.VALID && lInnerTd.getAdministrationDate().equals(td.getAdministrationDate())) {
										lInnerCountOfPriorValidShotsSameDay++;
										lDuplicateShotDiseases.addAll(lInnerTd.getVaccineComponent().getAllDiseasesTargetedForImmunity());
									}
									}
								if (lDoseNumberCalculatedBasedOnDiseasesTargetedByEachVaccineAdministered == false && lInnerCountOfPriorValidShotsSameDay > 0) {
									lIncrementDoseNumber = false;
								}
								else if (lDoseNumberCalculatedBasedOnDiseasesTargetedByEachVaccineAdministered == true && lDuplicateShotDiseases.contains(diseaseTargeted)) {
									// A duplicate shot is noted. Make note of the diseases 
									lIncrementDoseNumber = false;
								}
								else {		
									// the lDoseNumberCalculatedBasedOnDiseasesTargetedByEachVaccineAdministered is true
									lIncrementDoseNumber = true;
								}
								lDuplicateShotSameDayValidDoseFoundDate = td.getAdministrationDate();
								lDuplicateShotDiseases.add(diseaseTargeted);
							}
							else {
								lIncrementDoseNumber = true;
							}
						}
					}
					// END: determining whether or not disease tally should be tracked for this shot
					if (takeSkipDoseEntriesIntoAccount == false) {
						if (lIncrementDoseNumber) {
							tallyOfDoseNumberByDisease.put(diseaseTargeted,	new Integer(doseNumberForDisease));
							if (doseNumberForDisease > highestNonSkipDoseNumberToEntry) {
								highestNonSkipDoseNumberToEntry = doseNumberForDisease;
							}
						}
					} 
					else {
						Map<Integer, Integer> skipDoseEntriesForDisease = interimDosesToSkipByDisease.get(diseaseTargeted);
						Integer skipDoseEntryFromInt = new Integer(doseNumberForDisease);
						if (skipDoseEntriesForDisease != null && skipDoseEntriesForDisease.containsKey(skipDoseEntryFromInt)) {
							Integer skipDoseToInt = skipDoseEntriesForDisease.get(skipDoseEntryFromInt);
							int skipDoseTo = skipDoseToInt.intValue();
							if (statusThisTD != DoseStatus.VALID) {
								skipDoseTo = skipDoseTo-1;
								skipDoseToInt = new Integer(skipDoseTo);
							}
							if (skipDoseTo > highestSkipDoseNumberToEntry)
								highestSkipDoseNumberToEntry = skipDoseTo;
							tallyOfDoseNumberByDisease.put(diseaseTargeted,	skipDoseToInt);
						} 
						else {
							if (logger.isDebugEnabled()) {
								logger.debug(_METHODNAME + "dosenumber for disease: " + diseaseTargeted + "; dose number " + doseNumberForDisease + "; else: " + td);
							}
							if (lIncrementDoseNumber) {
								tallyOfDoseNumberByDisease.put(diseaseTargeted,	new Integer(doseNumberForDisease));
								if (doseNumberForDisease > highestNonSkipDoseNumberToEntry) {
									highestNonSkipDoseNumberToEntry = doseNumberForDisease;
								}
							}
						}
					}
				}
			}			
			lPreviouslyProcessedTD = td;
		}

		// Go through tally and for those diseases where skip dose is set, adjust it. If less than the dose to skip to, jump to the dose
		// number specified to skip to. If Remove all dose numbers tracked across immune diseases, but track the
		// highest dose number across them
		int leastDoseNumberAcrossDiseases = -1;
		int greatestDoseNumberAcrossDiseases = -1;
		for (String sdc : tallyOfDoseNumberByDisease.keySet()) {
			Integer tallyDoseNumberInt = tallyOfDoseNumberByDisease.get(sdc);
			if (logger.isDebugEnabled()) {
				logger.debug(_METHODNAME + "tally disease: " + sdc);
				logger.debug(_METHODNAME + "tally number: " + ((tallyDoseNumberInt != null) ? tallyDoseNumberInt : "null"));
			}
			if (tallyDoseNumberInt != null) {
				int tallyDoseNumber = tallyDoseNumberInt.intValue();
				// Record the leastDoseNumber
				if (leastDoseNumberAcrossDiseases == -1) {
					leastDoseNumberAcrossDiseases = tallyDoseNumber;
					if (logger.isDebugEnabled()) {
						logger.debug(_METHODNAME + "recorded least dose number " + leastDoseNumberAcrossDiseases);
					}
					if (tallyDoseNumber > highestNonSkipDoseNumberToEntry) {
						highestNonSkipDoseNumberToEntry = tallyDoseNumber;
					}
				} 
				else if (tallyDoseNumber < leastDoseNumberAcrossDiseases) {
					leastDoseNumberAcrossDiseases = tallyDoseNumber;
					if (logger.isDebugEnabled()) {
						logger.debug(_METHODNAME + "recorded least dose number " + leastDoseNumberAcrossDiseases);
					}
					if (tallyDoseNumber > highestNonSkipDoseNumberToEntry) {
						highestNonSkipDoseNumberToEntry = tallyDoseNumber;
					}
				}
				// Now record the greatestDoseNunmber
				if (greatestDoseNumberAcrossDiseases == -1) {
					greatestDoseNumberAcrossDiseases = tallyDoseNumber;
				}
				else if (tallyDoseNumber > greatestDoseNumberAcrossDiseases) {
					greatestDoseNumberAcrossDiseases = tallyDoseNumber;
				}
				if (logger.isDebugEnabled()) {
					logger.debug(_METHODNAME + "leastdosenumber: " + leastDoseNumberAcrossDiseases);
				}
			}
		}

		for (String sdc : diseaseImmunityDate.keySet()) {
			if (!tallyOfDoseNumberByDisease.containsKey(sdc)) {
				tallyOfDoseNumberByDisease.put(sdc, new Integer(leastDoseNumberAcrossDiseases));
			}
		}

		if (updateInternalSeriesDoseNumberCount) {
			for (String sdc : tallyOfDoseNumberByDisease.keySet()) {
				if (tallyOfDoseNumberByDisease.containsKey(sdc)) {
					Integer diseaseTally = tallyOfDoseNumberByDisease.get(sdc);
					int newTally = diseaseTally.intValue();
					diseaseTally = new Integer(newTally);
					this.interimEvaluationValidityCountByDisease.put(sdc, diseaseTally);
				}
			}
		}

		if (logger.isDebugEnabled()) {
			logger.debug(_METHODNAME + "highestSkipDoseNumberToEntry: " + highestSkipDoseNumberToEntry + "; highestNonSkipDoseNumberToEntry: " + highestNonSkipDoseNumberToEntry + 
					"; leastDoseNumberAcrossDiseases: " + leastDoseNumberAcrossDiseases + "; greatestDoseNumberAcrossDiseases: " + greatestDoseNumberAcrossDiseases); 
		}

		int doseNumberToReturn = (this.seriesRules.isDoseNumberCalculationBasedOnDiseasesTargetedByVaccinesAdministered() == true) ? leastDoseNumberAcrossDiseases : greatestDoseNumberAcrossDiseases; 
		if (returnTargetDoseNumber) {
			doseNumberToReturn = doseNumberToReturn+1;
			if (logger.isDebugEnabled()) {
				logger.debug(_METHODNAME + "Returning target count dose number: " + doseNumberToReturn);
			}
			return doseNumberToReturn;
		}
		else {
			if (logger.isDebugEnabled()) {
				logger.debug(_METHODNAME + "Returning actual count dose number: " + doseNumberToReturn);
			}
			return doseNumberToReturn;
		}
	}

	
	/**
	 * Get the last shot administered, null if none have been administered
	 */
	public TargetDose getLastShotAdministeredInSeries() {

		if (!targetDoses.isEmpty()) {
			return targetDoses.last();
		} 
		else {
			return null;
		}
	}

	/**
	 * Geth
	 * 
	 * @param doseNumber
	 * @return TargetDose, or null if not found
	 */
	public TargetDose getValidOrAcceptedShotByDoseNumber(int doseNumber) {

		if (targetDoses.isEmpty()) {
			return null;
		}

		for (TargetDose td : targetDoses) {
			if (td.getDoseNumberInSeries() == doseNumber) {
				DoseStatus ds = td.getStatus();
				if (ds == DoseStatus.VALID || ds == DoseStatus.ACCEPTED) {
					return td;
				}
			}
		}

		return null;
	}

	/**
	 * Determines if the series is complete, updates the seriesComplete instance variable, and returns the last target dose in this TargetSeries. Note that 
	 * seriesCompleteFlagManuallySet is _not_ updated by this method.
	 * 
	 * @return last dose in the series, regardless of whether the series is complete or null is no doses have been administered
	 */
	private TargetDose determineIfSeriesCompleteAndReturnLastDose() {

		TargetDose lastDoseAdministered = null;

		int numberOfEffectiveDoses = determineEffectiveNumberOfDosesInSeries();

		if (!targetDoses.isEmpty()) {
			lastDoseAdministered = targetDoses.last();
		} 
		else {
			if (seriesCompleteFlagManuallySet == false) {
				if (seriesRules.getNumberOfDosesInSeries() == 0) {
					seriesComplete = true;
					seriesCompleteAtDoseNumber = 1;
				} 
				else {
					seriesComplete = false;
				}
				return null;
			}
		}
		if (seriesCompleteFlagManuallySet == false) {
			int numberOfDosesInSeriesRule = seriesRules.getNumberOfDosesInSeries();
			if (lastDoseAdministered != null) {
				if (numberOfEffectiveDoses >= numberOfDosesInSeriesRule) {
					seriesComplete = true;
					seriesCompleteAtDoseNumber = numberOfEffectiveDoses;
				} 
				else {
					seriesComplete = false;
				}
			} 
			else {
				seriesComplete = false;
			}
		}
		return lastDoseAdministered;
	}

	/**
	 * Return a simple count of valid or accepted shots administered in this series. Does not take into account immunity or skipped doses
	 */
	public int determineNumberOfDosesAdministeredInSeries() {

		if (targetDoses == null) {
			return 0;
		}

		int i = 0;
		for (TargetDose td : targetDoses) {
			if (td.getIsValid() == true) {
				i++;
			}
		}

		return i;
	}

	/**
	 * Return a simple count of valid or accepted shots administered in this series before the specified date. If includeDate parameter   
	 * is true, the count also includes shots administered on the specified date.
	 * Does not take into account immunity or skipped doses. If the supplied date is null, this methods returns 0.
	 */
	public int determineNumberOfDosesAdministeredInSeriesByDate(Date pDate, boolean includeDate) {

		if (targetDoses == null || pDate == null) {
			return 0;
		}

		int i=0;
		for (TargetDose td : targetDoses) {
			if (td.getIsValid() == true) {
				Date shotDate = td.getAdministrationDate();
				if (shotDate != null) {
					int compareTo = pDate.compareTo(shotDate);
					if (includeDate && compareTo >= 0) {
						i++;
					}
					else if (! includeDate && compareTo > 0) {
						i++;
					}
					else 
						break;
				}
			}
		}

		return i;
	}

	/**
	 * Return a effective number of valid or accepted shots administered in this series before the specified date. If includeDate parameter   
	 * is true, the count also includes shots administered on the specified date. If the supplied date is null, this methods returns 0.
	 */
	public int determineEffectiveNumberOfDosesInSeriesByDate(Date pDate, boolean includeDate) {

		String _METHODNAME = "determineEffectiveNumberOfDosesInSeriesByDate(): ";
		
		logger.trace(_METHODNAME + "parameters passed in: " + pDate + "; includeDate " + includeDate);

		if (targetDoses == null || pDate == null) {
			return 0;
		}

		// First, find the dose that is on or before the specified date to use to determine the dose number
		TargetDose lTargetDoseOfInterest = null;
		for (TargetDose td : targetDoses) {
			Date shotDate = td.getAdministrationDate();
			if (shotDate != null) {
				int compareTo = pDate.compareTo(shotDate);
				if (includeDate && compareTo >= 0) {
					lTargetDoseOfInterest = td;
				}
				else if (! includeDate && compareTo > 0) {
					lTargetDoseOfInterest = td;
				}
				else {
					break;
				}
			}
		}

		logger.trace(_METHODNAME + ((lTargetDoseOfInterest == null) ? "lTargetDoseOfInterest is null" : lTargetDoseOfInterest.toString()));
		if (lTargetDoseOfInterest != null) {
			int doseNumber = doseNumberDeterminationUpdateUtility(lTargetDoseOfInterest, false, false, true, interimEvaluationValidityCountByDisease.keySet());
			logger.trace(_METHODNAME + "dose number returned for lTargetDoseOfInterest " + doseNumber);
			return doseNumber;
		}
		else {
			logger.trace(_METHODNAME + "Returning dose number 0");
			return 0;
		}
	}

	
	/**
	 * Return the number of effective valid (meaning VALID) doses in this TargetSeries. Note that the number reflects immunity and any skip doses in the series 
	 * (e.g. - if there are 3 doses to complete a series and the patient has immunity and there are no other diseases in question, 3 is returned even if no shots were administered)
	 * 
	 * @return effective number of valid and accepted doses, taking immunity and skip doses into account
	 */
	public int determineEffectiveNumberOfDosesInSeries() {

		// String _METHODNAME = "determineEffectiveNumberOfDosesInSeries()";

		if (this.targetDoses == null || this.targetDoses.isEmpty()) {
			return 0;
		}
		else {
			TargetDose lastDose = this.targetDoses.last();
			if (lastDose != null) {
				// Since this method is public and could be accessed as a Drools accessor method, the below call does NOT and MAY NOT update the state of this 
				// object (third parameter), and the antigen set is appropriately constant as well. 
				// TODO: cache this.
				return doseNumberDeterminationUpdateUtility(lastDose, false, false, true, interimEvaluationValidityCountByDisease.keySet());
			}
			else {
				return 0;
			}
		}
	}

	public void recommendNextShotBasedOnEarliestAgeRule(Date pEvalPersonBirthTime, Date pEvalDate)
			throws ImproperUsageException, InconsistentConfigurationException {

		recommendNextShotBasedOnSeriesAgeRule(pEvalPersonBirthTime, pEvalDate, RecommendationType.EARLIEST);
	}

	public void recommendNextShotBasedOnEarliestIntervalRule(Date pEvalDate)
			throws ImproperUsageException, InconsistentConfigurationException {

		recommendNextShotBasedOnSeriesIntervalRule(pEvalDate, RecommendationType.EARLIEST);
	}

	public void recommendNextShotBasedOnEarliestRecommendedAgeRule(Date pEvalPersonBirthTime, Date pEvalDate)
			throws ImproperUsageException, InconsistentConfigurationException {

		recommendNextShotBasedOnSeriesAgeRule(pEvalPersonBirthTime, pEvalDate, RecommendationType.EARLIEST_RECOMMENDED);
	}

	public void recommendNextShotBasedOnEarliestRecommendedIntervalRule(Date pEvalDate)
		throws ImproperUsageException, InconsistentConfigurationException {

		recommendNextShotBasedOnSeriesIntervalRule(pEvalDate, RecommendationType.EARLIEST_RECOMMENDED);
	}
	
	public void recommendNextShotBasedOnLatestRecommendedAgeRule(Date pEvalPersonBirthTime, Date pEvalDate)
			throws ImproperUsageException, InconsistentConfigurationException {

		recommendNextShotBasedOnSeriesAgeRule(pEvalPersonBirthTime, pEvalDate, RecommendationType.LATEST_RECOMMENDED);
	}

	public void recommendNextShotBasedOnLatestRecommendedIntervalRule(Date pEvalDate)
			throws ImproperUsageException, InconsistentConfigurationException {

		recommendNextShotBasedOnSeriesIntervalRule(pEvalDate, RecommendationType.LATEST_RECOMMENDED);
	}


	/**
	 * Check age against series rule for this dose and record a recommendation based on the age recommended in the series
	 * 
	 * @param pEvalPersonBirthTime
	 * @param pEvalDate Evaluation Date that this recommendation should be made against. If null, the current date is used.
	 * @throws ImproperUsageException
	 * @throws InconsistentConfigurationException
	 */
	private void recommendNextShotBasedOnSeriesAgeRule(Date pEvalPersonBirthTime, Date pEvalDate, RecommendationType pRecommendationDateType)
		throws ImproperUsageException, InconsistentConfigurationException {

		String _METHODNAME = "recommendNextShotBasedOnSeriesAgeRule(): ";

		if (pEvalPersonBirthTime == null) {
			String errStr = "NULL Date of Birth supplied";
			logger.error(_METHODNAME + errStr);
			throw new ImproperUsageException(_METHODNAME + errStr);
		}

		if (pRecommendationDateType == null) {
			String errStr = "NULL RecommendationDateType supplied";
			logger.error(_METHODNAME + errStr);
			throw new ImproperUsageException(_METHODNAME + errStr);
		}

		//TargetDose lastDoseAdministered = determineIfSeriesCompleteAndReturnLastDose();
		//if (logger.isDebugEnabled()) {
		//	logger.debug(_METHODNAME + "Last dose # administered: " + ((lastDoseAdministered == null) ? "none" : lastDoseAdministered.getDoseNumberInSeries()));
		//}

		// Series is Complete if latest dose # is > # valid/accepted doses required, or if latest dose # == # valid/accepted doses required in
		// series and that latest dose is accepted/valid
		boolean lIsSeriesComplete = isSeriesComplete();
		boolean lTargetSeasonExists = targetSeasonExists();
		if (lIsSeriesComplete == true && lTargetSeasonExists == false) {
			if (this.seriesRules.recurringDosesOccurAfterSeriesComplete() == false) {
				// The series is complete, and no other future shots are recommended. If shots are recurring for this series, it is assumed that a custom rule handles this
				Recommendation rec = new Recommendation(this);
				rec.setRecommendationStatus(RecommendationStatus.NOT_RECOMMENDED);
				rec.setRecommendationReason(BaseDataRecommendationReason._NOT_RECOMMENDED_COMPLETE_REASON.getCdsListItemName());
				if (pRecommendationDateType == RecommendationType.EARLIEST) {
					interimRecommendationsScheduleEarliest.add(rec);
				}
				else if (pRecommendationDateType == RecommendationType.EARLIEST_RECOMMENDED) {
					interimRecommendationsScheduleEarliestRecommendedAge.add(rec);
				}
				else if (pRecommendationDateType == RecommendationType.LATEST_RECOMMENDED) {
					interimRecommendationsScheduleLatestRecommendedAge.add(rec);
				}
			}
			return;
		}
		else if (lTargetSeasonExists == true && lIsSeriesComplete == true) {
			return;
		}

		// If the date of last shot is before the birthdate, set it to the birthdate
		int doseNumberInSeriesToRecommend = 0;
		if (this.manuallySetDoseNumberToRecommend != 0) {
			doseNumberInSeriesToRecommend = this.manuallySetDoseNumberToRecommend;
		}
		else {
			doseNumberInSeriesToRecommend = determineEffectiveNumberOfDosesInSeries() + 1;
		}
		if (logger.isDebugEnabled()) {
			logger.debug(_METHODNAME + "Dose number in series for recommendation: " + doseNumberInSeriesToRecommend);
		}
		
		// Obtain the right age and corresponding date
		DoseRule vaccineGroupDoseRule = obtainDoseRuleForSeriesByDoseNumber(doseNumberInSeriesToRecommend);
		if (vaccineGroupDoseRule == null) {
			if (seriesCompleteFlagManuallySet == true || seriesRules.getNumberOfDosesInSeries() == 0) {
				// Series completion flag was manually set, so it's possible that we are getting extra doses due to the series complete being manually set
				// to incomplete
				return;
			}
			else {
				logger.error(_METHODNAME + "Corresponding series vaccine group dose rule not found");
				throw new InconsistentConfigurationException("Corresponding series vaccine group dose rule not found");
			}
		}
		
		TimePeriod rAge = null;
		if (pRecommendationDateType == RecommendationType.EARLIEST) {
			rAge = vaccineGroupDoseRule.getMinimumAge();
		} 
		else if (pRecommendationDateType == RecommendationType.EARLIEST_RECOMMENDED) {
			rAge = vaccineGroupDoseRule.getEarliestRecommendedAge();
		} 
		else if (pRecommendationDateType == RecommendationType.LATEST_RECOMMENDED) {
			rAge = vaccineGroupDoseRule.getLatestRecommendedAge();
		} 
		else {
			String errStr = _METHODNAME + "unknown type specified for RecommendationDateType; not supported";
			throw new ImproperUsageException(_METHODNAME + errStr);
		}
		if (rAge == null) {
			if (pRecommendationDateType == RecommendationType.EARLIEST_RECOMMENDED) {
				String msg = "No routine age recommendation specified for dose " + vaccineGroupDoseRule.getDoseNumber() + 
						" in Vaccine Group " + seriesRules.getVaccineGroup();
				logger.warn(_METHODNAME + msg);
			}
			return;
		}

		// Now calculate the date that the next shot should be administered according to age rule, but if age for the series is before the start
		// date of the season (if a seasonal series), use the seasonal series start date as the minimum age date
		Date ageDate = TimePeriod.addTimePeriod(pEvalPersonBirthTime, rAge);
		if (targetSeasonExists()) {
			Date seasonStartDate = targetSeason.getFullySpecifiedSeasonStartDate().toDate();
			if (ageDate.before(seasonStartDate)) {
				ageDate = seasonStartDate;
			}
			//////// Don't do this.. let the recommended age go through; otherwise too much logic ? 
			// Date lSeasonEndDate = targetSeason.getFullySpecifiedSeasonEndDate().toDate();
			// If the recommended age is after the end date of the season and there is no off-season start date (which is indicative that there are no other seasons for this 
			// vaccine group, then return; no recommendation based on minimum age will be made.
			//if (lSeasonEndDate != null && ageDate.after(lSeasonEndDate) && targetSeason.getFullySpecifiedSeasonOffSeasonEndDate() == null) {
			//	return;
			//}
			///////
		}

		if (pEvalDate == null) {
			pEvalDate = new Date();
		}

		if (pRecommendationDateType == RecommendationType.EARLIEST) {
			Recommendation lEarliest = new Recommendation(this);
			lEarliest.setRecommendationDate(ageDate);
			populateInterimEarliestRecommendation(lEarliest, pEvalDate.before(ageDate) ? RecommendationStatus.RECOMMENDED_IN_FUTURE : RecommendationStatus.RECOMMENDED);
		} 
		else if (pRecommendationDateType == RecommendationType.EARLIEST_RECOMMENDED) {
			Recommendation lEarliestRec = new Recommendation(this);
			lEarliestRec.setRecommendationDate(ageDate);
			if (pEvalDate.before(ageDate)) {
				populateInterimEarliestRecommendedAgeRecommendation(lEarliestRec, RecommendationStatus.RECOMMENDED_IN_FUTURE);
			} 
			else {
				populateInterimEarliestRecommendedAgeRecommendation(lEarliestRec, RecommendationStatus.RECOMMENDED);
			}
		}
		else if (pRecommendationDateType == RecommendationType.LATEST_RECOMMENDED) {
			// Past due date is the latest recommended date (calculated via age or interval) - 1
			Date lOverdueDate = TimePeriod.addTimePeriod(ageDate, new TimePeriod(-1, DurationType.DAYS));
			Recommendation lLatestRecommended = new Recommendation(this);
			lLatestRecommended.setRecommendationDate(lOverdueDate);
			populateInterimLatestRecommendedAgeRecommendation(lLatestRecommended, pEvalDate.before(lOverdueDate) ? RecommendationStatus.RECOMMENDED_IN_FUTURE : RecommendationStatus.RECOMMENDED);
		}
	}


	/**
	 * Check interval against series rule for this dose and record a recommendation based on the routine interval
	 * 
	 * @param pEvalDate Evaluation Date that this recommendation should be made against. If null, the current date is used.
	 * @throws ImproperUsageException
	 * @throws InconsistentConfigurationException
	 */
	private void recommendNextShotBasedOnSeriesIntervalRule(Date pEvalDate, RecommendationType pRecommendationDateType)
		throws ImproperUsageException, InconsistentConfigurationException {

		String _METHODNAME = "recommendNextShotBasedOnSeriesIntervalRule(): ";

		if (pRecommendationDateType == null) {
			String errStr = "NULL RecommendationDateType supplied";
			logger.error(_METHODNAME + errStr);
			throw new ImproperUsageException(_METHODNAME + errStr);
		}

		TargetDose lastDoseAdministered = determineIfSeriesCompleteAndReturnLastDose();
		if (logger.isDebugEnabled()) {
			logger.debug(_METHODNAME + "Last dose # administered: " + ((lastDoseAdministered == null) ? "none" : lastDoseAdministered.getDoseNumberInSeries()));
		}
		if (lastDoseAdministered == null) {
			return;
		}

		int lastDoseAdministeredDoseNumber = lastDoseAdministered.getDoseNumberInSeries();
		boolean lTargetSeasonExists = targetSeasonExists();		
		if (! lTargetSeasonExists && lastDoseAdministeredDoseNumber > seriesRules.getNumberOfDosesInSeries()) {
			return;
		}					
		// Series is Complete if latest dose # is > # valid/accepted doses required, or if latest dose # == # valid/accepted doses required
		// in series and that latest dose is accepted/valid
		int doseRuleOfInterest = 0;
		// Prior dose to calculate interval from not supplied in parameters
		if (this.manuallySetDoseNumberToRecommend != 0) {
			doseRuleOfInterest = this.manuallySetDoseNumberToRecommend - 1;
		} 
		else {
			boolean lIsSeriesComplete = isSeriesComplete();
			if (lIsSeriesComplete == true && lTargetSeasonExists == false) { // (lTargetSeasonExists == false || (lTargetSeasonExists == true && targetSeason.getFullySpecifiedSeasonOffSeasonEndDate() == null))) {
				if (this.seriesRules.recurringDosesOccurAfterSeriesComplete() == false) {
					// The series is complete, and no other future shots are recommended. If shots are recurring for this series, it is assumed that a custom rule handles this
					Recommendation rec = new Recommendation(this);
					rec.setRecommendationStatus(RecommendationStatus.NOT_RECOMMENDED);
					rec.setRecommendationReason(BaseDataRecommendationReason._NOT_RECOMMENDED_COMPLETE_REASON.getCdsListItemName());
					if (pRecommendationDateType == RecommendationType.EARLIEST) {
						interimRecommendationsScheduleEarliest.add(rec);
					}
					else if (pRecommendationDateType == RecommendationType.EARLIEST_RECOMMENDED) {
						interimRecommendationsScheduleEarliestRecommendedInterval.add(rec);
					}
					else if (pRecommendationDateType == RecommendationType.LATEST_RECOMMENDED) {
						interimRecommendationsScheduleLatestRecommendedInterval.add(rec);
					}
				}
				return;
			}
			else if (lIsSeriesComplete == true && lTargetSeasonExists == true) {
				// We don't mark Seasonal Series complete here; some seasonal series can be completed and not others
				return;
			}
			else {
				doseRuleOfInterest = determineDoseNumberInSeries(lastDoseAdministered) - 1;
			}
		}

		if (doseRuleOfInterest == 0) {
			doseRuleOfInterest = 1;
			// Below doseRuleOfInterest REMOVED 10/9/2014 as per general rule: no interval from target dose 1 to dose 1 for inactivated vaccines. 
			// Create a separate live virus vaccine interval if needed.
			// TODO:this is temporary... more generic interval declarations forthcoming; however, currently there are no dose 1->1 settings
			// doseRuleOfInterest = 1;
			//
			// RATHER: change the above to the below return statement if it is decided that an interval _by default_ should be applied in this situation
			// return;							// ADDED as per above 10/9/2014
		}
		if (logger.isDebugEnabled()) {
			logger.debug(_METHODNAME + "Dose number from which to calculate interval: " + doseRuleOfInterest);
		}

		DoseRule doseRulePreviousDose = obtainDoseRuleForSeriesByDoseNumber(doseRuleOfInterest);
		if (doseRulePreviousDose == null) {
			if (seriesCompleteFlagManuallySet == true) {
				// Series completion flag was manually set, so it's possible that we are getting extra doses due to the series complete being manually set
				// to incomplete
				return;
			}
			else {
				logger.error(_METHODNAME + "Corresponding series vaccine group dose rule not found");
				throw new InconsistentConfigurationException("Corresponding series vaccine group dose rule not found");
			}
		}
		TimePeriod rInterval = doseRulePreviousDose.getEarliestRecommendedInterval();
		if (pRecommendationDateType == RecommendationType.EARLIEST) {
			rInterval = doseRulePreviousDose.getMinimumInterval();
		} 
		else if (pRecommendationDateType == RecommendationType.EARLIEST_RECOMMENDED) {
			rInterval = doseRulePreviousDose.getEarliestRecommendedInterval();
		} 
		else if (pRecommendationDateType == RecommendationType.LATEST_RECOMMENDED) {
			rInterval = doseRulePreviousDose.getLatestRecommendedInterval();
		} 
		else {
			String errStr = _METHODNAME + "LATEST specified for date; not supported yet";
			throw new ImproperUsageException(_METHODNAME + errStr);
		}
		if (rInterval == null) {
			if (pRecommendationDateType == RecommendationType.EARLIEST_RECOMMENDED) {
				String msg = "No routine intervalspecified for dose in Vaccine Group " + seriesRules.getVaccineGroup();
				logger.warn(_METHODNAME + msg);
			}
			return;
		}

		if (! rInterval.isTimePeriodSet()) {
			return;
		}

		// Now calculate the date that the next shot should be administered according to internal rule
		Date rIntervalDate = TimePeriod.addTimePeriod(lastDoseAdministered.getAdministrationDate(), rInterval);

		// If this is a Seasonal TargetSeries and the rIntervalDate is after the season end date and this season does not have an off-season, then do not make a recommendation
		// if (lTargetSeasonExists == true) {
		//	Date lSeasonEndDate = targetSeason.getFullySpecifiedSeasonEndDate().toDate();
		//	if (lSeasonEndDate != null && rIntervalDate.after(lSeasonEndDate) && targetSeason.getFullySpecifiedSeasonOffSeasonEndDate() == null) {
		//		return;
		//	}
		//}

		if (pEvalDate == null) {
			pEvalDate = new Date();
		}
		
		if (pRecommendationDateType == RecommendationType.EARLIEST) {
			Recommendation lEarliest = new Recommendation(this);
			lEarliest.setRecommendationDate(rIntervalDate);
			populateInterimEarliestRecommendation(lEarliest, pEvalDate.before(rIntervalDate) ? RecommendationStatus.RECOMMENDED_IN_FUTURE : RecommendationStatus.RECOMMENDED);
		} 
		else if (pRecommendationDateType == RecommendationType.LATEST_RECOMMENDED) {
			// Past due date is the latest recommended date (calculated via age or interval) - 1
			Date lOverdueDate = TimePeriod.addTimePeriod(rIntervalDate, new TimePeriod(-1, DurationType.DAYS));
			Recommendation lLatestRecommended = new Recommendation(this);
			lLatestRecommended.setRecommendationDate(lOverdueDate);
			// populate this in interim structure.... if there are age rule recommendations, they will need to be removed later
			populateInterimLatestRecommendedIntervalRecommendation(lLatestRecommended, pEvalDate.before(rIntervalDate) ? RecommendationStatus.RECOMMENDED_IN_FUTURE : RecommendationStatus.RECOMMENDED);
		}
		else if (pRecommendationDateType == RecommendationType.EARLIEST_RECOMMENDED) {
			Recommendation rec = new Recommendation(this);
			rec.setRecommendationDate(rIntervalDate);
			// populate this in interim structure.... if there are age rule recommendations, they will need to be removed later
			if (pEvalDate.before(rIntervalDate)) {
				populateInterimEarliestRecommendedIntervalRecommendation(rec, RecommendationStatus.RECOMMENDED_IN_FUTURE);
			} 
			else {
				populateInterimEarliestRecommendedIntervalRecommendation(rec, RecommendationStatus.RECOMMENDED);
			}
		}
	}


	/**
	 * Record recommendation status codes and date following the below business rules in the TargetSeries interimRecommendationsScheduleEarliest object.
	 *  - The supplied Recommendation object is updated with the chosen RecommendationStatus.
	 * @param rec Recommendation Object in which to record recommendation status codes and reasons. Simply returns if any supplied 
	 * parameter is null. 
	 * @param recommendationStatus
	 */
	private void populateInterimEarliestRecommendation(Recommendation rec, RecommendationStatus recommendationStatus) {

		String _METHODNAME = "populateInterimEarliestRecommendation(): ";
		if (rec == null || recommendationStatus == null) {
			logger.warn(_METHODNAME + "One or more supplied parameters null");
			return;
		}

		populateInterimRecommendationsAndRecordGenericReasonHelper(interimRecommendationsScheduleEarliest, rec, recommendationStatus);
	}


	/**
	 * Record recommendation status codes and date following the below business rules in the TargetSeries interimRecommendationsScheduleEarliestRecommendedAge object.
	 *  - The supplied Recommendation object is updated with the chosen RecommendationStatus.
	 * @param rec Recommendation Object in which to record recommendation status codes and reasons. Simply returns if any supplied 
	 * parameter is null. 
	 * @param recommendationStatus
	 */
	private void populateInterimEarliestRecommendedAgeRecommendation(Recommendation rec, RecommendationStatus recommendationStatus) {

		String _METHODNAME = "populateInterimEarliestRecommendedAgeRecommendation(): ";
		if (rec == null || recommendationStatus == null) {
			logger.warn(_METHODNAME + "One or more supplied parameters null");
			return;
		}

		populateInterimRecommendationsAndRecordGenericReasonHelper(interimRecommendationsScheduleEarliestRecommendedAge, rec, recommendationStatus);
	}


	/**
	 * Record recommendation status codes and date following the below business rules in the TargetSeries interimRecommendationsScheduleEarliestRecommendedInterval object.
	 *  - The supplied Recommendation object is updated with the chosen RecommendationStatus.
	 * @param rec Recommendation Object in which to record recommendation status codes and reasons. Simply returns if any supplied 
	 * parameter is null. 
	 * @param recommendationStatus
	 */
	private void populateInterimEarliestRecommendedIntervalRecommendation(Recommendation rec, RecommendationStatus recommendationStatus) {

		String _METHODNAME = "populateInterimEarliestRecommendedIntervalRecommendation(): ";
		if (rec == null || recommendationStatus == null) {
			logger.warn(_METHODNAME + "One or more supplied parameters null");
			return;
		}

		populateInterimRecommendationsAndRecordGenericReasonHelper(interimRecommendationsScheduleEarliestRecommendedInterval, rec, recommendationStatus);
	}


	/**
	 * Record recommendation status codes and date following the below business rules in the TargetSeries interimRecommendationsScheduleLatestRecommendedAge object.
	 * - The supplied Recommendation object is updated with the chosen RecommendationStatus.
	 * @param rec Recommendation Object in which to record recommendation status codes and reasons. Simply returns if any supplied 
	 * parameter is null.
	 * @param recommendationStatus
	 */
	private void populateInterimLatestRecommendedAgeRecommendation(Recommendation rec, RecommendationStatus recommendationStatus) {

		String _METHODNAME = "populateInterimLatestRecommendedAgeRecommendation(): ";
		if (rec == null || recommendationStatus == null) {
			logger.warn(_METHODNAME + "One or more supplied parameters null");
			return;
		}

		populateInterimRecommendationsAndRecordGenericReasonHelper(interimRecommendationsScheduleLatestRecommendedAge, rec, recommendationStatus);
	}

	/**
	 * Record recommendation status codes and date following the below business rules in the TargetSeries interimRecommendationsScheduleLatestRecommendedInterval object.
	 * - The supplied Recommendation object is updated with the chosen RecommendationStatus.
	 * @param rec Recommendation Object in which to record recommendation status codes and reasons. Simply returns if any supplied 
	 * parameter is null.
	 * @param recommendationStatus
	 */
	private void populateInterimLatestRecommendedIntervalRecommendation(Recommendation rec, RecommendationStatus recommendationStatus) {

		String _METHODNAME = "populateInterimLatestRecommendedIntervalRecommendation(): ";
		if (rec == null || recommendationStatus == null) {
			logger.warn(_METHODNAME + "One or more supplied parameters null");
			return;
		}

		populateInterimRecommendationsAndRecordGenericReasonHelper(interimRecommendationsScheduleLatestRecommendedInterval, rec, recommendationStatus);
	}


	/**
	 * Helper method to modify supplied Recommendation object with record recommendation status codes and reasons, and record generic reason in the
	 * specified interim Recommendations List. Also, the supplied Recommendation object is updated with the specified RecommendationStatus.
	 * Generic reasons for the recorded RecommendationStatus are automatically populated in the Recommended object as follows: 
	 *     + if RecommendationStatus.CONDITIONALLY_RECOMMENDED, then ICELogicHelper._RECOMMENDED_CONDITIONALLY_HIGH_RISK_REASON_CODE 
	 *     + if RecommendationStatus.NOT_RECOMMENDED, then ICELogicHelper._NOT_RECOMMENDED_NOT_SPECIFIED_REASON_CODE 
	 *     + if RecommendationStatus.RECOMMENDED_IN_FUTURE,then ICELogicHelper._RECOMMENDED_IN_FUTURE_REASON_CODE 
	 *     + if RecommendationStatus.RECOMMENDED, then ICELogicHelper._RECOMMENDED_DUE_NOW_REASON_CODE 
	 * If you wish to supply different reasons, then you must manually populate a CD and record it yourself in the Recommendation object before passing it
	 * into this one to be added to the Recommendations List
	 * 
	 * @param interimRecommendationsListInstanceToUpdate reference to the interim recommendations List to add the supplied recommendation object to,
	 * as well as the reason
	 * @param rec Recommendation Object in which to record recommendation status codes and reasons. Simply returns if any supplied 
	 * parameter is null. Recommendation date should be populated
	 * @param recommendationDate
	 * @param pRecommendationStatus
	 */
	private void populateInterimRecommendationsAndRecordGenericReasonHelper(List<Recommendation> interimRecommendationsListInstanceToUpdate, Recommendation rec, 
			RecommendationStatus pRecommendationStatus) {

		String _METHODNAME = "populateInterimRecommendationsAndRecordGenericReasonHelper(): ";
		if (rec == null || pRecommendationStatus == null || interimRecommendationsListInstanceToUpdate == null) {
			logger.warn(_METHODNAME + "One or more supplied parameters null");
			return;
		}
		
		BaseDataRecommendationReason lSRC = getGenericRecommendationReasonForRecommendationStatus(pRecommendationStatus);
		if (pRecommendationStatus == RecommendationStatus.CONDITIONALLY_RECOMMENDED) {
			rec.setRecommendationStatus(RecommendationStatus.CONDITIONALLY_RECOMMENDED);
			if (rec.getRecommendationReason() == null && lSRC != null) {
				rec.setRecommendationReason(lSRC.getCdsListItemName());
			}
			if (! interimRecommendationsListInstanceToUpdate.contains(rec)) {
				interimRecommendationsListInstanceToUpdate.add(rec);
			}
		} 
		else if (pRecommendationStatus == RecommendationStatus.NOT_RECOMMENDED) {
			rec.setRecommendationStatus(RecommendationStatus.NOT_RECOMMENDED);
			if (rec.getRecommendationReason() == null && lSRC != null) {
				rec.setRecommendationReason(lSRC.getCdsListItemName());
			}
			if (! interimRecommendationsListInstanceToUpdate.contains(rec)) {
				interimRecommendationsListInstanceToUpdate.add(rec);
			}
		} 
		else if (pRecommendationStatus == RecommendationStatus.RECOMMENDED_IN_FUTURE) {
			rec.setRecommendationStatus(RecommendationStatus.RECOMMENDED_IN_FUTURE);
			if (rec.getRecommendationReason() == null && lSRC != null) {
				rec.setRecommendationReason(lSRC.getCdsListItemName());
			}
			if (! interimRecommendationsListInstanceToUpdate.contains(rec)) {
				interimRecommendationsListInstanceToUpdate.add(rec);
			}
		} 
		else if (pRecommendationStatus == RecommendationStatus.RECOMMENDED) {
			rec.setRecommendationStatus(RecommendationStatus.RECOMMENDED);
			if (rec.getRecommendationReason() == null && lSRC != null) {
				rec.setRecommendationReason(lSRC.getCdsListItemName());
			}
			if (! interimRecommendationsListInstanceToUpdate.contains(rec)) {
				interimRecommendationsListInstanceToUpdate.add(rec);
			}
		}
		else {
			if (! interimRecommendationsListInstanceToUpdate.contains(rec)) {
				interimRecommendationsListInstanceToUpdate.add(rec);
			}			
		}
	}
	
	
	private BaseDataRecommendationReason getGenericRecommendationReasonForRecommendationStatus(RecommendationStatus pRecommendationStatus) {
		
		if (pRecommendationStatus == null) {
			return null;
		}
		
		if (pRecommendationStatus == RecommendationStatus.CONDITIONALLY_RECOMMENDED) {
			return BaseDataRecommendationReason._RECOMMENDED_CONDITIONALLY_HIGH_RISK_REASON;
		} 
		else if (pRecommendationStatus == RecommendationStatus.NOT_RECOMMENDED) {
			return BaseDataRecommendationReason._NOT_RECOMMENDED_NOT_SPECIFIED_REASON;
		} 
		else if (pRecommendationStatus == RecommendationStatus.RECOMMENDED_IN_FUTURE) {
			return BaseDataRecommendationReason._RECOMMENDED_IN_FUTURE_REASON;
		}
		else if (pRecommendationStatus == RecommendationStatus.RECOMMENDED) {
			return BaseDataRecommendationReason._RECOMMENDED_DUE_NOW_REASON;
		}
		else {
			return null;
		}
	}

	/**
	 * Add a recommendation for consideration in this TargetSeries. Note that this method will record *generic* reasons for the 
	 * recommendation as follows: 
	 *     + if RecommendationStatus.CONDITIONALLY_RECOMMENDED, then ICELogicHelper._RECOMMENDED_CONDITIONALLY_HIGH_RISK_REASON_CODE 
	 *     + if RecommendationStatus.NOT_RECOMMENDED, then ICELogicHelper._NOT_RECOMMENDED_NOT_SPECIFIED_REASON_CODE 
	 *     + if RecommendationStatus.RECOMMENDED_IN_FUTURE, then ICELogicHelper._RECOMMENDED_IN_FUTURE_REASON_CODE 
	 *     + if RecommendationStatus.RECOMMENDED, then ICELogicHelper._RECOMMENDED_DUE_NOW_REASON_CODE 
	 * If you wish to supply different reasons, then you must manually populate a CD and record it yourself
	 * 
	 * @param recommendationDate Date of this recommendation
	 * @param v Recommended vaccine
	 * @param recommendationStatus Specify a RecommendationStatus if you wish to be explicit; otherwise, the recommendation will either 
	 * be in the future or now based on date calculations with the supplied evaluation date of the next parameter
	 * @param pEvalDate Evaluation Date that this recommendation should be made against. If null, the current date is used.
	 */
	public void addInterimRecommendationForConsideration(Date recommendationDate, Vaccine v, RecommendationStatus recommendationStatus, Date pEvalDate) {

		String _METHODNAME = "addInterimRecommendationForConsideration(Date, Vaccine, RecommendationStatus, Date): ";
		// if (recommendationDate == null) {			TODO:
		//	return;
		// }
		
		Recommendation rec = null;
		try {
			rec = new Recommendation(this);
		} 
		catch (IllegalArgumentException ie) {
			String str = "Caught unexpected IllegalArgumentException instantiating a recommendation: this should not happen";
			logger.error(_METHODNAME + str);
			throw new IllegalStateException(str);
		}
		if (pEvalDate == null) {
			pEvalDate = new Date();
		}
		rec.setRecommendationDate(recommendationDate);
		rec.setRecommendedVaccine(v);
		if (logger.isDebugEnabled()) {
			logger.debug(_METHODNAME + "Recommendation: " + rec);
		}

		if (recommendationDate == null || (recommendationStatus != null && (recommendationStatus == RecommendationStatus.CONDITIONALLY_RECOMMENDED || 
			recommendationStatus == RecommendationStatus.NOT_RECOMMENDED || recommendationStatus == RecommendationStatus.RECOMMENDED || 
			recommendationStatus == RecommendationStatus.RECOMMENDED_IN_FUTURE))) {
			populateInterimRecommendationsAndRecordGenericReasonHelper(interimRecommendationsCustom, rec, rec.getRecommendationStatus());
		} 
		else if (recommendationDate != null && pEvalDate.before(recommendationDate)) {
			populateInterimRecommendationsAndRecordGenericReasonHelper(interimRecommendationsCustom, rec, RecommendationStatus.RECOMMENDED_IN_FUTURE);
		} 
		else {
			populateInterimRecommendationsAndRecordGenericReasonHelper(interimRecommendationsCustom, rec, RecommendationStatus.RECOMMENDED);
		}
	}
	
	
	/**
	 * Add a recommendation for consideration in this TargetSeries. Note that this method will record *generic* reasons for the 
	 * recommendation as follows: 
	 *     + if RecommendationStatus.CONDITIONALLY_RECOMMENDED, then ICELogicHelper._RECOMMENDED_CONDITIONALLY_HIGH_RISK_REASON_CODE 
	 *     + if RecommendationStatus.NOT_RECOMMENDED, then ICELogicHelper._NOT_RECOMMENDED_NOT_SPECIFIED_REASON_CODE 
	 *     + if RecommendationStatus.RECOMMENDED_IN_FUTURE, then ICELogicHelper._RECOMMENDED_IN_FUTURE_REASON_CODE 
	 *     + if RecommendationStatus.RECOMMENDED, then ICELogicHelper._RECOMMENDED_DUE_NOW_REASON_CODE 
	 * If you wish to supply different reasons, then you must manually populate a CD and record it yourself
	 * 
	 * @param recommendationDate Date of this recommendation
	 * @param recommendationStatus Specify a RecommendationStatus if you wish to be explicit; otherwise, the recommendation will either 
	 * be in the future or now based on date calculations with the supplied evaluation date of the next parameter
	 * @param pEvalDate Evaluation Date that this recommendation should be made against. If null, the current date is used.
	 */
	public void addInterimRecommendationForConsideration(Date recommendationDate, RecommendationStatus recommendationStatus, Date pEvalDate) {
		
		addInterimRecommendationForConsideration(recommendationDate, null, recommendationStatus, pEvalDate);
	}

	
	/**
	 * Add a recommendation for consideration in this TargetSeries. Note that this method will record *generic* reasons for the 
	 * recommendation as follows: 
	 *     + if RecommendationStatus.CONDITIONALLY_RECOMMENDED, then ICELogicHelper._RECOMMENDED_CONDITIONALLY_HIGH_RISK_REASON_CODE 
	 *     + if RecommendationStatus.NOT_RECOMMENDED, then ICELogicHelper._NOT_RECOMMENDED_NOT_SPECIFIED_REASON_CODE 
	 *     + if RecommendationStatus.RECOMMENDED_IN_FUTURE, then ICELogicHelper._RECOMMENDED_IN_FUTURE_REASON_CODE 
	 *     + if RecommendationStatus.RECOMMENDED, then ICELogicHelper._RECOMMENDED_DUE_NOW_REASON_CODE 
	 * If you wish to supply different reasons, then you must manually populate a CD and record the recommendation yourself by some 
	 * other means. Note that the recommendation will either be in the future or now based on date calculations with the supplied evaluation date
	 * 
	 * @param recommendationDate Date of this recommendation
	 * @param pEvalDate Evaluation Date that this recommendation should be made against. If null, the current date is used.
	 */
	public void addInterimRecommendationForConsideration(Date recommendationDate, Date pEvalDate) {
		addInterimRecommendationForConsideration(recommendationDate, null, pEvalDate);
	}

	/**
	 * Add a recommendation for consideration in this TargetSeries. Note that this method will record *generic* reasons for the 
	 * recommendation as follows: 
	 *     + if RecommendationStatus.CONDITIONALLY_RECOMMENDED, then ICELogicHelper._RECOMMENDED_CONDITIONALLY_HIGH_RISK_REASON_CODE 
	 *     + if RecommendationStatus.NOT_RECOMMENDED, then ICELogicHelper._NOT_RECOMMENDED_NOT_SPECIFIED_REASON_CODE 
	 *     + if RecommendationStatus.RECOMMENDED_IN_FUTURE, then ICELogicHelper._RECOMMENDED_IN_FUTURE_REASON_CODE 
	 *     + if RecommendationStatus.RECOMMENDED, then ICELogicHelper._RECOMMENDED_DUE_NOW_REASON_CODE 
	 * If you wish to supply different reasons, then you must manually populate a CD and record it yourself
	 * 
	 * @param recommendation Prepopulated recommendation to add. In this object, specify a RecommendationStatus if you wish to be explicit; 
	 * otherwise, the recommendation will either be in the future or now based on date calculations with the supplied evaluation date of the
	 * next parameter and recommendation date
	 * @param pEvalDate Evaluation Date that this recommendation should be made against. If null, the current date is used.
	 */
	public void addInterimRecommendationForConsideration(Recommendation recommendation, Date pEvalDate) {

		String _METHODNAME = "addInterimRecommendationForConsideration(Recommendation, Date): ";
		if (recommendation == null) {
			return;
		}

		if (logger.isDebugEnabled()) {
			logger.debug(_METHODNAME + "Recommendation: " + recommendation);
		}
		if (pEvalDate == null) {
			pEvalDate = new Date();
		}

		RecommendationStatus lRS = recommendation.getRecommendationStatus();
		if (lRS == null || (lRS != RecommendationStatus.CONDITIONALLY_RECOMMENDED && lRS != RecommendationStatus.NOT_RECOMMENDED && 
			lRS != RecommendationStatus.RECOMMENDED && lRS != RecommendationStatus.RECOMMENDED_IN_FUTURE)) {
			addInterimRecommendationForConsideration(recommendation.getRecommendationDate(), recommendation.getRecommendedVaccine(), null, pEvalDate);
		}
		else if (recommendation.getRecommendationDate() == null || recommendation.getRecommendationStatus() != null) {
			populateInterimRecommendationsAndRecordGenericReasonHelper(interimRecommendationsCustom, recommendation, recommendation.getRecommendationStatus());
		} 
		else if (recommendation.getRecommendationDate() != null && pEvalDate.before(recommendation.getRecommendationDate())) {
			populateInterimRecommendationsAndRecordGenericReasonHelper(interimRecommendationsCustom, recommendation, RecommendationStatus.RECOMMENDED_IN_FUTURE);
		} 
		else {
			populateInterimRecommendationsAndRecordGenericReasonHelper(interimRecommendationsCustom, recommendation, RecommendationStatus.RECOMMENDED);
		}
	}

	/**
	 * Finalizes the earliest, earliest recommended and latest recommended recommendations based on the interim recommendations made to this point. 
	 * After making this call, getFinalEarliestRecommendation(), getFinalRecommendation(), getFinalLatestRecommendation() can be called as desired.
	 * If any of these dates are for some reason adjusted (manually or otherwise) or interim recommendations are added or removed, this method must be 
	 * called again so that the earliest, recommended, overdue and latest are updated in this series. 
	 */
	public void finalizeRecommendationsForForecasting() {

		String _METHODNAME = "finalizeRecommendationsForForecasting(): ";
		
		/**
		 * First, determine the recommendation
		 */
		// Obtain determine overall recommendation status-- priority is: NOT_RECOMMENDED, CONDITIONALLY_RECOMMENDED, FUTURE_RECOMMENDED then RECOMMENDED
		// Then choose recommendation with latest date collected across all interim recommendations (including earliest) if the status is not NOT_RECOMMENDED
		List<Recommendation> lInterimRecommended = new ArrayList<Recommendation>();
		lInterimRecommended.addAll(interimRecommendationsScheduleEarliest);
		if (interimRecommendationsScheduleEarliestRecommendedAge.isEmpty() == false) {
			// Take dates calculated via earliest recommended age(s) if available
			lInterimRecommended.addAll(interimRecommendationsScheduleEarliestRecommendedAge);
		}
		// else if (interimRecommendationsScheduleEarliestRecommendedInterval.isEmpty() == false) {
		if (interimRecommendationsScheduleEarliestRecommendedInterval.isEmpty() == false) {
			// Otherwise use the earliest recommended date(s) calculated via earliest recommended interval
			lInterimRecommended.addAll(interimRecommendationsScheduleEarliestRecommendedInterval);
		}
		if (interimRecommendationsCustom.isEmpty() == false) {
			lInterimRecommended.addAll(interimRecommendationsCustom);
		}

		if (logger.isDebugEnabled()) {
			String lInterimRecommendedStr ="Interim Recommendations to be examined. ";
			int i=1;
			for (Recommendation r : lInterimRecommended) {
				lInterimRecommendedStr += " -- Interim Recommendation: " + i++ + ": " + r.getTargetSeriesIdentifier() + "; status : " + r.getRecommendationStatus() + "; date: " + r.getRecommendationDate() + "; vaccine " + r.getRecommendedVaccine();
			}
			logger.debug(_METHODNAME + lInterimRecommendedStr);
		}
		
		// Number of interim recommendations > 0 ? If so, continue; otherwise set to null all around
		int lInterimRecommendedSize = lInterimRecommended.size();
		if (lInterimRecommendedSize == 0 && this.finalRecommendations.size() == 0) {
			setRecommendationStatus(RecommendationStatus.NOT_RECOMMENDED);
			setFinalRecommendations(null);
			setFinalRecommendationDate(null);
			setFinalEarliestDate(null);
			setFinalLatestRecommendationDate(null);
		}
		else if (lInterimRecommendedSize == 0 && this.recommendationStatusPrior != null) {
			setRecommendationStatus(this.recommendationStatusPrior);
			if (logger.isDebugEnabled()) {
				logger.debug(_METHODNAME + "No interim recommendations to process.");
			}
			return;
		}
		else {
			// Determine range of statuses in recommendations and SELECT one according to priority: NOT_RECOMMENDED, CONDITIONALLY_RECOMMENDED, FUTURE_RECOMMENDED, 
			// then RECOMMENDED
			RecommendationStatus lFinalRecommendationStatus = null;
			// Date lFinalRecommendationDate = null;
			Date lFinalRecommendationDate = (getFinalRecommendationDate() != null) ? getFinalRecommendationDate() : null;
			List<RecommendationStatus> lRecommendationStatusesIterTmpvar = new ArrayList<RecommendationStatus>(); 
			for (Recommendation lR : lInterimRecommended) {
				RecommendationStatus lRS = lR.getRecommendationStatus();
				if (lRS == RecommendationStatus.NOT_RECOMMENDED) {
					// No reason to continue; NOT_RECOMMENDED is highest weighted status was found and the final recommendation date will be null
					lFinalRecommendationStatus = RecommendationStatus.NOT_RECOMMENDED;
					lFinalRecommendationDate = null;
					break;
				}
				Date lRDate = lR.getRecommendationDate();
				if (lRDate != null) {
					if (lFinalRecommendationDate == null) {
						lFinalRecommendationDate = lRDate;
					}
					else if (lRDate.after(lFinalRecommendationDate)) {
						lFinalRecommendationDate = lRDate;
					}
				}
				lRecommendationStatusesIterTmpvar.add(lR.getRecommendationStatus());
			}
			if (lFinalRecommendationStatus == null) {
				// A final recommendation status has not been determined yet; determine final recommendation status now
				if (lRecommendationStatusesIterTmpvar.contains(RecommendationStatus.CONDITIONALLY_RECOMMENDED)) {
					lFinalRecommendationStatus = RecommendationStatus.CONDITIONALLY_RECOMMENDED;
				}
				else if (lRecommendationStatusesIterTmpvar.contains(RecommendationStatus.RECOMMENDED_IN_FUTURE)) {
					lFinalRecommendationStatus = RecommendationStatus.RECOMMENDED_IN_FUTURE;
				}
				else {
					lFinalRecommendationStatus = RecommendationStatus.RECOMMENDED;
				}
			}

			// Now set the final recommendation (final recommendation date, recommendation status and Recommendation object list) for this TargetSeries instance
			setFinalRecommendationDate(lFinalRecommendationDate);
			setRecommendationStatus(lFinalRecommendationStatus);
			////
			List<RecommendationStatus> lRecStatusListOfInterestOtherNoDate = new ArrayList<RecommendationStatus>();
			lRecStatusListOfInterestOtherNoDate.add(RecommendationStatus.FORECASTING_COMPLETE);
			lRecStatusListOfInterestOtherNoDate.add(RecommendationStatus.FORECASTING_IN_PROGRESS);
			lRecStatusListOfInterestOtherNoDate.add(RecommendationStatus.NOT_FORECASTED);
			// Get List from these non-standard status of recommendations that do not have a recommendation date - e.g. - perhaps a vaccine is associated with them
			List<Recommendation> others = Recommendation.getRecommendationListSubsetWithSpecifiedStatuses(lInterimRecommended, lRecStatusListOfInterestOtherNoDate);
			List<Recommendation> eliminatedOthersWithRecDate = new ArrayList<Recommendation>();
			for (Recommendation otherRec : others) {
				if (otherRec.getRecommendationDate() == null) {
					eliminatedOthersWithRecDate.add(otherRec);
				}
			}
			if (lFinalRecommendationStatus == RecommendationStatus.NOT_RECOMMENDED) {
				List<RecommendationStatus> lRecStatusListOfInterest = new ArrayList<RecommendationStatus>();
				lRecStatusListOfInterest.add(RecommendationStatus.NOT_RECOMMENDED);
				setFinalRecommendations(Recommendation.getRecommendationListSubsetWithSpecifiedStatuses(lInterimRecommended, lRecStatusListOfInterest));
			}
			else if (lFinalRecommendationStatus == RecommendationStatus.CONDITIONALLY_RECOMMENDED) {
				List<RecommendationStatus> lRecStatusListOfInterest = new ArrayList<RecommendationStatus>();
				lRecStatusListOfInterest = new ArrayList<RecommendationStatus>();
				lRecStatusListOfInterest.add(RecommendationStatus.CONDITIONALLY_RECOMMENDED);
				lRecStatusListOfInterest.add(RecommendationStatus.RECOMMENDED_IN_FUTURE);
				lRecStatusListOfInterest.add(RecommendationStatus.RECOMMENDED);
				setFinalRecommendations(Recommendation.getRecommendationListSubsetWithSpecifiedStatuses(lInterimRecommended, lRecStatusListOfInterest));
				addFinalRecommendations(eliminatedOthersWithRecDate);
			}
			else if (lFinalRecommendationStatus == RecommendationStatus.RECOMMENDED_IN_FUTURE) {
				List<RecommendationStatus> lRecStatusListOfInterest = new ArrayList<RecommendationStatus>();
				lRecStatusListOfInterest = new ArrayList<RecommendationStatus>();
				lRecStatusListOfInterest.add(RecommendationStatus.RECOMMENDED_IN_FUTURE);
				lRecStatusListOfInterest.add(RecommendationStatus.RECOMMENDED);
				setFinalRecommendations(Recommendation.getRecommendationListSubsetWithSpecifiedStatuses(lInterimRecommended, lRecStatusListOfInterest));
				addFinalRecommendations(eliminatedOthersWithRecDate);
			}
			else if (lFinalRecommendationStatus == RecommendationStatus.RECOMMENDED) {
				List<RecommendationStatus> lRecStatusListOfInterest = new ArrayList<RecommendationStatus>();
				lRecStatusListOfInterest = new ArrayList<RecommendationStatus>();
				lRecStatusListOfInterest.add(RecommendationStatus.RECOMMENDED);
				setFinalRecommendations(Recommendation.getRecommendationListSubsetWithSpecifiedStatuses(lInterimRecommended, lRecStatusListOfInterest));
				addFinalRecommendations(eliminatedOthersWithRecDate);
			}
			else {
				// Catch all - NOT_RECOMMENDED - should not happen but just in case
				List<RecommendationStatus> lRecStatusListOfInterest = new ArrayList<RecommendationStatus>();
				lRecStatusListOfInterest = new ArrayList<RecommendationStatus>();
				lRecStatusListOfInterest.add(RecommendationStatus.NOT_RECOMMENDED);
				setFinalRecommendations(Recommendation.getRecommendationListSubsetWithSpecifiedStatuses(lInterimRecommended, lRecStatusListOfInterest));
			}

			/**
			 * Recommendation completed - now do earliest possible and latest recommendation. If the determined recommendation is NOT_RECOMMENDED, then so should be
			 * the earliest possible and latest recommended. Otherwise, set these if possible
			 */
			if (lFinalRecommendationStatus == RecommendationStatus.NOT_RECOMMENDED) {
				setFinalEarliestDate(null);
				setFinalLatestRecommendationDate(null);
			}
			else {
				Date lObtainLatestEarliest = ICELogicHelper.obtainLatestRecommendationDateFromRecommendationsList(interimRecommendationsScheduleEarliest);
				Date lPrevFinalEarliestDate = getFinalEarliestDate();
				if (lPrevFinalEarliestDate == null) {
					setFinalEarliestDate(lObtainLatestEarliest);
				}
				else if (lPrevFinalEarliestDate != null && lObtainLatestEarliest != null && lObtainLatestEarliest.before(lPrevFinalEarliestDate)) {
					setFinalEarliestDate(lObtainLatestEarliest);
				}

				// Now set the latest recommended date, if and only if the latest recommended age or interval date has been set. In this case, choose the latter of the
				// latest recommended age date and earliest date if available, otherwise the latter of the latest recommended interval date and earliest date
				Date lObtainUnadjustedLatest = null;
				if (interimRecommendationsScheduleLatestRecommendedAge.size() > 0) {
					lObtainUnadjustedLatest = ICELogicHelper.obtainLatestRecommendationDateFromRecommendationsList(interimRecommendationsScheduleLatestRecommendedAge); 
				}
				else if (interimRecommendationsScheduleLatestRecommendedInterval.size() > 0) {
					lObtainUnadjustedLatest = ICELogicHelper.obtainLatestRecommendationDateFromRecommendationsList(interimRecommendationsScheduleLatestRecommendedInterval);
				}
				Date lPrevFinalLatestDate = getFinalLatestRecommendationDate();
				if (lPrevFinalLatestDate != null) {
					if (lObtainUnadjustedLatest == null) {
						lObtainUnadjustedLatest = lPrevFinalLatestDate;
					}
					else if (lPrevFinalLatestDate.after(lObtainUnadjustedLatest)) {
						lObtainUnadjustedLatest = lPrevFinalLatestDate;
					}
				}
				if (lObtainUnadjustedLatest != null) {
					Date lFinalEarliestDate = getFinalEarliestDate();
					if (lFinalEarliestDate == null || lObtainUnadjustedLatest.after(lFinalEarliestDate)) {
						setFinalLatestRecommendationDate(lObtainUnadjustedLatest);
					}
					else {
						setFinalLatestRecommendationDate(lFinalEarliestDate);
					}
				}
			}		
		}

		// Reset interim recommendation tracking
		this.recommendationStatusPrior = getRecommendationStatus(); 
		interimRecommendationsScheduleEarliest = new ArrayList<Recommendation>();
		interimRecommendationsScheduleEarliestRecommendedAge = new ArrayList<Recommendation>();
		interimRecommendationsScheduleEarliestRecommendedInterval = new ArrayList<Recommendation>();
		interimRecommendationsScheduleLatestRecommendedAge = new ArrayList<Recommendation>();
		interimRecommendationsScheduleLatestRecommendedInterval = new ArrayList<Recommendation>();
		interimRecommendationsCustom = new ArrayList<Recommendation>();
		
		// Debug logging
		if (logger.isDebugEnabled()) {
			String lInterimRecommendedStr ="Final Recommendations. ";
			int i=1;
			for (Recommendation r : lInterimRecommended) {
				lInterimRecommendedStr += " --Final Recommendation: " + i++ + ": " + r.getTargetSeriesIdentifier() + "; status : " + r.getRecommendationStatus() + "; date: " + r.getRecommendationDate() + "; vaccine " + r.getRecommendedVaccine();
			}
			logger.debug(_METHODNAME + lInterimRecommendedStr);
		}

		
		// TODO: recommendation has changed - post-forecast checks should be permitted
		// setPostForecastCheckCompleted(false);
	}


	/**
	 * Check age for the supplied dose and record evaluation reason in supplied
	 * TargetDose's validReasons, acceptedReasons and/or invalidReasons list.
	 * 
	 * @param pEvalPersonBirthTime
	 * @param pTD
	 * @throws ImproperUsageException
	 * @throws InconsistentConfigurationException
	 */
	public void evaluateVaccineGroupMinimumAgeandRecordReason(Date pEvalPersonBirthTime, TargetDose pTD)
		throws ImproperUsageException, InconsistentConfigurationException {

		String _METHODNAME = "evaluateVaccineGroupMinimumAgeandRecordReason(): ";

		if (logger.isDebugEnabled()) {
			logger.debug(_METHODNAME + "series " + this.getSeriesName());
		}

		if (pTD == null || pEvalPersonBirthTime == null) {
			String errStr = "Invalid parameters supplied";
			logger.error(_METHODNAME + errStr);
			throw new ImproperUsageException(_METHODNAME + errStr);
		}

		if (isSeriesComplete()) {
			pTD.addAcceptedReason(BaseDataEvaluationReason._EXTRA_DOSE_EVALUATION_REASON.getCdsListItemName());
		}

		DoseRule seriesDoseRule = obtainDoseRuleForSeriesByTargetDose(pTD);
		if (seriesDoseRule == null) {
			// logger.error(_METHODNAME + "Corresponding series dose not found");
			// throw new ImproperUsageException("Corresponding series dose not found");
			String msg = "No series dose rule specified for requested dose number " + pTD.getDoseNumberInSeries();
			logger.warn(_METHODNAME + msg);
			return;
		}

		Date administrationDate = pTD.getAdministrationDate();
		if (administrationDate == null) {
			String str = "Vaccination date not supplied";
			logger.warn(_METHODNAME + str);
			throw new ImproperUsageException(str);
		}
		if (administrationDate.before(pEvalPersonBirthTime)) {
			String str = "Vaccination date supplied before birth date";
			logger.warn(_METHODNAME + str);
			pTD.addInvalidReason(BaseDataEvaluationReason._INVALID_AGE_EVALUATION_REASON.getCdsListItemName());
		}

		TimePeriod minimumAge = seriesDoseRule.getAbsoluteMinimumAge();
		if (minimumAge == null) {
			// There is no required age for this dose.
			String msg = "No minimum age specified for dose: " + pTD;
			logger.warn(_METHODNAME + msg);
			return;
		}
		
		int compareTo = TimePeriod.compareElapsedTimePeriodToDateRange(pEvalPersonBirthTime, administrationDate, minimumAge);
		if (compareTo < 0) {
			pTD.addInvalidReason(BaseDataEvaluationReason._BELOW_MINIMUM_AGE_EVALUATION_REASON.getCdsListItemName());
		}

	}

	/**
	 * Check interval and record evaluation reason in more recent TargetDose's
	 * validReasons, acceptedReasons and/or invalidReasons list.
	 * 
	 * @param pTD
	 * @param pTDprev
	 * @throws ImproperUsageException
	 * @throws InconsistentConfigurationException
	 */
	public void evaluateVaccineGroupMinimumIntervalAndRecordReason(TargetDose pTD, TargetDose pTDprev) 
		throws ImproperUsageException, ICECoreError {

		String _METHODNAME = "checkIntervalAndRecordEvaluationReason(): ";

		if (pTD == null || pTDprev == null) {
			String errStr = "Invalid parameters supplied";
			logger.error(_METHODNAME + errStr);
			throw new ImproperUsageException(_METHODNAME + errStr);
		}

		if (isSeriesComplete()) {
			pTD.addAcceptedReason(BaseDataEvaluationReason._EXTRA_DOSE_EVALUATION_REASON.getCdsListItemName());
		}

		Date previousDoseDate = pTDprev.getAdministrationDate();
		Date currentDoseDate = pTD.getAdministrationDate();
		int doseNumberForWhichToObtainRule = pTD.getDoseNumberInSeries();
		if (doseNumberForWhichToObtainRule > 1) {
			doseNumberForWhichToObtainRule--;
		}
		else {
			/////// return;			// TODO:this is temporary... more generic interval declarations forthcoming; however, currently there are no dose 1->1 settings
			doseNumberForWhichToObtainRule = 1;
		}
		DoseRule doseRulePreviousDose = obtainDoseRuleForSeriesByDoseNumber(doseNumberForWhichToObtainRule);
		TimePeriod minimumInterval = doseRulePreviousDose.getAbsoluteMinimumInterval();
		if (minimumInterval == null) {
			String msg = "No minimum interval specified for dose: " + pTDprev;
			logger.warn(_METHODNAME + msg);
			return;
		}

		int compareTo = TimePeriod.compareElapsedTimePeriodToDateRange(previousDoseDate, currentDoseDate, minimumInterval);
		TimePeriod elapsedTimePeriodBetweenDoses = null;
		try {
			elapsedTimePeriodBetweenDoses = TimePeriod.calculateElapsedTimePeriod(previousDoseDate, currentDoseDate, DurationType.DAYS);
		}
		catch (TimePeriodException tpe) {
			String errStr = _METHODNAME + "caught an unexpected TimePeriodException. Cannot continue";
			logger.error(errStr);
			throw new ICECoreError(errStr);
		}
		
		if (compareTo < 0 && ! elapsedTimePeriodBetweenDoses.getTimePeriodStringRepresentation().equals("0d")) {
			// The elapsed time between administered doses is less than the minimum interval and the doses were not administered on the same day. Therefore, below minimum interval 
			pTD.addInvalidReason(BaseDataEvaluationReason._BELOW_MINIMUM_INTERVAL_EVALUATION_REASON.getCdsListItemName());
		}
	}

	/**
	 * Obtain list of all permitted vaccines for the specified target dose
	 */
	public List<Vaccine> getAllPermittedVaccinesForTargetDose(int doseNumber) {
		
		if (doseNumber < 0 || doseNumber > seriesRules.getNumberOfDosesInSeries()) {
			return null;
		}

		DoseRule lDR = obtainDoseRuleForSeriesByDoseNumber(doseNumber);
		if (lDR == null) {
			return null;
		}
		else {
			return lDR.getAllPermittedVaccines();
		}
	}

	/**
	 * Obtain list of preferable vaccines for the specified target dose
	 */
	public List<Vaccine> getPreferableVaccinesForTargetDose(int doseNumber) {
		
		if (doseNumber < 0 || doseNumber > seriesRules.getNumberOfDosesInSeries()) {
			return null;
		}

		DoseRule lDR = obtainDoseRuleForSeriesByDoseNumber(doseNumber);
		if (lDR == null) {
			return null;
		}
		else {
			return lDR.getPreferableVaccines();
		}
	}

	/**
	 * Obtain list of allowable vaccines for the specified target dose
	 */
	public List<Vaccine> getAllowableVaccinesForTargetDose(int doseNumber) {
		
		if (doseNumber < 0 || doseNumber > seriesRules.getNumberOfDosesInSeries()) {
			return null;
		}

		DoseRule lDR = obtainDoseRuleForSeriesByDoseNumber(doseNumber);
		if (lDR == null) {
			return null;
		}
		else {
			return lDR.getAllowableVaccines();
		}
	}
	
	/**
	 * Obtain minimum interval in string format year, month, or day. e.g. -
	 * "4y", "5m", "6d"
	 * 
	 * @param pTD
	 * @return string representation, or null if none
	 */
	public String getAbsoluteMinimumIntervalForTargetDoseInStringFormat(TargetDose pTD) {

		String _METHODNAME = "getAbsoluteMinimumIntervalForTargetDoseInStringFormat(): ";
		TimePeriod t = null;
		try {
			t = getAbsoluteMinimumIntervalForTargetDose(pTD);
		} catch (ImproperUsageException ie) {
			logger.warn(_METHODNAME + "ImproperUsageException caught");
			return null;
		}
		if (t == null) {
			return null;
		}

		return t.getTimePeriodStringRepresentation();
	}

	/**
	 * Return the minimum interval for the specified dose; null if there is none
	 * 
	 * @param pTD
	 * @return TimePeriod
	 * @throws ImproperUsageException
	 */
	public TimePeriod getAbsoluteMinimumIntervalForTargetDose(TargetDose pTD)
		throws ImproperUsageException {

		String _METHODNAME = "getAbsoluteMinimumIntervalForTargetDose(): ";
		if (pTD == null) {
			String errStr = "Invalid parameters supplied";
			logger.error(_METHODNAME + errStr);
			throw new ImproperUsageException(_METHODNAME + errStr);
		}

		DoseRule seriesDoseRulePrev = obtainDoseRuleForSeriesByTargetDose(pTD);
		if (seriesDoseRulePrev == null) {
			String str = "Corresponding series dose not found";
			logger.error(_METHODNAME + str);
			throw new ImproperUsageException(str);
		}

		TimePeriod minimumInterval = seriesDoseRulePrev.getAbsoluteMinimumInterval();
		if (minimumInterval == null) {
			return null;
		}

		return minimumInterval;
	}

	/**
	 * Obtain minimum interval in string format year, month, or day. e.g. -
	 * "4y", "5m", "6d"
	 * 
	 * @param pTD
	 * @return string representation, or null if none
	 */
	public String getAbsoluteMinimumIntervalForTargetDoseInStringFormat(int targetDoseNumber) {

		String _METHODNAME = "getAbsoluteMinimumIntervalForTargetDoseInStringFormat(): ";
		TimePeriod t = null;
		try {
			t = getAbsoluteMinimumIntervalForTargetDose(targetDoseNumber);
		} 
		catch (ImproperUsageException ie) {
			logger.warn(_METHODNAME + "ImproperUsageException caught");
			return null;
		}

		if (t == null) {
			return null;
		}

		return t.getTimePeriodStringRepresentation();
	}

	public TimePeriod getAbsoluteMinimumIntervalForTargetDose(int targetDoseNumber) 
		throws ImproperUsageException {

		String _METHODNAME = "getAbsoluteMinimumIntervalForTargetDose(): ";
		if (targetDoseNumber <= 0) {
			String errStr = "Invalid parameters supplied";
			logger.error(_METHODNAME + errStr);
			throw new ImproperUsageException(_METHODNAME + errStr);
		}

		DoseRule seriesDoseRulePrev = obtainDoseRuleForSeriesByDoseNumber(targetDoseNumber);
		if (seriesDoseRulePrev == null) {
			String str = "Corresponding series dose not found";
			logger.error(_METHODNAME + str);
			throw new ImproperUsageException(str);
		}

		TimePeriod minimumInterval = seriesDoseRulePrev.getAbsoluteMinimumInterval();
		if (minimumInterval == null) {
			return null;
		}

		return minimumInterval;
	}

	/**
	 * 
	 * @param pTD
	 * @return TimePeriod representing the minimum age; null if the TargetDose
	 *         supplied is null or there is no rule associated with this dose
	 *         number; and finally, TimePeriod with duration set to 0 if there
	 *         is no corresponding minimum age
	 */
	public TimePeriod getAbsoluteMinimumAgeForTargetDose(int targetDoseNumber) {

		String _METHODNAME = "getAbsoluteMinimumAgeForTargetDose(): ";
		if (targetDoseNumber <= 0) {
			String errStr = "Invalid parameters supplied";
			logger.info(_METHODNAME + errStr);
			return null;
		}

		DoseRule seriesDoseRule = obtainDoseRuleForSeriesByDoseNumber(targetDoseNumber);
		if (seriesDoseRule == null) {
			String str = "Corresponding series dose not found";
			logger.info(_METHODNAME + str);
			return null;
		}

		TimePeriod minimumAge = seriesDoseRule.getAbsoluteMinimumAge();
		if (minimumAge == null) {
			String str = "No minimum age associated with this dose rule";
			logger.info(_METHODNAME + str);
			return new TimePeriod(0, DurationType.DAYS);
		}

		return minimumAge;
	}

	/**
	 * Obtain minimum interval in string format year, month, or day. e.g. -
	 * "4y", "5m", "6d"
	 * 
	 * @param pTD
	 * @return string representation, or null if none
	 */
	public String getAbsoluteMinimumAgeForTargetDoseInStringFormat(int targetDoseNumber) {

		TimePeriod t = getAbsoluteMinimumAgeForTargetDose(targetDoseNumber);
		if (t == null) {
			return null;
		}

		return t.getTimePeriodStringRepresentation();
	}

	/**
	 * 
	 * @param pTD
	 * @return TimePeriod representing the minimum age; null if the TargetDose
	 *         supplied is null or there is no rule associated with this dose
	 *         number; and finally, TimePeriod with duration set to 0 if there
	 *         is no corresponding minimum age
	 */
	public TimePeriod getAbsoluteMinimumAgeForTargetDose(TargetDose pTD) {

		String _METHODNAME = "getAbsoluteMinimumAgeForTargetDose(): ";
		if (pTD == null) {
			String errStr = "Invalid parameters supplied";
			logger.info(_METHODNAME + errStr);
			return null;
		}

		DoseRule seriesDoseRule = obtainDoseRuleForSeriesByTargetDose(pTD);
		if (seriesDoseRule == null) {
			String str = "Corresponding series dose not found";
			logger.info(_METHODNAME + str);
			return null;
		}

		TimePeriod minimumAge = seriesDoseRule.getAbsoluteMinimumAge();
		if (minimumAge == null) {
			String str = "No minimum age associated with this dose rule";
			logger.info(_METHODNAME + str);
			return new TimePeriod(0, DurationType.DAYS);
		}

		return minimumAge;

	}

	/**
	 * Obtain minimum interval in string format year, month, or day. e.g. -
	 * "4y", "5m", "6d"
	 * 
	 * @param pTD
	 * @return string representation, or null if none
	 */
	public String getMinimumIntervalForTargetDoseInStringFormat(TargetDose pTD) {

		String _METHODNAME = "getMinimumIntervalForTargetDoseInStringFormat(): ";
		TimePeriod t = null;
		try {
			t = getMinimumIntervalForTargetDose(pTD);
		} catch (ImproperUsageException ie) {
			logger.warn(_METHODNAME + "ImproperUsageException caught");
			return null;
		}
		if (t == null) {
			return null;
		}

		return t.getTimePeriodStringRepresentation();
	}

	/**
	 * Return the minimum interval for the specified dose; null if there is none
	 * 
	 * @param pTD
	 * @return TimePeriod
	 * @throws ImproperUsageException
	 */
	public TimePeriod getMinimumIntervalForTargetDose(TargetDose pTD)
			throws ImproperUsageException {

		String _METHODNAME = "getMinimumIntervalForTargetDose(): ";
		if (pTD == null) {
			String errStr = "Invalid parameters supplied";
			logger.error(_METHODNAME + errStr);
			throw new ImproperUsageException(_METHODNAME + errStr);
		}

		DoseRule seriesDoseRulePrev = obtainDoseRuleForSeriesByTargetDose(pTD);
		if (seriesDoseRulePrev == null) {
			String str = "Corresponding series dose not found";
			logger.error(_METHODNAME + str);
			throw new ImproperUsageException(str);
		}

		TimePeriod minimumInterval = seriesDoseRulePrev.getMinimumInterval();
		if (minimumInterval == null) {
			return null;
		}

		return minimumInterval;
	}

	/**
	 * Obtain minimum interval in string format year, month, or day. e.g. -
	 * "4y", "5m", "6d"
	 * 
	 * @param pTD
	 * @return string representation, or null if none
	 */
	public String getMinimumIntervalForTargetDoseInStringFormat(int targetDoseNumber) {

		String _METHODNAME = "getMinimumIntervalForTargetDoseInStringFormat(): ";
		TimePeriod t = null;
		try {
			t = getMinimumIntervalForTargetDose(targetDoseNumber);
		} catch (ImproperUsageException ie) {
			logger.warn(_METHODNAME + "ImproperUsageException caught");
			return null;
		}

		if (t == null) {
			return null;
		}

		return t.getTimePeriodStringRepresentation();
	}

	public TimePeriod getMinimumIntervalForTargetDose(int targetDoseNumber)
			throws ImproperUsageException {

		String _METHODNAME = "getMinimumIntervalForTargetDose(): ";
		if (targetDoseNumber <= 0) {
			String errStr = "Invalid parameters supplied";
			logger.error(_METHODNAME + errStr);
			throw new ImproperUsageException(_METHODNAME + errStr);
		}

		DoseRule seriesDoseRulePrev = obtainDoseRuleForSeriesByDoseNumber(targetDoseNumber);
		if (seriesDoseRulePrev == null) {
			String str = "Corresponding series dose not found";
			logger.error(_METHODNAME + str);
			throw new ImproperUsageException(str);
		}

		TimePeriod minimumInterval = seriesDoseRulePrev.getMinimumInterval();
		if (minimumInterval == null) {
			return null;
		}

		return minimumInterval;
	}

	/**
	 * 
	 * @param pTD
	 * @return TimePeriod representing the minimum age; null if the TargetDose
	 *         supplied is null or there is no rule associated with this dose
	 *         number; and finally, TimePeriod with duration set to 0 if there
	 *         is no corresponding minimum age
	 */
	public TimePeriod getMinimumAgeForTargetDose(int targetDoseNumber) {

		String _METHODNAME = "getMinimumAgeForTargetDose(): ";
		if (targetDoseNumber <= 0) {
			String errStr = "Invalid parameters supplied";
			logger.info(_METHODNAME + errStr);
			return null;
		}

		DoseRule seriesDoseRule = obtainDoseRuleForSeriesByDoseNumber(targetDoseNumber);
		if (seriesDoseRule == null) {
			String str = "Corresponding series dose not found";
			logger.info(_METHODNAME + str);
			return null;
		}

		TimePeriod minimumAge = seriesDoseRule.getMinimumAge();
		if (minimumAge == null) {
			String str = "No minimum age associated with this dose rule";
			logger.info(_METHODNAME + str);
			return new TimePeriod(0, DurationType.DAYS);
		}

		return minimumAge;
	}

	/**
	 * Obtain minimum interval in string format year, month, or day. e.g. -
	 * "4y", "5m", "6d"
	 * 
	 * @param pTD
	 * @return string representation, or null if none
	 */
	public String getMinimumAgeForTargetDoseInStringFormat(int targetDoseNumber) {

		TimePeriod t = getMinimumAgeForTargetDose(targetDoseNumber);
		if (t == null) {
			return null;
		}

		return t.getTimePeriodStringRepresentation();
	}

	/**
	 * 
	 * @param pTD
	 * @return TimePeriod representing the minimum age; null if the TargetDose
	 *         supplied is null or there is no rule associated with this dose
	 *         number; and finally, TimePeriod with duration set to 0 if there
	 *         is no corresponding minimum age
	 */
	public TimePeriod getMinimumAgeForTargetDose(TargetDose pTD) {

		String _METHODNAME = "getMinimumAgeForTargetDose(): ";
		if (pTD == null) {
			String errStr = "Invalid parameters supplied";
			logger.info(_METHODNAME + errStr);
			return null;
		}

		DoseRule seriesDoseRule = obtainDoseRuleForSeriesByTargetDose(pTD);
		if (seriesDoseRule == null) {
			String str = "Corresponding series dose not found";
			logger.info(_METHODNAME + str);
			return null;
		}

		TimePeriod minimumAge = seriesDoseRule.getMinimumAge();
		if (minimumAge == null) {
			String str = "No minimum age associated with this dose rule";
			logger.info(_METHODNAME + str);
			return new TimePeriod(0, DurationType.DAYS);
		}

		return minimumAge;

	}

	/**
	 * 
	 * @param pTD
	 * @return TimePeriod representing the recommended age; null if the TargetDose
	 *         supplied is null or there is no rule associated with this dose
	 *         number; and finally, TimePeriod with duration set to 0 if there
	 *         is no corresponding minimum age
	 */
	public TimePeriod getRecommendedAgeForTargetDose(TargetDose pTD) {

		String _METHODNAME = "getRecommendedAgeForTargetDose(): ";
		if (pTD == null) {
			String errStr = "Invalid parameters supplied";
			logger.info(_METHODNAME + errStr);
			return null;
		}

		DoseRule seriesDoseRule = obtainDoseRuleForSeriesByTargetDose(pTD);
		if (seriesDoseRule == null) {
			String str = "Corresponding series dose not found";
			logger.info(_METHODNAME + str);
			return null;
		}

		TimePeriod recommendedAge = seriesDoseRule.getEarliestRecommendedAge();
		if (recommendedAge == null) {
			String str = "No minimum age associated with this dose rule";
			logger.info(_METHODNAME + str);
			return new TimePeriod(0, DurationType.DAYS);
		}

		return recommendedAge;

	}
	
	/**
	 * 
	 * @param int target dose number
	 * @return TimePeriod representing the recommended age; null if the TargetDose
	 *         supplied is null or there is no rule associated with this dose
	 *         number; and finally, TimePeriod with duration set to 0 if there
	 *         is no corresponding minimum age
	 */
	public TimePeriod getRecommendedAgeForTargetDose(int targetDoseNumber) {

		String _METHODNAME = "getRecommendedAgeForTargetDose(): ";
		if (targetDoseNumber <= 0) {
			String errStr = "Invalid parameters supplied";
			logger.info(_METHODNAME + errStr);
			return null;
		}

		DoseRule seriesDoseRule = obtainDoseRuleForSeriesByDoseNumber(targetDoseNumber);
		if (seriesDoseRule == null) {
			String str = "Corresponding series dose not found";
			logger.info(_METHODNAME + str);
			return null;
		}

		TimePeriod recommendedAge = seriesDoseRule.getEarliestRecommendedAge();
		if (recommendedAge == null) {
			String str = "No recommended age associated with this dose rule";
			logger.info(_METHODNAME + str);
			return new TimePeriod(0, DurationType.DAYS);
		}

		return recommendedAge;
	}
	
	/**
	 * Obtain minimum interval in string format year, month, or day. e.g. -
	 * "4y", "5m", "6d"
	 * 
	 * @param pTD
	 * @return string representation, or null if none
	 */
	public String getRecommendedAgeForTargetDoseInStringFormat(int targetDoseNumber) {

		TimePeriod t = getRecommendedAgeForTargetDose(targetDoseNumber);
		if (t == null) {
			return null;
		}

		return t.getTimePeriodStringRepresentation();
	}	

	
	/**
	 * Return the recommended interval for the specified dose; null if there is
	 * none
	 * 
	 * @param pTD
	 * @return TimePeriod
	 * @throws ImproperUsageException
	 */
	public TimePeriod getRecommendedIntervalForTargetDose(TargetDose pTD)
		throws ImproperUsageException {

		String _METHODNAME = "getRecommendedIntervalForTargetDose(): ";
		if (pTD == null) {
			String errStr = "Invalid parameters supplied";
			logger.error(_METHODNAME + errStr);
			throw new ImproperUsageException(_METHODNAME + errStr);
		}

		DoseRule seriesDoseRulePrev = obtainDoseRuleForSeriesByTargetDose(pTD);
		if (seriesDoseRulePrev == null) {
			String str = "Corresponding previous series dose not found";
			logger.error(_METHODNAME + str);
			throw new ImproperUsageException(str);
		}

		TimePeriod recommendedInterval = seriesDoseRulePrev.getEarliestRecommendedInterval();
		if (recommendedInterval == null) {
			return null;
		}

		return recommendedInterval;
	}

	
	/**
	 * Return the recommended interval for the specified dose; null if there is none
	 * 
	 * @param pTD
	 * @return TimePeriod
	 * @throws ImproperUsageException
	 */
	public TimePeriod getRecommendedIntervalForTargetDose(int targetDoseNumber)
		throws ImproperUsageException {

		String _METHODNAME = "getRecommendedIntervalForTargetDose(): ";
		if (targetDoseNumber <= 0) {
			String errStr = "Invalid parameters supplied";
			logger.info(_METHODNAME + errStr);
			return null;
		}

		DoseRule seriesDoseRulePrev = obtainDoseRuleForSeriesByDoseNumber(targetDoseNumber);
		if (seriesDoseRulePrev == null) {
			String str = "Corresponding previous series dose not found";
			logger.error(_METHODNAME + str);
			throw new ImproperUsageException(str);
		}

		TimePeriod recommendedInterval = seriesDoseRulePrev.getEarliestRecommendedInterval();
		if (recommendedInterval == null) {
			return null;
		}

		return recommendedInterval;
	}
	
	
	public String getRecommendedIntervalForTargetDoseInStringFormat(int targetDoseNumber) 
		throws ImproperUsageException {

		TimePeriod t = getRecommendedIntervalForTargetDose(targetDoseNumber);
		if (t == null) {
			return null;
		}

		return t.getTimePeriodStringRepresentation();
	}	

	
	/**
	 * Return true if this TargetSeries contains the specified TargetDose; false
	 * if not
	 * 
	 * @param pTD
	 * @return boolean true or false
	 */
	public boolean containsTargetDose(TargetDose pTD) {

		if (pTD == null)
			return false;

		for (TargetDose td : targetDoses) {
			if (td.equals(pTD))
				return true;
		}
		return false;
	}

	public boolean areNoEarlierAdministeredShotsNotEvaluated(TargetDose pTD) {

		String _METHODNAME = "noEarlierAdministeredShotNotEvaluated() ";

		if (pTD == null) {
			return false;
		}
		
		Date targetDoseDate = pTD.getAdministrationDate();
		int administeredShotNumber = pTD.getAdministeredShotNumberInSeries();
		if (targetDoseDate == null) {
			String errStr = "TargetDose supplied does not contain a date";
			logger.warn(_METHODNAME + targetDoseDate);
			throw new IllegalArgumentException(errStr);
		}

		int maxAdministeredShotNumberWithSameDate = 0;
		int minAdminiteredShotNumberWithSameDate = 0;
		for (TargetDose td : targetDoses) {
			Date tdShotDate = td.getAdministrationDate();
			DoseStatus tdStatus = td.getStatus();
			int tdAdministeredShotNumber = td.getAdministeredShotNumberInSeries();
			if (tdShotDate == null) {
				String errStr = "shot in series found without a date";
				logger.error(_METHODNAME + errStr);
				throw new InconsistentConfigurationException(errStr);
			}
			int tdShotDateToTargetDoseDateComparison = tdShotDate.compareTo(targetDoseDate);
			if (tdStatus == DoseStatus.NOT_EVALUATED && tdShotDateToTargetDoseDateComparison < 0) {
				return false;
			}
			if (tdStatus == DoseStatus.NOT_EVALUATED && tdShotDateToTargetDoseDateComparison > 0) {
				break;
			}
			if (tdStatus == DoseStatus.NOT_EVALUATED && tdShotDateToTargetDoseDateComparison == 0) {
				if (minAdminiteredShotNumberWithSameDate == 0) {
					minAdminiteredShotNumberWithSameDate = tdAdministeredShotNumber;
				}
				if (maxAdministeredShotNumberWithSameDate == 0) {
					maxAdministeredShotNumberWithSameDate = tdAdministeredShotNumber;
				}
				if (tdAdministeredShotNumber > maxAdministeredShotNumberWithSameDate) {
					maxAdministeredShotNumberWithSameDate = tdAdministeredShotNumber;
				}
				if (tdAdministeredShotNumber < minAdminiteredShotNumberWithSameDate) {
					minAdminiteredShotNumberWithSameDate = tdAdministeredShotNumber;
				}
			}
		}
		
		if (minAdminiteredShotNumberWithSameDate < administeredShotNumber) {
			return false;
		}

		return true;
	}

	public int getNumberOfShotsAdministeredInSeries() {

		if (targetDoses != null) {
			return targetDoses.size();
		} else {
			return 0;
		}
	}

	/**
	 * Return the dose rule for the dose number in question in this target series 
	 * 
	 * @param doseNumber
	 * @return Dose containing rules for that dose, or null if there is no DoseRule for the specified dose number
	 */
	public DoseRule obtainDoseRuleForSeriesByDoseNumber(int doseNumber) {

		if (seriesRules == null) {
			return null;
		}
		List<DoseRule> seriesVaccineGroupDoseRules = seriesRules.getSeriesDoseRules();
		for (DoseRule d : seriesVaccineGroupDoseRules) {
			if (d.getDoseNumber() == doseNumber) {
				return d;
			}
		}

		return null;
	}
	
	/**
	 * Return dose rule for the shot most recently administered prior to the date specified in this target series
	 * @param date from which the most recently administered shot should be determined
	 * @return DoseRule containing the rules for the dose, or null if there is no shot administered prior to the specified date
	 */
	public DoseRule obtainDoseRuleOfAdmininsteredShotWithMostRecentDatePriorToTargetDose(TargetDose pTD) {

		if (seriesRules == null || pTD == null) {
			return null;
		}
		
		if (this.targetDoses.contains(pTD)) {
			TargetDose lTDOfInterest = pTD;
			boolean priorTargetDoseIdentified = false;
			while (lTDOfInterest != null) {
				TargetDose lTDPrior = this.targetDoses.lower(lTDOfInterest);
				if (lTDPrior == null) {
					break;
				}
				// Business logic of TargetDate ensures that the administration date is not null
				Date lTDOfInterestShotDate = lTDOfInterest.getAdministrationDate();
				Date lTDPriorShotDate = lTDPrior.getAdministrationDate();
				if (lTDPriorShotDate.before(lTDOfInterestShotDate)) {
					priorTargetDoseIdentified = true;
					break;
				}
				else {
					lTDOfInterest = lTDPrior;
				}
			}
			// Found the shot with the most recent prior date
			if (priorTargetDoseIdentified) {
				return obtainDoseRuleForSeriesByTargetDose(lTDOfInterest);
			}
			else {
				return null;
			}
		}

		return null;
	}

	
	/**
	 * Return the series dose rule corresponding to the supplied TargetDose.
	 * 
	 * @param pTD
	 *            TargetDose in this series for which we want to find the
	 *            matching series dose rule
	 * @return Series Dose rule, or null if not found
	 */
	public DoseRule obtainDoseRuleForSeriesByTargetDose(TargetDose pTD) {

		if (pTD == null) {
			return null;
		}
		if (seriesRules == null) {
			return null;
		}

		int doseNumber = pTD.getDoseNumberInSeries();
		List<DoseRule> seriesVaccineGroupDoseRules = seriesRules.getSeriesDoseRules();
		for (DoseRule d : seriesVaccineGroupDoseRules) {
			if (d.getDoseNumber() == doseNumber) {
				return d;
			}
		}

		return null;
	}
	
	
	/**
	 * Add a new DoseRule to the Series witht the specified DoseRule.
	 * @throws IllegalArgumentException if the DoseRule's dose number is not equal to the next dose number in this TargetSeries
	 */
	public void addVaccineGroupDoseRule(DoseRule pDR) {
	
		String _METHODNAME = "addVaccineGroupDoseRule(): ";
		if (pDR == null) {
			logger.warn(_METHODNAME + "DoseRule supplied is null; DoseRule was not added");
			return;
		}
		
		int lNextDoseNumber = seriesRules.getNumberOfDosesInSeries() + 1;
		if (pDR.getDoseNumber() != lNextDoseNumber) {
			String errStr = "Specified DoseRule's dose number is not the next dose number in this TargetSeries; addition cannot be made";
			logger.warn(_METHODNAME + errStr);
			throw new IllegalArgumentException(errStr);
		}
		
		// Make a copy of the SeriesRules object first... do not want to change default rules used by others
		SeriesRules lSR = SeriesRules.constructDeepCopyOfSeriesRulesObject(this.seriesRules);
		lSR.addSeriesDoseRule(pDR);
		this.seriesRules = lSR;
		isSeriesComplete();		// reset series complete flag
	}
	
	
	/**
	 * Modify the existing DoseRule in the Series with the specified DoseRule. 
	 * @throws IllegalArgumentException if the dose number of the specified DoseRule is not a valid dose number in this TargetSeries.
	 */
	public void modifyVaccineGroupDoseRule(DoseRule pDR) {
		
		String _METHODNAME = "modifyVaccineGroupDoseRule(): ";
		if (pDR == null) {
			logger.warn(_METHODNAME + "DoseRule supplied is null; existing DoseRule was not modified");
			return;
		}
		
		int lDoseNumber = pDR.getDoseNumber();
		if (lDoseNumber <= 0 || lDoseNumber > seriesRules.getNumberOfDosesInSeries()) {
			String errStr = "Specified DoseRule's dose number is not a dose number that exists in this TargetSeries; modification cannot be made";
			logger.warn(_METHODNAME + errStr);
			throw new IllegalArgumentException(errStr);
		}
		
		// Make a copy of the SeriesRules object first... do not want to change default rules used by others
		SeriesRules lSR = SeriesRules.constructDeepCopyOfSeriesRulesObject(this.seriesRules);
		lSR.modifySeriesDoseRule(pDR);
		this.seriesRules = lSR;
		isSeriesComplete();		// reset series complete flag
	}
	
	
	/**
	 * Make note that the patient has immunity to the specified series. This
	 * will affect evaluation of all remaining doses not yet evaluated in the
	 * series
	 * 
	 * @param pSDC SupportedDiseaseConcept
	 */
	public void markImmunityToSpecifiedDisease(String pSDC, Date pDateOfImmunity) {

		if (pSDC == null || pDateOfImmunity == null) {
			return;
		}

		if (this.interimEvaluationValidityCountByDisease.containsKey(pSDC)) {
			// Only mark the disease immunity if this series supports the
			// specified disease
			this.diseaseImmunityDate.put(pSDC, pDateOfImmunity);
		}
	}

	
	/**
	 * Calls addTargetDose(TargetDose, boolean) with overrideSeasonDateRestriction set to false
	 * @param targetDose
	 * @return true if TargetDose added, false if not
	 * If there is an associated Season in this TargetSeries, if the supplied TargetDose does not fall within the applicable Season, then it is not 
	 * added.
	 */
	protected boolean addTargetDoseToSeries(TargetDose targetDose) {
		
		return addTargetDoseToSeries(targetDose, false);
	}
	
	
	/**
	 * Add TargetDose to the TargetSeries. Also, set the the administered shot number for all TargetDosees in this TargetSeries in increasing order,
	 * based on the administered shot dates in this TargetSeries. If the current shot is not evaluated yet or invalid, this and other administered
	 * unevaluated shots may be set to the same dose number-- they're next possible valid or accepted shots [i.e. - doses] amongst others in the
	 * series. Upon initialization, all shots are set with doseNumber=1. Note that upon initialization, all shots are set to NOT_EVALUATED (they have
	 * not been evaluated yet), so all dose numbers will be set to 1 in this typical scenario for use of this method.
	 * 
	 * This version of the method allows the caller to override the check, for seasonal TargetSeries, which enforces the TargetDose fall between the  
	 * season start and end dates
	 * @param targetDose
	 * @param overrideSeasonDateRestriction
	 * @return true if TargetDose added, false if not
	 */
	protected boolean addTargetDoseToSeries(TargetDose targetDose, boolean overrideSeasonDateRestriction) {

		String _METHODNAME = "addTargetDoseToSeries(): ";

		boolean targetDoseAdded = false;
		if (targetDose == null) {
			return false;
		} 
		else {
			Date targetDoseDate = targetDose.getAdministrationDate();
			if (targetDoseDate == null) {
				String str = "Supplied TargetDose does not have an administration date";
				logger.warn(_METHODNAME + str);
				throw new IllegalArgumentException(str);
			}
			if (this.targetSeason == null || overrideSeasonDateRestriction || (this.targetSeason != null && this.targetSeason.dateIsApplicableToSeason(targetDoseDate))) { // Off-Season incl if needed
				this.targetDoses.add(targetDose);
				targetDoseAdded = true;
			}
		}

		int i = 1;
		Iterator<TargetDose> iter = this.targetDoses.iterator();
		while (iter.hasNext()) {
			TargetDose td = iter.next();
			td.setAdministeredShotNumberInSeries(i);
			i++;
			td.setDoseNumberInSeries(determineDoseNumberInSeries(td));
		}
		
		return targetDoseAdded;
	}
	

	/**
	 * Remove a target dose from TargetSeries. This method should be used to allow rule author says that the vaccine should not be evaluated as a part
	 * of this series. (e.g. CVX 109 may be evaluated as a part of PCV or PPSV depending circumstances).
	 * @param targetDose
	 */
	public void removeTargetDoseFromSeries(TargetDose targetDose) {
		
		// String _METHODNAME = "removeTargetDoseFromSeries(): ";
		
		if (targetDose == null) {
			return;
		}
		
		// Iterate through doses in the TargetSeries, so that doseNumber can be renumbered properly
		int i = 1;
		boolean foundShotToRemove = false;
		int prevDoseNumber = 1;
		Iterator<TargetDose> iter = this.targetDoses.iterator();
		while (iter.hasNext()) {
			TargetDose td = iter.next();
			if (foundShotToRemove == false && td.equals(targetDose)) {
				foundShotToRemove = true;
				prevDoseNumber = td.getDoseNumberInSeries();
				this.targetDoses.remove(td);
			}
			else if (foundShotToRemove == false) {
				i++;
				continue;
			}
			else {		// found shot to remove is true
				int nextDoseNumber = td.getDoseNumberInSeries();
				td.setAdministeredShotNumberInSeries(i);
				td.setDoseNumberInSeries(prevDoseNumber);
				prevDoseNumber = nextDoseNumber;
				i++;
			}
		}
	}
	
	public boolean targetSeasonExists() {
	
		if (targetSeason == null) {
			return false;
		}
		else {
			return true;
		}
	}
	
	public Season getTargetSeason() {
		
		return targetSeason;
	}
	
	/**
	 * Get the start date of the season for this TargetSeries, if any- (if this is not a seasonal series, null is returned)
	 * @return Date of the start date of the season, or null if none
	 */
	public Date getSeasonStartDate() {
		
		if (targetSeason == null) {
			return null;
		}
		else {
			return targetSeason.getFullySpecifiedSeasonStartDate().toDate();
		}
	}

	/**
	 * Get the end date of the season for this TargetSeries, if any- (if this is not a seasonal series, null is returned)
	 * @return Date of the end date of the target season, or null if none
	 */
	public Date getSeasonEndDate() {
		
		if (targetSeason == null) {
			return null;
		}
		else {
			return targetSeason.getFullySpecifiedSeasonEndDate().toDate();
		}
	}
	
	/**
	 * Get the off-season start date of the season for this TargetSeries, if any - (if this is not a seasonal series, null is returned)
	 * @return Date of the end date of the target season, or null if none
	 */
	public Date getOffSeasonStartDate() {
		
		if (targetSeason == null) {
			return null;
		}
		else {
			return targetSeason.getFullySpecifiedSeasonOffSeasonStartDate().toDate();
		}
	}
	
	/**
	 * Get the off-season start date of the season for this TargetSeries, if any- (if this is not a seasonal series, null is returned)
	 * @return Date of the end date of the target season, or null if none
	 */
	public Date getOffSeasonEndDate() {
		
		if (targetSeason == null) {
			return null;
		}
		else {
			return targetSeason.getFullySpecifiedSeasonOffSeasonEndDate().toDate();
		}
	}

	public String getSeriesName() {
		return seriesRules.getSeriesName();
	}

	public void setSeriesName(String seriesName) {
		seriesRules.setSeriesName(seriesName);
	}

	public String getVaccineGroup() {
		return seriesRules.getVaccineGroup();
	}

	/*
	public void setVaccineGroup(SupportedVaccineGroupConcept vaccineGroup) {
		seriesRules.setVaccineGroup(vaccineGroup);
	}
	*/
	
	/* 
	 * Returns the Set of shots tracked by this class, in ascending order by date, or empty if there are none
	 */
	public SortedSet<TargetDose> getTargetDoses() {
		return targetDoses;
	}

	public SeriesRules getSeriesRules() {
		return seriesRules;
	}

	public void setSeriesRules(SeriesRules seriesRules) {
		
		String _METHODNAME = "setSeriesRules(): ";
		if (seriesRules == null) {
			String errStr = "SeriesRules parameter was not supplied";
			logger.error(_METHODNAME + errStr);
			throw new IllegalArgumentException(errStr);
		}
		this.seriesRules = seriesRules;
	}
	
	public int getDoseAfterWhichSeriesWasMarkedComplete() {
		return this.seriesCompleteAtDoseNumber;
	}
	
	public boolean isSeriesComplete() {

		if (seriesCompleteFlagManuallySet == false)
			determineIfSeriesCompleteAndReturnLastDose();
		return seriesComplete;
	}

	/**
	 * Manually mark the series complete. If this method is called, it overrides any automated computations of series completeness based on the generic
	 * series tables rules, and therefore this series' completeness value will not be changed automatically based on automated generic series
	 * computations after this method has been called. The series completeness value will only be modified after the first call to this method if the
	 * caller explicitly calls this method again
	 * 
	 * @param pSeriesComplete
	 */
	public void setSeriesComplete(boolean pSeriesComplete) {
		this.seriesComplete = pSeriesComplete;
		if (pSeriesComplete == true) {
			this.seriesCompleteAtDoseNumber = determineEffectiveNumberOfDosesInSeries();
		}
		else {
			this.seriesCompleteAtDoseNumber = 0;
		}
		this.seriesCompleteFlagManuallySet = true;
	}

	public boolean isSelectedSeries() {
		return selectedSeries;
	}

	public void setSelectedSeries(boolean selectedSeries) {
		this.selectedSeries = selectedSeries;
	}

	public int getManuallySetDoseNumberToRecommend() {
		return manuallySetDoseNumberToRecommend;
	}

	public void setManuallySetDoseNumberToRecommend(int doseNumberToRecommend) {
		this.manuallySetDoseNumberToRecommend = doseNumberToRecommend;
	}
	
	/**
	 * Manually set the number of doses in the series to something other than autmoatically defined by the SeriesRules. Note that if the number of doses specified is 
	 * greater than that of the SeriesRules, the caller should be careful to write rules to handle those additional doses. This method also resets the series complete
	 * or not complete flag (unless previously manually set by the author).
	 * @param numberOfDosesInSeries
	public void setManuallySetNumberOfDosesInSeries(int numberOfDosesInSeries) {
		
		if (targetSeasonExists()) {
			SeriesRules sr = new SeriesRules(this.seriesRules.getSeriesName(), this.seriesRules.getVaccineGroup(), this.seriesRules.getSeasons());
			sr.setNumberOfDosesInSeries(numberOfDosesInSeries);
			this.seriesRules = sr;
		}
		else {
			SeriesRules sr = new SeriesRules(this.seriesRules.getSeriesName(), this.seriesRules.getVaccineGroup());
			sr.setNumberOfDosesInSeries(numberOfDosesInSeries);
			this.seriesRules = sr;
		}
	
		// Reset the series completion flag
		isSeriesComplete();
	}
	 */

	
	public List<String> getSeriesRulesProcessed() {
		return seriesRulesProcessed;
	}

	public void addSeriesRuleProcessed(String ruleName) {
		if (ruleName != null) {
			seriesRulesProcessed.add(ruleName);
		}
	}

	public Collection<String> getDiseasesSupportedByThisSeries() {

		return interimEvaluationValidityCountByDisease.keySet();
	}

	private int getInterimDiseaseEvaluationValidityCount(String disease) {

		if (disease == null) {
			return 0;
		}
		Integer i = interimEvaluationValidityCountByDisease.get(disease);
		if (i == null) {
			return 0;
		}

		return i.intValue();
	}
	
	public void setManuallySetAccountForLiveVirusIntervalsInRecommendation(boolean yesno) {
		
		manuallySetAccountForLiveVirusIntervalsInRecommendation = new Boolean(yesno);
	}


	public RecommendationStatus getRecommendationStatus() {
		return recommendationStatus;
	}

	/**
	 * Sets the RecommendationStatus to the specified value
	 * @param recommendationStatus
	 */
	public void setRecommendationStatus(RecommendationStatus recommendationStatus) {
		this.recommendationStatus = recommendationStatus;
	}
	
	
	/**
	 * If recommendationStatus is RECOMMENDED or RECOMMENDED_IN_FUTURE, check to see if the recommendationStatus should be changed according to the
	 * final recommendation date and evaluation time parameter; change it to the other if necessary
	 * e.g. - current recommendation status is RECOMMENDED, pEvalTime is 4/2/2010, and final recommendation date is 4/29/2010. Therefore, change the
	 * recommendationStatus from RECOMMENDED to RECOMMENDED_IN_FUTURE. 
	 * 
	 * If the supplied pEvalTime is null, finalRecommendationDate is null, or the current RecommendationStatus is not RECOMMENDED or RECOMMENDED_IN_FUTURE, 
	 * then this method has no effect.
	 */
	public void adjustRecommendationStatusByEvalTime(Date pEvalTime) {
		
		if (pEvalTime == null || this.finalRecommendationDate == null || 
			(recommendationStatus != RecommendationStatus.RECOMMENDED && recommendationStatus != RecommendationStatus.RECOMMENDED_IN_FUTURE)) {
			return;
		}
		
		RecommendationStatus lPriorRS = getRecommendationStatus();
		int compareTo = pEvalTime.compareTo(this.finalRecommendationDate);
		if (compareTo < 0) {
			setRecommendationStatus(RecommendationStatus.RECOMMENDED_IN_FUTURE);
		}
		else {
			setRecommendationStatus(RecommendationStatus.RECOMMENDED);
		}
		
		if (lPriorRS != getRecommendationStatus()) {
			BaseDataRecommendationReason lPriorRSReason = getGenericRecommendationReasonForRecommendationStatus(lPriorRS);
			BaseDataRecommendationReason lCurrentRSReason = getGenericRecommendationReasonForRecommendationStatus(getRecommendationStatus());
			for (Recommendation lRec : this.finalRecommendations) {
				// TODO: H/I
				if (lRec.getRecommendationReason() == lPriorRSReason.getCdsListItemName()) {
					lRec.setRecommendationReason(lCurrentRSReason.getCdsListItemName());
					lRec.setRecommendationStatus(getRecommendationStatus());
				}
			}
		}
	}

	
	/**
	 * Clear any prior recommendations; typically will be used if one wishes to reprocess all recommendations
	 */
	public void clearRecommendations() {
		
		setRecommendationVaccine(null); 
		setFinalEarliestDate(null);
		setFinalRecommendationDate(null);
		setFinalLatestRecommendationDate(null);
		setFinalRecommendations(null);
	}
	
	
	public Date getFinalRecommendationDate() {
		return finalRecommendationDate;
	}
	

	/**
	 * Set the final recommendation date. The final earliest and final latest recommendation dates are affected as follows:
	 * 1) If the earliest date is present and after the supplied recommendation date, the earliest date is also changed to the supplied recommendation date. 
	 * 2) If the latest (overdue) date is present and before the supplied recommendation date, then the latest date is changed to the supplied recommendation date.
	 * 3) If the supplied recommendation date is null, then the earliest and latest recommendation dates are changed to null.
	 * @param pFinalRecommendationDate
	 */
	public void setFinalRecommendationDate(Date pFinalRecommendationDate) {

		this.finalRecommendationDate = pFinalRecommendationDate;

		if (pFinalRecommendationDate != null) {
			// Check to ensure consistency with the earliest date
			Date lDate = getFinalEarliestDate();
			if (lDate != null && pFinalRecommendationDate.before(lDate)) {
				setFinalEarliestDate(pFinalRecommendationDate);
			}
			// Check to ensure consistency with the latest date
			lDate = getFinalLatestRecommendationDate();
			if (lDate != null && pFinalRecommendationDate.after(lDate)) {
				setFinalLatestRecommendationDate(pFinalRecommendationDate);
			}
		}
		else {
			setFinalEarliestDate(null);
			setFinalLatestRecommendationDate(null);
		}
	}

	public List<Recommendation> getFinalRecommendations() {
		return finalRecommendations;
	}

	/**
	 * Set the final earliest date. The final recommended and final latest recommended dates are affected as follows:
	 * 1) If the recommended date is present and before the supplied earliest date, then the recommendation date is changed to the supplied earliest date.
	 * 2) If the latest recommended date is present and before the supplied earliest date, then the latest date is changed to the supplied earliest date.
	 * 3) If the supplied earliest date is null, no changes are made to the recommended and latest recommendation dates. 
	 * @param finalEarliestDate
	 */
	public void setFinalEarliestDate(Date finalEarliestDate) {
		this.finalEarliestDate = finalEarliestDate;

		if (finalEarliestDate != null) {
			// Check to ensure consistency with recommendation date
			Date lDate = getFinalRecommendationDate();
			if (lDate != null && finalEarliestDate.after(lDate)) {
				setFinalRecommendationDate(finalEarliestDate);
			}
			// Check to ensure consistency with latest recommendation date
			lDate = getFinalLatestRecommendationDate();
			if (lDate != null && finalEarliestDate.after(lDate)) {
				setFinalLatestRecommendationDate(finalEarliestDate);
			}
		}
	}

	public Date getFinalEarliestDate() {
		return finalEarliestDate;
	}

	/**
	 * Set the final latest recommendation date. As with final earliest and final recommended, set with care. No adjustments are made to final earliest and
	 * final recommended dates regardless of what this value is set to.
	 * @param finalLatestRecommendationDate
	 */
	public void setFinalLatestRecommendationDate(Date finalLatestRecommendationDate) {
		this.finalLatestRecommendationDate = finalLatestRecommendationDate;
	}

	public Date getFinalLatestRecommendationDate() {
		return finalLatestRecommendationDate;
	}

	private void setFinalRecommendations(List<Recommendation> recommendation) {

		this.finalRecommendations = new ArrayList<Recommendation>();
		if (recommendation == null) {
			return;
		}
		for (Recommendation r : recommendation) {
			if (r != null) {
				this.finalRecommendations.add(r);
			}
		}
		/////// this.finalRecommendations = recommendation;
	}
	
	private void addFinalRecommendations(List<Recommendation> recommendations) {
		
		if (recommendations == null) {
			return;
		}
		for (Recommendation r : recommendations) {
			if (r != null) {
				this.finalRecommendations.add(r);
			}
		}
	}

	private void addFinalRecommendation(Recommendation recommendation) {
		
		if (recommendation != null) {
			this.finalRecommendations.add(recommendation);
		}
	}

	
	public boolean isImmunityToAllDiseasesRecorded() {
		return immunityToAllDiseasesRecorded;
	}

	
	public void setImmunityToAllDiseasesRecorded(boolean immunityToAllDiseasesRecorded) {
		this.immunityToAllDiseasesRecorded = immunityToAllDiseasesRecorded;
	}

	
	/*
	 * TargetSeriesComparator Note: this comparator imposes orderings that are inconsistent with equals.
	 */
	private class TargetSeriesComparator implements Comparator<TargetDose> {

		public int compare(TargetDose a, TargetDose b) {

			if (a == null && b == null) {
				return 0;
			}
			if (a == null && b != null) {
				return 1;
			}
			if (a != null && b == null) {
				return -1;
			}

			String uniqueIdA = a.getUniqueId();
			String uniqueIdB = b.getUniqueId();

			if (uniqueIdA != null && uniqueIdB != null && uniqueIdA.equals(uniqueIdB)) {
				return 0;
			}
			Date aDate = a.getAdministrationDate();
			Date bDate = b.getAdministrationDate();

			if (aDate == null && bDate == null) {
				return 0;
			}
			if (aDate == null && bDate != null) {
				return 1;
			}

			int comparison = aDate.compareTo(bDate);
			if (comparison == 0) {
				return 1;
			} else {
				return comparison;
			}
		}
	}


	@Override
	public String toString() {
		return "TargetSeries [getSeriesName()=" + getSeriesName()
				+ ", getVaccineGroup()=" + getVaccineGroup()
				+ ", getTargetSeason()=" + getTargetSeason() + "]";
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result	+ ((targetSeriesIdentifier == null) ? 0	: targetSeriesIdentifier.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof TargetSeries))
			return false;
		TargetSeries other = (TargetSeries) obj;
		if (targetSeriesIdentifier == null) {
			if (other.targetSeriesIdentifier != null)
				return false;
		} else if (!targetSeriesIdentifier.equals(other.targetSeriesIdentifier))
			return false;
		return true;
	}
	
}