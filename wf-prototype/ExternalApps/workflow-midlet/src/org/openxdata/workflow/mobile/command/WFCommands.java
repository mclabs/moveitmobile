package org.openxdata.workflow.mobile.command;

import javax.microedition.lcdui.Command;

public class WFCommands {

	public static Command cmdOpenWir = new Command("Open", Command.OK, 1);
	public static Command cmdPrevWir = new Command("Select WorkItems",Command.OK, 1);
	public static Command cmdDld = new Command("Download", Command.OK, 1);
	public static Command cmdSubmitWir = new Command("Submit workItem",Command.SCREEN, 1);
	public static Command cmdWirDld = new Command("Download WorkItems",	Command.SCREEN, 1);
	public static Command cmdDldSelected = new Command("Download Selected", Command.SCREEN, 1);

}
