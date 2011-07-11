package org.openxdata.workflow.mobile;

import java.util.Vector;

import org.openxdata.communication.ConnectionSettings;
import org.openxdata.communication.TransportLayer;
import org.openxdata.communication.TransportLayerListener;
import org.openxdata.db.util.Persistent;
import org.openxdata.forms.FormListener;
import org.openxdata.model.FormData;
import org.openxdata.model.FormDef;
import org.openxdata.model.QuestionData;

public abstract class FormListenerAdaptor implements FormListener, TransportLayerListener {

        public void afterFormCancelled(FormData data) {
        }

        public void afterFormDelete(FormData data) {
        }

        public void afterFormSaved(FormData data, boolean isNew) {
        }

        public boolean afterQuestionEdit(QuestionData data) {
                return true;
        }

        public boolean beforeFormCancelled(FormData data) {
                return true;
        }

        public boolean beforeFormDataListDisplay(FormDef formDef) {
                return true;
        }

        public boolean beforeFormDefListDisplay(Vector formDefList) {
                return true;
        }

        public boolean beforeFormDelete(FormData data) {
                return true;
        }

        public boolean beforeFormDisplay(FormData data) {
                return true;
        }

        public boolean beforeFormSaved(FormData data, boolean isNew) {
                return true;
        }

        public boolean beforeQuestionEdit(QuestionData data) {
                return true;
        }

        public void cancelled() {
        }

        public void downloaded(Persistent dataOutParams, Persistent dataOut) {
        }

        public void errorOccured(String errorMessage, Exception e) {
        }

        public void updateCommunicationParams() {
                String url = ConnectionSettings.getHttpUrl();
                if (url != null) {
                        Factory.getTransportLayer().setCommunicationParameter(TransportLayer.KEY_HTTP_URL, url);
                }

        }

        public void uploaded(Persistent dataOutParams, Persistent dataOut) {
        }
}
