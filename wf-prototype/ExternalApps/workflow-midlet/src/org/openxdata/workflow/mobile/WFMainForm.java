package org.openxdata.workflow.mobile;

import javax.microedition.lcdui.Display;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

import org.openxdata.communication.ConnectionSettings;
import org.openxdata.communication.TransportLayer;
import org.openxdata.forms.LogonListener;
import org.openxdata.forms.LogoutListener;
import org.openxdata.model.FormData;
import org.openxdata.workflow.mobile.presenter.FormPresenter;
import org.openxdata.workflow.mobile.presenter.WIRPresenter;

public class WFMainForm extends MIDlet implements LogonListener, LogoutListener {

        public class ListenerClass extends FormListenerAdaptor {

                public void updateCommunicationParams() {
                        String url = ConnectionSettings.getHttpUrl();
                        if (url != null) {
                                transportLayer.setCommunicationParameter(
                                        TransportLayer.KEY_HTTP_URL, url);
                        }
                }

                public boolean beforeFormDisplay(FormData data) {
                        formPresenter.beforeFormDisplay(data);
                        return true;
                }

                public boolean beforeFormSaved(FormData data, boolean isNew) {
                        return formPresenter.beforeFormSaved(data, isNew);
                }

                public void afterFormDelete(FormData data) {
                        formPresenter.afterFormDelete(data);
                }

                public boolean beforeFormDelete(FormData data) {
                        return formPresenter.beforeFormDelete(data);
                }
        }
        private FormListenerAdaptor listener;
        private TransportLayer transportLayer;
        private FormPresenter formPresenter;
        private WIRPresenter wirPresenter;
        private Display display;

        public WFMainForm() {
                super();
                this.listener = new ListenerClass();
                this.display = Display.getDisplay(this);
                Factory.setDisplay(display);
                Factory.setWfMainForm(this);
                this.wirPresenter = Factory.getWirPresenter();
                this.formPresenter = Factory.getFormPresenter();
                Factory.init();
        }

        protected void startApp() throws MIDletStateChangeException {
                try {
                        Factory.getUserMgr().logOn();
                } catch (Throwable e) {
                        e.printStackTrace();
                        throw new RuntimeException(e.toString());
                }
        }

        public FormListenerAdaptor getListener() {
                return listener;
        }

        public boolean onLoggedOn() {
                wirPresenter.showItems(true);
                return false;
        }

        protected void pauseApp() {
        }

        protected void destroyApp(boolean arg0) {
                Factory.dispose();
        }

        public void onLogonCancel() {
                destroyApp(false);
                notifyDestroyed();
        }

        public void onLogout() {
        }
}
