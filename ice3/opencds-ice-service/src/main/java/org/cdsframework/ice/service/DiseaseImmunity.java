/**
 * Copyright (C) 2015 New York City Department of Health and Mental Hygiene, Bureau of Immunization
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

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cdsframework.ice.supportingdata.tmp.SupportedDiseaseConcept;


public class DiseaseImmunity {

	private SupportedDiseaseConcept disease;
	private Date dateOfImmunity;
	private String immunityReason;
	
	private static Log logger = LogFactory.getLog(DiseaseImmunity.class);
	
	
	public DiseaseImmunity(SupportedDiseaseConcept pDiseaseImmunityObtained) {
		
		String _METHODNAME = "DiseaseImmunity(): ";
		if (pDiseaseImmunityObtained == null) {
			String str = "Disease argument for immunity not supplied";
			logger.warn(_METHODNAME + str);
			throw new IllegalArgumentException(str);
		}
		disease = pDiseaseImmunityObtained;
	}

	public DiseaseImmunity(SupportedDiseaseConcept pDisease, Date pDateOfImmunity) {
		
		this(pDisease);
		dateOfImmunity = pDateOfImmunity;
	}

	public DiseaseImmunity(SupportedDiseaseConcept pDisease, Date pDateOfImmunity, String pReason) {
		
		this(pDisease);
		dateOfImmunity = pDateOfImmunity;
		immunityReason = pReason;
	}
	
	public SupportedDiseaseConcept getDisease() {
		return disease;
	}

	
	public Date getDateOfImmunity() {
		return dateOfImmunity;
	}
	
	public String getImmunityReason() {
		return immunityReason;
	}
		
}
