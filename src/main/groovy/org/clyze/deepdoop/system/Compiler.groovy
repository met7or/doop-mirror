package org.clyze.deepdoop.system

import org.antlr.v4.runtime.ANTLRFileStream
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTreeWalker
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.clyze.deepdoop.actions.*
import org.clyze.deepdoop.datalog.*
import org.clyze.deepdoop.datalog.component.Component

class Compiler {

	static List<Result> compile(String outDir, String filename) {
		LogFactory.getLog(Compiler.class).info("[DD] COMPILE: " + filename)

		def results = []
		try {
			DatalogParser parser = new DatalogParser(
					new CommonTokenStream(
						new DatalogLexer(
							new ANTLRFileStream(filename))))
			def listener = new DatalogListenerImpl(filename)
			ParseTreeWalker.DEFAULT.walk(listener, parser.program())

			def p = listener.getProgram()

			PostOrderVisitor<IVisitable> v = new PostOrderVisitor<>(new FlatteningActor(p.comps))
			def flatP = p.accept(v) as Program

			LBCodeGenVisitingActor codeGenActor = new LBCodeGenVisitingActor(outDir)
			codeGenActor.visit(flatP)
			results = codeGenActor.getResults()

		} catch (IOException e) {
			e.printStackTrace()
		}
		return results
	}
}
