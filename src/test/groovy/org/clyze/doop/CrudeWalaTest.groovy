package org.clyze.doop

import org.clyze.analysis.Analysis
import spock.lang.Specification
import spock.lang.Unroll
import static org.clyze.doop.TestUtils.*

/**
 * Test WALA mode (fact generation and analysis).
 */
class CrudeWalaTest extends Specification {

	Analysis analysis

	@Unroll
	def "Crude testing WALA fact generation and analysis"() {
		when:
		Main.main((String[])["-i", "http://centauri.di.uoa.gr:8081/artifactory/DaCapo-benchmarks/2006/antlr.jar", "-a", "context-insensitive", "--id", "antlr-wala", "--dacapo", "--wala-fact-gen", "--platform", "java_8"])
		analysis = Main.analysis

		then:
		relationHasApproxSize(analysis, "CallGraphEdge", 73000)
		relationHasApproxSize(analysis, "VarPointsTo", 3200000)
		relationHasApproxSize(analysis, "ReachableContext", 11000)
	}
}
