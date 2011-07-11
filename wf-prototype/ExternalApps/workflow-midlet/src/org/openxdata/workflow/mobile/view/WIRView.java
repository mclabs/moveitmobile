package org.openxdata.workflow.mobile.view;

import java.util.Vector;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;

import org.openxdata.util.AlertMessageListener;
import org.openxdata.workflow.mobile.model.MWorkItem;
import org.openxdata.workflow.mobile.util.AlertMsgHelper;

public class WIRView implements AlertMessageListener {

        private List listDisplay;
        private Vector workItems;
        private Display display;

        public WIRView() {
                initScreen();
        }

        public WIRView(Display display2) {
                this();
                this.display = display2;
        }

        private void initScreen() {
                listDisplay = new List("Available WorkItems", List.IMPLICIT);
        }

        public void setWorkItems(Vector workItems) {
                if (workItems == null) {
                        this.workItems = new Vector(0);
                } else {
                        this.workItems = workItems;
                }
                refresh();
        }

        public Vector getWorkItems() {
                Vector workItemsCopy = new Vector();
                for (int i = 0; i < workItems.size(); i++) {
                        workItemsCopy.addElement(workItems.elementAt(i));
                }
                return workItemsCopy;
        }

        

        public void refresh() {
                listDisplay.deleteAll();
                for (int i = 0; i < workItems.size(); i++) {
                        MWorkItem wir = (MWorkItem) workItems.elementAt(i);
                        listDisplay.append(wir.getDisplayName(), null);
                }
        }

        public void showYourSelf() {
                display.setCurrent(listDisplay);
        }

        public Displayable getDisplayable() {
                return listDisplay;
        }

        public void addCommand(Command cmd) {
                listDisplay.addCommand(cmd);
        }

        public void setCommandListener(CommandListener l) {
                listDisplay.setCommandListener(l);
        }

        public MWorkItem getSelectedWorkItem() {
                int idx = listDisplay.getSelectedIndex();
                if (idx != -1) {
                        return (MWorkItem) workItems.elementAt(idx);
                }
                return null;
        }

        public void onAlertMessage(byte msg) {
                try {
                        display.setCurrent(listDisplay);
                } catch (Exception e) {
                        e.printStackTrace();
                }
        }

        public void showError(String string) {
                AlertMsgHelper.showError(display, listDisplay, string);
        }

        public void showMsg(String string) {
                AlertMsgHelper.showMsg(display, listDisplay, string);
        }
}
