package org.openxdata.workflow.mobile.presenter;

import java.util.Vector;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;

import org.openxdata.db.util.Persistent;
import org.openxdata.model.ResponseHeader;
import org.openxdata.util.DefaultCommands;
import org.openxdata.workflow.mobile.DownloadManager;
import org.openxdata.workflow.mobile.FormListenerAdaptor;
import org.openxdata.workflow.mobile.command.ActionDispatcher;
import org.openxdata.workflow.mobile.command.ActionListener;
import org.openxdata.workflow.mobile.command.Viewable;
import org.openxdata.workflow.mobile.command.WFCommands;
import org.openxdata.workflow.mobile.model.MWorkItemInfoList;
import org.openxdata.workflow.mobile.util.AlertMsgHelper;
import org.openxdata.workflow.mobile.view.WIRInfoView;

public class WIRInfoPresenter extends FormListenerAdaptor implements
        ActionListener, CommandListener, Viewable {

        private final ActionDispatcher dispacther;
        private final Display display;
        private final WIRInfoView view;
        private Viewable prevView;
        private final DownloadManager dldManager;

        public WIRInfoPresenter(WIRInfoView view, DownloadManager dldManager,
                Display display, ActionDispatcher dispacther) {
                this.view = view;
                this.dldManager = dldManager;
                this.display = display;
                this.dispacther = dispacther;
                this.dispacther.registerListener(WFCommands.cmdPrevWir, this);
                init();
        }

        private void init() {
                view.addCommand(WFCommands.cmdDldSelected);
                view.addCommand(DefaultCommands.cmdCancel);
                view.setCommanListener(this);
        }

        public void commandAction(Command c, Displayable d) {
                if (c == DefaultCommands.cmdCancel) {
                        prevView.show();
                } else if (c == WFCommands.cmdDldSelected) {
                        downloadSelectedWir();
                }

        }

        private void downloadSelectedWir() {
                Vector wirs = view.selectedWIRS();
                dispacther.fireAction(WFCommands.cmdDldSelected, this, wirs);
        }

        public boolean handle(Command cmd, Viewable disp, Object obj) {
                if (WFCommands.cmdPrevWir != cmd) {
                        return false;
                }
                this.prevView = disp;
                dldManager.dowloadWIRPreview(this);
                return false;
        }

        public void downloaded(Persistent dataOutParams, Persistent dataOut) {
                ResponseHeader rh = (ResponseHeader) dataOutParams;
                if (!rh.isSuccess()) {
                        return;// TODO show appropriate message
                }
                if (dataOut instanceof MWorkItemInfoList) {
                        handleInfoListDownloaded((MWorkItemInfoList) dataOut);
                }
        }

        public void errorOccured(String errorMessage, Exception e) {
                AlertMsgHelper.showMsg(display, prevView.getDisplay(), errorMessage);
                e.printStackTrace();
        }

        public void handleInfoListDownloaded(MWorkItemInfoList infoLisr) {
                view.setInfoList(infoLisr.getWorkItemInfo());
                view.showYourSelf();
        }

        public void show() {
                view.showYourSelf();
        }

        public Displayable getDisplay() {
                return view.getDisplayable();
        }
}
