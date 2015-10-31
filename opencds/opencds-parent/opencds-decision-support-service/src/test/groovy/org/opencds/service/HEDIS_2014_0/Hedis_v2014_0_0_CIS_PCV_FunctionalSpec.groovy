package org.opencds.service.HEDIS_2014_0;

import org.opencds.service.util.OpencdsClient
import org.opencds.service.util.VMRUtil

import spock.lang.Specification
import spock.lang.Unroll

public class Hedis_v2014_0_0_CIS_PCV_FunctionalSpec extends Specification {
	private static final String SAMPLE_ALL0001 = "src/test/resources/samples/hedis-all/SampleALL0001.xml"
	private static final String SAMPLE_CIS_PCV0001 = "src/test/resources/samples/hedis-cis_pcv/SampleCIS_PCV0001.xml"
	private static final String SAMPLE_CIS_PCV0002 = "src/test/resources/samples/hedis-cis_pcv/SampleCIS_PCV0002.xml"
	private static final String SAMPLE_CIS_PCV0003 = "src/test/resources/samples/hedis-cis_pcv/SampleCIS_PCV0003.xml"
	private static final String SAMPLE_CIS_PCV0004 = "src/test/resources/samples/hedis-cis_pcv/SampleCIS_PCV0004.xml"

	@Unroll
	def "test HEDIS CIS_PCV"() {
		when:
		def input = new File(vmr).text
		def params = [
			kmEvaluationRequest:[scopingEntityId: 'edu.utah', businessId: 'HEDIS_CIS_PCV', version: '2014.0.0'],
			specifiedTime: '2012-02-01'
		]
		def responsePayload = OpencdsClient.sendEvaluateAtSpecifiedTimeMessage(params, input)

	   then:
		def data = new XmlSlurper().parseText(responsePayload)
        def results = VMRUtil.getResults(data, '\\|')
		assertions.size() == results.assertions.size()
		assertions.each {entry ->
			assert results.assertions.containsKey(entry.key)
			if (entry.value) {
				assert results.assertions.get(entry.key) == entry.value
			}
		}
		numerator == results.measure.numerator
		denominator == results.measure.denominator
		
		where:
		vmr | assertions | denominator | numerator
		SAMPLE_ALL0001 | [C529: '', C569: '', reject: '', wrapper : ''] | '0' | '0'
		SAMPLE_CIS_PCV0001 | [C2700: '', C539: '', C54: '', C545: '', C859: '', numMet: '', wrapper: ''] | '1' | '1'
		SAMPLE_CIS_PCV0002 | [C2700: '', C54: '', C545: '', numNotMet: '', wrapper: ''] | '1' | '0'
		SAMPLE_CIS_PCV0003 | [denomNotMet: '', wrapper: ''] | '0' | '0'
		SAMPLE_CIS_PCV0004 | [denomNotMet: '', wrapper: ''] | '0' | '0'
		}
}