package org.clyze.doop

import groovy.transform.CompileStatic
import java.util.concurrent.*
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.apache.log4j.Level
import org.apache.log4j.Logger
import org.clyze.doop.core.*
import org.clyze.doop.system.FileOps

/**
 * The entry point for the standalone doop app.
 */
@CompileStatic
class Main {

    private static final Log logger = LogFactory.getLog(Main)
    private static final int DEFAULT_TIMEOUT = 180 // 3 hours

    static void main(String[] args) {

        Doop.initDoop(System.getenv("DOOP_HOME"), System.getenv("DOOP_OUT"), System.getenv("DOOP_CACHE"))
        Helper.initLogging("INFO", "${Doop.doopHome}/logs", true)

        try {

            // The builder for displaying usage should not include non-standard flags
            CliBuilder usageBuilder = CommandLineAnalysisFactory.createCliBuilder(false)
            // The builder for displaying usage of non-standard flags
            CliBuilder nonStandardUsageBuilder = CommandLineAnalysisFactory.createNonStandardCliBuilder()
            // The builder for actually parsing the arguments needs to include non-standard flags
            CliBuilder builder = CommandLineAnalysisFactory.createCliBuilder(true)

            if (!args) {
                usageBuilder.usage()
                return
            }

            def argsToParse
            String bloxOptions

            //Check for bloxbath options
            int len = args.length
            int index = len
            while (--index >= 0)
                if (args[index] == "--")
                    break

            if (index == -1) {
                argsToParse = args
                bloxOptions = null
            }
            else {
                argsToParse = args[0..index-1]
                bloxOptions = args[index+1..len-1].join(' ')
            }

            OptionAccessor cli = builder.parse(argsToParse)

            if (!cli || cli['h']) {
                usageBuilder.usage()
                return
            }
            else if (cli['X']) {
                nonStandardUsageBuilder.usage()
                return
            }

            String userTimeout
            Analysis analysis
            if (cli['p']) {
                //create analysis from the properties file & the cli options
                String file = cli['p'] as String
                File f = FileOps.findFileOrThrow(file, "Not a valid file: $file")
                File propsBaseDir = f.getParentFile()
                Properties props = FileOps.loadProperties(f)

                //There are no mandatory properties since the name and jars can be overridden too.
                /*
                try {
                    Helper.checkMandatoryProps(props)
                }
                catch(e) {
                    println e.getMessage()
                    return
                }
                */

                changeLogLevel(cli['l'] ?: props.getProperty("level"))

                userTimeout = cli['t'] ?: props.getProperty("timeout")

                analysis = new CommandLineAnalysisFactory().newAnalysis(propsBaseDir, props, cli)
            }
            else {
                //create analysis from the cli options
                try {
                    Helper.checkMandatoryArgs(cli)
                }
                catch(e) {
                    println e.getMessage()
                    usageBuilder.usage()
                    return
                }

                changeLogLevel(cli['l'])

                userTimeout = cli['t']

                analysis = new CommandLineAnalysisFactory().newAnalysis(cli)
            }

            int timeout = Helper.parseTimeout(userTimeout, DEFAULT_TIMEOUT)
            ExecutorService executor = Executors.newSingleThreadExecutor()
            try {
                executor.submit(new Runnable() {
                    @Override
                    void run() {
                        logger.info "Starting ${analysis.name} analysis on ${analysis.inputs[0]} - id: $analysis.id"
                        logger.debug analysis
                        analysis.options.BLOX_OPTS.value = bloxOptions
                        analysis.run()
                        new CommandLineAnalysisPostProcessor().process(analysis)
                    }
                }).get(timeout, TimeUnit.MINUTES)
            }
            catch (TimeoutException te) {
               logger.error("Timeout has expired ($timeout min).")
               System.exit(-1)
            }
            executor.shutdown()

        } catch (e) {
            if (logger.debugEnabled)
                logger.error(e.getMessage(), e)
            else
                logger.error(e.getMessage())
            System.exit(-1)
        }
    }

    private static void changeLogLevel(def logLevel) {
        if (logLevel) {
            switch (logLevel) {
                case "debug":
                    Logger.getRootLogger().setLevel(Level.DEBUG)
                    break
                case "info":
                    Logger.getRootLogger().setLevel(Level.INFO)
                    break
                case "error":
                    Logger.getRootLogger().setLevel(Level.ERROR)
                    break
                default:
                    logger.info "Invalid log level: $logLevel - using default (info)"
            }
        }
    }
}
