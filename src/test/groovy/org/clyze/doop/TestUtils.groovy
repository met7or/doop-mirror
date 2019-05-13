package org.clyze.doop

import java.nio.file.Files
import org.clyze.analysis.Analysis
import org.clyze.doop.utils.SouffleScript
import static org.clyze.utils.Helper.forEachLineIn

/**
 * Utility class with checker methods used by other tests.
 */
class TestUtils {
	static void relationHasApproxSize(Analysis analysis, String relation, int expectedSize) {
		log("relationHasApproxSize(${relation}) = ${expectedSize}")
		int actualSize = 0

		forEachLineIn("${analysis.database}/${relation}.csv", { actualSize++ })

		// We expect numbers to deviate by 10%.
		assert actualSize > (expectedSize * 0.9)
		assert actualSize < (expectedSize * 1.1)
	}

	static void relationHasExactSize(Analysis analysis, String relation, int expectedSize) {
		log("relationHasExactSize(${relation}) = ${expectedSize}")
		int actualSize = 0
		forEachLineIn("${analysis.database}/${relation}.csv", { actualSize++ })
		assert actualSize == expectedSize
	}

	/**
	 * Replacement of relationHasExactSize(..., ..., 0) that does not
	 * require reading (lots of) data.
	 */
	static void relationIsEmpty(Analysis analysis, String relation) {
		log("relationIsEmpty(${relation})")
		assert Files.size((new File("${analysis.database}/${relation}.csv")).toPath()) == 0
	}

	static void metricIsApprox(Analysis analysis, String metric, long expectedVal) {
		log("metricIsApprox(${metric}) = ${expectedVal}")
		long actualVal = -1

		String metrics = "${analysis.database}/Stats_Metrics.csv"
		(new File(metrics)).eachLine { line ->
			String[] values = line.split('\t')
			if ((values.size() == 3) && (values[1] == metric)) {
				actualVal = values[2] as long
			}
		}
		// We expect numbers to deviate by 10%.
		assert actualVal > (expectedVal * 0.9)
		assert actualVal < (expectedVal * 1.1)
	}

	// Check that the binary generated by Souffle exists.
	static void execExists(Analysis analysis) {
		log("execExists")
		assert true == (new File("${analysis.outDir}/${SouffleScript.EXE_NAME}")).exists()
	}

	// Trivial check.
	static void metaExists(Analysis analysis) {
		log("metaExists")
		assert true == (new File("${analysis.outDir}/meta")).exists()
	}

	/*
	 * Check that a local variable points to a value.
	 *
	 * @param analysis	  the analysis object
	 * @param local		  the name of the local
	 * @param value		  the value
	 * @param qualified	  if true, qualify relation name
	 */
	static void varPointsTo(Analysis analysis, String local, String value, boolean qualified) {
		log("varPointsTo('${local}') -> ${value}")
		String rel = qualified ? "mainAnalysis.VarPointsTo" : "VarPointsTo"
		findPair(analysis, rel, local, 3, value, 1)
	}
	// Simpler overloaded version.
	static void varPointsTo(Analysis analysis, String local, String value) {
		varPointsTo(analysis, local, value, false)
	}
	// Simpler overloaded version.
	static void varPointsToQ(Analysis analysis, String local, String value) {
		varPointsTo(analysis, local, value, true)
	}

	// Check that a static field points to a value.
	static void staticFieldPointsTo(Analysis analysis, String fld, String value) {
		log("staticFieldPointsTo('${fld}') -> ${value}")
		findPair(analysis, "mainAnalysis.StaticFieldPointsTo", fld, 2, value, 1)
	}

	static void arrayIndexPointsTo(Analysis analysis, String baseValue, String value, boolean qualified) {
		log("arrayIndexPointsTo('${baseValue}') -> ${value}")
		String rel = qualified ? "mainAnalysis.ArrayIndexPointsTo" : "ArrayIndexPointsTo"
		findPair(analysis, rel, baseValue, 3, value, 1)
	}

	static void varValue(Analysis analysis, String local, String value) {
		log("varValue('${local}') -> ${value}")
		findPair(analysis, "Server_Var_Values", local, 1, value, 2)
	}

	static void invoValue(Analysis analysis, String invo, String toMethod) {
		log("invoValue('${invo}') -> ${toMethod}")
		findPair(analysis, "Server_Invocation_Values", invo, 1, toMethod, 2)
	}

	static void methodSub(Analysis analysis, String method, String subMethod) {
		log("methodSub('${method}', '${subMethod}')")
		findPair(analysis, "Server_Method_Subtype", method, 0, subMethod, 1)
	}

	static void methodHandleCGE(Analysis analysis, String instr, String meth) {
		log("methodHandleCGE('${instr}') -> ${meth}")
		findPair(analysis, "mainAnalysis.MethodHandleCallGraphEdge", instr, 1, meth, 3)
	}

	static void normalCGE(Analysis analysis, String instr, String meth) {
		log("CallGraphEdge('${instr}') -> ${meth}")
		findPair(analysis, "CallGraphEdge", instr, 1, meth, 3)
	}

	static void lambdaCGE(Analysis analysis, String instr, String meth) {
		log("lambdaCGE('${instr}') -> ${meth}")
		findPair(analysis, "mainAnalysis.LambdaCallGraphEdge", instr, 1, meth, 3)
	}

	static void proxyCGE(Analysis analysis, String instr, String meth) {
		log("proxyCGE('${instr}') -> ${meth}")
		findPair(analysis, "mainAnalysis.ProxyCallGraphEdge", instr, 1, meth, 3)
	}

	// Check that an instance field points to a value.
	static void instanceFieldPointsTo(Analysis analysis, String fld, String value) {
		log("instanceFieldPointsTo('${fld}') -> ${value}")
		findPair(analysis, "mainAnalysis.InstanceFieldPointsTo", fld, 2, value, 1)
	}

	static void isLauncherActivity(Analysis analysis, String activity) {
		log("isLauncherActivity('${activity}')")
		find(analysis, "mainAnalysis.LauncherActivity", activity, true)
	}

	static void isActivity(Analysis analysis, String activity) {
		log("isActivity('${activity}')")
		find(analysis, "Activity", activity, true)
	}

	static void isBroadcastReceiver(Analysis analysis, String receiver) {
		log("isBroadcastReceiver('${receiver}')")
		find(analysis, "BroadcastReceiver", receiver, true)
	}

	static void isService(Analysis analysis, String service) {
		log("isService('${service}')")
		find(analysis, "Service", service, true)
	}

	static void isContentProvider(Analysis analysis, String provider) {
		log("isContentProvider('${provider}')")
		find(analysis, "ContentProvider", provider, true)
	}

	static void isApplicationPackage(Analysis analysis, String packageName) {
		log("isApplicationPackage('${packageName}')")
		find(analysis, "mainAnalysis.ApplicationPackage", packageName, true)
	}

	static void isLayoutControl(Analysis analysis, String id, String control) {
		log("isLayoutControl('${id}', '${control}')")
		findPair(analysis, "LayoutControl", id, 0, control, 1)
	}

	// Check that a method is reachable.
	static void methodIsReachable(Analysis analysis, String meth) {
		log("methodIsReachable('${meth}')")
		assert true == find(analysis, "Reachable", meth, true)
	}

	// Check that a test (defined in logic) succeeds.
	static void testSucceeds(Analysis analysis, String id) {
		log("testSucceeds(${id})")
		assert true == find(analysis, "TestId", id, true)
	}

	static void findPair(Analysis analysis, String relation,
						 String s1, int idx1, String s2, int idx2) {
		boolean found = false
		forEachLineIn("${analysis.database}/${relation}.csv",
					  { line ->
						  if (line) {
							  String[] values = line.split('\t')
							  String a = values[idx1]
							  String b = values[idx2]
							  if ((a == s1) && (b == s2)) {
								  found = true
							  }
						  }
					  })
		assert found == true
	}

	/**
	 * Finds a line in a relation. Useful for single-column relations.
	 *
	 * @param analysis	 the analysis object
	 * @param relation	 the relation name
	 * @param val		 the string to match against each line
	 * @param db		 true for database relations, false for facts
	 * @return			 true if the value was found, false otherwise
	 */
	static boolean find(Analysis analysis, String relation,
						String val, boolean db) {
		boolean found = false
		String rel = db ? "${analysis.database}/${relation}.csv" : "${analysis.factsDir}/${relation}.facts"
		forEachLineIn(rel, { if (it && (it == val)) { found = true }})
		return found
	}

	static void noSanityErrors(Analysis analysis) {
		noSanityErrors(analysis, true)
	}
	static void noSanityErrors(Analysis analysis, boolean checkPointsTo) {
		log("noSanityErrors")
		relationIsEmpty(analysis, "VarHasNoType")
		relationIsEmpty(analysis, "TypeIsNotConcreteType")
		relationIsEmpty(analysis, "InstructionIsNotConcreteInstruction")
		relationIsEmpty(analysis, "ValueHasNoType")
		relationIsEmpty(analysis, "ValueHasNoDeclaringType")
		relationIsEmpty(analysis, "ClassTypeIsInterfaceType")
		relationIsEmpty(analysis, "PrimitiveTypeIsReferenceType")
		relationIsEmpty(analysis, "basic.DuplicateMethodImplemented")
		relationIsEmpty(analysis, "basic.DuplicateMethodLookup")
		relationIsEmpty(analysis, "mainAnalysis.DuplicateContextRequest")
		relationIsEmpty(analysis, "mainAnalysis.DuplicateContextResponse")
		if (checkPointsTo) {
			relationIsEmpty(analysis, "NotReachableVarPointsTo")
			relationIsEmpty(analysis, "FieldPointsToWronglyTypedValue")
			relationIsEmpty(analysis, "VarPointsToWronglyTypedValue")
			relationIsEmpty(analysis, "VarPointsToMergedHeap")
			relationIsEmpty(analysis, "HeapAllocationHasNoType")
			relationIsEmpty(analysis, "ValueIsNeitherHeapNorNonHeap")
		} else {
			println("Skipping points-to sanity checks.")
		}
	}

	static void log(String msg) {
		println "Running check: ${msg}"
	}

	static void feature(String msg) {
		println "Feature: ${msg}"
	}
}
