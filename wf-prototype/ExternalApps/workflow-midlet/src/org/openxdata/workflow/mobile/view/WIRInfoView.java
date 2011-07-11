package org.openxdata.workflow.mobile.view;

import java.util.Vector;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;

import org.openxdata.workflow.mobile.model.MWorkItemInfo;

public class WIRInfoView {

    private final Display display;
    private Vector vector;
    private List list;

    public WIRInfoView(Display display) {
        this.display = display;
        list = new List("WorkItems Summary", List.MULTIPLE);

    }

    public void setInfoList(Vector workItemInfo) {
        vector = workItemInfo;
        refresh();
    }

    public void refresh() {
        if (vector == null) {
            return;
        }
        list.deleteAll();
        int size = vector.size();
        for (int i = 0; i < size; i++) {
            MWorkItemInfo info = (MWorkItemInfo) vector.elementAt(i);
            list.append(info.getDescription(), null);
        }
    }

    public void showYourSelf() {
        display.setCurrent(list);
    }

    public void addCommand(Command cmd) {
        list.addCommand(cmd);
    }

    public void setCommanListener(CommandListener listener) {
        list.setCommandListener(listener);
    }

    public void removeCommand(Command cmd) {
        list.removeCommand(cmd);
    }

    public Vector selectedWIRS() {
        Vector selectedWirs = new Vector();
        int size = vector.size();
        for (int i = 0; i < size; i++) {
            if (list.isSelected(i)) {
                MWorkItemInfo wir = (MWorkItemInfo) vector.elementAt(i);
                selectedWirs.addElement(wir.submissionWIR());
            }
        }
        return selectedWirs;
    }

    public Displayable getDisplayable() {
        return list;
    }
}
