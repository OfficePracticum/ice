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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cdsframework.cds.CdsConcept;
import org.cdsframework.cds.supportingdata.LocallyCodedCdsListItem;
import org.cdsframework.ice.supportingdata.ICEConceptType;
import org.cdsframework.ice.supportingdata.ICESupportingDataConfiguration;
import org.cdsframework.ice.supportingdata.LocallyCodedVaccineGroupItem;
import org.cdsframework.ice.supportingdata.LocallyCodedVaccineItem;
import org.cdsframework.ice.supportingdatatmp.SupportedDiseaseConcept;
import org.cdsframework.ice.supportingdatatmp.SupportedVaccineConcept;
import org.cdsframework.ice.supportingdatatmp.SupportedVaccineGroupConcept;
import org.opencds.common.exceptions.ImproperUsageException;


public class Schedule {

	private String scheduleId;
	private List<String> cdsVersions;
	private ICESupportingDataConfiguration iceSupportingDataConfiguration;
	
	// Structures to be deprecated
	private Map<SupportedVaccineGroupConcept, List<SupportedDiseaseConcept>> vaccineGroupDiseases;
	private Map<SupportedVaccineGroupConcept, List<SeriesRules>> vaccineGroupSeries;	// Vaccine Group -> List of SeriesRules
	private Map<SupportedVaccineConcept, Vaccine> supportedVaccinesMap;

	private static Log logger = LogFactory.getLog(Schedule.class);


	/**
	 * Initialize the Immunization Schedule. Throws an ImproperUsageException if any data (including supporting data) is improperly specified. Throws an 
	 * InconsistentConfigurationException if the supporting data is "inconsistent" in some manner
	 * @param pScheduleId The ID of the schedule
	 * @param pCdsVersions The CDS versions supported by this schedule
	 * @param pBaseKnowledgeRepositoryLocation the knowledge base directory location; where all of the knowledge modules are
	 * @throws ImproperUsageException
	 * @throws InconsistentConfigurationException If supporting data is inconsistent
	 */
	public Schedule(String pScheduleId, List<String> pCdsVersions, File pBaseKnowledgeRepositoryLocation) 
		throws ImproperUsageException, InconsistentConfigurationException {
		
		String _METHODNAME = "ScheduleImpl(): ";
		if (pScheduleId == null || pBaseKnowledgeRepositoryLocation == null) {
			String lErrStr = "Schedule not properly initialized: one or more parameters null";
			logger.error(_METHODNAME + lErrStr);
			throw new ImproperUsageException(lErrStr);
		}
		this.scheduleId = pScheduleId;
		this.cdsVersions = pCdsVersions;

		// Initialize the supporting data for all of the CDS versions specified and available from this base knowledge repository location
		this.iceSupportingDataConfiguration = new ICESupportingDataConfiguration(cdsVersions, pBaseKnowledgeRepositoryLocation);

		// Log initialization of Schedule
		StringBuilder lSbScheduleInfo = new StringBuilder(80);
		lSbScheduleInfo.append(_METHODNAME); lSbScheduleInfo.append("Completed Initialization of Schedule: "); lSbScheduleInfo.append(this.scheduleId);
		logger.info(lSbScheduleInfo.toString());
	}

	
	public String getScheduleId() {
		return scheduleId;
	}

	
	public void setScheduleId(String scheduleName) {
		this.scheduleId = scheduleName;
	}

	
	// TODO:
	/*
	public Map<SupportedVaccineGroupConcept, List<SeriesRules>> getScheduleSeries() {
		return vaccineGroupSeries;
	}
	*/
	

	/**
	 * Get SeriesRules based on vaccine group and series name. Returns the SeriesRules representing the specified series by name, or null if not found
	 */
	public SeriesRules getScheduleSeriesByName(String svg, String seriesName) {
		
		if (svg == null || seriesName == null) {
			return null;
		}

		LocallyCodedVaccineGroupItem lcvgi = this.iceSupportingDataConfiguration.getSupportedVaccineGroups().getVaccineGroupItem(svg);    // getSupportedCdsVaccineGroups().getVaccineGroupItem(svg);
		if (lcvgi == null) {
			return null;
		}
		
		List<SeriesRules> lSRs = this.iceSupportingDataConfiguration.getSupportedSeries().getSeriesRulesForSpecifiedVaccineGroup(lcvgi);
		if (lSRs == null || lSRs.isEmpty()) {
			return null;
		}
		for (SeriesRules sr : lSRs) {
			if (seriesName.equals(sr.getSeriesName())) {
				return sr;
			}
		}
		return null;
	}
	

	/**
	 * Utilizing supporting data. Return the Vaccine associated with its OpenCDS concept code value
	 * @param openCdsConceptValue
	 * @return Vaccine, or null if there is no associated Vaccine for the OpenCDS concept code provided
	 */
	public Vaccine getVaccineByOpenCDSConceptValue(String openCdsConceptValue) {

		if (openCdsConceptValue == null) {
			return null;
		}

		CdsConcept lIceVaccineConcept = new CdsConcept(openCdsConceptValue, true);		// All vaccine concepts are OpenCDS concepts
		LocallyCodedCdsListItem lVaccineCdsItem = this.iceSupportingDataConfiguration.getSupportedCdsConcepts().getCdsListItemAssociatedWithICEConceptTypeAndICEConcept(ICEConceptType.VACCINE, lIceVaccineConcept); 
		if (lVaccineCdsItem == null) {
			return null;
		}
		
		// Supporting data restrictions ensure all of the values are non-null
		LocallyCodedVaccineItem lcvi = this.iceSupportingDataConfiguration.getSupportedVaccines().getVaccineItem(lVaccineCdsItem.getCdsListName());
		if (lcvi == null) {
			return null;
		}
		
		return lcvi.getVaccine();
	}


	/**
	 * Utilizing supporting data. Obtain the list of diseases targeted by the specified vaccine group. Both the String supplied as the parameter and String returned  
	 * are compliant to LocallyCodedCdsListItem.getSupportedListConceptItemName().
	 * @param String specifying the vaccine group by name
	 * @return Collection of Strings representing the diseases targeted by the vaccine group; empty collection if none. If the specified vaccine group is  
	 * 		either null or not a vaccine group tracked in the supporting data, null is returned.
	 */
	public Collection<String> getDiseasesTargetedByVaccineGroup(String pVaccineGroupItemName) {
		
		if (pVaccineGroupItemName == null) {
			return null;
		}
		
		LocallyCodedVaccineGroupItem lCodedVaccineGroupItem = this.iceSupportingDataConfiguration.getSupportedVaccineGroups().getVaccineGroupItem(pVaccineGroupItemName);
		if (lCodedVaccineGroupItem == null) {
			return null;
		}
		
		// It's okay to simply return the list of cdsListItemNames directly; all these have been added as ICEConcepts too during supporting data initialization
		return lCodedVaccineGroupItem.getRelatedDiseasesCdsListItemNames();
	}
	

	/**
	 * Return true if the vaccine targets one or more of the specified diseases, false if it does not
	 */
	public boolean vaccineTargetsOneOrMoreOfSpecifiedDiseases(Vaccine vaccine, Collection<String> diseases) {
		
		if (diseases == null || vaccine == null || diseases.isEmpty() == true) {
			return false;
		}
		
		Collection<String> sds = vaccine.getAllDiseasesTargetedForImmunity();
		for (String sdc : sds) {
			if (diseases.contains(sdc)) {
				return true;
			}
		}
		
		return false;
	}
	
	
	/**
	 * Utilizing supporting data. Get the number of vaccine groups across which the specified diseases are handled.
	 * @param pSDCs The list of diseases in question
	 * @return int specifying number of vaccine groups encompassing the union of the specified diseases
	 */
	private int getCountOfVaccineGroupsEncompassingDiseases(List<String> pSDCs) {
		
		if (pSDCs == null) {
			return 0;
		}
		
		int i=0;
		Collection<LocallyCodedVaccineGroupItem> lAllVaccineGroups = this.iceSupportingDataConfiguration.getSupportedVaccineGroups().getAllVaccineGroupItems();
		for (LocallyCodedVaccineGroupItem lVaccineGroup : lAllVaccineGroups) {
			Collection<String> lAllRelatedDiseases = lVaccineGroup.getRelatedDiseasesCdsListItemNames();
			if (lAllRelatedDiseases != null) {			// should never be null
				for (String lRelatedDiseaseName : lAllRelatedDiseases) {
					if (pSDCs.contains(lRelatedDiseaseName)) {
						i++;
						break;
					}
				}
			}
		}
		
		return i;
	}


	// TODO: 
	public List<SeriesRules> getAllSeries() {

		// String _METHODNAME = "getCandidateSeries(): ";

		List<SeriesRules> allSeries = new ArrayList<SeriesRules>();
		if (vaccineGroupSeries == null) {
			return allSeries;
		}
		Collection<List<SeriesRules>> clts = vaccineGroupSeries.values();
		Iterator<List<SeriesRules>> clIter = clts.iterator();
		while (clIter.hasNext()) {
			List<SeriesRules> lts = clIter.next();
			if (lts != null) {
				allSeries.addAll(lts);
			}
		}

		return allSeries;
	}

	
	/**
	 * Incoming immunizations from the patient's history imply which diseases are being treated, and from this information, we return
	 * all VaccineGroup series associated with each of these diseases as potential series to try to evaluate and forecast against. 
	 * @return List<TargetSeries> candidate series to try to evaluate against; empty if no candidate series
	 */
	 // TODO: 
	public List<SeriesRules> getCandidateSeries() {

		List<SeriesRules> allSeries = new ArrayList<SeriesRules>();
		if (vaccineGroupSeries == null) {
			return allSeries;
		}

		List<SeriesRules> seriesRules = new ArrayList<SeriesRules>();
		List<SeriesRules> seriesForVG = vaccineGroupSeries.get(SupportedVaccineGroupConcept.HepB);
		if (seriesForVG != null)
			seriesRules.addAll(seriesForVG);
		seriesForVG = vaccineGroupSeries.get(SupportedVaccineGroupConcept.HepA);
		if (seriesForVG != null) 
			seriesRules.addAll(seriesForVG);
		seriesForVG = vaccineGroupSeries.get(SupportedVaccineGroupConcept.MMR);
		if (seriesForVG != null) {
			seriesRules.addAll(seriesForVG);
		}
		seriesForVG = vaccineGroupSeries.get(SupportedVaccineGroupConcept.Varicella);
		if (seriesForVG != null) {
			seriesRules.addAll(seriesForVG);
		}
		seriesForVG = vaccineGroupSeries.get(SupportedVaccineGroupConcept.Rotavirus);
		if (seriesForVG != null) {
			seriesRules.addAll(seriesForVG);
		}
		seriesForVG = vaccineGroupSeries.get(SupportedVaccineGroupConcept.Hib);
		if (seriesForVG != null) {
			seriesRules.addAll(seriesForVG);
		}
		seriesForVG = vaccineGroupSeries.get(SupportedVaccineGroupConcept.HPV);
		if (seriesForVG != null) {
			seriesRules.addAll(seriesForVG);
		}
		seriesForVG = vaccineGroupSeries.get(SupportedVaccineGroupConcept.PCV);
		if (seriesForVG != null) {
			seriesRules.addAll(seriesForVG);
		}
		seriesForVG = vaccineGroupSeries.get(SupportedVaccineGroupConcept.PPSV);
		if (seriesForVG != null) {
			seriesRules.addAll(seriesForVG);
		}
		seriesForVG = vaccineGroupSeries.get(SupportedVaccineGroupConcept.Influenza);
		if (seriesForVG != null) {
			seriesRules.addAll(seriesForVG);
		}
		seriesForVG = vaccineGroupSeries.get(SupportedVaccineGroupConcept.H1N1);
		if (seriesForVG != null) {
			seriesRules.addAll(seriesForVG);
		}
		seriesForVG = vaccineGroupSeries.get(SupportedVaccineGroupConcept.Meningococcal);
		if (seriesForVG != null) {
			seriesRules.addAll(seriesForVG);
		}
		seriesForVG = vaccineGroupSeries.get(SupportedVaccineGroupConcept.Polio);
		if (seriesForVG != null) {
			seriesRules.addAll(seriesForVG);
		}
		seriesForVG = vaccineGroupSeries.get(SupportedVaccineGroupConcept.DTP);
		if (seriesForVG != null) {
			seriesRules.addAll(seriesForVG);
		}
		
		return seriesRules;
	}
	
	
	/** 
	 * Check to make sure that all seasons do not overlap with each other, or if they do, they have the exact same season start and end dates. 
	 * All series in the vaccine group must be seasonal series, or none of them. If some are or others aren't, this method logs a warning and returns false.
	 * In addition, there cannot be more than one default series in a vaccine group.
	 * @param svgc vaccine group in which to check series consistency.
	 * @return true of these conditions are met, false if not.
	 */
	private boolean checkConsistencyOfSeasonsSupportingDataAcrossSeriesInVaccineGroup(SupportedVaccineGroupConcept svgc) {
		
		String _METHODNAME = "checkConsistencyOfSeasonsSupportingDataAcrossSeriesInVaccineGroup(): ";
		if (svgc == null) {
			return false;
		}
		
		List<SeriesRules> srs = vaccineGroupSeries.get(svgc);
		int countOfDefaultSeasonsAcrossSeries = 0;
		int countOfSeasons = 0;
		boolean aNonSeasonalSeriesExists = false;
		List<Season> seasonsTracker = new ArrayList<Season>();
		for (SeriesRules sr : srs) {
			if (countOfDefaultSeasonsAcrossSeries > 1) {
				logger.warn(_METHODNAME + "more than one default season across in vaccine group " + svgc.getConceptDisplayNameValue());
				return false;
			}
			List<Season> seriesSeasons = sr.getSeasons();
			if (seriesSeasons == null || seriesSeasons.isEmpty()) {
				aNonSeasonalSeriesExists = true;
				if (countOfSeasons > 0) {
					logger.warn(_METHODNAME + "a non-seasonal series was found in a vaccine group with seasons " + svgc.getConceptDisplayNameValue());
					return false;
				}
			}
			for (Season s : seriesSeasons) {
				if (aNonSeasonalSeriesExists) {
					logger.warn(_METHODNAME + "a non-seasonal series was found in a vaccine group with seasons " + svgc.getConceptDisplayNameValue());
					return false;
				}
				boolean lSeasonAlreadyEncountered = false;
				if (seasonsTracker.contains(s)) {
					lSeasonAlreadyEncountered = true;
				}
				else {
					countOfSeasons++;
				}
				if (s.isDefaultSeason() == true) {
					countOfDefaultSeasonsAcrossSeries++;
					if (countOfDefaultSeasonsAcrossSeries >= 2) {
						logger.warn(_METHODNAME + "more than one default season in Series in vaccine group " + svgc.getConceptDisplayNameValue());
						return false;
					}
				}
				else if (lSeasonAlreadyEncountered == false) {
					for (Season seasonIter : seasonsTracker) {
						// Check to see if the season start or end dates overlaps with another season. Overlaps are only allowed if the start and end dates 
						// of the season for the different series are exactly the same. Default seasons do not have a specified start or end date, so they are 
						// not checked here. (This is because if a fully-specified season can take place at a time when a default season is specified; it  
						// overrides the default season which will then not be used.)
						if (! s.seasonsHaveEquivalentStartAndEndDates(seasonIter) && s.seasonOverlapsWith(seasonIter)) {
							logger.warn(_METHODNAME + "overlapping seasons exist in vaccine group " + svgc.getConceptDisplayNameValue());
							return false;
						}
					}
					seasonsTracker.add(s);
				}
			}
		}
		
		int lNumberOfDistinctSeasons = seasonsTracker.size();
		// if (seasonsTracker.size() > 0) {
		if (lNumberOfDistinctSeasons > 0) {
			if (countOfDefaultSeasonsAcrossSeries != 1 && countOfDefaultSeasonsAcrossSeries != 0 ) {
				logger.warn(_METHODNAME + "a seasonal vaccine group must have exactly either 0 or 1 default seasons defined. The # of seasonal series " + 
					"found for " + "vaccine group " + svgc.getConceptDisplayNameValue() + ": " + countOfDefaultSeasonsAcrossSeries);
				return false;
			}
			else if (lNumberOfDistinctSeasons > 1 && countOfDefaultSeasonsAcrossSeries == 0) {
				logger.warn(_METHODNAME + "a seasonal vaccine group wiht more than one season defined must also have a default season defined. No default season has been defined");
				return false;
			}
			else {
				// This is a properly configured seasonal vaccine group with a default season for evaluation
				return true;
			}
		}
		else if (countOfDefaultSeasonsAcrossSeries == 0 && seasonsTracker.size() == 0) {
			// This is not a seasonal vaccine group
			return true;
		}
		else {
			return false;
		}
	}

	
	private void addSeriesToSchedule(SeriesRules pTS) 
		throws ImproperUsageException { 

		String _METHODNAME = "addVaccineGroupSeriesToSchedule(): ";
		if (pTS == null) {
			throw new ImproperUsageException(_METHODNAME + "null VaccineGroup or SeriesRules parameter supplied"); 
		}

		SupportedVaccineGroupConcept vg = pTS.getVaccineGroup();
		String requestedSeriesName = pTS.getSeriesName();
		if (requestedSeriesName == null || vg == null) {
			throw new ImproperUsageException(_METHODNAME + "supplied SeriesRules name or vaccine group is not populated"); 
		}
		List<SeriesRules> ts = vaccineGroupSeries.get(vg);
		if (ts != null) {
			// Check to make sure the SeriesRules name is unique
			for (SeriesRules sr : ts) {
				if (sr.getSeriesName().equalsIgnoreCase(requestedSeriesName)) {
					throw new ImproperUsageException(_METHODNAME + "SeriesRules with name " + requestedSeriesName + " already in schedule");
				}
			}
			ts.add(pTS);
			vaccineGroupSeries.put(vg, ts);
		}
		else {
			ts = new ArrayList<SeriesRules>();
			ts.add(pTS);
			vaccineGroupSeries.put(vg, ts);
		}
	}

}
