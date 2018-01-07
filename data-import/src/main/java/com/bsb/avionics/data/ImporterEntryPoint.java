/**
 * 
 */
package com.bsb.avionics.data;

import org.apache.logging.log4j.LogManager;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import com.bsb.avionics.data.controller.MainController;

/**
 * @author Marc PEZZETTI
 *
 */
public class ImporterEntryPoint {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		MainController controller = new MainController();
		CmdLineParser parser = new CmdLineParser(controller);
		try {
			parser.parseArgument(args);

			controller.launchProcess();
		} catch (CmdLineException cle) {
			LogManager.getLogger(ImporterEntryPoint.class).error("Exception while parsing the command line.", cle);
		}
	}

}
