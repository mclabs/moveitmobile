package org.openxdata.workflow.mobile;

import javax.microedition.lcdui.Display;

import org.openxdata.communication.TransportLayer;
import org.openxdata.forms.FormManager;
import org.openxdata.forms.UserManager;
import org.openxdata.workflow.mobile.command.ActionDispatcher;
import org.openxdata.workflow.mobile.presenter.FormPresenter;
import org.openxdata.workflow.mobile.presenter.WIRInfoPresenter;
import org.openxdata.workflow.mobile.presenter.WIRPresenter;
import org.openxdata.workflow.mobile.view.WIRInfoView;
import org.openxdata.workflow.mobile.view.WIRView;

public class Factory {

        private static ActionDispatcher commandDispatcher;
        private static WIRInfoPresenter infoPresenter;
        private static FormPresenter formPresenter;
        private static WIRPresenter wirPresenter;
        private static FormManager formManager;
        private static DownloadManager dldMgr;
        private static WFMainForm wfMainForm;
        private static TransportLayer tLayer;
        private static WIRInfoView infoView;
        private static UserManager userMgr;
        private static Display display;
        private static WIRView wirView;

        static {
                commandDispatcher = new ActionDispatcher();
        }

        public static Display getDisplay() {
                return display;
        }

        public static void setWfMainForm(WFMainForm wfMainForm) {
                Factory.wfMainForm = wfMainForm;
        }

        public static void setDisplay(Display display) {
                Factory.display = display;
                initTransportLayer();
        }

        private static void initTransportLayer() {
                if (display == null) {
                        return;
                }
                tLayer = new TransportLayer();
                tLayer.setDisplay(display);
                tLayer.setPrevScreen(getWirView().getDisplayable());
                tLayer.setDefaultCommunicationParameter(TransportLayer.KEY_HTTP_URL,
                        "http://localhost:8888/wirdownload");
        }

        public static UserManager getUserMgr() {
                if (userMgr == null) {
                        userMgr = new UserManager(display, getWirView().getDisplayable(),
                                "Workflow Mobile", wfMainForm);
                }
                return userMgr;
        }

        public static TransportLayer getTransportLayer() {
                return tLayer;
        }

        public static void setFormManager(FormManager formManager) {
                Factory.formManager = formManager;
        }

        public static FormManager getFormManager() {
                if (formManager == null) {
                        formManager = new FormManager("Workflow Mobile", getDisplay(),
                                wfMainForm.getListener(), getWirView().getDisplayable(),
                                getTransportLayer(), wfMainForm.getListener(), wfMainForm);
                        formManager.setUserManager(getUserMgr());
                        // FormManager.setGlobalInstance(formManager);
                }
                return formManager;
        }

        public static WIRView getWirView() {
                if (wirView == null) {
                        wirView = new WIRView(display);
                }
                return wirView;
        }

        public static DownloadManager getDldMgr() {
                if (dldMgr == null) {
                        dldMgr = new DownloadManager(getTransportLayer());
                        dldMgr.setUserMgr(getUserMgr());
                }
                return dldMgr;
        }

        public static WIRPresenter getWirPresenter() {
                if (wirPresenter == null) {
                        wirPresenter = new WIRPresenter(display, getDldMgr(), getWirView(),
                                commandDispatcher, tLayer);
                }
                return wirPresenter;
        }

        public static FormPresenter getFormPresenter() {
                if (formPresenter == null) {
                        formPresenter = new FormPresenter(getDisplay(), getFormManager(),
                                getDldMgr(), commandDispatcher);
                }
                return formPresenter;
        }

        public static WIRInfoPresenter getWirInfoPresenter() {
                if (infoPresenter == null) {
                        infoPresenter = new WIRInfoPresenter(getWirInfoView(), getDldMgr(),
                                getDisplay(), commandDispatcher);
                }
                return infoPresenter;
        }

        public static WIRInfoView getWirInfoView() {
                if (infoView == null) {
                        infoView = new WIRInfoView(getDisplay());
                }
                return infoView;
        }

        public static void init() {
                getWirInfoPresenter();
                getFormPresenter();
        }

        public static void dispose() {
                commandDispatcher.clear();
                infoPresenter = null;
                formPresenter = null;
                wirPresenter = null;
                formManager = null;
                dldMgr = null;
                wfMainForm = null;
                tLayer = null;
                infoView = null;
                userMgr = null;
                display = null;
                wirView = null;
        }
}
