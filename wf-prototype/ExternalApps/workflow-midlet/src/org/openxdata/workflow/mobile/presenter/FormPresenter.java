package org.openxdata.workflow.mobile.presenter;

import java.util.Date;
import java.util.Vector;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;

import org.openxdata.db.OpenXdataDataStorage;
import org.openxdata.db.util.Persistent;
import org.openxdata.db.util.StorageListener;
import org.openxdata.forms.FormManager;
import org.openxdata.model.FormData;
import org.openxdata.model.FormDef;
import org.openxdata.model.QuestionData;
import org.openxdata.model.UserListStudyDefList;
import org.openxdata.workflow.mobile.DownloadManager;
import org.openxdata.workflow.mobile.FormListenerAdaptor;
import org.openxdata.workflow.mobile.command.ActionDispatcher;
import org.openxdata.workflow.mobile.command.ActionListener;
import org.openxdata.workflow.mobile.command.Viewable;
import org.openxdata.workflow.mobile.command.WFCommands;
import org.openxdata.workflow.mobile.db.WFStorage;
import org.openxdata.workflow.mobile.model.MQuestionMap;
import org.openxdata.workflow.mobile.model.MWorkItem;
import org.openxdata.workflow.mobile.util.AlertMsgHelper;

public class FormPresenter extends FormListenerAdaptor implements
        ActionListener, StorageListener, Viewable {

        private final Display display;
        private FormManager formManager;
        private DownloadManager dldMgr;
        private MWorkItem workItem;
        private Viewable prevDisp;

        public FormPresenter(Display display, FormManager formManager,
                DownloadManager manager, ActionDispatcher dispatcher) {
                this.display = display;
                this.formManager = formManager;
                this.dldMgr = manager;
                dispatcher.registerListener(WFCommands.cmdOpenWir, this);

        }

        public boolean handle(Command cmd, Viewable disp, Object obj) {
                if (cmd != WFCommands.cmdOpenWir || obj == null) {
                        return false;
                }
                this.workItem = (MWorkItem) obj;
                this.prevDisp = disp;
                showFormFor((MWorkItem) obj, disp);
                return true;
        }

        public boolean beforeFormDisplay(FormData frmData) {
                // Do form prefilling before the form is displayed
                String formName = frmData.getDef().getVariableName();
                Vector prefilledQns = workItem.getPrefilledQns();
                for (int i = 0; i < prefilledQns.size(); i++) {
                        MQuestionMap qnMap = (MQuestionMap) prefilledQns.elementAt(i);
                        String questionName = "/" + formName + "/" + qnMap.getQuestion();
                        QuestionData qnData = frmData.getQuestion(questionName);
                        qnData.setTextAnswer(qnMap.getValue());
                        if (qnMap.isOutput()) {
                                qnData.getDef().setEnabled(true);
                        } else {
                                qnData.getDef().setEnabled(false);
                        }
                }
                return true;
        }

        public void showFormFor(MWorkItem wir, Viewable origScreen) {
                this.workItem = wir;
                FormDef formDef = WFStorage.getFormDefForWorkItem(wir);
                if (formDef == null) {
                        dldMgr.downloadStudies(this);
                } else {
                        showForm(formDef, origScreen);
                }
        }

        private void showForm(FormDef formDef, Viewable origDisplay) {
                FormData formData = WFStorage.getOrCreateFormData(workItem, formDef);
                workItem.setDataRecId(new Integer(formData.getRecordId()));
                WFStorage.saveMWorkItem(workItem, this);
                formManager.showForm(true, formData, true, origDisplay.getDisplay());
        }

        public void downloaded(Persistent dataOutParams, Persistent dataOut) {
                // TODO check for empty studies
                if (dataOut instanceof UserListStudyDefList) {
                        handleStudyAndUserDownloaded((UserListStudyDefList) dataOut);
                }
        }

        private void handleStudyAndUserDownloaded(
                UserListStudyDefList userStudyDefList) {
                WFStorage.saveUserListStudyDefList(userStudyDefList);
                showFormFor(workItem, prevDisp);
        }

        public void errorOccured(String errorMessage, Exception e) {
                AlertMsgHelper.showMsg(display, prevDisp.getDisplay(), errorMessage + e);
                if (e != null) {
                        e.printStackTrace();
                }
        }

        public void saveFormData(FormData data, boolean isNew) {
                String formName = data.getDef().getVariableName();
                data.setDateValue("/" + formName + "/endtime", new Date());
                OpenXdataDataStorage.saveFormData(workItem.getStudyId(), data);
        }

        public boolean beforeFormSaved(FormData data, boolean isNew) {
                saveFormData(data, isNew);
                updatePrefilledQuestions(data, workItem);
                prevDisp.show();
                return false;
        }

        public boolean beforeFormDelete(FormData data) {
                workItem.setDataRecId(new Integer(-1));
                WFStorage.saveMWorkItem(workItem, this);
                OpenXdataDataStorage.deleteFormData(workItem.getStudyId(), data);
                prevDisp.show();
                return false;
        }

        public void updatePrefilledQuestions(FormData data,  MWorkItem  workItem){
               Vector prefilledQns = workItem.getPrefilledQns();
                String formName = data.getDef().getVariableName();
                 for (int i = 0; i < prefilledQns.size(); i++) {
                        MQuestionMap qnMap = (MQuestionMap) prefilledQns.elementAt(i);
                        String questionName = "/" + formName + "/" + qnMap.getQuestion();
                        QuestionData qnData = data.getQuestion(questionName);
                        qnMap.setValue(qnData.getTextAnswer());
                }
        }

        public void show() {
        }

        public Displayable getDisplay() {
                return null;
        }
}
