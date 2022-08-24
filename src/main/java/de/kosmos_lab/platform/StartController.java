package de.kosmos_lab.platform;


import de.kosmos_lab.platform.persistence.Constants.RunMode;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.File;

class StartController {


    public static void main(String[] args) {
        Options options = new Options();
        options.addOption("help","print this message");

        options.addOption(Option.builder("c").longOpt("config")
                .argName("file")
                .hasArg()
                .desc("use given configfile")
                .build());
        options.addOption("t", "testing", false, "start in testing mode");
        CommandLineParser parser = new DefaultParser();
        try {
            // parse the command line arguments
            CommandLine line = parser.parse(options, args);
            if (line.hasOption("help")) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp( "java -jar kosmos.jar", options );
                System.exit(0);
            }
            File config = new File("config/config.json");
            if (line.hasOption("config")) {
                config = new File(line.getOptionValue("config"));

            }
            RunMode mode = RunMode.NORMAL;
            if (line.hasOption("testing")) {
                mode = RunMode.TEST;
            }

            new KosmoSController(config,mode);
        } catch (ParseException exp) {
            // oops, something went wrong
            //noinspection UseOfSystemOutOrSystemErr
            System.err.println("Parsing failed.  Reason: " + exp.getMessage());
        }

    }

}